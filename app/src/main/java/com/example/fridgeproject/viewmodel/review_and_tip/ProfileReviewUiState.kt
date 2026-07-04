package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.UserReviewUi

data class ProfileReviewUiState (
    val reviews: List<UserReviewUi> = emptyList(),
    val reviewToDeleteId: String? = null,
    val globalError: String = ""
)