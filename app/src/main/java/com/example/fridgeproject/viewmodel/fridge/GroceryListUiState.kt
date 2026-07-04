package com.example.fridgeproject.viewmodel.fridge

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.model.enums.IngredientWizardStep

data class GroceryListUiState (
    val groceryListId: String? = null,
    val groceryListIngredients: List<IngredientQuantityWithTime> = emptyList(),
    val selectedIngredients: List<IngredientQuantityWithTime> = emptyList(),
    val addingIngredient: Boolean = false,
    val newIngredient: IngredientQuantity = IngredientQuantity(),
    val addingIngredientStep: IngredientWizardStep = IngredientWizardStep.CATEGORY,
    val selectableIngredients: List<Ingredient> = emptyList(),
    val quantityInput: String = "0",
    val showExitDialog: Boolean = false,
    val isConfirmQuantityEnabled: Boolean = false,
    val showClearGroceryListDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)