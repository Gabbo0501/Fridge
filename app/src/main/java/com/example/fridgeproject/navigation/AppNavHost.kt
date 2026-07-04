package com.example.fridgeproject.navigation

import com.example.fridgeproject.navigation.graphs.*
import com.example.fridgeproject.navigation.utils.*

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.fridgeproject.camera.LocalCameraActions
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.ui.screens.camera.*
import com.example.fridgeproject.ui.screens.notification.*
import com.example.fridgeproject.ui.screens.recipe.*
import com.example.fridgeproject.viewmodel.*
import com.example.fridgeproject.viewmodel.notification.*
import com.example.fridgeproject.viewmodel.recipe.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    navActions: AppNavigationActions,
    exitRequestManager: ExitRequestManager,
    modifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = HomeGraph,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        homeGraph(navActions)

        //SINGLE-ROUTE
        composable<CreateRecipeRoute> {
            AuthenticatedRoute(navActions) {
                val cameraActions = LocalCameraActions.current
                val vm: CreateRecipeViewModel = viewModel(factory = CreateRecipeViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            RecipeEvent.Published -> onShowSnackbar("Recipe published successfully")
                            RecipeEvent.Updated -> onShowSnackbar("Recipe updated successfully")
                            RecipeEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            RecipeEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                            else -> {}
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateHome() }
                }
                CreateRecipeScreen(
                    newRecipe = uiState.newRecipe,
                    addingIngredient = uiState.addingIngredient,
                    addingIngredientStep = uiState.addingIngredientStep,
                    selectableIngredients = uiState.selectableIngredients,
                    newIngredient = uiState.newIngredient,
                    quantityInput = uiState.quantityInput,
                    currentEditedStepIndex = uiState.currentEditedStepIndex,
                    errors = uiState.errors,
                    showErrorDialog = uiState.showErrorDialog,
                    success =uiState.success,
                    isLoading = uiState.isLoading,
                    showExitDialog = uiState.showExitDialog,
                    showDifficultyDialog = uiState.showDifficultyDialog,
                    showTimeDialog = uiState.showTimeDialog,
                    showCostDialog = uiState.showCostDialog,
                    showDietDialog = uiState.showDietDialog,
                    showCuisineDialog = uiState.showCuisineDialog,
                    onDifficultyDialogOpen = { vm.showDifficultyDialog() },
                    onTimeDialogOpen = { vm.showTimeDialog() },
                    onCostDialogOpen = { vm.showCostDialog() },
                    onStatsDialogDismiss = { vm.dismissStatsDialog() },
                    onDietDialogOpen = { vm.showDietDialog() },
                    onCuisineDialogOpen = { vm.showCuisineDialog() },
                    onTagsDialogDismiss = { vm.dismissTagsDialog() },
                    onImageGalleryClick = {
                        cameraActions.onPickFromGallery { uri -> vm.updateImage(LocalImageInput.Gallery(uri)) }
                    },
                    onImageCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.updateImage(LocalImageInput.Camera(uri))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    updateTitle = {vm.updateTitle(it)},
                    updateDishType = {vm.updateDishType(it)},
                    toggleDiet = {vm.toggleDiet(it)},
                    toggleCuisine = {vm.toggleCuisine(it)},
                    updateDifficulty = {vm.updateDifficulty(it)},
                    updatePrepTime = {vm.updatePrepTime(it)},
                    updateCostRange = {vm.updateCostRange(it)},
                    updateDescription = {vm.updateDescription(it)},
                    initAddIngredientProcedure = {vm.initAddIngredientProcedure()},
                    cancelAddIngredientProcedure = {vm.cancelAddIngredientProcedure()},
                    updateNewIngredientCategory = {vm.updateNewIngredientCategory(it)},
                    updateNewIngredientName = {vm.updateNewIngredientName(it)},
                    updateNewIngredientQuantity = {vm.updateNewIngredientQuantity(it)},
                    onPreviousIngredientWizardStep = { vm.previousIngredientWizardStep() },
                    onNextIngredientWizardStep = { vm.nextIngredientWizardStep() },
                    addIngredient = {vm.addIngredient(it)},
                    updateIngredient = { vm.updateIngredient(it) },
                    removeIngredient = {vm.removeIngredient(it)},
                    onQuantityTextChange = { vm.updateNewIngredientQuantityText(it) },
                    onIncrementQuantity = { vm.incrementNewIngredientQuantity() },
                    onDecrementQuantity = { vm.decrementNewIngredientQuantity() },
                    isConfirmEnabled = uiState.isConfirmQuantityEnabled,
                    isEditMode = uiState.isEditMode,
                    addStep = {vm.addStep()},
                    updateStepDescription = {vm.updateStepDescription(it)},
                    onStepGalleryClick = {
                        cameraActions.onPickFromGallery { uri -> vm.updateStepImage(LocalImageInput.Gallery(uri)) }
                    },
                    onStepCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.updateStepImage(LocalImageInput.Camera(uri))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    removeStep = {vm.removeStep()},
                    switchLeftStep = {vm.switchLeftStep()},
                    switchRightStep = {vm.switchRightStep()},
                    onSaveRecipe = { vm.saveRecipe() },
                    onResetCreteState = {vm.resetCreateState()},
                    onNavigateToHome = {
                        if (uiState.success) {
                            navActions.navigateHome()
                        } else {
                            exitRequestManager.requestExitBefore { navActions.navigateHome() }
                        }
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onDismissErrorDialog = { vm.dismissErrorDialog() },
                )
            }
        }

        composable<CameraRoute> {
            val cameraManager = LocalCameraActions.current
            CameraScreen(
                cameraPreview = { cameraManager.cameraPreview() },
                onTakePhoto = { cameraManager.takePhoto() },
                onBack = { navActions.navigateBack() }
            )
        }
        composable<NotificationRoute>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "fridgelab://notifications/{profileUserId}" }
            )
        ) {
            AuthenticatedRoute(navActions) {
                val vm: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()

                NotificationScreen(
                    uiState = uiState,
                    onBackClick = { navActions.navigateBack() },
                    onMarkAsRead = { vm.markAsRead(it) },
                    onUserClick = { navActions.navigateToProfile(it) },
                    onRecipeClick = { navActions.navigateToRecipeDetails(it) },
                    onDeleteNotification = { vm.deleteNotification(it) },
                    onClearAll = { vm.clearAll() }
                )
            }
        }
        //NESTED-GRAPH
        recipeDetailGraph(navActions, exitRequestManager, onShowSnackbar)
        exploreGraph(navActions)
        profileGraph(navActions, exitRequestManager, onShowSnackbar)
        authGraph(navActions, exitRequestManager, onShowSnackbar)
        fridgeGraph(navActions, exitRequestManager)


    }
}