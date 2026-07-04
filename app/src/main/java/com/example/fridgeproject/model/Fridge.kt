package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Cuisine

data class Fridge(
    val id: String = "",
    val ownerId: String = "",
    val ingredients: List<IngredientQuantityWithTime> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

