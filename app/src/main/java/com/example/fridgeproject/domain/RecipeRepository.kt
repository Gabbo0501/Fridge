package com.example.fridgeproject.domain

import com.example.fridgeproject.model.LocalImageInput
import kotlinx.coroutines.flow.Flow
import com.example.fridgeproject.model.Recipe

interface RecipeRepository {

    fun getRecipeById(id: String): Flow<Recipe?>
    fun getRecipesByIds(ids: List<String>): Flow<List<Recipe>>

    fun getRecipeByAuthor(authorId: String): Flow<List<Recipe>>

    suspend fun saveRecipe(
        recipe: Recipe,
        pendingCoverImage: LocalImageInput? = null,
        pendingStepImages: Map<Int, LocalImageInput> = emptyMap()
    )

    suspend fun deleteRecipe(id: String)

    fun filterRecipes(query: String): Flow<List<Recipe>>

}