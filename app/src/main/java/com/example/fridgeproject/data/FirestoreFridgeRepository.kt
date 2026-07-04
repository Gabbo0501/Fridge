package com.example.fridgeproject.data

import com.example.fridgeproject.data.model.FirestoreFridge
import com.example.fridgeproject.data.model.FirestoreIngredientQuantityWithTime
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.data.model.FirestoreUser
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.AuthRepository
import com.example.fridgeproject.domain.FridgeRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.model.Fridge
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFridgeRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val ingredientRepository: IngredientRepository
) : FridgeRepository {

    private val fridgesCollection = firestore.collection(Collections.FRIDGES)

    override fun getFridgeById(id: String): Flow<Fridge?> =
        fridgeFlow(fridgesCollection.whereEqualTo(FieldPath.documentId(), id))
            .map { it.firstOrNull() }

    override fun getFridgeByOwner(ownerId: String): Flow<Fridge?> =
        fridgeFlow(fridgesCollection.whereEqualTo("ownerId", ownerId))
            .map { it.firstOrNull() }

    override suspend fun addIngredientToFridge(ingredient: IngredientQuantityWithTime) {
        val userId = authRepository.currentUserId ?: return
        val snapshot = fridgesCollection
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull()

        if (doc == null) {
            val newFirestoreFridge = FirestoreFridge(
                ownerId = userId,
                ingredients = listOf(ingredient.toFirestore()),
                updatedAt = System.currentTimeMillis()
            )
            fridgesCollection.add(newFirestoreFridge).await()
        } else {
            val fridge = doc.toObject(FirestoreFridge::class.java) ?: return
            val updated = fridge.ingredients.toMutableList()
            val existing = updated.indexOfFirst { it.name == ingredient.ingredient.name }

            if (existing >= 0) {
                updated[existing] = ingredient.toFirestore()
            } else {
                updated.add(ingredient.toFirestore())
            }

            doc.reference.update(
                "ingredients", updated,
                "updatedAt", System.currentTimeMillis()
            ).await()
        }
    }

    override suspend fun removeIngredientFromFridge(ingredientName: String) {
        val userId = authRepository.currentUserId ?: return
        val snapshot = fridgesCollection
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull() ?: return
        val fridge = doc.toObject(FirestoreFridge::class.java) ?: return

        val updated = fridge.ingredients.filter { it.name != ingredientName }

        doc.reference.update(
            "ingredients", updated,
            "updatedAt", System.currentTimeMillis()
        ).await()
    }

    override suspend fun saveFridge(fridge: Fridge) {
        if (fridge.id.isEmpty()) {
            fridgesCollection.add(fridge.toFirestore()).await()
        } else {
            fridgesCollection.document(fridge.id)
                .set(fridge.toFirestore())
                .await()
        }
    }

    override suspend fun emptyFridge(id: String) {
        fridgesCollection.document(id)
            .update(
                "ingredients", emptyList<FirestoreIngredientQuantityWithTime>(),
                "updatedAt", System.currentTimeMillis()
            )
            .await()
    }

    private fun fridgeFlow(query: Query): Flow<List<Fridge>> {
        // Mappa [Nome -> Ingredient]
        val ingredientsMapFlow = ingredientRepository.getAllIngredients()
            .map { list -> list.associateBy { it.name } }

        val rawFridgeFlow = query.snapshots().map { fridgeSnapshot ->
            fridgeSnapshot.documents.mapNotNull { document ->
                document.toObject(FirestoreFridge::class.java)?.let { fridge ->
                    document.id to fridge
                }
            }
        }

        return rawFridgeFlow.combine(ingredientsMapFlow) { fridgesList, ingredientsMap ->
            fridgesList.map { (id, fridge) ->
                fridge.toDomain(id = id, ingredientsMap = ingredientsMap)
            }.sortedBy { it.id }
        }
    }
}