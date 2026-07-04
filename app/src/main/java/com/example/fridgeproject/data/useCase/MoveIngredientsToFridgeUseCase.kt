package com.example.fridgeproject.data.useCase

import com.example.fridgeproject.data.model.FirestoreFridge
import com.example.fridgeproject.data.model.FirestoreGroceryList
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.AuthRepository
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MoveIngredientsToFridgeUseCase(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(ingredients: List<IngredientQuantityWithTime>) {
        val userId = authRepository.currentUserId ?: return

        val fridgeDoc = firestore.collection(Collections.FRIDGES)
            .whereEqualTo("ownerId", userId)
            .get().await()
            .documents.firstOrNull()

        val groceryDoc = firestore.collection(Collections.GROCERY_LISTS)
            .whereEqualTo("ownerId", userId)
            .get().await()
            .documents.firstOrNull() ?: return

        val batch = firestore.batch()

        val groceryList = groceryDoc.toObject(FirestoreGroceryList::class.java) ?: return
        val namesToRemove = ingredients.map { it.ingredient.name }.toSet()
        val updatedGrocery = groceryList.ingredients
            .filter { it.name !in namesToRemove }
        batch.update(groceryDoc.reference, "ingredients", updatedGrocery)

        if (fridgeDoc == null) {
            val newRef = firestore.collection(Collections.FRIDGES).document()
            batch.set(newRef, FirestoreFridge(
                ownerId = userId,
                ingredients = ingredients.map { it.toFirestore() },
                updatedAt = System.currentTimeMillis()
            ))
        } else {
            val fridge = fridgeDoc.toObject(FirestoreFridge::class.java) ?: return
            val updatedFridge = fridge.ingredients.toMutableList()
            ingredients.forEach { incoming ->
                val idx = updatedFridge.indexOfFirst { it.name == incoming.ingredient.name }
                if (idx >= 0) {
                    val existing = updatedFridge[idx]
                    updatedFridge[idx] = existing.copy(quantity = existing.quantity + incoming.quantity)
                }
                else updatedFridge.add(incoming.toFirestore())
            }
            batch.update(fridgeDoc.reference, "ingredients", updatedFridge,
                "updatedAt", System.currentTimeMillis())
        }

        batch.commit().await()
    }
}