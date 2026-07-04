package com.example.fridgeproject.viewmodel.auth

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.enums.AllergenWizardStep
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.OnboardingStep

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.PROFILE,
    val firstName: String = "",
    val lastName: String = "",
    val profileImage: ProfileImageSource = ProfileImageSource.Monogram,
    val cookingRole: CookingRole = CookingRole.AMATEUR,
    val shortBio: String = "",
    val socialProfiles: List<SocialProfile> = emptyList(),
    val diet: Diet = Diet.OMNIVORE,
    val allergens: List<Ingredient> = emptyList(),
    val addingAllergen: Boolean = false,
    val allergenWizardStep: AllergenWizardStep = AllergenWizardStep.CATEGORY,
    val selectedAllergenCategory: IngredientCategory = IngredientCategory.OTHERS,
    val selectableAllergens: List<Ingredient> = emptyList(),
    val selectedAllergenName: String = "",
    val isLoading: Boolean = false,
    val showExitDialog: Boolean = false,
    val error: String = ""
)
