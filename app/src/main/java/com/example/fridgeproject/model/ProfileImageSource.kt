package com.example.fridgeproject.model

sealed class ProfileImageSource {
    object Monogram : ProfileImageSource()
    data class Remote(val image: StoredImage) : ProfileImageSource()
    data class Local(val input: LocalImageInput) : ProfileImageSource()
}

fun ProfileImageSource.avatarUrl(): String? = when (this) {
    is ProfileImageSource.Remote -> image.url
    is ProfileImageSource.Local -> input.previewUrl()
    is ProfileImageSource.Monogram -> null
}
