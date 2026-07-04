package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.material3.SuggestionChipDefaults.suggestionChipBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.enums.Cuisine
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.DishType


@Composable
fun RecipeTagsRow(
    dishType: DishType,
    selectedDiets: List<Diet>,
    selectedCuisines: List<Cuisine>,
    edit: Boolean,
    showDietDialog: Boolean = false,
    showCuisineDialog: Boolean = false,
    onDietDialogOpen: () -> Unit = {},
    onCuisineDialogOpen: () -> Unit = {},
    onTagsDialogDismiss: () -> Unit = {},
    onDietToggle: (Diet) -> Unit = {},
    onCuisineToggle: (Cuisine) -> Unit = {}
) {
    if(edit) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // --- DIETS ---
            LabeledSection(label = "Change Suitable Diets", subLabel = "Click on the pencil to add/remove a Diet") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDietDialogOpen) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Diets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedDiets.forEach { diet ->
                            AssistChip(
                                onClick = { },
                                label = { Text(diet.desc) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.RestaurantMenu,
                                        null,
                                        Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    labelColor = MaterialTheme.colorScheme.primary,
                                    leadingIconContentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    borderWidth = 1.dp
                                )
                            )
                        }
                        if (selectedDiets.isEmpty()) {
                            Text(
                                "No diets selected",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // --- CUISINES ---
            LabeledSection(label = "Change Cuisine Types", subLabel = "Click on the pencil to add/remove a Cuisine") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCuisineDialogOpen) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Cuisines",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedCuisines.forEach { cuisine ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        cuisine.name.lowercase()
                                            .replaceFirstChar { it.uppercase() })
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Language,
                                        null,
                                        Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    labelColor = MaterialTheme.colorScheme.primary,
                                    leadingIconContentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    borderWidth = 1.dp
                                )
                            )
                        }
                        if (selectedCuisines.isEmpty()) {
                            Text(
                                "No cuisines selected",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOGS ---
        if (showDietDialog) {
            RecipeTagsDialog(
                title = "Select Diets",
                allOptions = Diet.entries.map { it.desc to selectedDiets.contains(it) },
                onToggle = { index -> onDietToggle(Diet.entries[index]) },
                onDismiss = onTagsDialogDismiss
            )
        }

        if (showCuisineDialog) {
            RecipeTagsDialog(
                title = "Select Cuisines",
                allOptions = Cuisine.entries.map {
                    it.name.lowercase()
                        .replaceFirstChar { char -> char.uppercase() } to selectedCuisines.contains(
                        it
                    )
                },
                onToggle = { index -> onCuisineToggle(Cuisine.entries[index]) },
                onDismiss = onTagsDialogDismiss
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. DishType - Sempre Arancione
                SuggestionChip(
                    onClick = { },
                    label = { Text(dishType.desc.uppercase().replaceFirstChar { it.uppercase() }) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = suggestionChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50.dp)
                )

                // 2. Diets - Grigie
                selectedDiets.forEach { diet ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(diet.desc) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        ),
                        border = suggestionChipBorder(
                            enabled = true,

                            ),
                        shape = RoundedCornerShape(50.dp)
                    )
                }

                // 3. Cuisines - Grigie
                selectedCuisines.forEach { cuisine ->
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                cuisine.name.lowercase().replaceFirstChar { it.uppercase() })
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        ),
                        border = suggestionChipBorder(
                            enabled = true,
                        ),
                        shape = RoundedCornerShape(50.dp)
                    )
                }
            }
        }
    }
}