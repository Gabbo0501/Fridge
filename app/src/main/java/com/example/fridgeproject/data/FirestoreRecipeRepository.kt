package com.example.fridgeproject.data

import android.util.Log
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.data.model.FirestoreRecipe
import com.example.fridgeproject.data.model.FirestoreUser
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.AuthRepository
import com.example.fridgeproject.domain.ImageStorageRepository
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.RecipeStep
import com.example.fridgeproject.model.enums.NotificationType
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRecipeRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val notificationRepository: NotificationRepository,
    private val ingredientRepository: IngredientRepository
) : RecipeRepository {

    private val recipesCollection = firestore.collection(Collections.RECIPES)
    private val usersCollection = firestore.collection(Collections.USERS)

    override fun getRecipeById(id: String): Flow<Recipe?> =
        recipeFlow(recipesCollection.whereEqualTo(FieldPath.documentId(), id)).map { it.firstOrNull() }

    override fun getRecipesByIds(ids: List<String>): Flow<List<Recipe>> =
        if (ids.isEmpty()) {
            flowOf(emptyList())
        } else {
            recipeFlow(recipesCollection).map { recipes ->
                recipes.filter { it.id in ids }
            }
        }

    override fun getRecipeByAuthor(authorId: String): Flow<List<Recipe>> =
        recipeFlow(recipesCollection.whereEqualTo("authorId", authorId))

    override suspend fun saveRecipe(
        recipe: Recipe,
        pendingCoverImage: LocalImageInput?,
        pendingStepImages: Map<Int, LocalImageInput>
    ) {
        val docRef = if (recipe.id.isBlank()) {
            recipesCollection.document()
        } else {
            recipesCollection.document(recipe.id)
        }
        val recipeId = docRef.id
        val ingredientsMap = ingredientRepository.getAllIngredients().first().associateBy { it.name }

        val existingRecipe = docRef.get().await()
            .toObject(FirestoreRecipe::class.java)
            ?.toDomain(id = recipeId, authorNickname = recipe.author, ingredientsMap = ingredientsMap)

        val currentUserId = authRepository.currentUserId
        val authorIdToSave = existingRecipe?.authorId ?: recipe.authorId.ifBlank { currentUserId.orEmpty() }
        if (authorIdToSave.isBlank()) return

        val coverImage = coverImageForSave(recipeId, recipe.image, pendingCoverImage)
        val steps = stepsForSave(recipeId, recipe.preparationSteps, pendingStepImages)
        val now = System.currentTimeMillis()
        val createdAtToSave = existingRecipe?.createdAt ?: recipe.createdAt

        val recipeToSave = recipe.copy(
            id = recipeId,
            authorId = authorIdToSave,
            image = coverImage,
            preparationSteps = steps,
            createdAt = createdAtToSave,
            updatedAt = now
        )

        docRef.set(recipeToSave.toFirestore()).await()
        if (existingRecipe != null) {
            removedStepImages(existingRecipe, steps).forEach { step ->
                imageStorageRepository.deleteRecipeStepImage(step.image)
            }
        }
        if (recipe.remixedFromRecipeId != null && existingRecipe == null) {
            try {
                val originalRecipeSnapshot = recipesCollection.document(recipe.remixedFromRecipeId).get().await()
                val originalAuthorId = originalRecipeSnapshot.getString("authorId")

                if (!originalAuthorId.isNullOrBlank() && originalAuthorId != authorIdToSave) {

                    val authorDoc = usersCollection.document(originalAuthorId).get().await()
                    val globalEnabled = authorDoc.getBoolean("receiveNotification") ?: true
                    val remixEnabled = authorDoc.getBoolean("receiveRemixNotification") ?: true

                    if (globalEnabled && remixEnabled) {
                        val deterministicNotificationId = "remix_${recipeId}"
                        val notification = Notification(
                            id = deterministicNotificationId,
                            userId = originalAuthorId,
                            type = NotificationType.REMIX,
                            triggerUserId = authorIdToSave,
                            recipeId = recipeId,
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                        notificationRepository.insertNotification(notification)
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestoreRecipeRepository", "Errore durante l'invio della notifica di remix", e)
            }
        }

        if (existingRecipe == null && !recipeToSave.suitableDiets.isEmpty()) {
            try {
                val usersSnapshot = usersCollection
                    .whereIn("diet", recipeToSave.suitableDiets)
                    .get()
                    .await()

                for (document in usersSnapshot.documents) {
                    val targetUserId = document.id

                    val globalEnabled = document.getBoolean("receiveNotification") ?: true
                    val newRecipeEnabled = document.getBoolean("receiveNewRecipeNotification") ?: true

                    if (targetUserId != authorIdToSave && globalEnabled && newRecipeEnabled) {
                        val deterministicNotificationId = "diet_${recipeId}_${targetUserId}"

                        val notification = Notification(
                            id = deterministicNotificationId,
                            userId = targetUserId,
                            type = NotificationType.RECOMMENDED_RECIPE,
                            triggerUserId = authorIdToSave,
                            recipeId = recipeId,
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                        notificationRepository.insertNotification(notification)
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestoreRecipeRepository", "Errore durante l'invio delle notifiche per diete conformi", e)
            }
        }
    }

    override suspend fun deleteRecipe(id: String) {
        try {
            val recipeDoc = recipesCollection.document(id).get().await()
            val isRemix = recipeDoc.getString("remixedFromRecipeId") != null

            imageStorageRepository.deleteRecipeImages(id)
            recipesCollection.document(id).delete().await()

            if (isRemix) {
                val deterministicNotificationId = "remix_${id}"
                notificationRepository.deleteNotification(deterministicNotificationId)
            }

            val dietNotificationsSnapshot = firestore.collection(Collections.NOTIFICATIONS)
                .whereEqualTo("recipeId", id)
                .whereEqualTo("type", "RECOMMENDED_RECIPE")
                .get()
                .await()

            if (!dietNotificationsSnapshot.isEmpty) {
                val batch = firestore.batch()
                for (doc in dietNotificationsSnapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreRecipeRepository", "Errore durante l'eliminazione della ricetta o notifica", e)
        }
    }

    override fun filterRecipes(query: String): Flow<List<Recipe>> =
        recipeFlow(recipesCollection).map { recipes ->
            val normalizedQuery = query.trim()
            recipes.filter {
                normalizedQuery.isBlank() ||
                        it.title.contains(normalizedQuery, ignoreCase = true) ||
                        it.author.contains(normalizedQuery, ignoreCase = true) ||
                        it.authorId.contains(normalizedQuery, ignoreCase = true)
            }
        }

    private fun recipeFlow(query: Query): Flow<List<Recipe>> {
        val ingredientsMapFlow = ingredientRepository.getAllIngredients()
            .map { list -> list.associateBy { it.name } }

        return combine(
            query.snapshots(),
            usersCollection.snapshots(),
            ingredientsMapFlow
        ) { recipeSnapshot, userSnapshot, ingredientsMap ->
            val users = userSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(FirestoreUser::class.java)?.let { user ->
                        document.id to user.nickname
                    }
                }
                .toMap()

            recipeSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(FirestoreRecipe::class.java)?.let { recipe ->
                        document.id to recipe
                    }
                }
                .map { (id, recipe) ->
                    recipe.toDomain(
                        id = id,
                        authorNickname = users[recipe.authorId] ?: "Unknown Author",
                        ingredientsMap = ingredientsMap // <-- Passata al dominio ricetta
                    )
                }
                .sortedBy { it.id }
        }
    }

    private suspend fun coverImageForSave(
        recipeId: String,
        currentImage: String?,
        pendingCoverImage: LocalImageInput?
    ): String? {
        if (pendingCoverImage != null) {
            return imageStorageRepository.uploadRecipeCover(recipeId, pendingCoverImage).url
        }
        return currentImage
    }

    private suspend fun stepsForSave(
        recipeId: String,
        steps: List<RecipeStep>,
        pendingStepImages: Map<Int, LocalImageInput>
    ): List<RecipeStep> =
        steps.mapIndexed { index, step ->
            val pendingStepImage = pendingStepImages[index]
            if (pendingStepImage != null) {
                val uploadedImage = imageStorageRepository.uploadRecipeStep(
                    recipeId = recipeId,
                    input = pendingStepImage
                )
                step.copy(image = uploadedImage.url)
            } else {
                step
            }
        }

    private fun removedStepImages(originalRecipe: Recipe, stepsToSave: List<RecipeStep>): List<RecipeStep> {
        val finalStepImageUrls = stepsToSave.map { it.image }.toSet()
        return originalRecipe.preparationSteps.filter { step ->
            step.image.isNotBlank() && step.image !in finalStepImageUrls
        }
    }
}