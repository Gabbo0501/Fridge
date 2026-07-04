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
import com.example.fridgeproject.model.UserTipUi
import com.example.fridgeproject.ui.components.AlertDialog
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.review_and_tip.UserTipRow

@Composable
fun MyTipsScreen(
    tips: List<UserTipUi>,
    error: String,
    onBackClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onRecipeAuthorClick: (String) -> Unit,
    onEditTipClick: (String) -> Unit,
    tipToDeleteId: String?,
    onDeleteTipClick: (String) -> Unit,
    onDeleteTipConfirm: () -> Unit,
    onDeleteTipDismiss: () -> Unit
){
    if (tipToDeleteId != null) {
        AlertDialog(
            title = "Delete tip?",
            text = "Are you sure you want to delete this tip? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = MaterialTheme.colorScheme.error,
            onConfirm = onDeleteTipConfirm,
            onDismiss = onDeleteTipDismiss
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
            item { PageHeader(title = "My Tips", onBackClick = onBackClick) }

            if (tips.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateComponent(
                            message = "You haven't shared any tips yet."
                        )
                    }
                }
            } else {
                itemsIndexed(tips) { index, tip ->
                    Box(
                        modifier = Modifier.padding(
                            bottom = if (index == tips.lastIndex) PageBottomPadding else 0.dp
                        )
                    ) {
                        UserTipRow(
                            tip = tip,
                            onRecipeClick = onRecipeClick,
                            onRecipeAuthorClick = onRecipeAuthorClick,
                            onEditClick = { onEditTipClick(tip.id) },
                            onDeleteClick = { onDeleteTipClick(tip.id) }
                        )
                    }
                }
            }
        }
    }

}