package com.example.fridgeproject.ui.screens.recipe
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.recipe.RecipeCard
import com.example.fridgeproject.ui.components.responsiveGridColumns


@Composable
fun RecipeResultScreen(
    recipes: List<RecipeShortUi>,
    currentUserId: String?,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    onClearFilters: () -> Unit,
    hasActiveFilters: Boolean,
    onRecipeCardClick: (String) -> Unit,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val columns = responsiveGridColumns()

    if (isLoading) {
        LoadingComponent()
    } else {
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
                    title = "Found for you",
                    onBackClick = onBack,
                    bottomPadding = 12.dp
                ) {
                    Row {
                        IconButton(onClick = onFilterClick) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = colorScheme.primary
                            )
                        }
                        if (hasActiveFilters) {
                            IconButton(onClick = onClearFilters) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear filters",
                                    tint = colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (recipes.isNotEmpty()) "${recipes.size} recipes found" else "",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (recipes.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateComponent(
                        message = "No recipes found.",
                        icon = Icons.Default.Search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding)
                    )
                }
            } else {
                val lastRowStart = ((recipes.lastIndex) / columns) * columns
                itemsIndexed(items = recipes) { index, recipe ->
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
}