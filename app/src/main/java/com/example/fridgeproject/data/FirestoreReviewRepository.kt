package com.example.fridgeproject.data

import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.ImageStorageRepository
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.data.model.FirestoreReview
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.Review
import com.example.fridgeproject.model.StoredImage
import com.example.fridgeproject.model.enums.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.round

class FirestoreReviewRepository(
    private val firestore: FirebaseFirestore,
    private val imageStorageRepository: ImageStorageRepository,
    private val notificationRepository: NotificationRepository
) : ReviewRepository {

    private val reviewsCollection = firestore.collection(Collections.REVIEWS)

    override fun getReviewById(id: String): Flow<Review?> =
        reviewsCollection.document(id)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(FirestoreReview::class.java)?.toDomain(snapshot.id)
            }

    override fun getReviewsByRecipe(recipeId: String): Flow<List<Review>> =
        reviewsFlow("recipeId", recipeId)

    override fun getReviewsByUser(userId: String): Flow<List<Review>> =
        reviewsFlow("userId", userId)

    override fun getReviewsCountByUser(userId: String): Flow<Int> =
        reviewsCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot -> snapshot.size() }

    override fun getAverageRatingByRecipe(recipeId: String): Flow<Float> =
        getAverageRatingsByRecipe().map { ratings -> ratings[recipeId] ?: 0f }

    override fun getAverageRatingsByRecipe(): Flow<Map<String, Float>> =
        allReviewsFlow().map { reviews ->
            reviews
                .groupBy { it.recipeId }
                .mapValues { (_, recipeReviews) -> recipeReviews.averageRating() }
        }

    override suspend fun saveReview(review: Review, pendingImages: List<LocalImageInput>) {
        if (review.stars !in MIN_REVIEW_STARS..MAX_REVIEW_STARS) {
            throw IllegalArgumentException(
                "Review stars must be between $MIN_REVIEW_STARS and $MAX_REVIEW_STARS"
            )
        }

        val existing = reviewsCollection
            .whereEqualTo("recipeId", review.recipeId)
            .whereEqualTo("userId", review.userId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val docRef = when {
            review.id.isNotBlank() -> reviewsCollection.document(review.id)
            existing != null -> existing.reference
            else -> reviewsCollection.document()
        }
        val existingReview = docRef.get().await()
            .toObject(FirestoreReview::class.java)
            ?.toDomain(docRef.id)
        val uploadedImages = uploadPendingReviewImages(docRef.id, pendingImages)
        val reviewToSave = review.copy(
            id = docRef.id,
            images = review.images + uploadedImages
        )
        val removedImages = existingReview?.let { removedStoredReviewImages(it, review) }.orEmpty()

        docRef.set(reviewToSave.toFirestore()).await()
        removedImages.forEach { image ->
            imageStorageRepository.deleteReviewImage(image)
        }

        try {
            val recipeDoc = firestore.collection(Collections.RECIPES)
                .document(review.recipeId)
                .get()
                .await()

            val recipeAuthorId = recipeDoc.getString("authorId")
            val deterministicNotificationId = "review_${review.userId}_${review.recipeId}"
            if (recipeAuthorId != null && recipeAuthorId != review.userId) {
                val authorDoc = firestore.collection(Collections.USERS).document(recipeAuthorId).get().await()
                val globalEnabled = authorDoc.getBoolean("receiveNotification") ?: true
                val reviewEnabled = authorDoc.getBoolean("receiveReviewNotification") ?: true
                if (globalEnabled && reviewEnabled) {
                    val reviewNotification = Notification(
                        id = deterministicNotificationId,
                        userId = recipeAuthorId,
                        type = NotificationType.REVIEW,
                        triggerUserId = review.userId,
                        recipeId = review.recipeId,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    notificationRepository.insertNotification(reviewNotification)
                }
            }
        } catch (e: Exception) {
            println("Errore durante la generazione della notifica: ${e.message}")
        }
    }

    override suspend fun deleteReview(id: String) {
        try {
            val revDoc = reviewsCollection.document(id).get().await()
            val userId = revDoc.getString("userId")
            val recipeId = revDoc.getString("recipeId")
            imageStorageRepository.deleteReviewImages(id)
            reviewsCollection.document(id).delete().await()
            if (!userId.isNullOrBlank() && !recipeId.isNullOrBlank()) {
                val deterministicNotificationId = "review_${userId}_${recipeId}"
                notificationRepository.deleteNotification(deterministicNotificationId)
            }
        } catch (e: Exception) {
            println("Errore durante l'eliminazione della review o della relativa notifica: ${e.message}")
        }
    }

    override suspend fun deleteReviewsByRecipe(recipeId: String) {
        val reviews = reviewsCollection
            .whereEqualTo("recipeId", recipeId)
            .get()
            .await()
            .documents

        reviews.forEach { document ->
            imageStorageRepository.deleteReviewImages(document.id)
            document.reference.delete().await()
        }
    }

    private fun reviewsFlow(field: String, value: String): Flow<List<Review>> =
        reviewsCollection
            .whereEqualTo(field, value)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(FirestoreReview::class.java)?.toDomain(document.id)
                    }
                    .sortedByDescending { it.date }
            }

    private fun allReviewsFlow(): Flow<List<Review>> =
        reviewsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(FirestoreReview::class.java)?.toDomain(document.id)
                    }
            }

    private fun List<Review>.averageRating(): Float {
        if (isEmpty()) return 0f
        val average = map { it.stars }.average().toFloat()
        return round(average * 10) / 10f
    }

    private suspend fun uploadPendingReviewImages(
        reviewId: String,
        pendingImages: List<LocalImageInput>
    ): List<StoredImage> {
        return pendingImages.map { input ->
            imageStorageRepository.uploadReviewImage(
                reviewId = reviewId,
                input = input
            )
        }
    }

    private fun removedStoredReviewImages(existingReview: Review, reviewToSave: Review): List<StoredImage> =
        existingReview.images - reviewToSave.images

    private companion object {
        const val MIN_REVIEW_STARS = 1
        const val MAX_REVIEW_STARS = 5
    }
}