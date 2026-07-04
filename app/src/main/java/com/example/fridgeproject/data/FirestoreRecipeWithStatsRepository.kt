package com.example.fridgeproject.data

import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.RecipeWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FirestoreRecipeWithStatsRepository(
    private val recipeRepository: RecipeRepository,
    private val reviewRepository: ReviewRepository
) : RecipeWithStatsRepository {

    override fun getRecipeWithRatingById(id: String): Flow<RecipeWithStats?> =
        combine(
            recipeRepository.getRecipeById(id),
            reviewRepository.getAverageRatingByRecipe(id)
        ) { recipe, rating ->
            recipe?.let {
                RecipeWithStats(
                    recipe = it,
                    rating = rating
                )
            }
        }

    override fun getRecipesWithRatingByIds(ids: List<String>): Flow<List<RecipeWithStats>> =
        combine(
            recipeRepository.getRecipesByIds(ids),
            reviewRepository.getAverageRatingsByRecipe()
        ) { recipes, ratings ->
            recipes.map { it.withRating(ratings) }
        }

    override fun getRecipesWithRatingByAuthor(authorId: String): Flow<List<RecipeWithStats>> =
        combine(
            recipeRepository.getRecipeByAuthor(authorId),
            reviewRepository.getAverageRatingsByRecipe()
        ) { recipes, ratings ->
            recipes.map { it.withRating(ratings) }
        }

    override fun filterRecipesWithRating(query: String): Flow<List<RecipeWithStats>> =
        combine(
            recipeRepository.filterRecipes(query),
            reviewRepository.getAverageRatingsByRecipe()
        ) { recipes, ratings ->
            recipes.map { it.withRating(ratings) }
        }

    private fun Recipe.withRating(ratings: Map<String, Float>): RecipeWithStats =
        RecipeWithStats(
            recipe = this,
            rating = ratings[id] ?: 0f
        )
}