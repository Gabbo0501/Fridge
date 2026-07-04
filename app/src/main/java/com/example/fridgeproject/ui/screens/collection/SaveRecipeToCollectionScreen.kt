package com.example.fridgeproject.ui.screens.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.collection.CollectionFormDialog
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.viewmodel.collection.SaveRecipeToCollectionUiState

@Composable
fun SaveRecipeToCollectionScreen(
    uiState: SaveRecipeToCollectionUiState,
    onBackClick: () -> Unit,
    onCollectionToggle: (String) -> Unit,
    onCreateCollectionClick: () -> Unit,
    onCollectionNameChange: (String) -> Unit,
    onCreateCollectionConfirm: () -> Unit,
    onCreateCollectionDismiss: () -> Unit
) {
    if (uiState.formDialog.isOpen) {
        CollectionFormDialog(
            name = uiState.formDialog.nameInput,
            isEdit = false,
            isSaving = uiState.isSavingCollection,
            nameError = uiState.formDialog.nameError,
            error = uiState.collectionFormError,
            onNameChange = onCollectionNameChange,
            onConfirm = onCreateCollectionConfirm,
            onDismiss = onCreateCollectionDismiss
        )
    }

    when {
        uiState.isLoading -> LoadingComponent()
        uiState.error != null && uiState.savedCollections.isEmpty() && uiState.availableCollections.isEmpty() ->
            ErrorComponent(uiState.error)

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 18.dp)
            ) {
                item { PageHeader(title = "Save to collection", onBackClick = onBackClick) }

                item {
                    Button(
                        onClick = onCreateCollectionClick,
                        enabled = !uiState.isUpdating && !uiState.isSavingCollection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "New collection")
                    }
                }
                item {
                    val hasCollections = uiState.savedCollections.isNotEmpty() ||
                        uiState.availableCollections.isNotEmpty()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        if (!hasCollections) {
                            EmptyStateComponent(
                                message = "No custom collections yet.",
                                icon = Icons.Default.Folder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = PageBottomPadding)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                uiState.savedCollections.forEach { collection ->
                                    CollectionToggleRow(
                                        collection = collection,
                                        checked = true,
                                        enabled = !uiState.isUpdating,
                                        onToggle = onCollectionToggle
                                    )
                                }

                                uiState.availableCollections.forEach { collection ->
                                    CollectionToggleRow(
                                        collection = collection,
                                        checked = false,
                                        enabled = !uiState.isUpdating,
                                        onToggle = onCollectionToggle
                                    )
                                }

                                uiState.error?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionToggleRow(
    collection: CustomCollection,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onToggle(collection.id) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(9.dp)
            )
        }

        Text(
            text = collection.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        )

        Checkbox(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onToggle(collection.id) }
        )
    }
}