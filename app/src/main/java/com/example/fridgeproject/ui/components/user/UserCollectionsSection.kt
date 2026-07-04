package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.SystemCollection
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.responsiveGridColumns
import com.example.fridgeproject.ui.components.collection.AddCollectionItem
import com.example.fridgeproject.ui.components.collection.UserCollectionItem

@Composable
fun UserCollectionsSection(
    favouriteCollection: SystemCollection?,
    collections: List<CustomCollection>,
    isOwner: Boolean,
    showCreateCollectionItem: Boolean = isOwner,
    onFavouriteClick: (SystemCollection) -> Unit,
    onCollectionClick: (CustomCollection) -> Unit,
    onCreateCollectionDialog: () -> Unit,
    onEditCollectionDialog: (CustomCollection) -> Unit,
    onDeleteCollectionDialog: (CustomCollection) -> Unit
) {
    val columns = responsiveGridColumns()
    val hasFavourite = favouriteCollection != null
    val totalItems = collections.size +
            (if (hasFavourite) 1 else 0) +
            (if (showCreateCollectionItem) 1 else 0)

    if (totalItems == 0) {
        EmptyStateComponent(
            message = "No collections yet.",
            modifier = Modifier.padding(top = 4.dp, bottom = PageBottomPadding),
            verticalPadding = 8.dp
        )
    } else {
        val rowCount = (totalItems + columns - 1) / columns
        repeat(rowCount) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (rowIndex == rowCount - 1) PageBottomPadding else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(columns) { columnIndex ->
                    val index = rowIndex * columns + columnIndex

                    if (index < totalItems) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (index == 0 && favouriteCollection != null) {
                                UserCollectionItem(
                                    name = "Favourites",
                                    recipeCount = favouriteCollection.recipeIds.size,
                                    isFavourites = true,
                                    onClick = { onFavouriteClick(favouriteCollection) }
                                )
                            } else if (index == totalItems - 1 && showCreateCollectionItem) {
                                AddCollectionItem(onClick = onCreateCollectionDialog)
                            } else {
                                val collectionIndex = index - if (hasFavourite) 1 else 0
                                val collection = collections[collectionIndex]

                                UserCollectionItem(
                                    name = collection.name,
                                    recipeCount = collection.recipeIds.size,
                                    isFavourites = false,
                                    isOwner = isOwner,
                                    onClick = { onCollectionClick(collection) },
                                    onEditClick = { onEditCollectionDialog(collection) },
                                    onDeleteClick = { onDeleteCollectionDialog(collection) }
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (rowIndex != rowCount - 1) {
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}