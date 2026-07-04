package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.UnitOfMeasure

data class IngredientQuantityWithTime(
    val ingredient: Ingredient = Ingredient(),
    val quantity: Float = 0f,
    val unit: UnitOfMeasure = UnitOfMeasure.G,
    val insertedAt: Long = System.currentTimeMillis()
    ) {
    val formattedQuantity: String
        get() = if (quantity % 1f == 0f) {
            quantity.toInt().toString()
        } else {
            quantity.toString()
        }

    fun toIngredientQuantity(): IngredientQuantity = IngredientQuantity(
        ingredient = ingredient,
        quantity = quantity,
        unit = unit
    )
}