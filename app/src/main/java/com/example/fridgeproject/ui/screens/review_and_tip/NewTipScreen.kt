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
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.review_and_tip.TipForm
import com.example.fridgeproject.viewmodel.review_and_tip.NewTipUiState

@Composable
fun NewTipScreen(
    uiState: NewTipUiState,
    onTypeChange: (TipType) -> Unit,
    onTipTextChange: (String) -> Unit,
    onPublishClick: () -> Unit,
    onBackRequest: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onNavigateBack: () -> Unit,
    onResetCreateState: () -> Unit
) {
    if (uiState.showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onResetCreateState()
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
        item { PageHeader(title = "Your Personal Touch", onBackClick = onBackRequest) }

        item {
            Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                TipForm(
                    type = uiState.type,
                    tipText = uiState.tipText,
                    isSaving = uiState.isSaving,
                    typeError = uiState.errors.type,
                    tipTextError = uiState.errors.tipText,
                    generalError = uiState.errors.general,
                    helperText = "Share what helps this recipe work better",
                    actionLabel = "Publish",
                    onTypeChange = onTypeChange,
                    onTipTextChange = onTipTextChange,
                    onActionClick = onPublishClick
                )
            }
        }
    }
}