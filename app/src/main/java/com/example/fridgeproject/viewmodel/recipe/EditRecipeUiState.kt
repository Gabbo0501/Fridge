package com.example.fridgeproject.viewmodel.recipe

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.RecipeStep
import com.example.fridgeproject.model.enums.*

data class EditRecipeUiState(
    // Business States
    val title: String = "",
    val description: String = "",
    val image: String? = null,
    val dishType: DishType = DishType.FIRST_COURSE,
    val costRange: CostRange = CostRange.FIVE,
    val difficulty: Difficulty = Difficulty.ONE,
    val preparationTimeSec: Long = 0L,
    val ingredients: List<IngredientQuantity> = emptyList(),
    val newIngredient: IngredientQuantity = IngredientQuantity(),
    val selectableIngredients : List<Ingredient> = emptyList(),
    val preparationSteps: List<RecipeStep> = emptyList(),
    val suitableDiets: List<Diet> = emptyList(),
    val cuisine: List<Cuisine> = emptyList(),
    val pendingImageInput: LocalImageInput? = null,
    val pendingStepImageInputs: Map<Int, LocalImageInput> = emptyMap(),
    val quantityInput: String = "0",
    val isConfirmQuantityEnabled: Boolean = false,
    val isEditMode: Boolean = false,

    // Add ingredients UI-only States
    val addingIngredient: Boolean = false,
    val addingIngredientStep : IngredientWizardStep = IngredientWizardStep.CATEGORY,

    //Edit Steps
    val currentEditedStepIndex: Int = 0,

    // UI-only States
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showExitDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val showDifficultyDialog: Boolean = false,
    val showTimeDialog: Boolean = false,
    val showCostDialog: Boolean = false,
    val showDietDialog: Boolean = false,
    val showCuisineDialog: Boolean = false,

    // Error States
    val errors: EditRecipeErrors = EditRecipeErrors()
)

data class EditRecipeErrors(
    val globalError: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val imageError: String? = null,
    val prepTimeError: String? = null,
    val ingredientsError: String? = null,
    val stepsError: String? = null,
    val stepsImageError: String? = null,
    val stepsDescError: String? = null
)

fun EditRecipeUiState.canCheckUnsavedChanges(): Boolean =
    !isLoading && !isSaving && !isSaved && errors.globalError.isNullOrBlank()
    
 fun EditRecipeUiState.hasChangesFrom(original: Recipe): Boolean =
    addingIngredient ||
            pendingImageInput != null ||
            pendingStepImageInputs.isNotEmpty() ||
            title != original.title ||
            description != original.description ||
            image != original.image ||
            dishType != original.dishType ||
            costRange != original.costRange ||
            difficulty != original.difficulty ||
            preparationTimeSec != original.preparationTimeSec ||
            ingredients != original.ingredients ||
            preparationSteps != original.preparationSteps ||
            suitableDiets != original.suitableDiets ||
            cuisine != original.cuisine