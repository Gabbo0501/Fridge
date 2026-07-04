package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.StoredImage

data class FirestoreProfileImageSource(
    val type: String = "MONOGRAM",
    val url: String = "",
    val storagePath: String = ""
)

data class FirestoreImageSource(
    val url: String = "",
    val storagePath: String = ""
)

fun FirestoreProfileImageSource.toDomain(): ProfileImageSource =
    when (type.uppercase()) {
        "REMOTE" -> url
            .takeIf { it.isNotBlank() }
            ?.let { ProfileImageSource.Remote(StoredImage(url = it, storagePath = storagePath)) }
            ?: ProfileImageSource.Monogram
        else -> ProfileImageSource.Monogram
    }

fun ProfileImageSource.toFirestore(): FirestoreProfileImageSource =
    when (this) {
        is ProfileImageSource.Remote -> FirestoreProfileImageSource(
            type = "REMOTE",
            url = image.url,
            storagePath = image.storagePath
        )
        is ProfileImageSource.Local -> FirestoreProfileImageSource("MONOGRAM")
        is ProfileImageSource.Monogram -> FirestoreProfileImageSource("MONOGRAM")
    }

fun FirestoreImageSource.toDomain(): StoredImage? =
    url
        .takeIf { it.isNotBlank() }
        ?.let { StoredImage(url = it, storagePath = storagePath) }

fun StoredImage.toFirestore(): FirestoreImageSource =
    FirestoreImageSource(
        url = url,
        storagePath = storagePath
    )
