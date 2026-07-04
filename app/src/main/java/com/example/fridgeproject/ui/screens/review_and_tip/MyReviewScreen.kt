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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.UserReviewUi
import com.example.fridgeproject.ui.components.AlertDialog
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.review_and_tip.UserReviewRow

@Composable
fun MyReviewScreen(
    reviews: List<UserReviewUi>,
    error: String,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onRecipeAuthorClick: (String) -> Unit,
    onEditReviewClick: (String) -> Unit,
    reviewToDeleteId: String?,
    onDeleteReviewClick: (String) -> Unit,
    onDeleteReviewConfirm: () -> Unit,
    onDeleteReviewDismiss: () -> Unit
){
    if (reviewToDeleteId != null) {
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

    if (error.isNotBlank()) {
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
        ErrorComponent(error)
        return
    }else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 25.dp),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            item { PageHeader(title = "My Reviews", onBackClick = onBackClick) }

            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateComponent(
                            message = "You haven't written any reviews yet."
                        )
                    }
                }
            } else {
                itemsIndexed(reviews) { index, review ->
                    Box(
                        modifier = Modifier.padding(
                            bottom = if (index == reviews.lastIndex) PageBottomPadding else 0.dp
                        )
                    ) {
                        UserReviewRow(
                            review = review,
                            onRecipeClick = onRecipeClick,
                            onRecipeAuthorClick = onRecipeAuthorClick,
                            onEditClick = { onEditReviewClick(review.id) },
                            onDeleteClick = { onDeleteReviewClick(review.id) }
                        )
                    }
                }
            }
        }
    }
}