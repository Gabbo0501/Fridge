package com.example.fridgeproject.ui.components.fridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.ui.components.recipe.RecipeCard
import kotlin.collections.chunked
import kotlin.collections.forEach

@Composable
fun FridgeRecipeList(
    recipes: List<RecipeShortUi>,
    currentUserId: String?,
    onRecipeCardClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        recipes.chunked(2).forEach { rowRecipes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowRecipes.forEach { recipe ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        RecipeCard(
                            recipe = recipe,
                            isOwner = recipe.authorId == currentUserId,
                            onRecipeCardClick = { onRecipeCardClick(recipe.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (rowRecipes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}