package com.example.fridgeproject.domain

import com.example.fridgeproject.model.RecipeWithStats
import kotlinx.coroutines.flow.Flow

interface RecipeWithStatsRepository {
    fun getRecipeWithRatingById(id: String): Flow<RecipeWithStats?>

    fun getRecipesWithRatingByIds(ids: List<String>): Flow<List<RecipeWithStats>>

    fun getRecipesWithRatingByAuthor(authorId: String): Flow<List<RecipeWithStats>>

    fun filterRecipesWithRating(query: String): Flow<List<RecipeWithStats>>
}