package com.example.fridgeproject.ui.components.wizard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.model.enums.IngredientWizardStep
import com.example.fridgeproject.ui.components.wizard.steps.WizardCategoryStep
import com.example.fridgeproject.ui.components.wizard.steps.WizardIngredientStep
import com.example.fridgeproject.ui.components.wizard.steps.WizardQuantityStep

@Composable
fun IngredientWizard(
    step: IngredientWizardStep,
    selectedCategory: IngredientCategory,
    ingredients: List<Ingredient>,
    selectedIngredient: String,
    quantityText: String,
    unit: UnitOfMeasure,
    isConfirmEnabled: Boolean,
    onClose: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onCategorySelect: (IngredientCategory) -> Unit,
    onIngredientSelect: (String) -> Unit,
    onQuantityTextChange: (String) -> Unit,
    onIncrementQuantity: () -> Unit,
    onDecrementQuantity: () -> Unit,
    onConfirm: () -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {

    when (step) {
        IngredientWizardStep.CATEGORY -> WizardCategoryStep(
            selected = selectedCategory,
            onSelect = onCategorySelect,
            onActionClick = onNext,
            stepCount = 3,
            onCloseClick = onClose,
            modifier = modifier,
            isActionEnabled = selectedCategory != IngredientCategory.OTHERS
        )

        IngredientWizardStep.INGREDIENT -> WizardIngredientStep(
            category = selectedCategory,
            ingredients = ingredients,
            selected = selectedIngredient,
            onSelect = onIngredientSelect,
            onActionClick = onNext,
            stepCount = 3,
            onBackClick = onBack,
            onCloseClick = onClose,
            modifier = modifier,
            isActionEnabled = selectedIngredient.isNotBlank()
        )

        IngredientWizardStep.QUANTITY -> WizardQuantityStep(
            ingredient = selectedIngredient,
            category = selectedCategory,
            quantityText = quantityText,
            unit = unit,
            onQuantityTextChange = onQuantityTextChange,
            onActionClick = onConfirm,
            onIncrementClick = onIncrementQuantity,
            onDecrementClick = onDecrementQuantity,
            stepCount = 3,
            onBackClick = if (isEditMode) null else onBack,
            onCloseClick = onClose,
            isConfirmEnabled = isConfirmEnabled,
            modifier = modifier
        )
    }
}