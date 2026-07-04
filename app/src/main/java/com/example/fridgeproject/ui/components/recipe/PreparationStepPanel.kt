package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeStep

@Composable
fun StepPanel(
    step: RecipeStep,
    stepIndex: Int,
    totalSteps: Int,
    imageError: String?,
    descError: String?,
    isImageMenuExpanded: Boolean,
    onImageMenuOpen: () -> Unit,
    onImageMenuDismiss: () -> Unit,
    onStepGalleryClick: (Int) -> Unit,
    onStepCameraClick: (Int) -> Unit,
    onRemoveStep: () -> Unit,
    onSwitchLeft: () -> Unit,
    onSwitchRight: () -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Step header: [nothing] — STEP N — [delete] ────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                "STEP ${stepIndex + 1}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.weight(1f))

            // Delete is disabled when there is only one step
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove step",
                tint = if (totalSteps > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(20.dp)
                    .then(
                        if (totalSteps > 1) Modifier.clickable { onRemoveStep() }
                        else Modifier
                    )
            )
        }

        // ── Image picker ──────────────────────────────────────────────────────
        ImagePickerCard(
            image = step.image,
            emptyText = "Add step image",
            heightDp = 160,
            isMenuExpanded = isImageMenuExpanded,
            onMenuOpen = onImageMenuOpen,
            onMenuDismiss = onImageMenuDismiss,
            onGalleryClick = { onStepGalleryClick(stepIndex) },
            onCameraClick = { onStepCameraClick(stepIndex) },
            errorText = imageError
        )


        // ── Instructions label ────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                "INSTRUCTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // ── Description text field ────────────────────────────────────────────
        OutlinedTextField(
            value = step.description,
            onValueChange = { onDescriptionChange(it) },
            placeholder = {
                Text(
                    "Explain this step in detail.\nMention specific tools or heat levels...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            isError = !descError.isNullOrBlank(),
            supportingText = { if (!descError.isNullOrBlank()) Text(descError) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // ── Prev / Next arrows ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous step",
                tint = if (stepIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (stepIndex > 0) Modifier.clickable { onSwitchLeft() }
                        else Modifier
                    )
            )
            Spacer(Modifier.width(24.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next step",
                tint = if (stepIndex < totalSteps - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (stepIndex < totalSteps - 1) Modifier.clickable { onSwitchRight() }
                        else Modifier
                    )
            )
        }
    }
}
