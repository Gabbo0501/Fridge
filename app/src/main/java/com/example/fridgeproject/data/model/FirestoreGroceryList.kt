package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.GroceryList
import com.example.fridgeproject.model.Ingredient

data class FirestoreGroceryList(
    val ownerId: String = "",
    val ingredients: List<FirestoreIngredientQuantityWithTime> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun FirestoreGroceryList.toDomain(id: String, ingredientsMap: Map<String, Ingredient>): GroceryList =
    GroceryList(
        id = id,
        ownerId = ownerId,
        ingredients = ingredients.map { it.toDomain(ingredientsMap) },
        updatedAt = updatedAt
    )

fun GroceryList.toFirestore(): FirestoreGroceryList =
    FirestoreGroceryList(
        ownerId = ownerId,
        ingredients = ingredients.map { it.toFirestore() },
        updatedAt = updatedAt
    )