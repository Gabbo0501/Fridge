package com.example.fridgeproject.navigation.graphs

import com.example.fridgeproject.navigation.*

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.model.toRecipeShortUi
import com.example.fridgeproject.ui.screens.recipe.ExploreCategoriesScreen
import com.example.fridgeproject.ui.screens.recipe.FilterRecipesScreen
import com.example.fridgeproject.ui.screens.recipe.RecipeResultScreen
import com.example.fridgeproject.viewmodel.recipe.hasActiveFilters
import com.example.fridgeproject.viewmodel.recipe.RecipeListViewModel

fun NavGraphBuilder.exploreGraph(navActions: AppNavigationActions) {
    navigation<ExploreGraph>(startDestination = ExploreRoute) {

        composable<ExploreRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.getExploreGraphEntry()
            }
            val vm: RecipeListViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = RecipeListViewModel.Factory
            )

            val uiState by vm.uiState.collectAsStateWithLifecycle()

            ExploreCategoriesScreen(
                onCategoryClick = {
                    vm.onExploreCategorySelected(it)
                    navActions.navigateTo(RecipesResultListRoute)
                },
                isLoading = uiState.isLoading
            )
        }

        composable<RecipesResultListRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.getExploreGraphEntry()
            }
            val vm: RecipeListViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = RecipeListViewModel.Factory
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            RecipeResultScreen(
                recipes = uiState.displayedRecipes.map { it.toRecipeShortUi() },
                currentUserId = SessionManagerFacade.currentUserId,
                onBack = { navActions.navigateToExplore() },
                onFilterClick = { navActions.navigateTo(FilterRoute) },
                onClearFilters = { vm.clearAllFilters() },
                hasActiveFilters = uiState.hasActiveFilters(),
                onRecipeCardClick = { navActions.navigateToRecipeDetails(it) },
                isLoading = uiState.isLoading
            )
        }

        composable<FilterRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.getExploreGraphEntry()
            }
            val vm: RecipeListViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = RecipeListViewModel.Factory
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            FilterRecipesScreen(
                searchRecipeQuery = uiState.searchRecipeQuery,
                searchAuthorQuery = uiState.searchAuthorQuery,
                searchIngredientsQuery = uiState.searchIngredientsQuery,
                selectedCategory = uiState.selectedCategory,
                selectedCookingTime = uiState.selectedCookingTime,
                selectedDifficulty = uiState.selectedDifficulty,
                difficultySliderPosition = uiState.difficultySliderPosition,
                selectedCostRange = uiState.selectedCostRange,
                costSliderPosition = uiState.costSliderPosition,
                onSearchRecipeQueryChanged = {vm.onSearchRecipeQueryChanged(it)},
                onSearchAuthorQueryChanged = {vm.onSearchAuthorQueryChanged(it)},
                onSearchIngredientsQueryChanged = {vm.onSearchIngredientsQueryChanged(it)},
                onCategorySelected = {vm.onCategorySelected(it)},
                onCookingTimeChanged = {vm.onCookingTimeChanged(it)},
                clearDifficulty = {vm.clearDifficulty()},
                onDifficultySliderMoved = {vm.onDifficultySliderMoved(it)},
                clearCostRange = {vm.clearCostRange()},
                onCostSliderMoved = {vm.onCostSliderMoved(it)},
                onBack = { navActions.navigateBack()},
                onShowResults = {
                    navActions.navigateTo(RecipesResultListRoute)
                },
            )
        }

     }
}
