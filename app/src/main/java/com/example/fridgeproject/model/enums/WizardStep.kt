package com.example.fridgeproject.model.enums

sealed interface WizardStep

enum class AllergenWizardStep : WizardStep {
    CATEGORY,
    INGREDIENT
}

enum class IngredientWizardStep : WizardStep {
    CATEGORY,
    INGREDIENT,
    QUANTITY
}

enum class OnboardingStep : WizardStep {
    PROFILE,
    DIET
}