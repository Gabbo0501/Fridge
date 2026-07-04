package com.example.fridgeproject.ui.components.fridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.model.enums.UnitOfMeasure

@Composable
fun IngredientWithTimeRow(
    ingredient: IngredientQuantityWithTime,
    onUpdateIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Qty badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = if(ingredient.unit.name != UnitOfMeasure.QB.name) 10.dp else 13.dp, vertical = 5.dp)
        ) {
            Text(
                if(ingredient.unit.name != UnitOfMeasure.QB.name) ingredient.formattedQuantity else "/",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Unit badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = if(ingredient.unit.name != UnitOfMeasure.QB.name) 10.dp else 13.dp, vertical = 5.dp)
        ) {
            Text(
                if(ingredient.unit.name != UnitOfMeasure.QB.name) ingredient.unit.toString() else "/",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Name
        Text(
            ingredient.ingredient.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Actions
        IconButton(
            onClick = { onUpdateIngredient(ingredient.ingredient.name)},
            modifier = Modifier.size(18.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        IconButton(
            onClick = { onRemoveIngredient(ingredient.ingredient.name)},
            modifier = Modifier.size(18.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

    }
}
