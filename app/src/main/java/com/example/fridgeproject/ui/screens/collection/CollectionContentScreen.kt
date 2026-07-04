package com.example.fridgeproject.ui.screens.collection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.ui.components.AlertDialog as FridgeAlertDialog
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.collection.CollectionFormDialog
import com.example.fridgeproject.ui.components.recipe.RecipeCard
import com.example.fridgeproject.ui.components.responsiveGridColumns
import com.example.fridgeproject.viewmodel.collection.CollectionUiState
import com.example.fridgeproject.viewmodel.user.UserCollectionFormMode

@Composable
fun CollectionContentScreen(
    uiState: CollectionUiState,
    currentUserId: String?,
    onBack: () -> Unit,
    onRecipeCardClick: (String) -> Unit,
    onEditCollectionClick: () -> Unit,
    onDeleteCollectionClick: () -> Unit,
    onCollectionNameChange: (String) -> Unit,
    onCollectionFormConfirm: () -> Unit,
    onCollectionFormDismiss: () -> Unit,
    onDeleteCollectionConfirm: () -> Unit,
    onDeleteCollectionDismiss: () -> Unit
) {
    val collectionToDelete = uiState.deleteDialog.collectionToDelete

    if (uiState.formDialog.isOpen) {
        CollectionFormDialog(
            name = uiState.formDialog.nameInput,
            isEdit = uiState.formDialog.mode == UserCollectionFormMode.EDIT,
            isSaving = uiState.isSaving,
            nameError = uiState.formDialog.nameError,
            error = uiState.actionError,
            onNameChange = onCollectionNameChange,
            onConfirm = onCollectionFormConfirm,
            onDismiss = onCollectionFormDismiss
        )
    }

    if (collectionToDelete != null) {
        val deleteError = uiState.deleteDialog.error
        FridgeAlertDialog(
            title = "Delete collection?",
            text = buildString {
                append("Are you sure you want to delete \"${collectionToDelete.name}\"? This action cannot be undone.")
                if (deleteError != null) {
                    append("\n\n")
                    append(deleteError)
                }
            },
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = MaterialTheme.colorScheme.error,
            onConfirm = onDeleteCollectionConfirm,
            onDismiss = onDeleteCollectionDismiss
        )
    }

    if (uiState.isLoading) {
        LoadingComponent()
    } else {
        CollectionContent(
            uiState = uiState,
            currentUserId = currentUserId,
            onBack = onBack,
            onRecipeCardClick = onRecipeCardClick,
            onEditCollectionClick = onEditCollectionClick,
            onDeleteCollectionClick = onDeleteCollectionClick
        )
    }
}

@Composable
private fun CollectionContent(
    uiState: CollectionUiState,
    currentUserId: String?,
    onBack: () -> Unit,
    onRecipeCardClick: (String) -> Unit,
    onEditCollectionClick: () -> Unit,
    onDeleteCollectionClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val columns = responsiveGridColumns()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            PageHeader(
                title = uiState.collectionName,
                onBackClick = onBack,
                bottomPadding = 12.dp
            ) {
                if (uiState.customCollection != null && uiState.isOwner) {
                    CollectionOptions(
                        onEditCollectionClick = onEditCollectionClick,
                        onDeleteCollectionClick = onDeleteCollectionClick
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = if (uiState.recipes.isNotEmpty()) "${uiState.recipes.size} recipes in collection" else "",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (uiState.recipes.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyStateComponent(
                    message = uiState.error ?: "No recipes in this collection.",
                    icon = Icons.Default.Folder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = PageBottomPadding)
                )
            }
        } else {
            val lastRowStart = ((uiState.recipes.lastIndex) / columns) * columns
            itemsIndexed(items = uiState.recipes) { index, recipe ->
                RecipeCard(
                    recipe = recipe,
                    isOwner = recipe.authorId == currentUserId,
                    onRecipeCardClick = { onRecipeCardClick(recipe.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index >= lastRowStart) PageBottomPadding else 0.dp)
                )
            }
        }
    }
}

@Composable
private fun CollectionOptions(
    modifier: Modifier = Modifier,
    onEditCollectionClick: () -> Unit,
    onDeleteCollectionClick: () -> Unit
) {
    Row(modifier = modifier) {
        IconButton(onClick = onEditCollectionClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit collection",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onDeleteCollectionClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete collection",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}