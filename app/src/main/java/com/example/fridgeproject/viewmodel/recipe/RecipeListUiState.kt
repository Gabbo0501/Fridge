package com.example.fridgeproject.viewmodel.recipe

import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.RecipeWithStats
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.PrepTime

enum class ScreenPhase { EXPLORE, FILTER, RESULTS }

data class RecipeListUiState (
    val recipes: List<RecipeWithStats> = emptyList(),
    val userNickname: String? = "", // per la HomePage
    val dailyMenu: List<RecipeWithStats> = emptyList(),
    val displayedRecipes: List<RecipeWithStats> = emptyList(),
    val popularRecipes: List<RecipeWithStats> = emptyList(), // più like
    val newRecipes: List<RecipeWithStats> = emptyList(), // più recenti
    val featuredRecipes: List<RecipeWithStats> = emptyList(), // maggiore rating
    val isLoading: Boolean = false,
    val searchRecipeQuery: String = "",
    val searchAuthorQuery: String = "",
    val searchIngredientsQuery: String = "",
    val phase: ScreenPhase = ScreenPhase.EXPLORE,
    val selectedCategory: DishType? = null,
    val selectedDifficulty: Difficulty? = null,
    val selectedCostRange: CostRange? = null,
    val selectedCookingTime: PrepTime? = null,
    val selectedRecipe: Recipe? = null,
    val costSliderPosition: Float = 0f,
    val difficultySliderPosition: Float = 0f,
)

fun RecipeListUiState.hasActiveFilters(): Boolean =
    searchRecipeQuery.isNotBlank() ||
        searchAuthorQuery.isNotBlank() ||
        searchIngredientsQuery.isNotBlank() ||
        selectedCategory != null ||
        selectedDifficulty != null ||
        selectedCostRange != null ||
        selectedCookingTime != null