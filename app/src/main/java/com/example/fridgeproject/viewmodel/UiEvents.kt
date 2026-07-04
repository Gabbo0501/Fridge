package com.example.fridgeproject.viewmodel

sealed interface RecipeEvent {
    data object RecipeDeleted : RecipeEvent
    data object ReviewDeleted : RecipeEvent
    data object TipDeleted : RecipeEvent
    data object Published : RecipeEvent
    data object Updated : RecipeEvent
    data object ExitAllowed : RecipeEvent
    data object ExitCancelled : RecipeEvent
    data object MissingIngredientsAddedToGroceryList: RecipeEvent

}

sealed interface ReviewEvent {
    data object Published : ReviewEvent
    data object Updated : ReviewEvent
    data object Deleted : ReviewEvent
    data object ExitAllowed : ReviewEvent
    data object ExitCancelled : ReviewEvent
}

sealed interface TipEvent {
    data object Published : TipEvent
    data object Updated : TipEvent
    data object Deleted : TipEvent
    data object ExitAllowed : TipEvent
    data object ExitCancelled : TipEvent
}

sealed interface CollectionEvent {
    data object Created : CollectionEvent
    data object Updated : CollectionEvent
    data object Deleted : CollectionEvent
}

sealed interface SettingsEvent {
    data object ProfileUpdated : SettingsEvent
    data object PreferencesUpdated : SettingsEvent
    data object LoggedOut : SettingsEvent
    data object ExitAllowed : SettingsEvent
    data object ExitCancelled : SettingsEvent
}

sealed interface AuthEvent {
    data object LoggedIn : AuthEvent
    data object ProfileCreated : AuthEvent
    data object OnboardingSkipped : AuthEvent
    data object ExitAllowed : AuthEvent
    data object ExitCancelled : AuthEvent
}

sealed interface FridgeEvent {
    data object FridgeUpdated : FridgeEvent
    data object FridgeCleared : FridgeEvent
    data object ExitAllowed : FridgeEvent
    data object ExitCancelled : FridgeEvent
}

sealed interface GroceryListEvent {
    data object GroceryListUpdated : GroceryListEvent
    data object MovedToFridge : GroceryListEvent
    data object GroceryListCleared : GroceryListEvent
    data object ExitAllowed : GroceryListEvent
    data object ExitCancelled : GroceryListEvent
}