package com.example.fridgeproject.data

import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.data.model.FirestoreIngredient
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.IngredientCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreIngredientRepository(
    private val firestore: FirebaseFirestore
) : IngredientRepository {

    private val ingredientsCollection = firestore.collection(Collections.INGREDIENTS)

//    override suspend fun migrateIfEmpty() {
//        val snapshot = ingredientsCollection.limit(1).get().await()
//        if (snapshot.isEmpty) uploadCatalog()
//    }

//    override suspend fun uploadCatalog() {
//        val batch = firestore.batch()
//        IngredientCatalog.allIngredients.forEach { ingredient ->
//            val docId = ingredient.name.lowercase().replace(" ", "_")
//            val ref = ingredientsCollection.document(docId)
//            batch.set(ref, ingredient.toFirestore())
//        }
//        batch.commit().await()
//    }

    override fun getAllIngredients(): Flow<List<Ingredient>> =
        ingredientsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(FirestoreIngredient::class.java)?.toDomain() }
                    .sortedBy { it.name }
            }

    override fun getIngredientsByCategory(category: IngredientCategory): Flow<List<Ingredient>> =
        ingredientsCollection
            .whereEqualTo("category", category.name)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(FirestoreIngredient::class.java)?.toDomain() }
                    .sortedBy { it.name }
            }
}