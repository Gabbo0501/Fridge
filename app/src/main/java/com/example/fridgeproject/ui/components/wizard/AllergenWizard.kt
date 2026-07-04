package com.example.fridgeproject.ui.components.wizard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.AllergenWizardStep
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.ui.components.wizard.steps.WizardCategoryStep
import com.example.fridgeproject.ui.components.wizard.steps.WizardIngredientStep

@Composable
fun AllergenWizard(
    step: AllergenWizardStep,
    selectedCategory: IngredientCategory,
    ingredients: List<Ingredient>,
    selectedIngredient: String,
    onClose: () -> Unit,
    onBack: () -> Unit,
    onCategorySelect: (IngredientCategory) -> Unit,
    onCategoryNext: () -> Unit,
    onIngredientSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {

    when (step) {
        AllergenWizardStep.CATEGORY -> WizardCategoryStep(
            selected = selectedCategory,
            onSelect = onCategorySelect,
            onActionClick = onCategoryNext,
            stepCount = 2,
            onCloseClick = onClose,
            modifier = modifier,
            isActionEnabled = selectedCategory != IngredientCategory.OTHERS
        )

        AllergenWizardStep.INGREDIENT -> WizardIngredientStep(
            category = selectedCategory,
            ingredients = ingredients,
            selected = selectedIngredient,
            onSelect = onIngredientSelect,
            onActionClick = onConfirm,
            stepCount = 2,
            onBackClick = onBack,
            onCloseClick = onClose,
            modifier = modifier,
            isActionEnabled = selectedIngredient.isNotBlank()
        )
    }
}