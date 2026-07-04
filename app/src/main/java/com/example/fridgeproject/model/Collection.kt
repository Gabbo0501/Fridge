package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.SystemCollectionType

data class CustomCollection(
    val id: String,
    val ownerId: String,
    val name: String,
    val recipeIds: List<String>
)

data class SystemCollection(
    val id: String,
    val ownerId: String,
    val type: SystemCollectionType,
    val recipeIds: List<String>
)