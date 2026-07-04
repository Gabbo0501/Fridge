package com.example.fridgeproject.navigation.graphs

import com.example.fridgeproject.navigation.*
import com.example.fridgeproject.navigation.utils.*

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.camera.LocalCameraActions
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.ui.screens.review_and_tip.NewReviewScreen
import com.example.fridgeproject.ui.screens.review_and_tip.NewTipScreen
import com.example.fridgeproject.ui.screens.recipe.RecipeProposalEditScreen
import com.example.fridgeproject.ui.screens.recipe.RecipeProposalScreen
import com.example.fridgeproject.ui.screens.review_and_tip.RecipeReviewsListScreen
import com.example.fridgeproject.ui.screens.review_and_tip.RecipeTipsListScreen
import com.example.fridgeproject.ui.screens.recipe.RemixRecipeScreen
import com.example.fridgeproject.ui.screens.collection.SaveRecipeToCollectionScreen
import com.example.fridgeproject.viewmodel.recipe.EditRecipeViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.NewReviewViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.NewTipViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.RecipeReviewViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.RecipeTipsViewModel
import com.example.fridgeproject.viewmodel.recipe.RecipeViewModel
import com.example.fridgeproject.viewmodel.recipe.RemixRecipeViewModel
import com.example.fridgeproject.viewmodel.RecipeEvent
import com.example.fridgeproject.viewmodel.ReviewEvent
import com.example.fridgeproject.viewmodel.collection.SaveRecipeToCollectionViewModel
import com.example.fridgeproject.viewmodel.TipEvent

