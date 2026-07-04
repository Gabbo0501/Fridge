package com.example.fridgeproject.ui.screens.review_and_tip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.ui.components.AlertDialog
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.review_and_tip.RecipeReviewRow
import com.example.fridgeproject.viewmodel.review_and_tip.RecipeReviewsUiState

@Composable
fun RecipeReviewsListScreen(
    uiState: RecipeReviewsUiState,
    onBackClick: () -> Unit,
    onAddReviewClick: () -> Unit,
    onDeleteReviewClick: (String) -> Unit,
    onDeleteReviewConfirm: () -> Unit,
    onDeleteReviewDismiss: () -> Unit,
    onAuthorClick: (String) -> Unit
){
    if (uiState.reviewToDeleteId != null) {
        AlertDialog(
            title = "Delete review?",
            text = "Are you sure you want to delete this review? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = MaterialTheme.colorScheme.error,
            onConfirm = onDeleteReviewConfirm,
            onDismiss = onDeleteReviewDismiss
        )
    }

    if (uiState.globalError.isNotBlank()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        ErrorComponent(uiState.globalError)
        return
    } else if (uiState.isLoading) {
        LoadingComponent()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 25.dp),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            item {
                PageHeader(title = "Recipe Reviews", onBackClick = onBackClick) {
                    if (uiState.canAddReview) {
                        IconButton(onClick = onAddReviewClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add review",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            val hasAnyReview = uiState.myReview != null || uiState.otherReviews.isNotEmpty()
            if (!hasAnyReview) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateComponent(
                            message = "There are no reviews for this recipe yet."
                        )
                    }
                }
            } else {
                uiState.myReview?.let { review ->
                    item {
                        Box(
                            modifier = Modifier.padding(
                                bottom = if (uiState.otherReviews.isEmpty()) PageBottomPadding else 0.dp
                            )
                        ) {
                            RecipeReviewRow(
                                review = review,
                                isHighlighted = true,
                                onAuthorClick = onAuthorClick,
                                onDeleteClick = { onDeleteReviewClick(review.id) }
                            )
                        }
                    }
                }
                itemsIndexed(uiState.otherReviews) { index, review ->
                    Box(
                        modifier = Modifier.padding(
                            bottom = if (index == uiState.otherReviews.lastIndex) PageBottomPadding else 0.dp
                        )
                    ) {
                        RecipeReviewRow(
                            review = review,
                            onAuthorClick = onAuthorClick
                        )
                    }
                }
            }
        }
    }
}