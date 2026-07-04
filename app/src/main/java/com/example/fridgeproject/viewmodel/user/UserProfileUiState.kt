package com.example.fridgeproject.viewmodel.user

import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.model.SystemCollection
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.PrepTime

data class UserProfileUiState(
    val profile: UserProfileDetailsState = UserProfileDetailsState(),
    val recipes: UserProfileRecipeState = UserProfileRecipeState(),
    val collections: UserProfileCollectionsState = UserProfileCollectionsState(),
    val followers: UserProfileFollowersState = UserProfileFollowersState(),
    val selectedTabIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String = "",
)

data class UserProfileDetailsState(
    val userProfile: UserProfile? = null,
    val isOwner: Boolean = false
)

data class UserProfileRecipeState(
    val recipes: List<RecipeShortUi> = emptyList(),
    val filteredRecipes: List<RecipeShortUi> = emptyList(),
    val recipeSearchQuery: String = "",
    val selectedRecipeCookingTime: PrepTime? = null,
    val selectedRecipeDifficulty: Difficulty? = null,
    val recipeDifficultySliderPosition: Float = 0f,
    val recipeCostSliderPosition: Float = 0f,
    val selectedRecipeCostRange: CostRange? = null
)

data class UserProfileCollectionsState(
    val favouriteCollection: SystemCollection? = null,
    val customCollections: List<CustomCollection> = emptyList(),
    val filteredFavouriteCollection: SystemCollection? = null,
    val filteredCustomCollections: List<CustomCollection> = emptyList(),
    val collectionSearchQuery: String = "",
    val formDialog: UserCollectionFormDialogState = UserCollectionFormDialogState(),
    val deleteDialog: UserCollectionDeleteDialogState = UserCollectionDeleteDialogState(),
    val isSaving: Boolean = false,
    val error: String? = null
)

data class UserCollectionFormDialogState(
    val isOpen: Boolean = false,
    val mode: UserCollectionFormMode = UserCollectionFormMode.CREATE,
    val nameInput: String = "",
    val collectionToEdit: CustomCollection? = null,
    val nameError: String? = null
)

enum class UserCollectionFormMode {
    CREATE,
    EDIT
}

data class UserCollectionDeleteDialogState(
    val collectionToDelete: CustomCollection? = null,
    val isDeleting: Boolean = false,
    val error: String? = null
)

data class UserProfileFollowersState(
    val isFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followedCount: Int = 0
)