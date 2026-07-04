package com.example.fridgeproject.ui.screens.fridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.fridgeproject.ui.theme.FridgeBackground
import com.example.fridgeproject.ui.theme.NavGrey

@Composable
fun MyFridgeScreen (
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    fridgeTabContent: @Composable () -> Unit,
    groceryTabContent: @Composable () -> Unit,
    ingredientWizard: (@Composable () -> Unit)?
) {
    val tabs = listOf("Fridge", "Grocery List")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FridgeBackground)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = FridgeBackground,
            divider = {},
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else NavGrey,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> fridgeTabContent()
            1 -> groceryTabContent()
        }
    }
    ingredientWizard?.invoke()
}


