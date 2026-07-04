package com.example.fridgeproject.domain

import com.example.fridgeproject.model.GroceryList
import com.example.fridgeproject.model.IngredientQuantityWithTime
import kotlinx.coroutines.flow.Flow


interface GroceryListRepository {
    fun getGroceryListById(id: String): Flow<GroceryList?>

    fun getGroceryListByOwner(ownerId: String): Flow<GroceryList?>

    suspend fun addIngredientToGroceryList(ingredient: IngredientQuantityWithTime)

    suspend fun removeIngredientFromGroceryList(ingredientName: String)

    suspend fun clearGroceryList(id: String)
}