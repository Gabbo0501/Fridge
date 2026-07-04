package com.example.fridgeproject.ui.screens.fridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.fridge.FridgeRecipeList
import com.example.fridgeproject.ui.components.recipe.RecipeRow


@Composable
fun FridgeRecipeResultScreen(
    recipes: List<RecipeShortUi>,
    doableRecipes: List<RecipeShortUi>,
    missingQuantityRecipes: List<RecipeShortUi>,
    currentUserId: String?,
    title: String = "Found for you",
    hasFilters: Boolean = true,
    hasActiveFilters: Boolean = false,
    onBack: () -> Unit,
    onFilterClick: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    onRecipeCardClick: (String) -> Unit,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    if (isLoading) {
        LoadingComponent()
    } else {
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (hasFilters) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        IconButton(onClick = onFilterClick) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = colorScheme.primary
                            )
                        }
                        if (hasActiveFilters) {
                            IconButton(onClick = onClearFilters) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear filters",
                                    tint = colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recipes.isNotEmpty()) {
                Text(
                    text = "${recipes.size} recipes found",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recipes.isEmpty()) {
                EmptyStateComponent(
                    message = "No recipes found.",
                    icon = Icons.Default.Search,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Ricette fattibili
                if(doableRecipes.isNotEmpty()) {
                    RecipeRow(
                        title = "Perfect for you!",
                        subtitle = "You have all necessary ingredients and quantities",
                        currentUserId = currentUserId,
                        recipes = doableRecipes,
                        onRecipeCardClick = onRecipeCardClick
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }

                // Ricette per cui mancano alcune quantità
                if(missingQuantityRecipes.isNotEmpty()) {
                    RecipeRow(
                        title = "Almost There!",
                        subtitle = "You have all ingredients, but you are short on some quantities",
                        currentUserId = currentUserId,
                        recipes = missingQuantityRecipes,
                        onRecipeCardClick = onRecipeCardClick
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }


                HorizontalDivider(
                    modifier = Modifier
                        .width(130.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape),
                    thickness = 3.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ricette per cui si ha almeno un ingrediente
                Text(
                    text = "Partial Matches",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "You have at least one ingredient",
                    fontSize = 16.sp,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                FridgeRecipeList(
                    recipes = recipes,
                    currentUserId = currentUserId,
                    onRecipeCardClick = onRecipeCardClick
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
