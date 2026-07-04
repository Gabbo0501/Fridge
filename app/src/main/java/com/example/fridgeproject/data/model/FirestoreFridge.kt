package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Fridge
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure

data class FirestoreFridge(
    val ownerId: String = "",
    val ingredients: List<FirestoreIngredientQuantityWithTime> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreIngredientQuantityWithTime(
    val name: String = "",
    val quantity: Float = 0f,
    val unit: String = UnitOfMeasure.G.name,
    val insertedAt: Long = System.currentTimeMillis()
)

fun FirestoreFridge.toDomain(id: String, ingredientsMap: Map<String, Ingredient>): Fridge =
    Fridge(
        id = id,
        ownerId = ownerId,
        ingredients = ingredients.map { it.toDomain(ingredientsMap) },
        updatedAt = updatedAt
    )

fun Fridge.toFirestore(): FirestoreFridge =
    FirestoreFridge(
        ownerId = ownerId,
        ingredients = ingredients.map { it.toFirestore() },
        updatedAt = updatedAt
    )

fun FirestoreIngredientQuantityWithTime.toDomain(ingredientsMap: Map<String, Ingredient>): IngredientQuantityWithTime {
    val domainIngredient = ingredientsMap[name] ?: Ingredient(
        name = name,
        category = IngredientCategory.OTHERS,
        defaultUnit = unit.toUnitOfMeasure()
    )

    return IngredientQuantityWithTime(
        ingredient = domainIngredient,
        quantity = quantity,
        unit = unit.toUnitOfMeasure(),
        insertedAt = insertedAt
    )
}

fun IngredientQuantityWithTime.toFirestore(): FirestoreIngredientQuantityWithTime =
    FirestoreIngredientQuantityWithTime(
        name = ingredient.name,
        quantity = quantity,
        unit = unit.name,
        insertedAt = insertedAt
    )

private fun String.toUnitOfMeasure(): UnitOfMeasure =
    try {
        UnitOfMeasure.valueOf(uppercase())
    } catch (e: Exception) {
        UnitOfMeasure.G
    }