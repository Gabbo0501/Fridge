package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.recipe.RecipeCard
import com.example.fridgeproject.ui.components.responsiveGridColumns

@Composable
fun UserRecipeGrid(
    recipes: List<RecipeShortUi>,
    isOwner: Boolean,
    onRecipeCardClick: (String) -> Unit
) {
    val size = responsiveGridColumns()
    val validRecipes = recipes.filter { !it.image.isNullOrBlank() }

    if (validRecipes.isEmpty()) {
        EmptyStateComponent(
            message = "No recipes published yet.",
            modifier = Modifier.padding(top = 4.dp, bottom = PageBottomPadding),
            verticalPadding = 8.dp
        )
    } else {
        val rows = validRecipes.chunked(size)
        rows.forEachIndexed { rowIndex, rowRecipes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (rowIndex == rows.lastIndex) PageBottomPadding else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowRecipes.forEach { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        isOwner,
                        onRecipeCardClick = { onRecipeCardClick(recipe.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(size - rowRecipes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (rowIndex != rows.lastIndex) {
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}