package com.example.fridgeproject.model

import android.net.Uri

data class StoredImage(
    val url: String = "",
    val storagePath: String = ""
)

sealed class LocalImageInput {
    abstract val uri: Uri
    data class Gallery(override val uri: Uri) : LocalImageInput()
    data class Camera(override val uri: Uri) : LocalImageInput()
}

fun LocalImageInput.previewUrl(): String = uri.toString()