package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.RecipeReviewUi
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.review_and_tip.RecipeReviewRow

@Composable
fun RecipeReviewsComponent(
    myReview: RecipeReviewUi?,
    otherReviews: List<RecipeReviewUi>,
    onExpandReviewList: () -> Unit,
    showAddReviewButton: Boolean = false,
    onAddReviewClick: () -> Unit = {},
    onDeleteReviewClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (showAddReviewButton) {
                IconButton(
                    onClick = onAddReviewClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add review",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        val hasAnyReview = myReview != null || otherReviews.isNotEmpty()

        if (!hasAnyReview) {
            EmptyStateComponent(
                message = "No reviews yet",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } else {
            myReview?.let {review ->
                RecipeReviewRow(
                    review = review,
                    isHighlighted = true,
                    onAuthorClick = onAuthorClick,
                    onDeleteClick = { onDeleteReviewClick(review.id) }
                )
            }

            otherReviews.forEach { review ->
                RecipeReviewRow(
                    review = review,
                    onAuthorClick = onAuthorClick
                )
            }
        }

        if (hasAnyReview) {
            Text(
                text = "See all",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onExpandReviewList() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
