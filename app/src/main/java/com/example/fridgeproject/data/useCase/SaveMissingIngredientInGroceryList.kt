package com.example.fridgeproject.data.useCase

import com.example.fridgeproject.data.model.FirestoreGroceryList
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.AuthRepository
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.collections.forEach

class SaveMissingIngredientInGroceryList(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(ingredients: List<IngredientQuantityWithTime>) {
        val userId = authRepository.currentUserId ?: return

        val groceryDoc = firestore.collection(Collections.GROCERY_LISTS)
            .whereEqualTo("ownerId", userId)
            .get().await()
            .documents.firstOrNull()

        val batch = firestore.batch()

        if (groceryDoc == null) {
            val newRef = firestore.collection(Collections.GROCERY_LISTS).document()
            batch.set(newRef, FirestoreGroceryList(
                ownerId = userId,
                ingredients = ingredients.map { it.toFirestore() },
                updatedAt = System.currentTimeMillis()
            ))
        } else {
            val groceryList = groceryDoc.toObject(FirestoreGroceryList::class.java) ?: return
            val updatedGroceryList = groceryList.ingredients.toMutableList()
            ingredients.forEach { incoming ->
                val idx = updatedGroceryList.indexOfFirst { it.name == incoming.ingredient.name }
                if (idx >= 0) {
                    val existing = updatedGroceryList[idx]
                    updatedGroceryList[idx] = existing.copy(quantity = existing.quantity + incoming.quantity)
                }
                else updatedGroceryList.add(incoming.toFirestore())
            }
            batch.update(groceryDoc.reference, "ingredients", updatedGroceryList,
                "updatedAt", System.currentTimeMillis())
        }

        batch.commit().await()
    }
}