package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.RecipeTipUi
import com.example.fridgeproject.model.UserTipUi

@Composable
fun RecipeTipContent(
    tip: RecipeTipUi,
    title: String,
    labelText: String,
    labelColor: Color,
    contentColor: Color,
    secondaryContentColor: Color,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = labelColor)) {
                    append("$labelText ")
                }
                append(tip.comment)
            },
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = contentColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryContentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = if (onTitleClick != null) {
                        Modifier.clickable { onTitleClick() }
                    } else {
                        Modifier
                    }
                )
                Text(
                    text = " - ${tip.date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryContentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (onDeleteClick != null) {
                ReviewTipActionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete tip",
                    tint = contentColor,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
fun UserTipContent(
    tip: UserTipUi,
    recipeTitle: String,
    labelText: String,
    labelColor: Color,
    contentColor: Color,
    secondaryContentColor: Color,
    modifier: Modifier = Modifier,
    onRecipeClick: ((String) -> Unit)? = null,
    onRecipeAuthorClick: ((String) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = recipeTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = if (onRecipeClick != null) {
                    Modifier.clickable { onRecipeClick(tip.recipeId) }
                } else {
                    Modifier
                }
            )
            Row {
                if (onEditClick != null) {
                    ReviewTipActionIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit tip",
                        tint = contentColor,
                        onClick = onEditClick
                    )
                }
                if (onDeleteClick != null) {
                    ReviewTipActionIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete tip",
                        tint = contentColor,
                        onClick = onDeleteClick
                    )
                }
            }
        }

        RecipeAuthorAndDate(
            authorUsername = tip.recipeAuthorUsername,
            authorId = tip.recipeAuthorId,
            date = tip.date,
            color = secondaryContentColor,
            onRecipeAuthorClick = onRecipeAuthorClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = labelColor)) {
                    append("$labelText ")
                }
                append(tip.comment)
            },
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = contentColor
        )
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
