package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.DishType
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.FavoriteBorder

//Barra azioni Like, Preferiti, Save e Import
@Composable
fun RecipeActionsBar(
    rating: Float,
    likes: Int,
    isFavorite: Boolean,
    onRemixClick: (() -> Unit)? = null,
    onLikeToggle: (() -> Unit)? = null,
    onSaveClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            ActionItem(
                icon = Icons.Filled.Star,
                label = rating.toString(),
                iconColor = MaterialTheme.colorScheme.primary
            )

            ActionItem(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = likes.toString(),
                iconColor = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onLikeToggle
            )
            ActionItem(
                icon = Icons.Filled.BookmarkBorder,
                label = "Save",
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onSaveClick
            )
            ActionItem(
                icon = Icons.Filled.ContentCopy,
                label = "Remix",
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onRemixClick
            )
        }
    }
}

//Icona unica
@Composable
fun ActionItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



//Sezione Tag Ricetta
@Composable
fun RecipeTagsSection(dishType: DishType, suitableDiets: List<Diet>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            RecipeTag(
                text = dishType.desc,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.primary
            )
        }
        items(suitableDiets){ diet ->
            Spacer(modifier = Modifier.width(10.dp))
            RecipeTag(
                text = diet.desc,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecipeTag(text: String, backgroundColor: Color, textColor: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}