package com.example.fridgeproject.ui.components.wizard

import androidx.compose.runtime.Composable
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.OnboardingStep
import com.example.fridgeproject.ui.components.wizard.steps.WizardDietStep
import com.example.fridgeproject.ui.components.wizard.steps.WizardProfileStep
import com.example.fridgeproject.viewmodel.auth.OnboardingUiState

@Composable
fun OnboardingWizard(
    uiState: OnboardingUiState,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
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
    if (uiState.addingAllergen) {
        AllergenWizard(
            step = uiState.allergenWizardStep,
            selectedCategory = uiState.selectedAllergenCategory,
            ingredients = uiState.selectableAllergens,
            selectedIngredient = uiState.selectedAllergenName,
            onClose = onCloseAllergenWizard,
            onBack = onBackAllergenWizard,
            onCategorySelect = onAllergenCategoryChange,
            onCategoryNext = onAllergenCategoryNext,
            onIngredientSelect = onAllergenNameChange,
            onConfirm = onConfirmAllergen
        )
        return
    }

    when (uiState.currentStep) {
        OnboardingStep.PROFILE -> WizardProfileStep(
            firstName = uiState.firstName,
            lastName = uiState.lastName,
            profileImage = uiState.profileImage,
            cookingRole = uiState.cookingRole,
            shortBio = uiState.shortBio,
            socialProfiles = uiState.socialProfiles,
            onProfileCameraClick = onProfileCameraClick,
            onProfileGalleryClick = onProfileGalleryClick,
            onRemoveProfileImageClick = onRemoveProfileImageClick,
            onCookingRoleChange = onCookingRoleChange,
            onShortBioChange = onShortBioChange,
            onSocialProfilesChange = onSocialProfilesChange,
            onActionClick = onNextClick,
            stepCount = 2,
            onSkipClick = onSkipClick
        )

        OnboardingStep.DIET -> WizardDietStep(
            diet = uiState.diet,
            allergens = uiState.allergens,
            onDietChange = onDietChange,
            onAddAllergenClick = onOpenAllergenWizard,
            onRemoveAllergen = onRemoveAllergen,
            onActionClick = onNextClick,
            stepCount = 2,
            onBackClick = onBackClick,
            onSkipClick = onSkipClick
        )
    }
}