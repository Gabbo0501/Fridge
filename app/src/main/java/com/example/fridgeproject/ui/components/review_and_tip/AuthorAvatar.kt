package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.example.fridgeproject.ui.components.user.UserMonogramAvatar

@Composable
fun AuthorAvatar(
    avatarUrl: String?,
    firstName: String?,
    lastName: String?,
    size: Dp,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)

    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = contentDescription,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop
        )
    } else {
        UserMonogramAvatar(
            firstName = firstName.orEmpty(),
            lastName = lastName.orEmpty(),
            modifier = avatarModifier,
            isLarge = false
        )
    }
}