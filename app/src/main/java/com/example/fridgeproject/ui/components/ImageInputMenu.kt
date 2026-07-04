package com.example.fridgeproject.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ImageInputMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRemoveClick: (() -> Unit)? = null
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Select from gallery") },
            onClick = {
                onDismiss()
                onGalleryClick()
            }
        )
        DropdownMenuItem(
            text = { Text("Use camera") },
            onClick = {
                onDismiss()
                onCameraClick()
            }
        )
        if (onRemoveClick != null) {
            DropdownMenuItem(
                text = { Text("Remove photo") },
                onClick = {
                    onDismiss()
                    onRemoveClick()
                }
            )
        }
    }
}