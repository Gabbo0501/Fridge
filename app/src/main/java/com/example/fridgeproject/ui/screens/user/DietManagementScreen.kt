package com.example.fridgeproject.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.AllergenWizardStep
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.user.UserAllergenSelector
import com.example.fridgeproject.ui.components.user.UserDietSelector
import com.example.fridgeproject.ui.components.wizard.AllergenWizard
import com.example.fridgeproject.ui.components.recipe.BottomActionRow

@Composable
fun DietManagementScreen(
    diet: Diet,
    allergens: List<Ingredient>,
    addingAllergen: Boolean,
    allergenWizardStep: AllergenWizardStep,
    selectedAllergenCategory: IngredientCategory,
    selectableAllergens: List<Ingredient>,
    selectedAllergenName: String,
    error: String,
    showExitDialog: Boolean,
    onDietChange: (Diet) -> Unit,
    OnOpenAllergenWizard: () -> Unit,
    onCloseAllergenWizard: () -> Unit,
    onBackAllergenWizard: () -> Unit,
    onUpdateAllergenCategory: (IngredientCategory) -> Unit,
    onConfirmAllergenCategory: () -> Unit,
    onUpdateAllergenName: (String) -> Unit,
    onAddSelectedAllergen: () -> Unit,
    onRemoveAllergens: (Ingredient) -> Unit,
    onSavePreferences: () -> Unit,
    onBackClick: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit
){
    if (showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    if (error.isNotBlank()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        return
    }else {
        if (addingAllergen) {
            AllergenWizard(
                step = allergenWizardStep,
                selectedCategory = selectedAllergenCategory,
                ingredients = selectableAllergens,
                selectedIngredient = selectedAllergenName,
                onClose = onCloseAllergenWizard,
                onBack = onBackAllergenWizard,
                onCategorySelect = onUpdateAllergenCategory,
                onCategoryNext = onConfirmAllergenCategory,
                onIngredientSelect = onUpdateAllergenName,
                onConfirm = onAddSelectedAllergen
            )
            return
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 25.dp, top = 8.dp, end = 25.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageHeader(onBackClick = onBackClick) {
                IconButton(onClick = onSavePreferences) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            UserDietSelector(
                selected = diet,
                onSelect = onDietChange
            )
            Spacer(modifier = Modifier.height(40.dp))

            UserAllergenSelector(
                allergens,
                OnOpenAllergenWizard,
                onRemoveAllergens,
                Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))
            BottomActionRow(
                cancel = onBackClick,
                save = onSavePreferences,
                saveLabel = "Save Changes"
            )
            Spacer(modifier = Modifier.height(PageBottomPadding))
        }
    }
}