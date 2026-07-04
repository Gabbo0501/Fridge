package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.ui.theme.FridgeOrange
import com.example.fridgeproject.ui.theme.FridgeTipDont
import com.example.fridgeproject.viewmodel.recipe.MissingQuantityDetail

@Composable
fun FridgePercentageSection(
    percentage: Int,
    missingIngredients: List<IngredientQuantity>,
    missingQuantityIngredients: List<MissingQuantityDetail>,
    addMissingIngredientsToGroceryList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if(percentage < 100) MaterialTheme.colorScheme.outlineVariant else Color(0xFF2E7D32),)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Match percentage: ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percentage%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if(percentage < 100) FridgeTipDont else Color(0xFF2E7D32)
                )
            }

            // Barra progressi
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if(percentage < 100) FridgeTipDont else Color(0xFF2E7D32),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Ingredienti mancanti
            if (missingIngredients.isNotEmpty() || missingQuantityIngredients.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    missingQuantityIngredients.forEach { detail ->
                        val reqIng = detail.recipeRequirement
                        val availableQty = detail.availableInFridge

                        val unitStr = if (reqIng.unit == UnitOfMeasure.QB) "" else reqIng.unit.name.lowercase()
                        val reqQty = reqIng.quantity.toInt()

                        Text(
                            text = "- Insufficient '${reqIng.ingredient.name}' ($availableQty/${reqQty}$unitStr)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    missingIngredients.forEach { reqIng ->
                        val amountStr = if (reqIng.unit == UnitOfMeasure.QB) "to taste" else "${reqIng.quantity.toInt()} ${reqIng.unit.name.lowercase()}"
                        Text(
                            text = "- Missing '${reqIng.ingredient.name}' ($amountStr)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Pulsante per aggiungere alla lista della spesa
                Button(
                    onClick = addMissingIngredientsToGroceryList,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .wrapContentWidth()
                        .padding(top = 8.dp)
                        .height(38.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FridgeOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Grocery List",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                 // Tutto disonibile
                 Text(
                    text = "✓ You have all the necessary ingredients in your fridge!",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}