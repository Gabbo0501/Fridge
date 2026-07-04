package com.example.fridgeproject.data

import android.content.Context
import com.example.fridgeproject.domain.ImageStorageRepository
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.StoredImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import java.util.UUID
import kotlinx.coroutines.tasks.await

class FirebaseImageStorageRepository(
    private val context: Context,
    private val storage: FirebaseStorage
) : ImageStorageRepository {

    override suspend fun uploadRecipeCover(recipeId: String, input: LocalImageInput): StoredImage =
        upload(path = "recipes/$recipeId/cover.jpg", input = input)

    override suspend fun uploadRecipeStep(
        recipeId: String,
        input: LocalImageInput
    ): StoredImage =
        upload(path = "recipes/$recipeId/steps/${UUID.randomUUID()}.jpg", input = input)

    override suspend fun uploadProfileImage(userId: String, input: LocalImageInput): StoredImage =
        upload(path = "users/$userId/profile.jpg", input = input)

    override suspend fun uploadReviewImage(
        reviewId: String,
        input: LocalImageInput
    ): StoredImage =
        upload(path = "reviews/$reviewId/${UUID.randomUUID()}.jpg", input = input)

    override suspend fun deleteReviewImage(image: StoredImage) {
        deletePath(image.storagePath)
    }

    override suspend fun deleteProfileImages(userId: String) {
        if (userId.isBlank()) return
        deleteFolder("users/$userId")
    }

    override suspend fun deleteRecipeStepImage(imageUrl: String) {
        deleteUrl(imageUrl)
    }

    override suspend fun deleteRecipeImages(recipeId: String) {
        if (recipeId.isBlank()) return
        deleteFolder("recipes/$recipeId")
    }

    override suspend fun deleteReviewImages(reviewId: String) {
        if (reviewId.isBlank()) return
        deleteFolder("reviews/$reviewId")
    }

    private suspend fun upload(path: String, input: LocalImageInput): StoredImage {
        val ref = storage.reference.child(path)
        context.contentResolver.openInputStream(input.uri).use { stream ->
            requireNotNull(stream) { "Cannot open image input stream" }
            ref.putStream(stream).await()
        }
        return StoredImage(
            url = ref.downloadUrl.await().toString(),
            storagePath = path
        )
    }

    private suspend fun deletePath(path: String) {
        if (path.isBlank()) return
        try {
            storage.reference.child(path).delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    private suspend fun deleteFolder(path: String) {
        val result = storage.reference.child(path).listAll().await()
        result.items.forEach { item ->
            item.delete().await()
        }
        result.prefixes.forEach { prefix ->
            deleteFolder(prefix.path)
        }
    }

    private suspend fun deleteUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            storage.getReferenceFromUrl(url).delete().await()
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) true else throw e
        }
    }
}
