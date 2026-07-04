package com.example.fridgeproject.viewmodel.user

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.AllergenWizardStep
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory

data class EditDietUiState(
    val diet: Diet = Diet.OMNIVORE,
    val allergens: List<Ingredient> = emptyList(),
    val addingAllergen: Boolean = false,
    val allergenWizardStep: AllergenWizardStep = AllergenWizardStep.CATEGORY,
    val selectedAllergenCategory: IngredientCategory = IngredientCategory.OTHERS,
    val selectableAllergens: List<Ingredient> = emptyList(),
    val selectedAllergenName: String = "",
    val showExitDialog: Boolean = false,
    val globalError : String = ""
)

fun EditDietUiState.canCheckUnsavedChanges(): Boolean =
    globalError.isBlank()

fun EditDietUiState.hasChangesFrom(originalDiet: Diet, originalAllergens: List<Ingredient>): Boolean =
    addingAllergen ||
            diet != originalDiet ||
            allergens.map { it.name }.toSet() != originalAllergens.map { it.name }.toSet()