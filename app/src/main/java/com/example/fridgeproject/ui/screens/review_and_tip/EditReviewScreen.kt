package com.example.fridgeproject.ui.screens.review_and_tip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.StoredImage
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.review_and_tip.ReviewForm
import com.example.fridgeproject.viewmodel.review_and_tip.EditReviewUiState

@Composable
fun EditReviewScreen(
    uiState: EditReviewUiState,
    onRatingChange: (Int) -> Unit,
    onReviewTextChange: (String) -> Unit,
    onRemoveImage: (StoredImage) -> Unit,
    onRemovePendingImage: (LocalImageInput) -> Unit,
    onSaveClick: () -> Unit,
    onBackRequest: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onNavigateBack: () -> Unit,
    onResetEditState: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick : () -> Unit
) {
    if (uiState.showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onResetEditState()
            onNavigateBack()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        item { PageHeader(title = "Edit Review", onBackClick = onBackRequest) }

        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                    LoadingComponent()
                }
            }
        } else {
            item {
                Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                    ReviewForm(
                        rating = uiState.rating,
                        reviewText = uiState.reviewText,
                        images = uiState.images,
                        pendingImages = uiState.pendingImages,
                        isSaving = uiState.isSaving,
                        ratingError = uiState.errors.rating,
                        reviewTextError = uiState.errors.reviewText,
                        imagesError = uiState.errors.images,
                        generalError = uiState.errors.general,
                        helperText = "Update your rating and note",
                        actionLabel = "Save",
                        onGalleryClick = onGalleryClick,
                        onRatingChange = onRatingChange,
                        onReviewTextChange = onReviewTextChange,
                        onRemoveImage = onRemoveImage,
                        onRemovePendingImage = onRemovePendingImage,
                        onActionClick = onSaveClick,
                        onCameraClick = onCameraClick,
                    )
                }
            }
        }
    }
}