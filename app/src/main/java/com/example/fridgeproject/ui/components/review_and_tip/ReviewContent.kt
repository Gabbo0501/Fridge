package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fridgeproject.model.RecipeReviewUi
import com.example.fridgeproject.model.StoredImage
import com.example.fridgeproject.model.UserReviewUi

@Composable
fun RecipeReviewContent(
    review: RecipeReviewUi,
    title: String,
    contentColor: Color,
    secondaryContentColor: Color,
    filledStarColor: Color,
    emptyStarColor: Color,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                modifier = if (onTitleClick != null) {
                    Modifier.clickable { onTitleClick() }
                } else {
                    Modifier
                }
            )
            Text(
                text = review.date,
                style = MaterialTheme.typography.labelSmall,
                color = secondaryContentColor
            )
        }

        ReviewStars(
            stars = review.stars,
            filledStarColor = filledStarColor,
            emptyStarColor = emptyStarColor
        )

        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = contentColor
        )

        if (review.images.isNotEmpty()) {
            ReviewImagesCarousel(images = review.images)
        }

        if (onDeleteClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ReviewTipActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete review",
                    tint = contentColor,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
fun UserReviewContent(
    review: UserReviewUi,
    contentColor: Color,
    secondaryContentColor: Color,
    filledStarColor: Color,
    emptyStarColor: Color,
    modifier: Modifier = Modifier,
    onRecipeClick: ((String) -> Unit)? = null,
    onRecipeAuthorClick: ((String) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = review.recipeTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                modifier = if (onRecipeClick != null) {
                    Modifier.clickable { onRecipeClick(review.recipeId) }
                } else {
                    Modifier
                }
            )
        }

        RecipeAuthorAndDate(
            authorUsername = review.recipeAuthorUsername,
            authorId = review.recipeAuthorId,
            date = review.date,
            color = secondaryContentColor,
            onRecipeAuthorClick = onRecipeAuthorClick
        )

        ReviewStars(
            stars = review.stars,
            filledStarColor = filledStarColor,
            emptyStarColor = emptyStarColor
        )

        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = contentColor
        )

        if (review.images.isNotEmpty()) {
            ReviewImagesCarousel(images = review.images)
        }

        if (onEditClick != null || onDeleteClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (onEditClick != null) {
                    ReviewTipActionIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit review",
                        tint = contentColor,
                        onClick = onEditClick
                    )
                }
                if (onDeleteClick != null) {
                    ReviewTipActionIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete review",
                        tint = contentColor,
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeAuthorAndDate(
    authorUsername: String?,
    authorId: String?,
    date: String,
    color: Color,
    onRecipeAuthorClick: ((String) -> Unit)?
) {
    if (authorUsername.isNullOrBlank()) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    } else {
        Row(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = "by ",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = authorUsername,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = if (onRecipeAuthorClick != null && authorId != null) {
                    Modifier.clickable { onRecipeAuthorClick(authorId) }
                } else {
                    Modifier
                }
            )
            Text(
                text = " - $date",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewImagesCarousel(images: List<StoredImage>) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { images.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 8.dp),
        preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) { i ->
        val image = images[i]
        val imageShape = MaterialTheme.shapes.extraLarge
        AsyncImage(
            modifier = Modifier
                .height(205.dp)
                .maskClip(imageShape),
            model = image.url,
            contentDescription = "Review image",
            contentScale = ContentScale.Crop
        )
    }
}
