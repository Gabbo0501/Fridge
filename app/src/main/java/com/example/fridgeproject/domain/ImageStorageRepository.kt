package com.example.fridgeproject.domain

import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.StoredImage

interface ImageStorageRepository {
    suspend fun uploadRecipeCover(recipeId: String, input: LocalImageInput): StoredImage
    suspend fun uploadRecipeStep(recipeId: String, input: LocalImageInput): StoredImage
    suspend fun uploadProfileImage(userId: String, input: LocalImageInput): StoredImage
    suspend fun uploadReviewImage(reviewId: String, input: LocalImageInput): StoredImage
    suspend fun deleteReviewImage(image: StoredImage)
    suspend fun deleteProfileImages(userId: String)
    suspend fun deleteRecipeStepImage(imageUrl: String)
    suspend fun deleteRecipeImages(recipeId: String)
    suspend fun deleteReviewImages(reviewId: String)
}
