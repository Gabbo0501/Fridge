package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.StoredImage

data class NewReviewUiState(
    val rating: Int = 0,
    val reviewText: String = "",
    val images: List<StoredImage> = emptyList(),
    val pendingImages: List<LocalImageInput> = emptyList(),
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val showExitDialog: Boolean = false,
    val errors: NewReviewErrors = NewReviewErrors()
)

data class NewReviewErrors(
    val rating: String = "",
    val reviewText: String = "",
    val images: String = "",
    val general: String = ""
)

fun NewReviewUiState.canCheckUnsavedChanges(): Boolean =
    !success && !isSaving

fun NewReviewUiState.hasChangesFromDefault(): Boolean =
    rating != 0 ||
            reviewText.isNotBlank() ||
            images.isNotEmpty() ||
            pendingImages.isNotEmpty()