package com.example.fridgeproject.ui.components.user

import com.example.fridgeproject.model.enums.Diet
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import com.example.fridgeproject.ui.components.responsiveGridColumns

@Composable
fun UserDietSelector(
    selected: Diet,
    onSelect: (Diet) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = responsiveGridColumns()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Diet",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val chunkedDiets = Diet.entries.chunked(columns)

            chunkedDiets.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { diet ->
                        val isSelected = selected == diet

                        val cardBgColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val iconBgColor = if (isSelected) Color.LightGray else MaterialTheme.colorScheme.onPrimary
                        val iconTintColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.15f)
                                .clickable { onSelect(diet) },
                            shape = RoundedCornerShape(28.dp),
                            color = cardBgColor,
                            border = BorderStroke(width = 1.5.dp, color = borderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getDietIcon(diet),
                                        contentDescription = diet.desc,
                                        tint = iconTintColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = diet.desc,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun getDietIcon(diet: Diet): ImageVector {
    return when (diet.name.uppercase()) {
        "VEGETARIAN" -> Icons.Default.Eco
        "VEGAN" -> Icons.Default.Grass
        "KETO" -> Icons.Default.WaterDrop
        "OMNIVORE" -> Icons.Default.Restaurant
        "PALEO" -> Icons.Default.NoFood
        "GLUTEN_FREE" -> Icons.Default.Grass
        else -> Icons.Default.Circle
    }
}