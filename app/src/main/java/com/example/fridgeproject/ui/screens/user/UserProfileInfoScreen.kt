package com.example.fridgeproject.ui.screens.user

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.model.SystemCollection
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.ui.components.AlertDialog
import com.example.fridgeproject.ui.components.CustomHorizontalDivider
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.StaticSearchBar
import com.example.fridgeproject.ui.components.collection.CollectionFormDialog
import com.example.fridgeproject.ui.components.user.UserCollectionsSection
import com.example.fridgeproject.ui.components.user.UserProfileHeader
import com.example.fridgeproject.ui.components.user.UserProfileImage
import com.example.fridgeproject.ui.components.user.UserRecipeGrid
import com.example.fridgeproject.ui.components.user.UserRecipesHeader
import com.example.fridgeproject.ui.components.user.UserSocialProfiles
import com.example.fridgeproject.ui.components.user.UserStatsSection
import com.example.fridgeproject.ui.components.user.UserTabSelector
import com.example.fridgeproject.viewmodel.user.UserCollectionFormMode
import com.example.fridgeproject.viewmodel.user.UserProfileUiState


@Composable
fun UserProfileInfoScreen(
    uiState: UserProfileUiState,
    onRecipeSearchQueryChange: (String) -> Unit,
    onCollectionSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onFollowerClick: () -> Unit,
    onFollowedClick: () -> Unit,
    onRecipeCardClick: (String) -> Unit,
    onOpenFiltersClick: () -> Unit,
    onClearAllFilters: () -> Unit,
    onCollectionClick: (String, Boolean) -> Unit,
    onCreateCollectionDialog: () -> Unit = {},
    onCollectionNameChange: (String) -> Unit = {},
    onCollectionFormConfirm: () -> Unit = {},
    onCollectionFormDismiss: () -> Unit = {},
    onEditCollectionDialog: (CustomCollection) -> Unit = {},
    onDeleteCollectionDialog: (CustomCollection) -> Unit = {},
    onDeleteCollectionConfirm: () -> Unit = {},
    onDeleteCollectionDismiss: () -> Unit = {},
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFollowClick: () -> Unit
) {
    val hasActiveFilters = uiState.recipes.selectedRecipeCookingTime != null ||
            uiState.recipes.selectedRecipeDifficulty != null ||
            uiState.recipes.selectedRecipeCostRange != null
    val totalRecipeCount = uiState.recipes.recipes.size
    val profile = uiState.profile.userProfile
    val collectionToDelete = uiState.collections.deleteDialog.collectionToDelete

    if (uiState.collections.formDialog.isOpen) {
        CollectionFormDialog(
            name = uiState.collections.formDialog.nameInput,
            isEdit = uiState.collections.formDialog.mode == UserCollectionFormMode.EDIT,
            isSaving = uiState.collections.isSaving,
            nameError = uiState.collections.formDialog.nameError,
            error = uiState.collections.error,
            onNameChange = onCollectionNameChange,
            onConfirm = onCollectionFormConfirm,
            onDismiss = onCollectionFormDismiss
        )
    }

    if (collectionToDelete != null) {
        val deleteError = uiState.collections.deleteDialog.error
        AlertDialog(
            title = "Delete collection?",
            text = buildString {
                append("Are you sure you want to delete \"${collectionToDelete.name}\"? This action cannot be undone.")
                if (deleteError != null) {
                    append("\n\n")
                    append(deleteError)
                }
            },
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = colorScheme.error,
            onConfirm = onDeleteCollectionConfirm,
            onDismiss = onDeleteCollectionDismiss
        )
    }

    when {
        uiState.isLoading -> LoadingComponent()
        profile == null -> ErrorComponent("Profile not found")
        else -> UserProfileContent(
            profile = profile,
            uiState = uiState,
            hasActiveFilters = hasActiveFilters,
            totalRecipeCount = totalRecipeCount,
            onClearAllFilters = onClearAllFilters,
            onRecipeSearchQueryChange = onRecipeSearchQueryChange,
            onCollectionSearchQueryChange = onCollectionSearchQueryChange,
            onBackClick = onBackClick,
            onFollowerClick = onFollowerClick,
            onFollowedClick = onFollowedClick,
            onRecipeCardClick = onRecipeCardClick,
            onOpenFiltersClick = onOpenFiltersClick,
            onCollectionClick = onCollectionClick,
            onCreateCollectionDialog = onCreateCollectionDialog,
            onEditCollectionDialog = onEditCollectionDialog,
            onDeleteCollectionDialog = onDeleteCollectionDialog,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            onFollowClick = onFollowClick
        )
    }
}

