package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure

data class Ingredient(
    val name: String = "",
    val category: IngredientCategory = IngredientCategory.OTHERS,
    val defaultUnit: UnitOfMeasure = UnitOfMeasure.G
)
