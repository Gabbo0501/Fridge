package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.ProfileImageSource

@Composable
fun UserHeaderEdit(
    imageSource: ProfileImageSource,
    firstName: String,
    lastName: String,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserProfileImage(
            isOwner = true,
            isEditing = true,
            imageSource = imageSource,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onRemoveClick = onRemoveImageClick,
            firstName = firstName,
            lastName = lastName
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}