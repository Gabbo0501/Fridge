package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty

// ── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun DialogTitle(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PreviewBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 28.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

// ── DifficultyDialog ─────────────────────────────────────────────────────────

@Composable
fun DifficultyDialog(
    current: Difficulty,
    onConfirm: (Difficulty) -> Unit,
    onDismiss: () -> Unit
) {
    val entries = Difficulty.entries
    var selection by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(16.dp),
        title = { DialogTitle("Difficulty") },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreviewBadge(label = selection.value.toString())

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entries.first().value.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(entries.last().value.toString(),  color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }

                Slider(
                    value = entries.indexOf(selection).toFloat(),
                    onValueChange = {
                        selection = entries[it.toInt().coerceIn(0, entries.lastIndex)]
                    },
                    valueRange = 0f..entries.lastIndex.toFloat(),
                    steps = entries.lastIndex - 1,
                    colors = SliderDefaults.colors(
                        thumbColor         = MaterialTheme.colorScheme.primary,
                        activeTrackColor   = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline,
                        activeTickColor    = Color.Transparent,
                        inactiveTickColor  = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selection) },
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("OK", color = MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

// ── TimeInputDialog ──────────────────────────────────────────────────────────

@Composable
fun TimeInputDialog(
    current: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hoursText   by remember { mutableStateOf((current / 60).toString()) }
    var minutesText by remember { mutableStateOf((current % 60).toString()) }

    val hInt = hoursText.toIntOrNull() ?: 0
    val mInt = (minutesText.toIntOrNull() ?: 0).coerceIn(0, 59)

    val preview = when {
        hInt == 0 && mInt == 0 -> "—"
        hInt == 0 -> "${mInt}m"
        mInt == 0 -> "${hInt}h"
        else      -> "${hInt}h ${mInt}m"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(16.dp),
        title = { DialogTitle("Preparation Time") },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreviewBadge(label = preview)

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeField(
                        value         = hoursText,
                        onValueChange = { hoursText = it.filter(Char::isDigit) },
                        label         = "Hours",
                        modifier      = Modifier.weight(1f)
                    )
                    Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 24.sp, fontWeight = FontWeight.Light)
                    TimeField(
                        value         = minutesText,
                        onValueChange = {
                            val filtered = it.filter(Char::isDigit)
                            minutesText = when {
                                filtered.isEmpty()                          -> filtered
                                (filtered.toIntOrNull() ?: 0) > 59        -> "59"
                                else                                        -> filtered
                            }
                        },
                        label         = "Min (0–59)",
                        modifier      = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm((hInt * 60 + mInt).toLong()) },
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("OK", color = MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 11.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine    = true,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor    = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor     = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
            cursorColor          = MaterialTheme.colorScheme.primary
        ),
        shape    = RoundedCornerShape(10.dp),
        modifier = modifier
    )
}

// ── CostDialog ───────────────────────────────────────────────────────────────

@Composable
fun CostDialog(
    current: CostRange,
    onConfirm: (CostRange) -> Unit,
    onDismiss: () -> Unit
) {
    val entries = CostRange.entries
    var selection by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(16.dp),
        title = { DialogTitle("Cost Range") },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreviewBadge(label = selection.description)

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entries.first().description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(entries.last().description,  color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }

                Slider(
                    value = entries.indexOf(selection).toFloat(),
                    onValueChange = {
                        selection = entries[it.toInt().coerceIn(0, entries.lastIndex)]
                    },
                    valueRange = 0f..entries.lastIndex.toFloat(),
                    steps = entries.lastIndex - 1,
                    colors = SliderDefaults.colors(
                        thumbColor         = MaterialTheme.colorScheme.primary,
                        activeTrackColor   = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline,
                        activeTickColor    = Color.Transparent,
                        inactiveTickColor  = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selection) },
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("OK", color = MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}