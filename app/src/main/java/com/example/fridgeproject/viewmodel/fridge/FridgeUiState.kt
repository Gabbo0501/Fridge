package com.example.fridgeproject.viewmodel.fridge

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.model.enums.IngredientWizardStep

data class FridgeUiState(
    val fridgeId: String? = null,
    val selectedTabIndex: Int = 0,
    val fridgeIngredients: List<IngredientQuantityWithTime> = emptyList(),
    val addingIngredient: Boolean = false,
    val addingIngredientStep: IngredientWizardStep = IngredientWizardStep.CATEGORY,
    val selectableIngredients: List<Ingredient> = emptyList(),
    val newIngredient: IngredientQuantity = IngredientQuantity(),
    val quantityInput: String = "0",
    val isConfirmQuantityEnabled: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val showExitDialog: Boolean = false,
    val showClearFridgeDialog: Boolean = false,
    val error: String? = null
)