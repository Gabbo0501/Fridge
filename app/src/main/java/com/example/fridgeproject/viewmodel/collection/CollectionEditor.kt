package com.example.fridgeproject.viewmodel.collection

import com.example.fridgeproject.viewmodel.user.UserCollectionDeleteDialogState
import com.example.fridgeproject.viewmodel.user.UserCollectionFormDialogState
import com.example.fridgeproject.viewmodel.user.UserCollectionFormMode

import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.model.CustomCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionEditorState(
    val formDialog: UserCollectionFormDialogState = UserCollectionFormDialogState(),
    val deleteDialog: UserCollectionDeleteDialogState = UserCollectionDeleteDialogState(),
    val isSaving: Boolean = false,
    val error: String? = null
)

class CollectionEditor(
    private val repository: CollectionRepository,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(CollectionEditorState())
    val state: StateFlow<CollectionEditorState> = _state.asStateFlow()

    fun showCreateDialog() {
        _state.update {
            it.copy(
                formDialog = UserCollectionFormDialogState(
                    isOpen = true,
                    mode = UserCollectionFormMode.CREATE
                ),
                error = null
            )
        }
    }

    fun showEditDialog(collection: CustomCollection) {
        _state.update {
            it.copy(
                formDialog = UserCollectionFormDialogState(
                    isOpen = true,
                    mode = UserCollectionFormMode.EDIT,
                    nameInput = collection.name,
                    collectionToEdit = collection
                ),
                error = null
            )
        }
    }

    fun dismissFormDialog() {
        _state.update {
            if (it.isSaving) {
                it
            } else {
                it.copy(
                    formDialog = UserCollectionFormDialogState(),
                    error = null
                )
            }
        }
    }

    fun updateNameInput(name: String) {
        _state.update {
            it.copy(
                formDialog = it.formDialog.copy(
                    nameInput = name,
                    nameError = null
                ),
                error = null
            )
        }
    }

    fun saveCollection(
        ownerId: String? = null,
        onSaved: (UserCollectionFormMode) -> Unit = {}
    ) {
        val formDialog = _state.value.formDialog
        val name = validateCollectionName(formDialog.nameInput) ?: return
        val collection = when (formDialog.mode) {
            UserCollectionFormMode.CREATE -> {
                val owner = ownerId ?: return
                CustomCollection(
                    id = "",
                    ownerId = owner,
                    name = name,
                    recipeIds = emptyList()
                )
            }

            UserCollectionFormMode.EDIT -> formDialog.collectionToEdit?.copy(name = name) ?: return
        }

        scope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            try {
                repository.saveCustomCollection(collection)
                _state.update {
                    it.copy(
                        formDialog = UserCollectionFormDialogState(),
                        isSaving = false,
                        error = null
                    )
                }
                onSaved(formDialog.mode)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "Could not save collection"
                    )
                }
            }
        }
    }

    fun showDeleteDialog(collection: CustomCollection) {
        _state.update {
            it.copy(
                deleteDialog = UserCollectionDeleteDialogState(
                    collectionToDelete = collection
                )
            )
        }
    }

    fun dismissDeleteDialog() {
        _state.update {
            if (it.deleteDialog.isDeleting) {
                it
            } else {
                it.copy(deleteDialog = UserCollectionDeleteDialogState())
            }
        }
    }

    fun deleteCollection(onDeleted: () -> Unit = {}) {
        val collection = _state.value.deleteDialog.collectionToDelete ?: return

        scope.launch {
            _state.update {
                it.copy(
                    deleteDialog = it.deleteDialog.copy(
                        isDeleting = true,
                        error = null
                    )
                )
            }

            try {
                repository.deleteCustomCollection(collection.id)
                _state.update {
                    it.copy(deleteDialog = UserCollectionDeleteDialogState())
                }
                onDeleted()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        deleteDialog = it.deleteDialog.copy(
                            isDeleting = false,
                            error = "Could not delete collection"
                        )
                    )
                }
            }
        }
    }

    private fun validateCollectionName(nameInput: String): String? {
        val name = nameInput.trim()
        if (name.isBlank()) {
            _state.update {
                it.copy(
                    formDialog = it.formDialog.copy(
                        nameError = "Collection name is required"
                    )
                )
            }
            return null
        }
        return name
    }
}
