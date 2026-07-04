package com.example.fridgeproject.domain

import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.SystemCollection
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getCustomCollectionById(id: String): Flow<CustomCollection?>
    fun getSystemCollectionById(id: String): Flow<SystemCollection?>
    fun getUserCustomCollections(userId: String): Flow<List<CustomCollection>>
    fun getFavourites(userId: String): Flow<SystemCollection?>
    suspend fun createSystemCollections(userId: String)
    suspend fun toggleFavourite(userId: String, recipeId: String)
    suspend fun toggleRecipeInCustomCollection(collectionId: String, recipeId: String)
    suspend fun saveCustomCollection(collection: CustomCollection)
    suspend fun deleteCustomCollection(collectionId: String)
    suspend fun deleteCollectionsByUser(userId: String)
    suspend fun deleteOwnershipsByRecipe(recipeId: String)
}