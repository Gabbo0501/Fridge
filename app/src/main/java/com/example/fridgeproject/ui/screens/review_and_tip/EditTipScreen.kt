package com.example.fridgeproject.ui.screens.review_and_tip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.enums.TipType
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.review_and_tip.TipForm
import com.example.fridgeproject.viewmodel.review_and_tip.EditTipUiState

@Composable
fun EditTipScreen(
    uiState: EditTipUiState,
    onTypeChange: (TipType) -> Unit,
    onTipTextChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackRequest: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onNavigateBack: () -> Unit,
    onResetEditState: () -> Unit
) {
    if (uiState.showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onResetEditState()
            onNavigateBack()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        item { PageHeader(title = "Edit Tip", onBackClick = onBackRequest) }

        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                    LoadingComponent()
                }
            }
        } else {
            item {
                Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                    TipForm(
                        type = uiState.type,
                        tipText = uiState.tipText,
                        isSaving = uiState.isSaving,
                        typeError = uiState.errors.type,
                        tipTextError = uiState.errors.tipText,
                        generalError = uiState.errors.general,
                        helperText = "Update your personal touch",
                        actionLabel = "Save",
                        onTypeChange = onTypeChange,
                        onTipTextChange = onTipTextChange,
                        onActionClick = onSaveClick
                    )
                }
            }
        }
    }
}