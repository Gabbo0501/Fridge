package com.example.fridgeproject.domain

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.IngredientCategory
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllIngredients(): Flow<List<Ingredient>>
    fun getIngredientsByCategory(category: IngredientCategory): Flow<List<Ingredient>>
    //    suspend fun uploadCatalog()
    //    suspend fun migrateIfEmpty()
}