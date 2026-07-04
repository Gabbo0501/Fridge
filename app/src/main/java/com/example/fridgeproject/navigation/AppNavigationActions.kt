package com.example.fridgeproject.navigation


import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.toRoute

class AppNavigationActions(private val navController: NavController) {

    fun getHomeGraphEntry(): NavBackStackEntry {
        return navController.getBackStackEntry<HomeGraph>()
    }
    fun getExploreGraphEntry(): NavBackStackEntry {
        return navController.getBackStackEntry<ExploreGraph>()
    }
    fun getProfileGraphEntry(): NavBackStackEntry {
        return navController.getBackStackEntry<ProfileGraph>()
    }

    fun getFridgeGraphEntry(): NavBackStackEntry {
        return navController.getBackStackEntry<FridgeGraph>()
    }

    fun navigateHome() {
        navController.navigate(HomeGraph) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateBack() = navController.popBackStack()

    fun navigateTo(route: Any) = navController.navigate(route)

    fun navigateToSettings(profileId: String) {
        navController.navigate(SettingsGraph(profileId)) {
            launchSingleTop = true
        }
    }
    fun navigateToProfile(profileId: String) {
        navController.navigate(ProfileGraph(profileId)) {
            launchSingleTop = true
        }
    }

    fun navigateToCollection(
        collectionId: String,
        isFavouriteCollection: Boolean
    ) {
        navController.navigate(CollectionRoute(collectionId, isFavouriteCollection))
    }

    fun navigateToAuth() {
        navController.navigate(AuthGraph) {
            launchSingleTop = true
        }
    }

    fun navigateToExplore() {
        navController.navigate(ExploreGraph) {
            launchSingleTop = true
        }
    }
    fun navigateToRecipeDetails(recipeId: String) {
        navController.navigate(RecipeDetailGraph(recipeId)){
            launchSingleTop = true
        }
    }

    fun isViewingOwnProfile(currentUserId: String?): Boolean {
        val isLoggedIn = currentUserId != null

        if (!isLoggedIn)
            return false

        val isProfileOpen = navController.currentDestination?.hierarchy?.any {
            it.hasRoute(ProfileGraph::class)
        } == true

        if (!isProfileOpen) return false

        val openedProfileId = navController.getBackStackEntry<ProfileGraph>()
            .toRoute<ProfileGraph>()
            .profileUserId

        return openedProfileId == currentUserId
    }
}