package com.example.fridgeproject.viewmodel.fridge

import com.example.fridgeproject.model.RecipeWithStats
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.PrepTime

data class FridgeRecipeListUiState(
    val displayedRecipes: List<RecipeWithStats> = emptyList(),
    val doableRecipes: List<RecipeWithStats> = emptyList(),
    val missingQuantityRecipes: List<RecipeWithStats> = emptyList(),
    val searchIngredientsQuery: String = "",
    val selectedCategory: DishType? = null,
    val selectedCookingTime: PrepTime? = null,
    val selectedDifficulty: Difficulty? = null,
    val difficultySliderPosition: Float = 0f,
    val selectedCostRange: CostRange? = null,
    val costSliderPosition: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)

fun FridgeRecipeListUiState.hasActiveFilters(): Boolean =
    searchIngredientsQuery.isNotBlank() ||
        selectedCategory != null ||
        selectedDifficulty != null ||
        selectedCostRange != null ||
        selectedCookingTime != null