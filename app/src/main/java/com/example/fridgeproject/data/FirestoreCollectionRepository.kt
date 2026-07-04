package com.example.fridgeproject.data

import com.example.fridgeproject.data.model.FirestoreCollectionOwnership
import com.example.fridgeproject.data.model.FirestoreCustomCollection
import com.example.fridgeproject.data.model.FirestoreSystemCollection
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.SystemCollection
import com.example.fridgeproject.model.enums.NotificationType
import com.example.fridgeproject.model.enums.SystemCollectionType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.collections.chunked
import kotlin.collections.forEach

class FirestoreCollectionRepository(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) : CollectionRepository {

    private val customCollections = firestore.collection(Collections.CUSTOM_COLLECTIONS)
    private val systemCollections = firestore.collection(Collections.SYSTEM_COLLECTIONS)
    private val collectionOwnerships = firestore.collection(Collections.COLLECTION_OWNERSHIPS)

    override fun getCustomCollectionById(id: String): Flow<CustomCollection?> =
        combine(
            customCollections.document(id).snapshots(),
            recipeIdsInCollection(id)
        ) { snapshot, recipeIds ->
            snapshot.toObject(FirestoreCustomCollection::class.java)?.toDomain(snapshot.id, recipeIds)
        }

    override fun getSystemCollectionById(id: String): Flow<SystemCollection?> =
        combine(
            systemCollections.document(id).snapshots(),
            recipeIdsInCollection(id)
        ) { snapshot, recipeIds ->
            snapshot.toObject(FirestoreSystemCollection::class.java)?.toDomain(snapshot.id, recipeIds)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserCustomCollections(userId: String): Flow<List<CustomCollection>> =
        customCollections
            .whereEqualTo("ownerId", userId)
            .snapshots()
            .flatMapLatest { snapshot ->
                val collections = snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(FirestoreCustomCollection::class.java)
                            ?.toDomain(document.id, emptyList())
                    }

                if (collections.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(collections.map { collection ->
                        recipeIdsInCollection(collection.id).map { recipeIds ->
                            collection.copy(recipeIds = recipeIds)
                        }
                    }) { updatedCollections ->
                        updatedCollections.toList()
                    }
                }
            }

    override fun getFavourites(userId: String): Flow<SystemCollection?> =
        combine(
            favouritesDocument(userId).snapshots(),
            recipeIdsInCollection(favouritesDocument(userId).id)
        ) { snapshot, recipeIds ->
            snapshot.toObject(FirestoreSystemCollection::class.java)?.toDomain(snapshot.id, recipeIds)
        }

    override suspend fun createSystemCollections(userId: String) {
        val favourites = SystemCollection(
            id = systemCollectionDocumentId(userId, SystemCollectionType.FAVOURITES),
            ownerId = userId,
            type = SystemCollectionType.FAVOURITES,
            recipeIds = emptyList()
        )

        systemCollections
            .document(favourites.id)
            .set(favourites.toFirestore())
            .await()
    }

    override suspend fun toggleFavourite(userId: String, recipeId: String) {
        val (isLiked, recipeAuthorId) = firestore.runTransaction { transaction ->
            val docRef = favouritesDocument(userId)
            val ownershipRef = collectionOwnerships.document(ownershipDocumentId(docRef.id, recipeId))
            val recipeRef = firestore.collection(Collections.RECIPES).document(recipeId)
            val favouritesSnapshot = transaction.get(docRef)
            val ownershipSnapshot = transaction.get(ownershipRef)
            val recipeSnapshot = transaction.get(recipeRef)
            check(favouritesSnapshot.exists()) { "Favourites collection not found for user $userId" }

            val authorId = recipeSnapshot.getString("authorId")
            if (ownershipSnapshot.exists()) {
                transaction.delete(ownershipRef)
                false to authorId
            } else {
                transaction.set(
                    ownershipRef,
                    FirestoreCollectionOwnership(
                        collectionId = docRef.id,
                        recipeId = recipeId
                    )
                )
                true to authorId
            }
        }.await()
        if (recipeAuthorId != null && recipeAuthorId != userId) {
            val deterministicNotificationId = "like_${userId}_${recipeId}"
            if (isLiked){
                val authorDoc = firestore.collection(Collections.USERS).document(recipeAuthorId).get().await()
                val globalEnabled = authorDoc.getBoolean("receiveNotification") ?: true
                val likeEnabled = authorDoc.getBoolean("receiveLikeNotification") ?: true
                if (globalEnabled && likeEnabled) {
                    val notification = Notification(
                        id = deterministicNotificationId,
                        userId = recipeAuthorId,
                        type = NotificationType.LIKE,
                        triggerUserId = userId,
                        recipeId = recipeId,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    notificationRepository.insertNotification(notification)
                }
            }
            else{
                notificationRepository.deleteNotification(deterministicNotificationId)
            }
        }
    }

    override suspend fun toggleRecipeInCustomCollection(collectionId: String, recipeId: String) {
        firestore.runTransaction { transaction ->
            val ownershipRef = collectionOwnerships.document(ownershipDocumentId(collectionId, recipeId))
            val ownershipSnapshot = transaction.get(ownershipRef)

            if (ownershipSnapshot.exists()) {
                transaction.delete(ownershipRef)
            } else {
                transaction.set(
                    ownershipRef,
                    FirestoreCollectionOwnership(
                        collectionId = collectionId,
                        recipeId = recipeId
                    )
                )
            }
        }.await()
    }

    override suspend fun saveCustomCollection(collection: CustomCollection) {
        val docRef = if (collection.id.isBlank()) {
            customCollections.document()
        } else {
            customCollections.document(collection.id)
        }

        docRef.set(collection.copy(id = docRef.id).toFirestore()).await()
    }

    override suspend fun deleteCustomCollection(collectionId: String) {
        deleteOwnershipsByCollection(collectionId)
        customCollections.document(collectionId).delete().await()
    }

    override suspend fun deleteCollectionsByUser(userId: String) {
        val customCollectionDocuments = customCollections
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
            .documents
        val systemCollectionDocuments = systemCollections
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
            .documents

        (customCollectionDocuments + systemCollectionDocuments).forEach { document ->
            deleteOwnershipsByCollection(document.id)
        }

        customCollectionDocuments.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().await()
        }

        systemCollectionDocuments.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().await()
        }
    }

    override suspend fun deleteOwnershipsByRecipe(recipeId: String) {
        val documents = collectionOwnerships
            .whereEqualTo("recipeId", recipeId)
            .get()
            .await()
            .documents

        documents.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().await()
        }
    }

    private suspend fun deleteOwnershipsByCollection(collectionId: String) {
        val documents = collectionOwnerships
            .whereEqualTo("collectionId", collectionId)
            .get()
            .await()
            .documents

        documents.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().await()
        }
    }

    private fun recipeIdsInCollection(collectionId: String): Flow<List<String>> =
        collectionOwnerships
            .whereEqualTo("collectionId", collectionId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(FirestoreCollectionOwnership::class.java)?.recipeId
                    }
                    .distinct()
            }

    private fun favouritesDocument(userId: String) =
        systemCollections.document(systemCollectionDocumentId(userId, SystemCollectionType.FAVOURITES))

    private fun ownershipDocumentId(collectionId: String, recipeId: String): String =
        "${collectionId}_${recipeId}"
}

private fun systemCollectionDocumentId(userId: String, type: SystemCollectionType): String =
    "${userId}_${type.name.lowercase()}"