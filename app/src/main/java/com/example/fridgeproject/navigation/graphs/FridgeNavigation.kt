package com.example.fridgeproject.navigation.graphs

import com.example.fridgeproject.navigation.*
import com.example.fridgeproject.navigation.utils.*


import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.model.toRecipeShortUi
import com.example.fridgeproject.ui.components.AlertDialog
import com.example.fridgeproject.ui.components.fridge.FridgeComponent
import com.example.fridgeproject.ui.components.grocerylist.GroceryListComponent
import com.example.fridgeproject.ui.components.wizard.IngredientWizard
import com.example.fridgeproject.ui.screens.fridge.FridgeRecipeResultScreen
import com.example.fridgeproject.ui.screens.fridge.MyFridgeScreen
import com.example.fridgeproject.viewmodel.FridgeEvent
import com.example.fridgeproject.ui.screens.fridge.*
import com.example.fridgeproject.viewmodel.GroceryListEvent
import com.example.fridgeproject.viewmodel.fridge.FridgeRecipeListViewModel
import com.example.fridgeproject.viewmodel.fridge.FridgeViewModel
import com.example.fridgeproject.viewmodel.fridge.GroceryListViewModel
import com.example.fridgeproject.viewmodel.fridge.hasActiveFilters

fun NavGraphBuilder.fridgeGraph(
    navActions: AppNavigationActions,
    exitRequestManager: ExitRequestManager
) {
    navigation<FridgeGraph>(startDestination = MyFridgeRoute) {
        composable<MyFridgeRoute> { backStackEntry ->
            AuthenticatedRoute(navActions) {
                val parentEntry = remember(backStackEntry) { navActions.getFridgeGraphEntry() }

                val fridgeVM: FridgeViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = FridgeViewModel.Factory)
                val fridgeUiState by fridgeVM.uiState.collectAsStateWithLifecycle()

                val groceryVM: GroceryListViewModel = viewModel(viewModelStoreOwner = parentEntry, factory = GroceryListViewModel.Factory)
                val groceryUiState by groceryVM.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    fridgeVM.events.collect { event ->
                        when (event) {
                            FridgeEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            FridgeEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                            else -> {}
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    groceryVM.events.collect { event ->
                        when (event) {
                            GroceryListEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            GroceryListEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                            else -> {}
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest {
                        when {
                            groceryVM.hasUnsavedChanges() -> groceryVM.requestExit()
                            fridgeVM.hasUnsavedChanges() -> fridgeVM.requestExit()
                            else -> exitRequestManager.executePendingNavigation()
                        }
                    }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }

                if(fridgeUiState.showClearFridgeDialog) {
                    AlertDialog(
                        title = "Clear the Fridge?",
                        text = "Are you sure you want to remove all ingredients from your Fridge? This action cannot be undone.",
                        confirmButtonText = "Clear All",
                        dismissButtonText = "Cancel",
                        confirmTextColor = MaterialTheme.colorScheme.error,
                        onConfirm = { fridgeVM.clearFridge() },
                        onDismiss = { fridgeVM.dismissClearFridgeDialog() }
                    )
                }
                if(groceryUiState.showClearGroceryListDialog) {
                    AlertDialog(
                        title = "Clear the Grocery list?",
                        text = "Are you sure you want to remove all ingredients from your Grocery list? This action cannot be undone.",
                        confirmButtonText = "Clear All",
                        dismissButtonText = "Cancel",
                        confirmTextColor = MaterialTheme.colorScheme.error,
                        onConfirm = { groceryVM.clearGroceryList() },
                        onDismiss = { groceryVM.dismissClearGroceryListDialog() }
                    )
                }
                MyFridgeScreen(
                    selectedTabIndex = fridgeUiState.selectedTabIndex,
                    onTabSelected = { fridgeVM.selectTab(it) },
                    fridgeTabContent = {
                        FridgeComponent(
                            addingIngredient = fridgeUiState.addingIngredient,
                            showExitDialog = fridgeUiState.showExitDialog,
                            onConfirmExit = { fridgeVM.confirmExit() },
                            onDismissExitDialog = { fridgeVM.dismissExitDialog() },
                            fridgeIngredients = fridgeUiState.fridgeIngredients,
                            onSearchRecipes = { navActions.navigateTo(FridgeRecipesResultListRoute) },
                            isLoading = fridgeUiState.isLoading,
                            onClearFridge = { fridgeVM.requestClearFridge() },
                            initAddIngredientProcedure = { fridgeVM.initAddIngredientProcedure() },
                            updateIngredient = { fridgeVM.updateIngredient(it) },
                            removeIngredient = { fridgeVM.removeIngredient(it) },

                        )
                    },
                    groceryTabContent = {
                        GroceryListComponent(
                            groceryListIngredients = groceryUiState.groceryListIngredients,
                            isLoading = groceryUiState.isLoading,
                            selectedIngredients = groceryUiState.selectedIngredients,
                            addingIngredient = groceryUiState.addingIngredient,
                            clearGroceryList = { groceryVM.requestClearGroceryList() },
                            initAddIngredientProcedure= { groceryVM.initAddIngredientProcedure()},
                            removeIngredient = { groceryVM.removeIngredient(it) },
                            updateIngredient = {groceryVM.updateIngredient(it)},
                            selectIngredient = {groceryVM.selectIngredient(it)},
                            unselectIngredient = {groceryVM.unselectIngredient(it)},
                            moveSelectedIngredientsToFridge = {groceryVM.moveSelectedIngredientsToFridge()},
                            showExitDialog = groceryUiState.showExitDialog,
                            confirmExit = {groceryVM.confirmExit()},
                            dismissExitDialog = {groceryVM.dismissExitDialog()},
                        )
                    },
                    ingredientWizard =  when {
                        fridgeUiState.addingIngredient -> ({
                            IngredientWizard(
                                step = fridgeUiState.addingIngredientStep,
                                selectedCategory = fridgeUiState.newIngredient.ingredient.category,
                                ingredients = fridgeUiState.selectableIngredients,
                                selectedIngredient = fridgeUiState.newIngredient.ingredient.name,
                                quantityText = fridgeUiState.quantityInput,
                                unit = fridgeUiState.newIngredient.unit,
                                onClose = {
                                    exitRequestManager.requestExitBefore {
                                        fridgeVM.cancelAddIngredientProcedure()
                                    }
                                },
                                onBack = { fridgeVM.previousIngredientWizardStep() },
                                onCategorySelect = { fridgeVM.updateNewIngredientCategory(it) },
                                onIngredientSelect = { fridgeVM.updateNewIngredientName(it) },
                                onNext = { fridgeVM.nextIngredientWizardStep() },
                                onConfirm = { fridgeVM.addIngredient(fridgeUiState.newIngredient) },
                                onQuantityTextChange = { fridgeVM.updateNewIngredientQuantityText(it) },
                                onIncrementQuantity = { fridgeVM.incrementNewIngredientQuantity() },
                                onDecrementQuantity = { fridgeVM.decrementNewIngredientQuantity() },
                                isConfirmEnabled = fridgeUiState.isConfirmQuantityEnabled,
                                isEditMode = fridgeUiState.isEditMode,
                            )
                        })
                        groceryUiState.addingIngredient -> ({
                            IngredientWizard(
                                step = groceryUiState.addingIngredientStep,
                                selectedCategory = groceryUiState.newIngredient.ingredient.category,
                                ingredients = groceryUiState.selectableIngredients,
                                selectedIngredient = groceryUiState.newIngredient.ingredient.name,
                                quantityText = groceryUiState.quantityInput,
                                unit = groceryUiState.newIngredient.unit,
                                onClose = {
                                    exitRequestManager.requestExitBefore {
                                        groceryVM.cancelAddIngredientProcedure()
                                    }
                                },
                                onBack = { groceryVM.previousIngredientWizardStep() },
                                onCategorySelect = { groceryVM.updateNewIngredientCategory(it) },
                                onIngredientSelect = { groceryVM.updateNewIngredientName(it) },
                                onNext = { groceryVM.nextIngredientWizardStep() },
                                onConfirm = { groceryVM.addIngredient(groceryUiState.newIngredient) },
                                onQuantityTextChange = { groceryVM.updateNewIngredientQuantityText(it) },
                                onIncrementQuantity = { groceryVM.incrementNewIngredientQuantity() },
                                onDecrementQuantity = { groceryVM.decrementNewIngredientQuantity() },
                                isConfirmEnabled = groceryUiState.isConfirmQuantityEnabled,
                                isEditMode = groceryUiState.isEditMode,
                            )
                        })
                        else -> null
                    }
                )
            }
        }

        composable<FridgeRecipesResultListRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navActions.getFridgeGraphEntry() }
            val vm: FridgeRecipeListViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = FridgeRecipeListViewModel.Factory
            )

            val uiState by vm.uiState.collectAsStateWithLifecycle()

            FridgeRecipeResultScreen(
                recipes = uiState.displayedRecipes.map { it.toRecipeShortUi() },
                doableRecipes = uiState.doableRecipes.map { it.toRecipeShortUi() },
                missingQuantityRecipes = uiState.missingQuantityRecipes.map { it.toRecipeShortUi() },
                currentUserId = SessionManagerFacade.currentUserId,
                onBack = { navActions.navigateTo(MyFridgeRoute) },
                onFilterClick = { navActions.navigateTo(FridgeRecipesResultRoute) },
                onClearFilters = { vm.clearAllFilters() },
                hasActiveFilters = uiState.hasActiveFilters(),
                onRecipeCardClick = { navActions.navigateToRecipeDetails(it) },
                isLoading = uiState.isLoading
            )
        }

        composable<FridgeRecipesResultRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navActions.getFridgeGraphEntry() }
            val vm: FridgeRecipeListViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = FridgeRecipeListViewModel.Factory
            )

            val uiState by vm.uiState.collectAsStateWithLifecycle()

            FridgeFilterScreen(
                searchIngredientsQuery = uiState.searchIngredientsQuery,
                selectedCategory = uiState.selectedCategory,
                selectedCookingTime = uiState.selectedCookingTime,
                selectedDifficulty = uiState.selectedDifficulty,
                difficultySliderPosition = uiState.difficultySliderPosition,
                selectedCostRange = uiState.selectedCostRange,
                costSliderPosition = uiState.costSliderPosition,
                onSearchIngredientsQueryChanged = {vm.onSearchIngredientsQueryChanged(it)},
                onCategorySelected = {vm.onCategorySelected(it)},
                onCookingTimeChanged = {vm.onCookingTimeChanged(it)},
                clearDifficulty = {vm.clearDifficulty()},
                onDifficultySliderMoved = {vm.onDifficultySliderMoved(it)},
                clearCostRange = {vm.clearCostRange()},
                onCostSliderMoved = {vm.onCostSliderMoved(it)},
                onBack = { navActions.navigateBack()},
                onSearch = { navActions.navigateTo(FridgeRecipesResultListRoute) },
            )
        }
    }
}
