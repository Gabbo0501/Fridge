package com.example.fridgeproject.domain

import com.example.fridgeproject.model.Fridge
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.IngredientQuantityWithTime
import kotlinx.coroutines.flow.Flow

interface FridgeRepository {

    fun getFridgeById(id: String): Flow<Fridge?>

    fun getFridgeByOwner(ownerId: String): Flow<Fridge?>

    suspend fun addIngredientToFridge(ingredient: IngredientQuantityWithTime)

    suspend fun removeIngredientFromFridge(ingredientName: String)

    suspend fun saveFridge(fridge: Fridge)

    suspend fun emptyFridge(id: String)
}