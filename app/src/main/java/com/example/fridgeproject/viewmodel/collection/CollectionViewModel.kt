package com.example.fridgeproject.viewmodel.collection

import com.example.fridgeproject.viewmodel.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.toRecipeShortUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModel(
    private val collectionId: String,
    private val isFavouriteCollection: Boolean,
    private val collectionRepository: CollectionRepository,
    private val recipeWithStatsRepository: RecipeWithStatsRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(CollectionUiState(isLoading = true))
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()
    private val _events = Channel<CollectionEvent>()
    val events = _events.receiveAsFlow()
    private val collectionEditor = CollectionEditor(collectionRepository, viewModelScope)

    init {
        observeCollectionEditor()
        if (isFavouriteCollection) {
            observeFavouriteCollection()
        } else {
            observeCustomCollection()
        }
    }

    private fun observeFavouriteCollection() {
        viewModelScope.launch {
            collectionRepository.getSystemCollectionById(collectionId)
                .flatMapLatest { collection ->
                    if (collection == null) {
                        flowOf(
                            CollectionUiState(
                                isLoading = false,
                                error = "Favourites collection not found"
                            )
                        )
                    } else {
                        recipeWithStatsRepository.getRecipesWithRatingByIds(collection.recipeIds)
                            .map { recipeList ->
                                CollectionUiState(
                                    collectionName = "Favorites",
                                    customCollection = null,
                                    recipes = recipeList.map { it.toRecipeShortUi() },
                                    isOwner = false,
                                    isLoading = false,
                                    error = if (recipeList.isEmpty()) "No recipes found in this collection" else null
                                )
                            }
                    }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "Database Error: ${e.localizedMessage}")
                    }
                }
                .collect { collectionState ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            collectionName = collectionState.collectionName,
                            customCollection = collectionState.customCollection,
                            recipes = collectionState.recipes,
                            isOwner = collectionState.isOwner,
                            isLoading = collectionState.isLoading,
                            error = collectionState.error
                        )
                    }
                }
        }
    }

    private fun observeCustomCollection() {
        viewModelScope.launch {
            collectionRepository.getCustomCollectionById(collectionId)
                .flatMapLatest { collection ->
                    if (collection == null) {
                        flowOf(
                            CollectionUiState(
                                isLoading = false,
                                error = "Collection not found"
                            )
                        )
                    } else {
                        recipeWithStatsRepository.getRecipesWithRatingByIds(collection.recipeIds)
                            .map { recipeList ->
                                CollectionUiState(
                                    collectionName = collection.name,
                                    customCollection = collection,
                                    recipes = recipeList.map { it.toRecipeShortUi() },
                                    isOwner = collection.ownerId == SessionManagerFacade.currentUserId,
                                    isLoading = false,
                                    error = if (recipeList.isEmpty()) "No recipes found in this collection" else null
                                )
                            }
                    }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "Database Error: ${e.localizedMessage}")
                    }
                }
                .collect { collectionState ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            collectionName = collectionState.collectionName,
                            customCollection = collectionState.customCollection,
                            recipes = collectionState.recipes,
                            isOwner = collectionState.isOwner,
                            isLoading = collectionState.isLoading,
                            error = collectionState.error
                        )
                    }
                }
        }
    }

    private fun observeCollectionEditor() {
        viewModelScope.launch {
            collectionEditor.state.collect { editorState ->
                _uiState.update {
                    it.copy(
                        formDialog = editorState.formDialog,
                        deleteDialog = editorState.deleteDialog,
                        isSaving = editorState.isSaving,
                        actionError = editorState.error
                    )
                }
            }
        }
    }


    fun showEditCollectionDialog() {
        val collection = _uiState.value.customCollection ?: return
        collectionEditor.showEditDialog(collection)
    }

    fun dismissCollectionFormDialog() {
        collectionEditor.dismissFormDialog()
    }

    fun updateCollectionNameInput(name: String) {
        collectionEditor.updateNameInput(name)
    }

    fun saveCustomCollection() {
        collectionEditor.saveCollection {
            _events.trySend(CollectionEvent.Updated)
        }
    }

    fun showDeleteCollectionDialog() {
        val collection = _uiState.value.customCollection ?: return
        collectionEditor.showDeleteDialog(collection)
    }

    fun dismissDeleteCollectionDialog() {
        collectionEditor.dismissDeleteDialog()
    }

    fun deleteCustomCollection(onDeleted: () -> Unit = {}) {
        collectionEditor.deleteCollection {
            _events.trySend(CollectionEvent.Deleted)
            onDeleted()
        }
    }

    companion object {
        fun provideFactory(
            collectionId: String,
            isFavouriteCollection: Boolean
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                CollectionViewModel(
                    collectionId = collectionId,
                    isFavouriteCollection = isFavouriteCollection,
                    collectionRepository = app.container.collectionRepository,
                    recipeWithStatsRepository = app.container.recipeWithStatsRepository
                )
            }
        }
    }
}
