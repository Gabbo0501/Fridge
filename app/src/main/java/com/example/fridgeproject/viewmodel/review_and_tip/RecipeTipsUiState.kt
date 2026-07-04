package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.RecipeTipUi

data class RecipeTipsUiState (
    val recipeId: String = "",
    val myTip: RecipeTipUi? = null,
    val otherTips: List<RecipeTipUi> = emptyList(),
    val canAddTip: Boolean = false,
    val tipToDeleteId: String? = null,
    val isLoading: Boolean = false,
    val globalError: String = ""
)