@Composable
private fun UserProfileContent(
    profile: UserProfile,
    uiState: UserProfileUiState,
    hasActiveFilters: Boolean,
    totalRecipeCount: Int,
    onClearAllFilters: () -> Unit,
    onRecipeSearchQueryChange: (String) -> Unit,
    onCollectionSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onFollowerClick: () -> Unit,
    onFollowedClick: () -> Unit,
    onRecipeCardClick: (String) -> Unit,
    onOpenFiltersClick: () -> Unit,
    onCollectionClick: (String, Boolean) -> Unit,
    onCreateCollectionDialog: () -> Unit,
    onEditCollectionDialog: (CustomCollection) -> Unit,
    onDeleteCollectionDialog: (CustomCollection) -> Unit,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFollowClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isHorizontal = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isHorizontal) {
        UserProfileInfoHorizontal(
            profile = profile,
            recipes = uiState.recipes.filteredRecipes,
            totalRecipeCount = totalRecipeCount,
            hasActiveFilters = hasActiveFilters,
            onClearAllFilters = onClearAllFilters,
            favouriteCollection = uiState.collections.filteredFavouriteCollection,
            userCollections = uiState.collections.filteredCustomCollections,
            totalCollectionCount = uiState.collections.customCollections.size +
                    if (uiState.collections.favouriteCollection != null) 1 else 0,
            isOwner = uiState.profile.isOwner,
            isFollowing = uiState.followers.isFollowing,
            followersCount = uiState.followers.followersCount,
            followedCount = uiState.followers.followedCount,
            onFollowerClick = onFollowerClick,
            onFollowedClick = onFollowedClick,
            onRecipeCardClick = onRecipeCardClick,
            onOpenFiltersClick = onOpenFiltersClick,
            recipeSearchQuery = uiState.recipes.recipeSearchQuery,
            collectionSearchQuery = uiState.collections.collectionSearchQuery,
            onRecipeSearchQueryChange = onRecipeSearchQueryChange,
            onCollectionSearchQueryChange = onCollectionSearchQueryChange,
            onCollectionClick = onCollectionClick,
            onCreateCollectionDialog = onCreateCollectionDialog,
            onEditCollectionDialog = onEditCollectionDialog,
            onDeleteCollectionDialog = onDeleteCollectionDialog,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            onFollowClick = onFollowClick
        )
    } else {
        UserProfileInfoVertical(
            profile = profile,
            recipes = uiState.recipes.filteredRecipes,
            totalRecipeCount = totalRecipeCount,
            hasActiveFilters = hasActiveFilters,
            onClearAllFilters = onClearAllFilters,
            favouriteCollection = uiState.collections.filteredFavouriteCollection,
            userCollections = uiState.collections.filteredCustomCollections,
            totalCollectionCount = uiState.collections.customCollections.size +
                    if (uiState.collections.favouriteCollection != null) 1 else 0,
            isOwner = uiState.profile.isOwner,
            isFollowing = uiState.followers.isFollowing,
            followersCount = uiState.followers.followersCount,
            followedCount = uiState.followers.followedCount,
            onBackClick = onBackClick,
            onFollowerClick = onFollowerClick,
            onFollowedClick = onFollowedClick,
            onRecipeCardClick = onRecipeCardClick,
            onOpenFiltersClick = onOpenFiltersClick,
            recipeSearchQuery = uiState.recipes.recipeSearchQuery,
            collectionSearchQuery = uiState.collections.collectionSearchQuery,
            onRecipeSearchQueryChange = onRecipeSearchQueryChange,
            onCollectionSearchQueryChange = onCollectionSearchQueryChange,
            onCollectionClick = onCollectionClick,
            onCreateCollectionDialog = onCreateCollectionDialog,
            onEditCollectionDialog = onEditCollectionDialog,
            onDeleteCollectionDialog = onDeleteCollectionDialog,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            onFollowClick = onFollowClick
        )
    }
}

