package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.StoredImage
import com.example.fridgeproject.model.previewUrl
import com.example.fridgeproject.ui.components.ImageInputMenu

@Composable
fun ReviewImagesInput(
    images: List<StoredImage>,
    pendingImages: List<LocalImageInput>,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRemoveImage: (StoredImage) -> Unit,
    onRemovePendingImage: (LocalImageInput) -> Unit,
    modifier: Modifier = Modifier,
    maxImages: Int = 3
) {
    var expanded by remember { mutableStateOf(false) }
    val imageCount = images.size + pendingImages.size
    val emptySlotCount = (maxImages - imageCount - if (imageCount < maxImages) 1 else 0)
        .coerceAtLeast(0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Photos",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            images.forEach { image ->
                ReviewImageTile(
                    imageUrl = image.url,
                    onRemoveImage = { onRemoveImage(image) },
                    modifier = Modifier.weight(1f)
                )
            }

            pendingImages.forEach { input ->
                ReviewImageTile(
                    imageUrl = input.previewUrl(),
                    onRemoveImage = { onRemovePendingImage(input) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (imageCount < maxImages) {
                AddReviewImageTile(
                    onClick = { expanded = true },
                    modifier = Modifier.weight(1f)
                )
            }

            repeat(emptySlotCount) {
                Box(modifier = Modifier.weight(1f))
            }
        }

        Box {
            ImageInputMenu(
                expanded = expanded,
                onDismiss = { expanded = false },
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick
            )
        }
    }
}

@Composable
private fun ReviewImageTile(
    imageUrl: String,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Review photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onRemoveImage),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddReviewImageTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add photo",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}