package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure

data class FirestoreIngredient(
    val name: String = "",
    val category: String = "",
    val defaultUnit: String = ""
)

fun FirestoreIngredient.toDomain(): Ingredient = Ingredient(
    name = name,
    category = category.toIngredientCategory(),
    defaultUnit = defaultUnit.toUnitOfMeasure()
)

fun Ingredient.toFirestore(): FirestoreIngredient = FirestoreIngredient(
    name = name,
    category = category.name,
    defaultUnit = defaultUnit.name
)

private fun String.toIngredientCategory(): IngredientCategory =
    try {
        IngredientCategory.valueOf(uppercase())
    } catch (e: Exception) {
        IngredientCategory.OTHERS
    }

private fun String.toUnitOfMeasure(): UnitOfMeasure =
    try {
        UnitOfMeasure.valueOf(uppercase())
    } catch (e: Exception) {
        UnitOfMeasure.G
    }