package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty

// ── Unified component ─────────────────────────────────────────────────────────

@Composable
fun RecipeStatsRow(
    difficulty: Difficulty,
    prepTime: Long,
    costRange: CostRange,
    editable: Boolean = false,
    showDifficultyDialog: Boolean = false,
    showTimeDialog: Boolean = false,
    showCostDialog: Boolean = false,
    onDifficultyDialogOpen: () -> Unit = {},
    onTimeDialogOpen: () -> Unit = {},
    onCostDialogOpen: () -> Unit = {},
    onStatsDialogDismiss: () -> Unit = {},
    onDifficultyChange: (Difficulty) -> Unit = {},
    onTimeChange: (Long) -> Unit = {},
    onCostChange: (CostRange) -> Unit = {}
) {
    val hours   = prepTime / 60
    val minutes = prepTime % 60
    val timeLabel = when {
        hours == 0L  -> "${minutes}min"
        minutes == 0L -> "${hours}h"
        else          -> "${hours}h ${minutes}m"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            StatItem(
                icon    = Icons.Filled.QueryStats,
                label   = "DIFFICULTY",
                value   = "${difficulty.value}/5",
                editable = editable,
                onClick  = onDifficultyDialogOpen
            )
            VerticalDivider(
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.height(60.dp).width(10.dp)
            )
            StatItem(
                icon    = Icons.Filled.Timer,
                label   = "TIME",
                value   = timeLabel,
                editable = editable,
                onClick  = onTimeDialogOpen
            )
            VerticalDivider(
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.height(60.dp).width(10.dp)
            )
            StatItem(
                icon    = Icons.Filled.Euro,
                label   = "COST",
                value   = costRange.description,
                editable = editable,
                onClick  = onCostDialogOpen
            )
        }
    }

    // ── Dialogs (solo se editable) ────────────────────────────────────────────
    if (showDifficultyDialog) {
        DifficultyDialog(
            current   = difficulty,
            onConfirm = { onDifficultyChange(it); onStatsDialogDismiss() },
            onDismiss = onStatsDialogDismiss
        )
    }
    if (showTimeDialog) {
        TimeInputDialog(
            current   = prepTime,
            onConfirm = { onTimeChange(it); onStatsDialogDismiss() },
            onDismiss = onStatsDialogDismiss
        )
    }
    if (showCostDialog) {
        CostDialog(
            current   = costRange,
            onConfirm = { onCostChange(it); onStatsDialogDismiss() },
            onDismiss = onStatsDialogDismiss
        )
    }
}

// ── StatItem ──────────────────────────────────────────────────────────────────

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    editable: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (editable) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
        } else Modifier
    ) {
        Icon(
            imageVector     = icon,
            contentDescription = label,
            tint            = MaterialTheme.colorScheme.primary,
            modifier        = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.primary,
            textAlign  = TextAlign.Center
        )
    }
}