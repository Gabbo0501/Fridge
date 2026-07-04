package com.example.fridgeproject.model.enums

enum class UnitOfMeasure {
    G,
    KG,
    PCS,
    QB,
    ML,
    L
}

fun UnitOfMeasure.defaultIngredientQuantity(): Float =
    when (this) {
        UnitOfMeasure.G,
        UnitOfMeasure.ML -> 100f
        UnitOfMeasure.KG,
        UnitOfMeasure.PCS,
        UnitOfMeasure.QB,
        UnitOfMeasure.L -> 1f
    }