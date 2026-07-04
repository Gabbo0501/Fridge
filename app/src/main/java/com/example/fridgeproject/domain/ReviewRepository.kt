package com.example.fridgeproject.domain

import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewById(id: String): Flow<Review?>

    fun getReviewsByRecipe(recipeId: String): Flow<List<Review>>

    fun getReviewsByUser(userId: String): Flow<List<Review>>

    fun getReviewsCountByUser(userId: String): Flow<Int>

    fun getAverageRatingByRecipe(recipeId: String): Flow<Float>

    fun getAverageRatingsByRecipe(): Flow<Map<String, Float>>

    suspend fun saveReview(review: Review, pendingImages: List<LocalImageInput> = emptyList())

    suspend fun deleteReview(id: String)

    suspend fun deleteReviewsByRecipe(recipeId: String)
}