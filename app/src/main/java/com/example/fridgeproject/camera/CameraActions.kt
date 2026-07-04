package com.example.fridgeproject.camera

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

data class CameraManager(
    val onTakePhoto: (onResult: (Uri) -> Unit) -> Unit,
    val takePhoto: () -> Unit,
    val onPickFromGallery: (onResult: (Uri) -> Unit) -> Unit,
    val cameraPreview:  @Composable () -> Unit,
)

val LocalCameraActions = compositionLocalOf<CameraManager> {
    error("CameraActions not provided")
}
