package com.example.fridgeproject.navigation.graphs

import com.example.fridgeproject.navigation.*

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.model.toRecipeShortUi
import com.example.fridgeproject.ui.screens.home.*
import com.example.fridgeproject.viewmodel.recipe.RecipeListViewModel

fun NavGraphBuilder.homeGraph(navActions: AppNavigationActions) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navActions.getHomeGraphEntry() }
            val vm: RecipeListViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = RecipeListViewModel.Factory
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            HomeScreen(
                recipes = uiState.displayedRecipes.map { it.toRecipeShortUi() },
                userNickname = uiState.userNickname,
                currentUserId = SessionManagerFacade.currentUserId,
                dailyMenu = uiState.dailyMenu.map { it.toRecipeShortUi() },
                popularRecipes = uiState.popularRecipes.map { it.toRecipeShortUi() },
                newRecipes = uiState.newRecipes.map { it.toRecipeShortUi() },
                featuredRecipes = uiState.featuredRecipes.map { it.toRecipeShortUi() },
                onRecipeCardClick = { navActions.navigateToRecipeDetails(it) },
                isLoading = uiState.isLoading
            )
        }
    }
}
