package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeStep

@Composable
fun PreparationSection(
    steps: List<RecipeStep>,
    currentEditedStepIndex: Int,
    imageError: String?,
    descError: String?,
    isStepImageMenuExpanded: Boolean,
    onStepImageMenuOpen: () -> Unit,
    onStepImageMenuDismiss: () -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (RecipeStep) -> Unit,
    onSwitchLeft: () -> Unit,
    onSwitchRight: () -> Unit,
    onStepGalleryClick: () -> Unit,
    onStepCameraClick: () -> Unit,
    onDescriptionChange: ( String) -> Unit
) {

    val currentIndex = when {
        steps.isEmpty() -> 0
        currentEditedStepIndex in steps.indices -> currentEditedStepIndex
        else -> steps.lastIndex
    }
    val currentStep = steps.getOrNull(currentIndex)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Preparation",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Insert procedure for preparation",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Add step button ───────────────────────────────────────────────
            OutlinedButton(
                onClick = onAddStep,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(
                    horizontal = 12.dp, vertical = 6.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add step",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Add step", fontSize = 13.sp)
            }
        }

        if (steps.isNotEmpty()) {
            Text(
                text = "${currentIndex + 1} / ${steps.size}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }

        if (currentStep != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                StepPanel(
                    step = currentStep,
                    stepIndex = currentIndex,
                    totalSteps = steps.size,
                    imageError = imageError,
                    descError = descError,
                    isImageMenuExpanded = isStepImageMenuExpanded,
                    onImageMenuOpen = onStepImageMenuOpen,
                    onImageMenuDismiss = onStepImageMenuDismiss,
                    onStepGalleryClick = { onStepGalleryClick() },
                    onStepCameraClick = { onStepCameraClick() },
                    onRemoveStep = { onRemoveStep(currentStep) },
                    onSwitchLeft = onSwitchLeft,
                    onSwitchRight = onSwitchRight,
                    onDescriptionChange = {onDescriptionChange(it) }
                )
            }
        } else {
            // Empty state
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "No steps yet. Tap \"Add step\" to begin.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
