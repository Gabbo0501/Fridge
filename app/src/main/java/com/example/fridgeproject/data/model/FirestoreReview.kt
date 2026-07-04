package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Review

data class FirestoreReview(
    val recipeId: String = "",
    val userId: String = "",
    val stars: Int = 0,
    val date: String = "",
    val comment: String = "",
    val images: List<FirestoreImageSource> = emptyList()
)

fun FirestoreReview.toDomain(id: String): Review =
    Review(
        id = id,
        recipeId = recipeId,
        userId = userId,
        stars = stars,
        date = date,
        comment = comment,
        images = images.mapNotNull { it.toDomain() }
    )

fun Review.toFirestore(): FirestoreReview =
    FirestoreReview(
        recipeId = recipeId,
        userId = userId,
        stars = stars,
        date = date,
        comment = comment,
        images = images.map { it.toFirestore() }
    )