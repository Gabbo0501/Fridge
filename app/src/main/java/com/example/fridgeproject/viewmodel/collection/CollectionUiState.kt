package com.example.fridgeproject.viewmodel.collection

import com.example.fridgeproject.viewmodel.user.UserCollectionDeleteDialogState
import com.example.fridgeproject.viewmodel.user.UserCollectionFormDialogState

import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.model.RecipeShortUi

data class CollectionUiState(
    val collectionName: String = "",
    val customCollection: CustomCollection? = null,
    val recipes: List<RecipeShortUi> = emptyList(),
    val isOwner: Boolean = false,
    val formDialog: UserCollectionFormDialogState = UserCollectionFormDialogState(),
    val deleteDialog: UserCollectionDeleteDialogState = UserCollectionDeleteDialogState(),
    val isSaving: Boolean = false,
    val actionError: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
