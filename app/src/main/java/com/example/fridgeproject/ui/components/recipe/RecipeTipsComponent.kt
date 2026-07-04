package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.RecipeTipUi
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.review_and_tip.RecipeTipRow

@Composable
fun RecipeTipsComponent(
    myTip: RecipeTipUi?,
    otherTips: List<RecipeTipUi>,
    onExpandTipsList: () -> Unit,
    showAddTipButton: Boolean = false,
    onAddTipClick: () -> Unit = {},
    onDeleteTipClick: (String) -> Unit = {},
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
                text = "Tips",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (showAddTipButton) {
                IconButton(
                    onClick = onAddTipClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add tip",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        val hasAnyTip = myTip != null || otherTips.isNotEmpty()

        if (!hasAnyTip) {
            EmptyStateComponent(
                message = "No tips yet",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } else {
            myTip?.let {tip ->
                RecipeTipRow(
                    tip = tip,
                    isHighlighted = true,
                    onAuthorClick = onAuthorClick,
                    onDeleteClick = { onDeleteTipClick(tip.id) }
                )
            }

            otherTips.forEach { tip ->
                RecipeTipRow(
                    tip = tip,
                    onAuthorClick = onAuthorClick
                )
            }
        }

        if (hasAnyTip) {
            Text(
                text = "See all",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onExpandTipsList() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
