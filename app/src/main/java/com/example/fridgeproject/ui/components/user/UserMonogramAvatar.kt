package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.fridgeproject.ui.theme.FridgeAvatarBackground

@Composable
fun UserMonogramAvatar(
    firstName: String,
    lastName: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = true,
    backgroundColor: Color = FridgeAvatarBackground,
    textStyle: TextStyle = if (isLarge) {
        MaterialTheme.typography.headlineMedium
    } else {
        MaterialTheme.typography.titleMedium
    },
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val initials = userInitialsFromName(firstName, lastName)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = textStyle,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private fun userInitialsFromName(firstName: String, lastName: String): String {
    val first = firstName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val second = lastName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    return (first + second).take(2)
}