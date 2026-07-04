package com.example.fridgeproject.ui.screens.home
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.recipe.DailyRecipesCarousel
import com.example.fridgeproject.ui.components.recipe.RecipeCard
import com.example.fridgeproject.ui.components.recipe.RecipeRow
import com.example.fridgeproject.ui.components.responsiveGridColumns

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    currentUserId: String?,
    userNickname: String?,
    recipes: List<RecipeShortUi>,
    dailyMenu: List<RecipeShortUi>,
    popularRecipes: List<RecipeShortUi>,
    newRecipes: List<RecipeShortUi>,
    featuredRecipes: List<RecipeShortUi>,
    onRecipeCardClick: (String) -> Unit,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val columns = responsiveGridColumns()

    if(isLoading){
        LoadingComponent()
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier
                .fillMaxSize()
                .background(colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.padding(
                        bottom = if (recipes.isEmpty()) PageBottomPadding else 0.dp
                    )
                ) {
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    //Header
//                    WelcomeHeader(userNickname)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ricette del giorno
                    DailyRecipesCarousel(
                        recipes = dailyMenu,
                        onRecipeCardClick = onRecipeCardClick
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Ricette famose (+ like)
                    RecipeRow(
                        title = "Community Favorites",
                        subtitle = "The recipes everyone is loving right now",
                        currentUserId = currentUserId,
                        recipes = popularRecipes,
                        onRecipeCardClick = onRecipeCardClick
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Ricette recenti (+ recenti)
                    RecipeRow(
                        title = "Fresh from the Kitchen",
                        subtitle = "Discover the latest dishes added by the community",
                        currentUserId = currentUserId,
                        recipes = newRecipes,
                        onRecipeCardClick = onRecipeCardClick
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Ricette con maggiore rating (+ rating)
                    RecipeRow(
                        title = "Top-Rated Masterpieces",
                        subtitle = "Highest marks from the community",
                        currentUserId = currentUserId,
                        recipes = featuredRecipes,
                        onRecipeCardClick = onRecipeCardClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    /* TODO aggiungere altre categorie in futuro */

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(
                        modifier = Modifier
                            .width(130.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape),
                        thickness = 3.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Get inspired by more recipes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Griglia con le ricette proposte
            val shownRecipes = recipes.take(50)
            val lastRowStart = if (shownRecipes.isEmpty()) 0 else ((shownRecipes.lastIndex) / columns) * columns
            itemsIndexed(
                items = shownRecipes,
                key = { _, recipe -> recipe.id }
            ) { index, recipe ->
                RecipeCard(
                    recipe = recipe,
                    isOwner = recipe.authorId == currentUserId,
                    onRecipeCardClick = { onRecipeCardClick(recipe.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index >= lastRowStart) PageBottomPadding else 0.dp)
                )
            }
        }
    }
}