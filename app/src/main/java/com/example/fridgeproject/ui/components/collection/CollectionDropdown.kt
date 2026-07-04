package com.example.fridgeproject.ui.components.collection

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu as MaterialDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CollectionDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    MaterialDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = colorScheme.background,
        shadowElevation = 8.dp

    ) {
        DropdownMenuItem(
            text = { Text("Edit", color = colorScheme.primary) },
            leadingIcon = {
                Icon(Icons.Default.Edit, null, tint = colorScheme.primary)
            },
            onClick = {
                onDismiss()
                onEditClick()
            }
        )
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
        DropdownMenuItem(
            text = { Text("Delete" , color = colorScheme.primary) },
            leadingIcon = {
                Icon(Icons.Default.Delete, null, tint = colorScheme.primary)
            },
            onClick = {
                onDismiss()
                onDeleteClick()
            }
        )
    }
}