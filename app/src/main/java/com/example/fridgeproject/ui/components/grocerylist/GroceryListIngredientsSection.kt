package com.example.fridgeproject.ui.components.grocerylist

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
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
fun GroceryListIngredientsSection(
    ingredients: List<IngredientQuantityWithTime>,
    selectedIngredients: List<IngredientQuantityWithTime>,
    initAddIngredientProcedure: () -> Unit,
    updateIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    clearGroceryList: () -> Unit,
    selectIngredient : (IngredientQuantityWithTime) -> Unit,
    unselectIngredient : (IngredientQuantityWithTime) -> Unit,
    moveSelectedIngredientsToFridge: () -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if ( ingredients.isEmpty()){
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
                            "Your grocery list is empty! Manually add a new ingredient or explore the recipes and take note of missing ingredients",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.primary
                        )

                    }
                }else{
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
                                "Selected items will be automatically added to 'My Fridge' with the quantities shown. Please ensure the amounts are correct before saving.",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.primary
                            )

                        }
                        Spacer(Modifier.height(0.5.dp))
                    }

                    // Ingredient rows
                    ingredients.forEach { ingredient ->
                        GroceryListIngredientRow(
                            ingredient = ingredient,
                            checked = selectedIngredients.contains(ingredient),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectIngredient(ingredient)
                                } else {
                                    unselectIngredient(ingredient)
                                }
                            },
                            onUpdateIngredient = updateIngredient,
                            onRemoveIngredient = onRemoveIngredient
                        )
                    }
                }
                // Add item and Cancel button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BottomActionRow(
                        isCancellable = ingredients.isNotEmpty(),
                        cancel = clearGroceryList,
                        save = initAddIngredientProcedure,
                        saveLabel = "Add ingredient",
                        cancelLabel = "Reset"
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Save button
                Button(
                    onClick = moveSelectedIngredientsToFridge,
                    enabled = selectedIngredients.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Move To Fridge", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}