package com.example.fridgeproject.viewmodel.collection

import com.example.fridgeproject.model.CustomCollection
import com.example.fridgeproject.viewmodel.user.UserCollectionFormDialogState

data class SaveRecipeToCollectionUiState(
    val savedCollections: List<CustomCollection> = emptyList(),
    val availableCollections: List<CustomCollection> = emptyList(),
    val formDialog: UserCollectionFormDialogState = UserCollectionFormDialogState(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isSavingCollection: Boolean = false,
    val collectionFormError: String? = null,
    val error: String? = null
)