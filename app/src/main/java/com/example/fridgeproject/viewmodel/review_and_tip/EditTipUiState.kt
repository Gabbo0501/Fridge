package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.Tip
import com.example.fridgeproject.model.enums.TipType

data class EditTipUiState(
    val type: TipType? = null,
    val tipText: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val showExitDialog: Boolean = false,
    val errors: EditTipErrors = EditTipErrors()
)

data class EditTipErrors(
    val type: String = "",
    val tipText: String = "",
    val general: String = ""
)

fun EditTipUiState.canCheckUnsavedChanges(): Boolean =
    !isLoading && !isSaving && !success && errors.general.isBlank()

fun EditTipUiState.hasChangesFrom(original: Tip): Boolean =
    type != original.type ||
            tipText != original.comment