package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Review
import com.example.fridgeproject.model.StoredImage

data class EditReviewUiState(
    val rating: Int = 0,
    val reviewText: String = "",
    val images: List<StoredImage> = emptyList(),
    val pendingImages: List<LocalImageInput> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val showExitDialog: Boolean = false,
    val errors: EditReviewErrors = EditReviewErrors()
)

data class EditReviewErrors(
    val rating: String = "",
    val reviewText: String = "",
    val images: String = "",
    val general: String = ""
)

fun EditReviewUiState.canCheckUnsavedChanges(): Boolean =
    !isLoading && !isSaving && !success && errors.general.isBlank()

fun EditReviewUiState.hasChangesFrom(original: Review): Boolean =
    rating != original.stars ||
            reviewText != original.comment ||
            images != original.images ||
            pendingImages.isNotEmpty()