package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Tip
import com.example.fridgeproject.model.enums.TipType

data class FirestoreTip(
    val recipeId: String = "",
    val userId: String = "",
    val type: String = TipType.DO.name,
    val date: String = "",
    val comment: String = ""
)

fun FirestoreTip.toDomain(id: String): Tip =
    Tip(
        id = id,
        recipeId = recipeId,
        userId = userId,
        type = type.toTipType(),
        date = date,
        comment = comment
    )

fun Tip.toFirestore(): FirestoreTip =
    FirestoreTip(
        recipeId = recipeId,
        userId = userId,
        type = type.name,
        date = date,
        comment = comment
    )

private fun String.toTipType(): TipType =
    try {
        TipType.valueOf(uppercase())
    } catch (e: Exception) {
        TipType.DO
    }
