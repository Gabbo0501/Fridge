package com.example.fridgeproject.viewmodel.collection

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
import com.example.fridgeproject.navigation.SaveRecipeToCollectionRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SaveRecipeToCollectionViewModel(
    savedStateHandle: SavedStateHandle,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SaveRecipeToCollectionRoute>()
    private val recipeId = route.recipeId

    private val _uiState = MutableStateFlow(SaveRecipeToCollectionUiState(isLoading = true))
    val uiState: StateFlow<SaveRecipeToCollectionUiState> = _uiState.asStateFlow()

    private val collectionEditor = CollectionEditor(collectionRepository, viewModelScope)

    init {
        observeCollections()
        observeCollectionEditor()
    }

    private fun observeCollections() {
        val userId = SessionManagerFacade.currentUserId
        if (userId == null) {
            _uiState.update {
                SaveRecipeToCollectionUiState(isLoading = false, error = "Login required")
            }
            return
        }

        viewModelScope.launch {
            collectionRepository.getUserCustomCollections(userId).collect { collections ->
                val (savedCollections, availableCollections) = collections
                    .partition { collection -> recipeId in collection.recipeIds }

                _uiState.update {
                    it.copy(
                        savedCollections = savedCollections,
                        availableCollections = availableCollections,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun toggleCollection(collectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            try {
                collectionRepository.toggleRecipeInCustomCollection(collectionId, recipeId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Could not update collection")
                }
            } finally {
                _uiState.update { it.copy(isUpdating = false) }
            }
        }
    }

    private fun observeCollectionEditor() {
        viewModelScope.launch {
            collectionEditor.state.collect { editorState ->
                _uiState.update {
                    it.copy(
                        formDialog = editorState.formDialog,
                        isSavingCollection = editorState.isSaving,
                        collectionFormError = editorState.error
                    )
                }
            }
        }
    }

    fun showCreateCollectionDialog() {
        collectionEditor.showCreateDialog()
    }

    fun dismissCreateCollectionDialog() {
        collectionEditor.dismissFormDialog()
    }

    fun updateCollectionName(name: String) {
        collectionEditor.updateNameInput(name)
    }

    fun saveCollection() {
        collectionEditor.saveCollection(ownerId = SessionManagerFacade.currentUserId)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                SaveRecipeToCollectionViewModel(
                    savedStateHandle = savedStateHandle,
                    collectionRepository = app.container.collectionRepository
                )
            }
        }
    }
}