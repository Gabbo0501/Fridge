package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.RecipeReviewUi
import com.example.fridgeproject.model.UserReviewUi

@Composable
fun RecipeReviewRow(
    review: RecipeReviewUi,
    isHighlighted: Boolean = false,
    onAuthorClick: ((String) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val containerColor = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = if (isHighlighted) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = containerColor,
        border = if (isHighlighted) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuthorAvatar(
                avatarUrl = review.userAvatarUrl,
                firstName = review.firstName,
                lastName = review.lastName,
                size = 48.dp,
                contentDescription = "Review author",
                modifier = if (onAuthorClick != null) {
                    Modifier.clickable { onAuthorClick(review.userId) }
                } else {
                    Modifier
                }
            )

            RecipeReviewContent(
                review = review,
                title = review.userName.orEmpty(),
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                filledStarColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                emptyStarColor = if (isHighlighted) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.weight(1f),
                onTitleClick = onAuthorClick?.let {
                    { it(review.userId) }
                },
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
fun UserReviewRow(
    review: UserReviewUi,
    onRecipeClick: ((String) -> Unit)? = null,
    onRecipeAuthorClick: ((String) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        UserReviewContent(
            review = review,
            contentColor = MaterialTheme.colorScheme.onSurface,
            secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            filledStarColor = MaterialTheme.colorScheme.primary,
            emptyStarColor = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(20.dp),
            onRecipeClick = onRecipeClick,
            onRecipeAuthorClick = onRecipeAuthorClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}