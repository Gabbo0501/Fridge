package com.example.fridgeproject.data

import com.example.fridgeproject.data.model.FirestoreGroceryList
import com.example.fridgeproject.data.model.FirestoreIngredientQuantityWithTime
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.AuthRepository
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.GroceryListRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.model.GroceryList
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.collections.map
import kotlin.collections.sortedBy

class FirestoreGroceryListRepository(
    firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val ingredientRepository: IngredientRepository
): GroceryListRepository {
    private val groceryListsCollection = firestore.collection(Collections.GROCERY_LISTS)

    override fun getGroceryListById(id: String): Flow<GroceryList?> =
        groceryListFlow(groceryListsCollection.whereEqualTo(FieldPath.documentId(), id))
            .map { it.firstOrNull() }
    override fun getGroceryListByOwner(ownerId: String): Flow<GroceryList?> =
        groceryListFlow(groceryListsCollection.whereEqualTo("ownerId", ownerId))
            .map { it.firstOrNull() }

    override suspend fun addIngredientToGroceryList(ingredient: IngredientQuantityWithTime) {
        val userId = authRepository.currentUserId ?: return
        val snapshot = groceryListsCollection
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull()

        if (doc == null) {
            val newFirestoreGroceryList = FirestoreGroceryList(
                ownerId = userId,
                ingredients = listOf(ingredient.toFirestore()),
                updatedAt = System.currentTimeMillis()
            )
            groceryListsCollection.add(newFirestoreGroceryList).await()
        } else {
            val groceryList = doc.toObject(FirestoreGroceryList::class.java) ?: return
            val updated = groceryList.ingredients.toMutableList()
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


    override suspend fun removeIngredientFromGroceryList(ingredientName: String) {
        val userId = authRepository.currentUserId ?: return
        val snapshot = groceryListsCollection
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
        val doc = snapshot.documents.firstOrNull() ?: return
        val groceryList = doc.toObject(FirestoreGroceryList::class.java) ?: return

        val updated = groceryList.ingredients.filter { it.name != ingredientName }

        doc.reference.update(
            "ingredients", updated,
            "updatedAt", System.currentTimeMillis()
        ).await()
    }

    override suspend fun clearGroceryList(id: String) {
        groceryListsCollection.document(id)
            .update(
                "ingredients", emptyList<FirestoreIngredientQuantityWithTime>(),
                "updatedAt", System.currentTimeMillis()
            )
            .await()
    }

    private fun groceryListFlow(query: Query): Flow<List<GroceryList>> {
        val ingredientsMapFlow = ingredientRepository.getAllIngredients()
            .map { list -> list.associateBy { it.name } }

        val rawGroceryListFlow = query.snapshots().map { groceryListSnapshot ->
            groceryListSnapshot.documents.mapNotNull { document ->
                document.toObject(FirestoreGroceryList::class.java)?.let { groceryList ->
                    document.id to groceryList
                }
            }
        }

        return rawGroceryListFlow.combine(ingredientsMapFlow) { groceryList, ingredientsMap ->
            groceryList.map { (id, groceryList) ->
                groceryList.toDomain(id = id, ingredientsMap = ingredientsMap)
            }.sortedBy { it.id }
        }
    }
}