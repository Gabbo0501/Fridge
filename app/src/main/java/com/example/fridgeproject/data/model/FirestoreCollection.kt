package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.SystemCollection
import com.example.fridgeproject.model.enums.SystemCollectionType

data class FirestoreCollectionOwnership(
    val collectionId: String = "",
    val recipeId: String = ""
)

data class FirestoreCustomCollection(
    val ownerId: String = "",
    val name: String = ""
)

data class FirestoreSystemCollection(
    val ownerId: String = "",
    val type: String = SystemCollectionType.FAVOURITES.name
)

fun FirestoreCustomCollection.toDomain(
    id: String,
    recipeIds: List<String>
): CustomCollection =
    CustomCollection(
        id = id,
        ownerId = ownerId,
        name = name,
        recipeIds = recipeIds
    )

fun CustomCollection.toFirestore(): FirestoreCustomCollection =
    FirestoreCustomCollection(
        ownerId = ownerId,
        name = name
    )

fun FirestoreSystemCollection.toDomain(
    id: String,
    recipeIds: List<String>
): SystemCollection =
    SystemCollection(
        id = id,
        ownerId = ownerId,
        type = type.toSystemCollectionType(),
        recipeIds = recipeIds
    )

fun SystemCollection.toFirestore(): FirestoreSystemCollection =
    FirestoreSystemCollection(
        ownerId = ownerId,
        type = type.name
    )

private fun String.toSystemCollectionType(): SystemCollectionType =
    try {
        SystemCollectionType.valueOf(uppercase())
    } catch (e: Exception) {
        SystemCollectionType.FAVOURITES
    }