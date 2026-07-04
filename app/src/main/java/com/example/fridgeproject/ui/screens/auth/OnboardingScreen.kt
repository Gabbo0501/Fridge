package com.example.fridgeproject.ui.screens.auth

import androidx.compose.runtime.Composable
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.wizard.OnboardingWizard
import com.example.fridgeproject.viewmodel.auth.OnboardingUiState

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onProfileCameraClick: () -> Unit,
    onProfileGalleryClick: () -> Unit,
    onRemoveProfileImageClick: () -> Unit,
    onCookingRoleChange: (CookingRole) -> Unit,
    onShortBioChange: (String) -> Unit,
    onSocialProfilesChange: (List<SocialProfile>) -> Unit,
    onDietChange: (Diet) -> Unit,
    onOpenAllergenWizard: () -> Unit,
    onCloseAllergenWizard: () -> Unit,
    onBackAllergenWizard: () -> Unit,
    onAllergenCategoryChange: (IngredientCategory) -> Unit,
    onAllergenCategoryNext: () -> Unit,
    onAllergenNameChange: (String) -> Unit,
    onConfirmAllergen: () -> Unit,
    onRemoveAllergen: (Ingredient) -> Unit
) {
    if (uiState.showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    OnboardingWizard(
        uiState = uiState,
        onNextClick = onNextClick,
        onBackClick = onBackClick,
        onSkipClick = onSkipClick,
        onProfileCameraClick = onProfileCameraClick,
        onProfileGalleryClick = onProfileGalleryClick,
        onRemoveProfileImageClick = onRemoveProfileImageClick,
        onCookingRoleChange = onCookingRoleChange,
        onShortBioChange = onShortBioChange,
        onSocialProfilesChange = onSocialProfilesChange,
        onDietChange = onDietChange,
        onOpenAllergenWizard = onOpenAllergenWizard,
        onCloseAllergenWizard = onCloseAllergenWizard,
        onBackAllergenWizard = onBackAllergenWizard,
        onAllergenCategoryChange = onAllergenCategoryChange,
        onAllergenCategoryNext = onAllergenCategoryNext,
        onAllergenNameChange = onAllergenNameChange,
        onConfirmAllergen = onConfirmAllergen,
        onRemoveAllergen = onRemoveAllergen
    )
}