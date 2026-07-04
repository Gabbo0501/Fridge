package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.TipType

data class Tip(
    val id: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val type: TipType = TipType.DO,
    val date: String = "",
    val comment: String = ""
)