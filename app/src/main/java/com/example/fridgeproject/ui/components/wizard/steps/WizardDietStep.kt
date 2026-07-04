package com.example.fridgeproject.ui.components.wizard.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.user.UserAllergenSelector
import com.example.fridgeproject.ui.components.user.UserDietSelector
import com.example.fridgeproject.ui.components.wizard.shared.WizardButton
import com.example.fridgeproject.ui.components.wizard.shared.WizardProgressHeader

@Composable
fun WizardDietStep(
    diet: Diet,
    allergens: List<Ingredient>,
    onDietChange: (Diet) -> Unit,
    onAddAllergenClick: () -> Unit,
    onRemoveAllergen: (Ingredient) -> Unit,
    onActionClick: () -> Unit,
    stepCount: Int,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onSkipClick: (() -> Unit)? = null,
    isActionEnabled: Boolean = true
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues()
    ) {
        item {
            WizardProgressHeader(
                title = "Setup",
                stepIndex = 2,
                stepCount = stepCount,
                onBack = onBackClick,
                rightTextAction = if (onSkipClick != null) "Skip" else null,
                onRightTextAction = onSkipClick,
                sideSlotSize = 40.dp,
                horizontalPadding = 12.dp,
                verticalPadding = 10.dp
            )
        }

        item {
            UserDietSelector(
                selected = diet,
                onSelect = onDietChange,
                modifier = Modifier
                    .height(560.dp)
                    .padding(horizontal = 24.dp)
            )
        }
        item {
            UserAllergenSelector(
                allergens = allergens,
                onAddAllergenClick = onAddAllergenClick,
                onRemoveAllergen = onRemoveAllergen,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            WizardButton(
                enabled = isActionEnabled,
                onClick = onActionClick,
                text = "Finish",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = PageBottomPadding)
            )
        }
    }
}