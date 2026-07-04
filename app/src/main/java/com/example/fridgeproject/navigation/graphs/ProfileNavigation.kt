package com.example.fridgeproject.navigation.graphs

import com.example.fridgeproject.navigation.*
import com.example.fridgeproject.navigation.utils.*

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.ui.screens.collection.CollectionContentScreen
import com.example.fridgeproject.ui.screens.user.FollowedScreen
import com.example.fridgeproject.ui.screens.user.FollowerScreen
import com.example.fridgeproject.ui.screens.recipe.ProfileRecipeFilterScreen
import com.example.fridgeproject.ui.screens.user.UserProfileInfoScreen
import com.example.fridgeproject.viewmodel.CollectionEvent
import com.example.fridgeproject.viewmodel.collection.CollectionViewModel
import com.example.fridgeproject.viewmodel.user.FollowViewModel
import com.example.fridgeproject.viewmodel.user.UserProfileViewModel

fun NavGraphBuilder.profileGraph(
    navActions: AppNavigationActions,
    exitRequestManager: ExitRequestManager,
    onShowSnackbar: (String) -> Unit = {}
) {
    navigation<ProfileGraph>(startDestination = ProfileRoute) {

        composable<ProfileRoute> {backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.getProfileGraphEntry()
            }
            val profileUserId = parentEntry.toRoute<ProfileGraph>().profileUserId

            ProfileRouteGuard(
                profileUserId = profileUserId,
                navActions = navActions
            ) {
                val vm: UserProfileViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = UserProfileViewModel.Factory
                )
                val uiState by vm.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            CollectionEvent.Created -> onShowSnackbar("Collection created successfully")
                            CollectionEvent.Updated -> onShowSnackbar("Collection updated successfully")
                            CollectionEvent.Deleted -> onShowSnackbar("Collection deleted successfully")
                        }
                    }
                }

                UserProfileInfoScreen(
                    uiState = uiState,
                    onBackClick = { navActions.navigateBack() },
                    onFollowerClick = {
                        uiState.profile.userProfile?.id?.let { navActions.navigateTo(FollowerRoute(it)) }
                    },
                    onFollowedClick = {
                        uiState.profile.userProfile?.id?.let { navActions.navigateTo(FollowedRoute(it)) }
                    },
                    onRecipeCardClick = { navActions.navigateToRecipeDetails(it) },
                    onOpenFiltersClick = { navActions.navigateTo(ProfileRecipeFilterRoute) },
                    onClearAllFilters = { vm.clearAllFilters() },
                    onRecipeSearchQueryChange = { vm.updateRecipeSearchQuery(it) },
                    onCollectionSearchQueryChange = { vm.updateCollectionSearchQuery(it) },
                    onCollectionClick = { id, isFavouriteCollection ->
                        navActions.navigateToCollection(id, isFavouriteCollection)
                    },
                    onCreateCollectionDialog = { vm.showCreateCollectionDialog() },
                    onCollectionNameChange = { vm.updateCollectionNameInput(it) },
                    onCollectionFormConfirm = { vm.saveCustomCollection() },
                    onCollectionFormDismiss = { vm.dismissCollectionFormDialog() },
                    onEditCollectionDialog = { vm.showEditCollectionDialog(it) },
                    onDeleteCollectionDialog = { vm.showDeleteCollectionDialog(it) },
                    onDeleteCollectionConfirm = { vm.deleteCustomCollection() },
                    onDeleteCollectionDismiss = { vm.dismissDeleteCollectionDialog() },
                    selectedTabIndex = uiState.selectedTabIndex,
                    onTabSelected = { vm.selectTab(it) },
                    onFollowClick = {vm.toggleFollow()}
                )
            }
        }

        composable<ProfileRecipeFilterRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.getProfileGraphEntry()
            }
            val vm: UserProfileViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = UserProfileViewModel.Factory
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            ProfileRecipeFilterScreen(
                selectedCookingTime = uiState.recipes.selectedRecipeCookingTime,
                onCookingTimeChanged = { vm.onRecipeCookingTimeChanged(it) },
                selectedDifficulty = uiState.recipes.selectedRecipeDifficulty,
                clearDifficulty = { vm.clearRecipeDifficulty() },
                difficultySliderPosition = uiState.recipes.recipeDifficultySliderPosition,
                onDifficultySliderMoved = { vm.onRecipeDifficultySliderMoved(it) },
                selectedCostRange = uiState.recipes.selectedRecipeCostRange,
                clearCostRange = { vm.clearRecipeCostRange() },
                costSliderPosition = uiState.recipes.recipeCostSliderPosition,
                onCostSliderMoved = { vm.onRecipeCostSliderMoved(it) },
                onBackClick = { navActions.navigateBack() }
            )
        }


        composable<CollectionRoute> { backStackEntry ->
            val route: CollectionRoute = backStackEntry.toRoute()

            val vm: CollectionViewModel = viewModel(
                factory = CollectionViewModel.provideFactory(
                    collectionId = route.collectionId,
                    isFavouriteCollection = route.isFavouriteCollection
                )
            )

            val uiState by vm.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        CollectionEvent.Created -> onShowSnackbar("Collection created successfully")
                        CollectionEvent.Updated -> onShowSnackbar("Collection updated successfully")
                        CollectionEvent.Deleted -> {
                            navActions.navigateBack()
                            onShowSnackbar("Collection deleted successfully")
                        }
                    }
                }
            }

            CollectionContentScreen(
                uiState = uiState,
                currentUserId = SessionManagerFacade.currentUserId,
                onBack = { navActions.navigateBack() },
                onRecipeCardClick = { navActions.navigateToRecipeDetails(it) },
                onEditCollectionClick = { vm.showEditCollectionDialog() },
                onDeleteCollectionClick = { vm.showDeleteCollectionDialog() },
                onCollectionNameChange = { vm.updateCollectionNameInput(it) },
                onCollectionFormConfirm = { vm.saveCustomCollection() },
                onCollectionFormDismiss = { vm.dismissCollectionFormDialog() },
                onDeleteCollectionConfirm = { vm.deleteCustomCollection() },
                onDeleteCollectionDismiss = { vm.dismissDeleteCollectionDialog() }
            )
        }

        composable<FollowerRoute> { backStackEntry ->
            val route: FollowerRoute = backStackEntry.toRoute()

            val vm: FollowViewModel = viewModel(
                factory = FollowViewModel.provideFactory(
                    userId = route.profileUserId
                )
            )

            val uiState by vm.uiState.collectAsStateWithLifecycle()
            FollowerScreen(
                followers = uiState.followers,
                loggedUserId = uiState.loggedUserId,
                loggedUserFollowingIds = uiState.loggedUserFollowingIds,
                searchQuery = uiState.searchAuthorQuery,
                onFollowClick = { targetUserId: String, isFollowing: Boolean -> vm.toggleFollow(targetUserId, isFollowing)},
                onBackClick = {navActions.navigateBack()},
                onFollowerProfileClick = {navActions.navigateToProfile(it)},
                onSearch = {newQuery: String -> vm.onSearchRecipeQueryChanged(newQuery)}
            )
        }

        composable<FollowedRoute> { backStackEntry ->
            val route: FollowedRoute = backStackEntry.toRoute()

            val vm: FollowViewModel = viewModel(
                factory = FollowViewModel.provideFactory(
                    userId = route.profileUserId
                )
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            FollowedScreen(
                followed = uiState.followed,
                loggedUserId = uiState.loggedUserId,
                loggedUserFollowingIds = uiState.loggedUserFollowingIds,
                searchQuery = uiState.searchAuthorQuery,
                onFollowClick = { targetUserId: String, isFollowing: Boolean -> vm.toggleFollow(targetUserId, isFollowing)},
                onBackClick = { navActions.navigateBack() },
                onFollowedProfileClick = { navActions.navigateToProfile(it) },
                onSearch = {newQuery: String -> vm.onSearchRecipeQueryChanged(newQuery)}
            )
        }

        settingsGraph(navActions, exitRequestManager, onShowSnackbar)
    }
}
