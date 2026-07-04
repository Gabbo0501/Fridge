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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.RecipeTipUi
import com.example.fridgeproject.model.UserTipUi
import com.example.fridgeproject.model.enums.TipType
import com.example.fridgeproject.ui.theme.FridgeTipDo
import com.example.fridgeproject.ui.theme.FridgeTipDont

@Composable
fun RecipeTipRow(
    tip: RecipeTipUi,
    isHighlighted: Boolean = false,
    onAuthorClick: ((String) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val accentColor = tip.type.tipAccentColor()
    val labelText = tip.type.tipLabelText()
    val labelColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else accentColor
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
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(2.dp, if (isHighlighted) MaterialTheme.colorScheme.primary else accentColor),
        shadowElevation = if (isHighlighted) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AuthorAvatar(
                avatarUrl = tip.userAvatarUrl,
                firstName = tip.firstName,
                lastName = tip.lastName,
                size = 44.dp,
                contentDescription = "Tip author",
                modifier = if (onAuthorClick != null) {
                    Modifier.clickable { onAuthorClick(tip.userId) }
                } else {
                    Modifier
                }
            )

            RecipeTipContent(
                tip = tip,
                title = tip.userName.orEmpty(),
                labelText = labelText,
                labelColor = labelColor,
                contentColor = contentColor,
                secondaryContentColor = secondaryContentColor,
                onTitleClick = onAuthorClick?.let {
                    { it(tip.userId) }
                },
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
fun UserTipRow(
    tip: UserTipUi,
    onRecipeClick: ((String) -> Unit)? = null,
    onRecipeAuthorClick: ((String) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val accentColor = tip.type.tipAccentColor()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, accentColor),
        shadowElevation = 1.dp
    ) {
        UserTipContent(
            tip = tip,
            recipeTitle = tip.recipeTitle.orEmpty(),
            labelText = tip.type.tipLabelText(),
            labelColor = accentColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(16.dp),
            onRecipeClick = onRecipeClick,
            onRecipeAuthorClick = onRecipeAuthorClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

private fun TipType.tipAccentColor() = when (this) {
    TipType.DO -> FridgeTipDo
    TipType.DONT -> FridgeTipDont
}

private fun TipType.tipLabelText() = when (this) {
    TipType.DO -> "DO:"
    TipType.DONT -> "DON'T:"
}