fun NavGraphBuilder.recipeDetailGraph(
    navActions: AppNavigationActions,
    exitRequestManager: ExitRequestManager,
    onShowSnackbar: (String) -> Unit = {}
) {
    navigation<RecipeDetailGraph>(startDestination = RecipeDetailRoute) {

        composable<RecipeDetailRoute> {
            val vm: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory)
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        RecipeEvent.RecipeDeleted -> {
                            vm.resetDeleteState()
                            val currentUserId = SessionManagerFacade.currentUserId
                            if (currentUserId != null) {
                                navActions.navigateToProfile(currentUserId)
                            } else {
                                navActions.navigateHome()
                            }
                            onShowSnackbar("Recipe deleted successfully")
                        }
                        RecipeEvent.ReviewDeleted -> {
                            onShowSnackbar("Review deleted successfully")
                        }
                        RecipeEvent.TipDeleted -> {
                            onShowSnackbar("Tip deleted successfully")
                        }
                        RecipeEvent.MissingIngredientsAddedToGroceryList -> {
                            onShowSnackbar("Missing Ingredients Added To Grocery List")
                        }
                        else -> {}
                    }
                }
            }

            RecipeProposalScreen(
                uiState = uiState,
                isLoggedIn = SessionManagerFacade.isLoggedIn,
                onBackClick = { navActions.navigateBack() },
                onEditClick = { uiState.content.recipe?.let { navActions.navigateTo(EditRecipeRoute(it.id)) } },
                onRemixClick = {
                    if (SessionManagerFacade.isLoggedIn) uiState.content.recipe?.let { navActions.navigateTo(RemixRecipeRoute(it.id)) }
                    else navActions.navigateToAuth()
                },
                onExpandReviewList = { uiState.content.recipe?.let { navActions.navigateTo(RecipeReviewListRoute(it.id))}},
                onAddReviewClick = { uiState.content.recipe?.let { navActions.navigateTo(NewReviewRoute(it.id)) } },
                onExpandTipsList = {uiState.content.recipe?.let { navActions.navigateTo(RecipeTipsListRoute(it.id))}},
                onAddTipClick = { uiState.content.recipe?.let { navActions.navigateTo(NewTipRoute(it.id)) } },
                onAuthorClick = { navActions.navigateToProfile(it) },
                onDeleteClick = {
                    if (SessionManagerFacade.isLoggedIn) vm.showDeleteRecipeDialog()
                    else navActions.navigateToAuth()
                },
                onDeleteConfirm = {
                    if (SessionManagerFacade.isLoggedIn) vm.deleteRecipe()
                    else navActions.navigateToAuth()
                },
                onDeleteDismiss = { vm.dismissDeleteRecipeDialog() },
                onDeleteReviewClick = {
                    if (SessionManagerFacade.isLoggedIn) vm.requestDeleteReview(it)
                    else navActions.navigateToAuth()
                },
                onDeleteReviewConfirm = {
                    if (SessionManagerFacade.isLoggedIn) vm.confirmDeleteReview()
                    else navActions.navigateToAuth()
                },
                onDeleteReviewDismiss = { vm.dismissDeleteReviewDialog() },
                onDeleteTipClick = {
                    if (SessionManagerFacade.isLoggedIn) vm.requestDeleteTip(it)
                    else navActions.navigateToAuth()
                },
                onDeleteTipConfirm = {
                    if (SessionManagerFacade.isLoggedIn) {
                        vm.confirmDeleteTip()
                    }
                    else navActions.navigateToAuth()
                },
                onDeleteTipDismiss = { vm.dismissDeleteTipDialog() },
                onLikeToggle = {
                    if (SessionManagerFacade.isLoggedIn) vm.toggleFavorite()
                    else navActions.navigateToAuth()
                },
                onSaveClick = {
                    if (SessionManagerFacade.isLoggedIn) {
                        uiState.content.recipe?.let { navActions.navigateTo(SaveRecipeToCollectionRoute(it.id)) }
                    }
                    else navActions.navigateToAuth()
                },
                addMissingIngredientsToGroceryList = {
                    if (SessionManagerFacade.isLoggedIn) vm.addMissingIngredientsToGroceryList()
                    else navActions.navigateToAuth()
                }
            )
        }

        composable<SaveRecipeToCollectionRoute> {
            AuthenticatedRoute(navActions) {
                val vm: SaveRecipeToCollectionViewModel = viewModel(factory = SaveRecipeToCollectionViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()

                SaveRecipeToCollectionScreen(
                    uiState = uiState,
                    onBackClick = { navActions.navigateBack() },
                    onCollectionToggle = { vm.toggleCollection(it) },
                    onCreateCollectionClick = { vm.showCreateCollectionDialog() },
                    onCollectionNameChange = { vm.updateCollectionName(it) },
                    onCreateCollectionConfirm = { vm.saveCollection() },
                    onCreateCollectionDismiss = { vm.dismissCreateCollectionDialog() }
                )
            }
        }

        composable<EditRecipeRoute> {
            AuthenticatedRoute(navActions) {
                val cameraActions = LocalCameraActions.current
                val vm: EditRecipeViewModel = viewModel(factory = EditRecipeViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            RecipeEvent.Updated -> onShowSnackbar("Recipe updated successfully")
                            RecipeEvent.Published -> onShowSnackbar("Recipe published successfully")
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
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                RecipeProposalEditScreen(
                    uiState = uiState,
                    onTitleChange = { vm.updateTitle(it) },
                    onDescriptionChange = { vm.updateDescription(it) },
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
                    onPrepTimeChange = { vm.updatePrepTime(it) },
                    onDifficultyChange = { vm.updateDifficulty(it) },
                    onDishTypeChange = { vm.updateDishType(it) },
                    onCostRangeChange = { vm.updateCostRange(it) },
                    onAddIngredient = { vm.addIngredient(it) },
                    onRemoveIngredient = { vm.removeIngredient(it) },
                    onInitAddIngredientProcedure = { vm.initAddIngredientProcedure() },
                    onCancelAddIngredientProcedure = { vm.cancelAddIngredientProcedure() },
                    onUpdateNewIngredientCategory = { vm.updateNewIngredientCategory(it) },
                    onUpdateNewIngredientName = { vm.updateNewIngredientName(it) },
                    onUpdateNewIngredientQuantity = { vm.updateNewIngredientQuantity(it) },
                    onUpdateNewIngredientUnit = { vm.updateNewIngredientUnit(it) },
                    onPreviousIngredientWizardStep = { vm.previousIngredientWizardStep() },
                    onNextIngredientWizardStep = { vm.nextIngredientWizardStep() },
                    onQuantityTextChange = { vm.updateNewIngredientQuantityText(it) },
                    onUpdateIngredient = { vm.updateIngredient(it) },
                    onIncrementQuantity = { vm.incrementNewIngredientQuantity() },
                    onDecrementQuantity = { vm.decrementNewIngredientQuantity() },
                    isConfirmEnabled = uiState.isConfirmQuantityEnabled,
                    isEditMode = uiState.isEditMode,
                    onAddStep = { vm.addStep() },
                    onRemoveStep = { vm.removeStep() },
                    onStepGalleryClick = {
                        cameraActions.onPickFromGallery { uri -> vm.updateImageStep(LocalImageInput.Gallery(uri)) }
                    },
                    onStepCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.updateImageStep(LocalImageInput.Camera(uri))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    onUpdateStepDescription = { vm.updateStepDescription(it) },
                    onSwitchLeftStep = { vm.switchLeftStep() },
                    onSwitchRightStep = { vm.switchRightStep() },
                    onDietToggle = { vm.toggleDiet(it) },
                    onCuisineToggle = { vm.toggleCuisine(it) },
                    onDifficultyDialogOpen = { vm.showDifficultyDialog() },
                    onTimeDialogOpen = { vm.showTimeDialog() },
                    onCostDialogOpen = { vm.showCostDialog() },
                    onStatsDialogDismiss = { vm.dismissStatsDialog() },
                    onDietDialogOpen = { vm.showDietDialog() },
                    onCuisineDialogOpen = { vm.showCuisineDialog() },
                    onTagsDialogDismiss = { vm.dismissTagsDialog() },
                    onSaveClick = { vm.saveRecipe() },
                    onResetClick = { vm.onReset() },
                    onBackClick = { 
                        if (uiState.isSaved) {
                            navActions.navigateBack()
                        } else {
                            exitRequestManager.requestExitBefore { navActions.navigateBack() }
                        }
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onDismissErrorDialog = { vm.dismissErrorDialog() }
                )
            }
        }

        composable<RemixRecipeRoute> {
            AuthenticatedRoute(navActions) {
                val cameraActions = LocalCameraActions.current
                val vm: RemixRecipeViewModel = viewModel(factory = RemixRecipeViewModel.Factory)
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
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                RemixRecipeScreen(
                    uiState = uiState,
                    onTitleChange = { vm.updateTitle(it) },
                    onDescriptionChange = { vm.updateDescription(it) },
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
                    onPrepTimeChange = { vm.updatePrepTime(it) },
                    onDifficultyChange = { vm.updateDifficulty(it) },
                    onDishTypeChange = { vm.updateDishType(it) },
                    onCostRangeChange = { vm.updateCostRange(it) },
                    onAddIngredient = { vm.addIngredient(it) },
                    onUpdateIngredient = { vm.updateIngredient(it) },
                    onRemoveIngredient = { vm.removeIngredient(it) },
                    onInitAddIngredientProcedure = { vm.initAddIngredientProcedure() },
                    onCancelAddIngredientProcedure = { vm.cancelAddIngredientProcedure() },
                    onUpdateNewIngredientCategory = { vm.updateNewIngredientCategory(it) },
                    onUpdateNewIngredientName = { vm.updateNewIngredientName(it) },
                    onPreviousIngredientWizardStep = { vm.previousIngredientWizardStep() },
                    onNextIngredientWizardStep = { vm.nextIngredientWizardStep() },
                    onQuantityTextChange = { vm.updateNewIngredientQuantityText(it) },
                    onIncrementQuantity = { vm.incrementNewIngredientQuantity() },
                    onDecrementQuantity = { vm.decrementNewIngredientQuantity() },
                    isConfirmEnabled = uiState.isConfirmQuantityEnabled,
                    isEditMode = uiState.isEditMode,
                    onAddStep = { vm.addStep() },
                    onRemoveStep = { vm.removeStep() },
                    onStepGalleryClick = {
                        cameraActions.onPickFromGallery { uri -> vm.updateImageStep(LocalImageInput.Gallery(uri)) }
                    },
                    onStepCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.updateImageStep(LocalImageInput.Camera(uri))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    onUpdateStepDescription = { vm.updateStepDescription(it) },
                    onSwitchLeftStep = { vm.switchLeftStep() },
                    onSwitchRightStep = { vm.switchRightStep() },
                    onDietToggle = { vm.toggleDiet(it) },
                    onCuisineToggle = { vm.toggleCuisine(it) },
                    onDifficultyDialogOpen = { vm.showDifficultyDialog() },
                    onTimeDialogOpen = { vm.showTimeDialog() },
                    onCostDialogOpen = { vm.showCostDialog() },
                    onStatsDialogDismiss = { vm.dismissStatsDialog() },
                    onDietDialogOpen = { vm.showDietDialog() },
                    onCuisineDialogOpen = { vm.showCuisineDialog() },
                    onTagsDialogDismiss = { vm.dismissTagsDialog() },
                    onSaveClick = { vm.publishRemix() },
                    onBackClick = {
                        if (uiState.isSaved) {
                            navActions.navigateBack()
                        } else {
                            exitRequestManager.requestExitBefore { navActions.navigateBack() }
                        } 
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onDismissErrorDialog = { vm.dismissErrorDialog() }
                )
            }
        }

        composable<RecipeTipsListRoute>{
            val vm: RecipeTipsViewModel = viewModel(factory = RecipeTipsViewModel.Factory)
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        TipEvent.Deleted -> onShowSnackbar("Tip deleted successfully")
                        TipEvent.Published -> onShowSnackbar("Tip published successfully")
                        TipEvent.Updated -> onShowSnackbar("Tip updated successfully")
                        TipEvent.ExitAllowed -> Unit
                        TipEvent.ExitCancelled -> Unit
                    }
                }
            }
            RecipeTipsListScreen(
                uiState = uiState,
                onBackClick = { navActions.navigateBack() },
                onAddTipClick = { navActions.navigateTo(NewTipRoute(uiState.recipeId)) },
                onDeleteTipClick = {
                    if (SessionManagerFacade.isLoggedIn) vm.requestDeleteTip(it)
                    else navActions.navigateToAuth()
                },
                onDeleteTipConfirm = {
                    if (SessionManagerFacade.isLoggedIn) vm.confirmDeleteTip()
                    else navActions.navigateToAuth()
                },
                onDeleteTipDismiss = { vm.dismissDeleteTipDialog() },
                onAuthorClick = { navActions.navigateToProfile(it) }
            )
        }

        composable<RecipeReviewListRoute>{
            val vm: RecipeReviewViewModel = viewModel(factory = RecipeReviewViewModel.Factory)
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        ReviewEvent.Deleted -> onShowSnackbar("Review deleted successfully")
                        ReviewEvent.Published -> onShowSnackbar("Review published successfully")
                        ReviewEvent.Updated -> onShowSnackbar("Review updated successfully")
                        ReviewEvent.ExitAllowed -> Unit
                        ReviewEvent.ExitCancelled -> Unit
                    }
                }
            }
            RecipeReviewsListScreen(
                uiState = uiState,
                onBackClick = { navActions.navigateBack() },
                onAddReviewClick = { navActions.navigateTo(NewReviewRoute(uiState.recipeId)) },
                onDeleteReviewClick = {
                    if (SessionManagerFacade.isLoggedIn) vm.requestDeleteReview(it)
                    else navActions.navigateToAuth()
                },
                onDeleteReviewConfirm = {
                    if (SessionManagerFacade.isLoggedIn) vm.confirmDeleteReview()
                    else navActions.navigateToAuth()
                },
                onDeleteReviewDismiss = { vm.dismissDeleteReviewDialog() },
                onAuthorClick = { navActions.navigateToProfile(it) }
            )
        }

        composable<NewReviewRoute> {
            AuthenticatedRoute(navActions) {
                val cameraActions = LocalCameraActions.current
                val vm: NewReviewViewModel = viewModel(factory = NewReviewViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            ReviewEvent.Published -> onShowSnackbar("Review published successfully")
                            ReviewEvent.Updated -> onShowSnackbar("Review updated successfully")
                            ReviewEvent.Deleted -> onShowSnackbar("Review deleted successfully")
                            ReviewEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            ReviewEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                NewReviewScreen(
                    uiState = uiState,
                    onRatingChange = { vm.updateRating(it) },
                    onReviewTextChange = { vm.updateReviewText(it) },
                    onRemoveImage = { vm.removeReviewImage(it) },
                    onRemovePendingImage = { vm.removePendingReviewImage(it) },
                    onPublishClick = { vm.publishReview() },
                    onBackRequest = { exitRequestManager.requestExitBefore { navActions.navigateBack() } },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onNavigateBack = { navActions.navigateBack() },
                    onResetCreateState = { vm.resetCreateState() },
                    onCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.addReviewImage(LocalImageInput.Camera(uri))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    onGalleryClick = {
                        cameraActions.onPickFromGallery { uri -> vm.addReviewImage(LocalImageInput.Gallery(uri)) }
                    },
                )
            }
        }

        composable<NewTipRoute> {
            AuthenticatedRoute(navActions) {
                val vm: NewTipViewModel = viewModel(factory = NewTipViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            TipEvent.Published -> onShowSnackbar("Tip published successfully")
                            TipEvent.Updated -> onShowSnackbar("Tip updated successfully")
                            TipEvent.Deleted -> onShowSnackbar("Tip deleted successfully")
                            TipEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            TipEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                NewTipScreen(
                    uiState = uiState,
                    onTypeChange = { vm.updateType(it) },
                    onTipTextChange = { vm.updateTipText(it) },
                    onPublishClick = { vm.publishTip() },
                    onBackRequest = { exitRequestManager.requestExitBefore { navActions.navigateBack() } },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onNavigateBack = { navActions.navigateBack() },
                    onResetCreateState = { vm.resetCreateState() }
                )
            }
        }

    }
}
