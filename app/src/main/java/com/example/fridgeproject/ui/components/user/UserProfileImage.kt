package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.previewUrl
import com.example.fridgeproject.ui.components.ImageInputMenu

@Composable
fun UserProfileImage(
    isOwner: Boolean,
    isEditing: Boolean,
    imageSource: ProfileImageSource,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveClick: (() -> Unit)? = null,
    firstName: String? = "",
    lastName: String? = ""
) {
    // stato per gestire l'apertura del menu a comparsa
    var expanded by remember { mutableStateOf(false) }

    // box esterno per centrare il tutto
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        //box con immagine del profilo
        Box(
            modifier = Modifier
                .size(150.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
            ) {
                //switch tra i vari casi di immagini di profilo
                when (imageSource) {
                    is ProfileImageSource.Monogram -> {
                        //img iniziali nome
                        UserMonogramAvatar(
                            firstName = firstName.orEmpty(),
                            lastName = lastName.orEmpty(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is ProfileImageSource.Local -> {
                        AsyncImage(
                            model = imageSource.input.previewUrl(),
                            contentDescription = "Selected profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    is ProfileImageSource.Remote -> {
                        AsyncImage(
                            model = imageSource.image.url,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // bottone con l'icona della camera
            if (isOwner && isEditing) {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    ImageInputMenu(
                        expanded = expanded,
                        onDismiss = { expanded = false },
                        onGalleryClick = onGalleryClick,
                        onCameraClick = onCameraClick,
                        onRemoveClick = onRemoveClick?.takeUnless { imageSource is ProfileImageSource.Monogram }
                    )
                }
            }
        }
    }
}