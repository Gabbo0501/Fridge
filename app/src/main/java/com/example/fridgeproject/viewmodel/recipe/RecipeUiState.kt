package com.example.fridgeproject.viewmodel.recipe

import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.RecipeUi

data class RecipeUiState (
    val content: RecipeContentState = RecipeContentState(),
    val permissions: RecipePermissionsState = RecipePermissionsState(),
    val deleteDialog: RecipeDeleteDialogState = RecipeDeleteDialogState(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RecipeContentState(
    val recipe: RecipeUi? = null,
    val percentageFridge: Int = 0,
    val missingIngredients: List<IngredientQuantity> = emptyList(),
    val missingQuantityIngredients: List<MissingQuantityDetail> = emptyList(),
    val isFavorite: Boolean = false
)

data class RecipePermissionsState(
    val isOwner: Boolean = false,
    val canAddReview: Boolean = false,
    val canAddTip: Boolean = false
)

data class RecipeDeleteDialogState(
    val showDeleteRecipeDialog: Boolean = false,
    val reviewToDeleteId: String? = null,
    val tipToDeleteId: String? = null
)

data class MissingQuantityDetail(
    val recipeRequirement: IngredientQuantity,
    val availableInFridge: Int
)