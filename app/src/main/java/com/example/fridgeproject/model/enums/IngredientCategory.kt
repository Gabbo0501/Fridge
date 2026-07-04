package com.example.fridgeproject.model.enums

enum class IngredientCategory(val displayName: String, val icon: String) {
    MEAT("Meat", "🥩"),
    FISH("Fish", "🐟"),
    DAIRY("Dairy", "🧀"),
    VEGETABLES("Vegetables", "🥦"),
    FRUITS("Fruits", "🍎"),
    GRAINS_GLUTEN("Grains with Gluten", "🌾"),
    GRAINS_GLUTEN_FREE("Gluten-Free Grains", "🌽"),
    LEGUMES("Legumes", "🫘"),
    EGGS("Eggs", "🥚"),
    SPICES_HERBS("Spices and Herbs", "🌿"),
    OILS_FATS("Oils and Fats", "🫒"),
    SWEETENERS("Sweeteners", "🍯"),
    NUTS_SEEDS("Nuts and Seeds", "🥜"),
    OTHERS("Other", "")
}