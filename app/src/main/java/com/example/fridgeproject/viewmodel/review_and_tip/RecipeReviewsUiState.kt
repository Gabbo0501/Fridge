package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.RecipeReviewUi

data class RecipeReviewsUiState (
    val recipeId: String = "",
    val myReview: RecipeReviewUi? = null,
    val otherReviews: List<RecipeReviewUi> = emptyList(),
    val canAddReview: Boolean = false,
    val reviewToDeleteId: String? = null,
    val isLoading: Boolean = false,
    val globalError: String = ""
)