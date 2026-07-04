package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.enums.TipType

data class NewTipUiState(
    val type: TipType? = null,
    val tipText: String = "",
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val showExitDialog: Boolean = false,
    val errors: NewTipErrors = NewTipErrors()
)

data class NewTipErrors(
    val type: String = "",
    val tipText: String = "",
    val general: String = ""
)

fun NewTipUiState.canCheckUnsavedChanges(): Boolean =
    !success && !isSaving

fun NewTipUiState.hasChangesFromDefault(): Boolean =
    type != null || tipText.isNotBlank()