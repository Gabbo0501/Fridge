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
import com.example.fridgeproject.ui.components.review_and_tip.RecipeTipRow
import com.example.fridgeproject.viewmodel.review_and_tip.RecipeTipsUiState

@Composable
fun RecipeTipsListScreen(
    uiState: RecipeTipsUiState,
    onBackClick: () -> Unit,
    onAddTipClick: () -> Unit,
    onDeleteTipClick: (String) -> Unit,
    onDeleteTipConfirm: () -> Unit,
    onDeleteTipDismiss: () -> Unit,
    onAuthorClick: (String) -> Unit
){
    if (uiState.tipToDeleteId != null) {
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
    } else if(uiState.isLoading) {
        LoadingComponent()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 25.dp),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            item {
                PageHeader(title = "Recipe Tips", onBackClick = onBackClick) {
                    if (uiState.canAddTip) {
                        IconButton(onClick = onAddTipClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tip",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            val hasAnyTip = uiState.myTip != null || uiState.otherTips.isNotEmpty()
            if (!hasAnyTip) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateComponent(
                            message = "There are no tips for this recipe yet."
                        )
                    }
                }
            } else {
                uiState.myTip?.let { tip ->
                    item {
                        Box(
                            modifier = Modifier.padding(
                                bottom = if (uiState.otherTips.isEmpty()) PageBottomPadding else 0.dp
                            )
                        ) {
                            RecipeTipRow(
                                tip = tip,
                                isHighlighted = true,
                                onAuthorClick = onAuthorClick,
                                onDeleteClick = { onDeleteTipClick(tip.id) }
                            )
                        }
                    }
                }
                itemsIndexed(uiState.otherTips) { index, tip ->
                    Box(
                        modifier = Modifier.padding(
                            bottom = if (index == uiState.otherTips.lastIndex) PageBottomPadding else 0.dp
                        )
                    ) {
                        RecipeTipRow(
                            tip = tip,
                            onAuthorClick = onAuthorClick
                        )
                    }
                }
            }
        }
    }
}