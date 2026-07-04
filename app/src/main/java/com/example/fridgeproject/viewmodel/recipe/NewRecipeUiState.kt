package com.example.fridgeproject.viewmodel.recipe

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.enums.IngredientWizardStep

data class CreateRecipeUiState (
    val newRecipe: Recipe = Recipe(),
    val addingIngredient: Boolean = false,
    val addingIngredientStep : IngredientWizardStep = IngredientWizardStep.CATEGORY,
    val selectableIngredients : List<Ingredient> = emptyList(),
    val newIngredient: IngredientQuantity = IngredientQuantity(),
    val currentEditedStepIndex: Int = 0,
    val pendingImageInput: LocalImageInput? = null,
    val pendingStepImageInputs: Map<Int, LocalImageInput> = emptyMap(),
    val success: Boolean = false,
    val quantityInput: String = "0",
    val isConfirmQuantityEnabled: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val showExitDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val showDifficultyDialog: Boolean = false,
    val showTimeDialog: Boolean = false,
    val showCostDialog: Boolean = false,
    val showDietDialog: Boolean = false,
    val showCuisineDialog: Boolean = false,
    val errors: CreateRecipeErrors = CreateRecipeErrors()
)

data class CreateRecipeErrors(
    val image : String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val stepImage : String = "",
    val stepDescription : String = "",
    val general : String = ""
)

fun CreateRecipeUiState.canCheckUnsavedChanges(): Boolean =
    !success && !isLoading

fun CreateRecipeUiState.hasChangesFromDefault(): Boolean {
    val defaultRecipe = Recipe()
    return addingIngredient ||
            pendingImageInput != null ||
            pendingStepImageInputs.isNotEmpty() ||
            newRecipe.title.isNotBlank() ||
            newRecipe.description.isNotBlank() ||
            !newRecipe.image.isNullOrBlank() ||
            newRecipe.suitableDiets.isNotEmpty() ||
            newRecipe.cuisine.isNotEmpty() ||
            newRecipe.costRange != defaultRecipe.costRange ||
            newRecipe.difficulty != defaultRecipe.difficulty ||
            newRecipe.preparationTimeSec != defaultRecipe.preparationTimeSec ||
            newRecipe.ingredients.isNotEmpty() ||
            newRecipe.preparationSteps.isNotEmpty()
}