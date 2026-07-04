package com.example.fridgeproject.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.PrepTime
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.FilterSquareButton

@Composable
fun ProfileRecipeFilterScreen(
    selectedCookingTime: PrepTime?,
    onCookingTimeChanged: (PrepTime) -> Unit,
    selectedDifficulty: Difficulty?,
    clearDifficulty: () -> Unit,
    difficultySliderPosition: Float,
    onDifficultySliderMoved: (Float)-> Unit,
    selectedCostRange: CostRange?,
    clearCostRange: () -> Unit,
    costSliderPosition: Float,
    onCostSliderMoved: (Float)-> Unit,
    onBackClick: () -> Unit
){
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ){
        item { PageHeader(onBackClick = onBackClick) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FilterSquareButton("Short", "(5-20min)", isSelected = selectedCookingTime == PrepTime.SHORT,
                    icon = Icons.Default.Schedule, onClick = { onCookingTimeChanged(PrepTime.SHORT)})
                FilterSquareButton("Medium", "(20min-1hr)", isSelected = selectedCookingTime == PrepTime.MEDIUM,
                    icon = Icons.Default.Schedule, onClick = { onCookingTimeChanged(PrepTime.MEDIUM)})
                FilterSquareButton("Long", "(1hr+)",isSelected = selectedCookingTime == PrepTime.LONG,
                    icon = Icons.Default.Schedule, onClick = { onCookingTimeChanged(PrepTime.LONG)})
            }
        }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Difficulty: ${selectedDifficulty?.value ?: "-"}/5",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (selectedDifficulty != null) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reset difficulty",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {  clearDifficulty() }
                    )
                }
            }}
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Slider(
                    value = difficultySliderPosition,
                    onValueChange = {  onDifficultySliderMoved(it) },
                    valueRange = 0f..Difficulty.entries.lastIndex.toFloat(),
                    steps = Difficulty.entries.lastIndex - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.outlineVariant,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Very Easy", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text("Hard", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item{Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cost range: ${selectedCostRange?.description ?: "-"}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            if (selectedCostRange != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reset cost range",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {  clearCostRange() }
                )
            }
        }}
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item{
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Slider(
                    value = costSliderPosition,
                    onValueChange = {  onCostSliderMoved(it) },
                    valueRange = 0f..CostRange.entries.lastIndex.toFloat(),
                    steps = CostRange.entries.lastIndex - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.outlineVariant,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(CostRange.FIVE.description, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(CostRange.TEN.description, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(CostRange.TWENTY.description, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(CostRange.OVER_TWENTY.description, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
        item{
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = PageBottomPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onBackClick) {Text("Search Recipes") }
            }
        }
    }
}
