package com.example.fridgeproject.ui.components.fridge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.ui.components.recipe.BottomActionRow
import kotlin.collections.forEach

@Composable
fun IngredientsWithTimeSection(
    ingredients: List<IngredientQuantityWithTime> = emptyList(),
    initAddIngredientProcedure: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onUpdateIngredient: (String) -> Unit,
    onClearFridge: () -> Unit,
    hideHeaderDisclaimer: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ingredients",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        Surface(
            shape  = RoundedCornerShape(16.dp),
            color  = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info banner
                if(!hideHeaderDisclaimer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Quantities are relative to 1 person",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.primary
                        )

                    }
                    Spacer(Modifier.height(0.5.dp))
                }

                // Ingredient rows
                ingredients.forEach { ingredient ->
                    IngredientWithTimeRow(
                        ingredient = ingredient,
                        onUpdateIngredient = onUpdateIngredient,
                        onRemoveIngredient = onRemoveIngredient
                    )
                }
                Spacer(Modifier.height(5.dp))

                // Add/Clear button
                BottomActionRow(
                    isCancellable = ingredients.isNotEmpty(),
                    cancel = onClearFridge,
                    save = initAddIngredientProcedure,
                    saveLabel = "Add ingredient",
                    cancelLabel = "Reset"
                )
            }
        }
    }
}