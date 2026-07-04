package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.ui.components.responsiveGridColumns

@Composable
fun RecipeRow(
    title: String,
    subtitle: String = "",
    currentUserId: String?,
    recipes: List<RecipeShortUi>,
    onRecipeCardClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val visibleCards = responsiveGridColumns()
    val itemSpacing = 12.dp

    Column {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = (maxWidth - itemSpacing * (visibleCards - 1)) / visibleCards

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                items(
                    items = recipes,
                    key = { it.id }
                ) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        isOwner = recipe.authorId == currentUserId,
                        onRecipeCardClick = { onRecipeCardClick(recipe.id) },
                        modifier = Modifier.width(cardWidth)
                    )
                }
            }
        }
    }
}