@Composable
fun UserProfileInfoVertical(
    profile: UserProfile,
    recipes: List<RecipeShortUi>,
    totalRecipeCount: Int = recipes.size,
    hasActiveFilters: Boolean = false,
    onClearAllFilters: () -> Unit = {},
    favouriteCollection: SystemCollection?,
    userCollections: List<CustomCollection>,
    totalCollectionCount: Int,
    isOwner: Boolean,
    isFollowing: Boolean,
    onBackClick: () -> Unit,
    onFollowerClick: () -> Unit,
    onFollowedClick: () -> Unit,
    onRecipeCardClick: (String) -> Unit,
    onOpenFiltersClick: () -> Unit,
    recipeSearchQuery: String,
    collectionSearchQuery: String,
    onRecipeSearchQueryChange: (String) -> Unit,
    onCollectionSearchQueryChange: (String) -> Unit,
    onCollectionClick: (String, Boolean) -> Unit,
    onCreateCollectionDialog: () -> Unit,
    onEditCollectionDialog: (CustomCollection) -> Unit,
    onDeleteCollectionDialog: (CustomCollection) -> Unit,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFollowClick: () -> Unit,
    followersCount: Int,
    followedCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        if (!isOwner) {
            item { PageHeader(onBackClick = onBackClick) }
        }
        item {
            UserProfileImage(
                isOwner = isOwner,
                isEditing = false,
                imageSource = profile.profileImage,
                onCameraClick = {},
                onGalleryClick = {},
                firstName = profile.firstName,
                lastName = profile.lastName
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item { UserProfileHeader(profile, isOwner, onFollowClick, isFollowing) }
        item { UserStatsSection(totalRecipeCount, onFollowerClick, onFollowedClick, followersCount, followedCount) }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item { CustomHorizontalDivider() }

        item { Spacer(modifier = Modifier.height(18.dp)) }
        item { UserSocialProfiles(profile.socialProfiles) }
        item { Spacer(modifier = Modifier.height(25.dp)) }

        item {
            UserTabSelector(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
        }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        when (selectedTabIndex) {
            0 -> {
                // Recipes Tab
                item { UserRecipesHeader(isOwner, totalRecipeCount) }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item {
                    StaticSearchBar(
                        searchQuery = recipeSearchQuery,
                        onSearchQueryChange = onRecipeSearchQueryChange,
                        onOpenFiltersClick = onOpenFiltersClick,
                        onClearFilters = onClearAllFilters,
                        hasActiveFilters = hasActiveFilters
                    )
                }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item { UserRecipeGrid(recipes, isOwner, { onRecipeCardClick(it) }) }
            }

            1 -> {
                // Collections Tab
                item { UserRecipesHeader(isOwner, totalCollectionCount, title = "My Collection", otherUserTitle = "Collections") }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item {
                    StaticSearchBar(
                        searchQuery = collectionSearchQuery,
                        onSearchQueryChange = onCollectionSearchQueryChange,
                        placeholder = "Search your collections..."
                    )
                }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item {
                    UserCollectionsSection(
                        favouriteCollection = favouriteCollection,
                        collections = userCollections,
                        isOwner = isOwner,
                        showCreateCollectionItem = isOwner && collectionSearchQuery.isBlank(),
                        onFavouriteClick = { collection ->
                            onCollectionClick(collection.id, true)
                        },
                        onCollectionClick = { collection ->
                            onCollectionClick(collection.id, false)
                        },
                        onCreateCollectionDialog = onCreateCollectionDialog,
                        onEditCollectionDialog = onEditCollectionDialog,
                        onDeleteCollectionDialog = onDeleteCollectionDialog
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileInfoHorizontal(
    profile: UserProfile,
    recipes: List<RecipeShortUi>,
    totalRecipeCount: Int = recipes.size,
    hasActiveFilters: Boolean = false,
    onClearAllFilters: () -> Unit = {},
    favouriteCollection: SystemCollection?,
    userCollections: List<CustomCollection>,
    totalCollectionCount: Int,
    isOwner: Boolean,
    isFollowing : Boolean,
    onFollowerClick: () -> Unit,
    onFollowedClick: () -> Unit,
    onRecipeCardClick: (String) -> Unit,
    onOpenFiltersClick: () -> Unit,
    recipeSearchQuery: String,
    collectionSearchQuery: String,
    onRecipeSearchQueryChange: (String) -> Unit,
    onCollectionSearchQueryChange: (String) -> Unit,
    onCollectionClick: (String, Boolean) -> Unit,
    onCreateCollectionDialog: () -> Unit,
    onEditCollectionDialog: (CustomCollection) -> Unit,
    onDeleteCollectionDialog: (CustomCollection) -> Unit,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFollowClick: () -> Unit,
    followersCount: Int,
    followedCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserProfileImage(
                        isOwner = isOwner,
                        isEditing = false,
                        imageSource = profile.profileImage,
                        onCameraClick = {},
                        onGalleryClick = {},
                        firstName = profile.firstName,
                        lastName = profile.lastName
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.width(30.dp))

                Column(
                    modifier = Modifier.weight(1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserProfileHeader(profile, isOwner, onFollowClick, isFollowing)
                    Spacer(modifier = Modifier.height(25.dp))
                    UserStatsSection(totalRecipeCount, onFollowerClick, onFollowedClick, followersCount, followedCount)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item { UserSocialProfiles(profile.socialProfiles) }
        item { Spacer(modifier = Modifier.height(25.dp)) }

        item { CustomHorizontalDivider() }

        item { Spacer(modifier = Modifier.height(25.dp)) }

        item {
            UserTabSelector(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
        }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        when (selectedTabIndex) {
            0 -> {
                // Recipes Tab
                item { UserRecipesHeader(isOwner, totalRecipeCount) }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item {
                    StaticSearchBar(
                        searchQuery = recipeSearchQuery,
                        onSearchQueryChange = onRecipeSearchQueryChange,
                        onOpenFiltersClick = onOpenFiltersClick,
                        onClearFilters = onClearAllFilters,
                        hasActiveFilters = hasActiveFilters
                    )
                }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item { UserRecipeGrid(recipes, isOwner, { onRecipeCardClick(it) }) }
            }
            1 -> {
                // Collections Tab
                item { UserRecipesHeader(isOwner, totalCollectionCount, title = "My Collection", otherUserTitle = "Collections") }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item {
                    StaticSearchBar(
                        searchQuery = collectionSearchQuery,
                        onSearchQueryChange = onCollectionSearchQueryChange,
                        placeholder = "Search your collections..."
                    )
                }
                item { Spacer(modifier = Modifier.height(25.dp)) }
                item {
                    UserCollectionsSection(
                        favouriteCollection = favouriteCollection,
                        collections = userCollections,
                        isOwner = isOwner,
                        showCreateCollectionItem = isOwner && collectionSearchQuery.isBlank(),
                        onFavouriteClick = { collection ->
                            onCollectionClick(collection.id, true)
                        },
                        onCollectionClick = { collection ->
                            onCollectionClick(collection.id, false)
                        },
                        onCreateCollectionDialog = onCreateCollectionDialog,
                        onEditCollectionDialog = onEditCollectionDialog,
                        onDeleteCollectionDialog = onDeleteCollectionDialog
                    )
                }
            }
        }
    }
}