package com.example.fridgeproject.model

data class Review(
    val id: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val stars: Int = 0,
    val date: String = "",
    val comment: String = "",
    val images: List<StoredImage> = emptyList()
)