package com.example.fridgeproject.navigation

import kotlinx.serialization.Serializable


@Serializable
data object HomeGraph
@Serializable
data object HomeRoute
@Serializable
data object HomeFilterRoute
@Serializable
data class ProfileGraph( val profileUserId: String?)
@Serializable
data object ProfileRoute
@Serializable
data object ProfileRecipeFilterRoute
@Serializable
data class CollectionRoute(
    val collectionId: String,
    val isFavouriteCollection: Boolean
)
@Serializable
data class NotificationRoute( val profileUserId: String)
@Serializable
data class FollowerRoute( val profileUserId: String)
@Serializable
data class FollowedRoute( val profileUserId: String)
@Serializable
data class SettingsGraph( val profileUserId: String)

@Serializable data object SettingsRoute
@Serializable data class EditProfileRoute( val profileUserId: String)
@Serializable data class DietRoute( val profileUserId: String)
@Serializable data class NotificationSettingsRoute( val profileUserId: String)
@Serializable data class MyTipsRoute( val profileUserId: String)
@Serializable data class MyReviewsRoute( val profileUserId: String)
@Serializable data object LogOutRoute
@Serializable data object RegistrationRoute
@Serializable
data object ExploreGraph
@Serializable
data object ExploreRoute
@Serializable
data object RecipesResultListRoute
@Serializable
data object FilterRoute

@Serializable
data class RecipeDetailGraph (val recipeId: String)
@Serializable
data object RecipeDetailRoute
@Serializable
data object CreateRecipeRoute
@Serializable
data class EditRecipeRoute (val recipeId: String)
@Serializable
data class RemixRecipeRoute (val recipeId: String)
@Serializable
data class SaveRecipeToCollectionRoute(val recipeId: String)

@Serializable
data class RecipeTipsListRoute (val recipeId: String)
@Serializable
data class RecipeReviewListRoute (val recipeId: String)
@Serializable
data class NewReviewRoute(val recipeId: String)
@Serializable
data class EditReviewRoute(val reviewId: String)
@Serializable
data class NewTipRoute(val recipeId: String)
@Serializable
data class EditTipRoute(val tipId: String)

@Serializable data class FridgeGraph( val profileUserId: String?)
@Serializable data object MyFridgeRoute
@Serializable data object FridgeRecipesResultRoute
@Serializable data object FridgeRecipesResultListRoute

@Serializable data object AuthGraph
@Serializable data object LogInRoute
@Serializable data object SignInRoute
@Serializable data object OnboardingRoute
@Serializable data object CameraRoute