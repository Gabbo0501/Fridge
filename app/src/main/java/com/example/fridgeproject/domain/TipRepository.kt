package com.example.fridgeproject.domain

import com.example.fridgeproject.model.Tip
import kotlinx.coroutines.flow.Flow

interface TipRepository {
    fun getTipById(id: String): Flow<Tip?>

    fun getTipsByRecipe(recipeId: String): Flow<List<Tip>>

    fun getTipsByUser(userId: String): Flow<List<Tip>>

    fun getTipsCountByUser(userId: String): Flow<Int>

    suspend fun saveTip(tip: Tip)

    suspend fun deleteTip(id: String)

    suspend fun deleteTipsByRecipe(recipeId: String)
}