package com.example.fridgeproject.model

data class GroceryList (
    val id: String = "",
    val ownerId: String = "",
    val ingredients: List<IngredientQuantityWithTime> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)