package com.example.fridgeproject.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun UnsavedChangesDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = "Discard changes?",
        text = "Are you sure you want to leave? Your changes will be lost.",
        confirmButtonText = "Leave",
        dismissButtonText = "Cancel",
        confirmTextColor = MaterialTheme.colorScheme.error,
        onConfirm = onConfirmExit,
        onDismiss = onDismiss
    )
}