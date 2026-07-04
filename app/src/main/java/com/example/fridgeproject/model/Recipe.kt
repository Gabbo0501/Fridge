package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Cuisine

data class Recipe(
    val id: String = "",
    val authorId: String = "",
    val author: String = "",
    val remixedFromRecipeId: String? = null,
    val title: String = "",
    val description: String = "",
    val image: String? = null,
    val dishType: DishType = DishType.FIRST_COURSE,
    val suitableDiets: List<Diet> = emptyList(),
    val cuisine: List<Cuisine> = emptyList(),
    val costRange: CostRange = CostRange.FIVE,
    val difficulty: Difficulty = Difficulty.ONE,
    val preparationTimeSec: Long = 0L,
    val likes: Int = 0,
    val ingredients: List<IngredientQuantity> = emptyList(),
    val preparationSteps: List<RecipeStep> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class RecipeWithStats(
    val recipe: Recipe,
    val rating: Float = 0f
)