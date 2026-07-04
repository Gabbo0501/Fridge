package com.example.fridgeproject.viewmodel.user

import com.example.fridgeproject.viewmodel.*
import com.example.fridgeproject.viewmodel.collection.*

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.domain.FollowerRepository
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.RecipeShortUi
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.PrepTime
import com.example.fridgeproject.model.toRecipeShortUi
import com.example.fridgeproject.model.utils.toPrepTime
import com.example.fridgeproject.navigation.ProfileGraph
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val recipeWithStatsRepository: RecipeWithStatsRepository,
    private val collectionRepository: CollectionRepository,
    private val followerRepository: FollowerRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ProfileGraph>()
    private val profileUserIdFromRoute = route.profileUserId

    private val _uiState = MutableStateFlow(UserProfileUiState(isLoading = true))
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    private val _events = Channel<CollectionEvent>()
    val events = _events.receiveAsFlow()
    private val collectionEditor = CollectionEditor(collectionRepository, viewModelScope)

    init {
        observeProfile()
        observeCollectionEditor()
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProfile() {
        viewModelScope.launch {
            SessionManagerFacade.currentUserStateFlow
                .map { currentUserId ->
                    val targetUserId = if (!profileUserIdFromRoute.isNullOrBlank()) {
                        profileUserIdFromRoute
                    } else {
                        currentUserId.orEmpty()
                    }
                    currentUserId to targetUserId
                }
                .flatMapLatest { (currentUserId, targetUserId) ->
                    if (targetUserId.isBlank()) {
                        flowOf(null)
                    } else {
                        // 1. INTEGRAZIONE: Aggiungiamo i due flow dei follower nel combine
                        val followerInfoFlow = combine(
                            followerRepository.getFollowersFlow(targetUserId),
                            followerRepository.getFollowingFlow(targetUserId)
                        ) { followers, following ->
                            followers to following
                        }
                        combine(
                            userRepository.getUserById(targetUserId),
                            recipeWithStatsRepository.getRecipesWithRatingByAuthor(targetUserId),
                            collectionRepository.getUserCustomCollections(targetUserId),
                            collectionRepository.getFavourites(targetUserId),
                            followerInfoFlow
                        ) { profile, recipeStats, customCollections, favouriteCollection, followInfo ->

                            val (followersList, followingList) = followInfo

                            val nickname = profile?.nickname.orEmpty()
                            val recipes = recipeStats.map { recipeWithStats ->
                                recipeWithStats.toRecipeShortUi(nickname)
                            }

                            val isOwner = currentUserId == targetUserId
                            val checkFollow = if (!isOwner && currentUserId != null) {
                                followersList.any { it.id == currentUserId }
                            } else {
                                false
                            }

                            UserProfileUiState(
                                profile = UserProfileDetailsState(
                                    userProfile = profile,
                                    isOwner = isOwner,
                                ),
                                recipes = UserProfileRecipeState(
                                    recipes = recipes
                                ),
                                collections = UserProfileCollectionsState(
                                    customCollections = customCollections,
                                    favouriteCollection = favouriteCollection
                                ),
                                followers = UserProfileFollowersState(
                                    followersCount = followersList.size,
                                    followedCount = followingList.size,
                                    isFollowing = checkFollow
                                ),
                                isLoading = false
                            )
                        }
                    }
                }
                .collect { loadedState ->
                    if (loadedState == null) {
                        _uiState.update { UserProfileUiState(isLoading = false) }
                        return@collect
                    }

                    try {
                        _uiState.update { currentState ->
                            val updatedState = loadedState.copy(
                                followers = loadedState.followers.copy(
                                    followersCount = loadedState.followers.followersCount,
                                    followedCount = loadedState.followers.followedCount,
                                    isFollowing = loadedState.followers.isFollowing
                                ),
                                recipes = loadedState.recipes.copy(
                                    recipeSearchQuery = currentState.recipes.recipeSearchQuery,
                                    selectedRecipeCookingTime = currentState.recipes.selectedRecipeCookingTime,
                                    selectedRecipeDifficulty = currentState.recipes.selectedRecipeDifficulty,
                                    recipeDifficultySliderPosition = currentState.recipes.recipeDifficultySliderPosition,
                                    recipeCostSliderPosition = currentState.recipes.recipeCostSliderPosition,
                                    selectedRecipeCostRange = currentState.recipes.selectedRecipeCostRange
                                ),
                                collections = loadedState.collections.copy(
                                    collectionSearchQuery = currentState.collections.collectionSearchQuery,
                                    formDialog = currentState.collections.formDialog,
                                    deleteDialog = currentState.collections.deleteDialog,
                                    isSaving = currentState.collections.isSaving,
                                    error = currentState.collections.error
                                ),
                                selectedTabIndex = currentState.selectedTabIndex,
                                error = ""
                            )
                            updatedState.copy(
                                recipes = updatedState.recipes.copy(
                                    filteredRecipes = applyRecipeFilters(updatedState)
                                ),
                                collections = applyCollectionFilters(updatedState).copy(
                                    formDialog = updatedState.collections.formDialog,
                                    deleteDialog = updatedState.collections.deleteDialog,
                                    isSaving = updatedState.collections.isSaving,
                                    error = updatedState.collections.error
                                )
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Error during profile loading")
                        }
                    }
                }
        }
    }

    private fun observeCollectionEditor() {
        viewModelScope.launch {
            collectionEditor.state.collect { editorState ->
                _uiState.update {
                    it.copy(
                        collections = it.collections.copy(
                            formDialog = editorState.formDialog,
                            deleteDialog = editorState.deleteDialog,
                            isSaving = editorState.isSaving,
                            error = editorState.error
                        )
                    )
                }
            }
        }
    }

    fun saveCustomCollection() {
        collectionEditor.saveCollection(ownerId = _uiState.value.profile.userProfile?.id) { mode ->
            val event = when (mode) {
                UserCollectionFormMode.CREATE -> CollectionEvent.Created
                UserCollectionFormMode.EDIT -> CollectionEvent.Updated
            }
            _events.trySend(event)
        }
    }

    fun deleteCustomCollection() {
        collectionEditor.deleteCollection {
            _events.trySend(CollectionEvent.Deleted)
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun showDeleteCollectionDialog(collection: CustomCollection) {
        collectionEditor.showDeleteDialog(collection)
    }

    fun dismissDeleteCollectionDialog() {
        collectionEditor.dismissDeleteDialog()
    }

    private fun applyRecipeFilters(state: UserProfileUiState): List<RecipeShortUi> {
        return state.recipes.recipes.filter { recipe ->
            val matchesQuery = state.recipes.recipeSearchQuery.isEmpty() ||
                    recipe.title.contains(state.recipes.recipeSearchQuery, ignoreCase = true)

            val matchesDifficulty = state.recipes.selectedRecipeDifficulty == null ||
                    recipe.difficulty == state.recipes.selectedRecipeDifficulty

            val matchesPriceCat = state.recipes.selectedRecipeCostRange == null ||
                    recipe.costRange == state.recipes.selectedRecipeCostRange

            val matchesTime = state.recipes.selectedRecipeCookingTime == null ||
                    recipe.preparationTimeSec.toPrepTime() == state.recipes.selectedRecipeCookingTime

            matchesQuery && matchesDifficulty && matchesPriceCat && matchesTime
        }
    }

    private fun applyCollectionFilters(state: UserProfileUiState): UserProfileCollectionsState {
        val query = state.collections.collectionSearchQuery
        val filteredFavouriteCollection = state.collections.favouriteCollection?.takeIf {
            query.isBlank() || "Favourites".contains(query, ignoreCase = true)
        }
        val filteredCustomCollections = state.collections.customCollections.filter { collection ->
            query.isBlank() || collection.name.contains(query, ignoreCase = true)
        }

        return state.collections.copy(
            filteredFavouriteCollection = filteredFavouriteCollection,
            filteredCustomCollections = filteredCustomCollections
        )
    }

    fun updateRecipeSearchQuery(query: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(recipeSearchQuery = query)
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun updateCollectionSearchQuery(query: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                collections = currentState.collections.copy(collectionSearchQuery = query)
            )
            updatedState.copy(collections = applyCollectionFilters(updatedState))
        }
    }

    fun onRecipeCookingTimeChanged(newTime: PrepTime) {
        _uiState.update { currentState ->
            val updatedTime = if (currentState.recipes.selectedRecipeCookingTime == newTime) null else newTime
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(selectedRecipeCookingTime = updatedTime)
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun onRecipeDifficultySliderMoved(position: Float) {
        val index = position.toInt().coerceIn(0, Difficulty.entries.lastIndex)
        val difficulty = Difficulty.entries[index]
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(
                    recipeDifficultySliderPosition = position,
                    selectedRecipeDifficulty = difficulty
                )
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun clearRecipeDifficulty() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(
                    selectedRecipeDifficulty = null,
                    recipeDifficultySliderPosition = 0f
                )
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun onRecipeCostSliderMoved(position: Float) {
        val index = position.toInt().coerceIn(0, CostRange.entries.lastIndex)
        val cost = CostRange.entries[index]
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(
                    recipeCostSliderPosition = position,
                    selectedRecipeCostRange = cost
                )
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun clearRecipeCostRange() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(
                    selectedRecipeCostRange = null,
                    recipeCostSliderPosition = 0f
                )
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun clearAllFilters() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                recipes = currentState.recipes.copy(
                    selectedRecipeCookingTime = null,
                    selectedRecipeDifficulty = null,
                    selectedRecipeCostRange = null,
                    recipeDifficultySliderPosition = 0f,
                    recipeCostSliderPosition = 0f
                )
            )
            updatedState.copy(
                recipes = updatedState.recipes.copy(filteredRecipes = applyRecipeFilters(updatedState))
            )
        }
    }

    fun showCreateCollectionDialog() {
        collectionEditor.showCreateDialog()
    }

    fun showEditCollectionDialog(collection: CustomCollection) {
        collectionEditor.showEditDialog(collection)
    }

    fun dismissCollectionFormDialog() {
        collectionEditor.dismissFormDialog()
    }

    fun updateCollectionNameInput(name: String) {
        collectionEditor.updateNameInput(name)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun toggleFollow() {
        val currentUserId = SessionManagerFacade.currentUserId
        val targetUser = uiState.value.profile.userProfile
        if(currentUserId == null || targetUser == null)
            return

        val isCurrentlyFollowing = uiState.value.followers.isFollowing
        viewModelScope.launch {
            if (isCurrentlyFollowing) {
                followerRepository.unfollowUser(currentUserId, targetUser.id)
            } else {
                followerRepository.followUser(currentUserId, targetUser.id)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                UserProfileViewModel(
                    savedStateHandle = savedStateHandle,
                    userRepository = app.container.userRepository,
                    recipeWithStatsRepository = app.container.recipeWithStatsRepository,
                    collectionRepository = app.container.collectionRepository,
                    followerRepository = app.container.followerRepository
                )
            }
        }
    }
}
