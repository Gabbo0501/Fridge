package com.example.fridgeproject.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.PrepTime
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.CustomChip
import com.example.fridgeproject.ui.components.FilterInputField
import com.example.fridgeproject.ui.components.FilterSquareButton

@Composable
fun FilterRecipesScreen(
    searchRecipeQuery: String,
    searchAuthorQuery: String,
    searchIngredientsQuery: String,
    selectedCategory : DishType?,
    selectedCookingTime : PrepTime?,
    selectedDifficulty: Difficulty?,
    difficultySliderPosition : Float,
    selectedCostRange : CostRange?,
    costSliderPosition : Float,
    onSearchRecipeQueryChanged : (String) -> Unit,
    onSearchAuthorQueryChanged : (String) -> Unit,
    onSearchIngredientsQueryChanged : (String) -> Unit,
    onCategorySelected : (DishType) -> Unit,
    onCookingTimeChanged : (PrepTime) -> Unit,
    clearDifficulty : () -> Unit,
    onDifficultySliderMoved : (Float) -> Unit,
    clearCostRange : () -> Unit,
    onCostSliderMoved : (Float) -> Unit,
    onBack: () -> Unit,
    onShowResults: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(start = 20.dp, top = 8.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PageHeader(onBackClick = onBack)

        // Search fields
        FilterInputField(
            value = searchRecipeQuery,
            onValueChange = { onSearchRecipeQueryChanged(it)},
            placeholder = "Search by recipe name",
            icon = Icons.Default.Search
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterInputField(
            value = searchAuthorQuery,
            onValueChange = { onSearchAuthorQueryChanged(it)},
            placeholder = "Search by author",
            icon = Icons.Default.Person
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterInputField(
            value = searchIngredientsQuery,
            onValueChange = {  onSearchIngredientsQueryChanged(it) },
            placeholder = "Search by ingredients (e.g. Tomato)",
            icon = Icons.Default.Restaurant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CATEGORY
        Text("Category", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomChip("Appetizer", isSelected = selectedCategory == DishType.APPETIZER,
                onClick = { onCategorySelected(DishType.APPETIZER)})
            CustomChip("First Course", isSelected = selectedCategory == DishType.FIRST_COURSE,
                onClick = { onCategorySelected(DishType.FIRST_COURSE)})
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CustomChip("Second Course", isSelected = selectedCategory == DishType.SECOND_COURSE,
                onClick = { onCategorySelected(DishType.SECOND_COURSE)})
            CustomChip("Sides", isSelected = selectedCategory == DishType.SIDE_DISH,
                onClick = { onCategorySelected(DishType.SIDE_DISH)})
            CustomChip("Dessert", isSelected = selectedCategory == DishType.DESSERT,
                onClick = { onCategorySelected(DishType.DESSERT)})
        }

        // PREPARATION TIME
        Text("Preparation Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FilterSquareButton("Short", "(5-20min)", isSelected = selectedCookingTime == PrepTime.SHORT,
                icon = Icons.Default.Schedule, onClick = { onCookingTimeChanged(PrepTime.SHORT)})
            FilterSquareButton("Medium", "(20min-1hr)", isSelected = selectedCookingTime == PrepTime.MEDIUM,
                icon = Icons.Default.Schedule, onClick = { onCookingTimeChanged(PrepTime.MEDIUM)})
            FilterSquareButton("Long", "(1hr+)",isSelected = selectedCookingTime == PrepTime.LONG,
                icon = Icons.Default.Schedule, onClick = { onCookingTimeChanged(PrepTime.LONG)})
        }

        // DIFFICULTY
        Spacer(Modifier.height(24.dp))
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
        }
        Spacer(Modifier.height(4.dp))
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

        // PRICE RANGE
        // COST RANGE
        Spacer(Modifier.height(24.dp))
        Row(
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
        }
        Spacer(Modifier.height(4.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
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

        Spacer(modifier = Modifier.height(32.dp))

        // SEARCH BUTTON
        Button(
            onClick = {onShowResults()},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Text("Search Recipes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))

        }
    }
}

