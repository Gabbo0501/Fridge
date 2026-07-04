package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.SocialPlatform

data class SocialProfile(
    val platform: SocialPlatform = SocialPlatform.INSTAGRAM,
    val username: String = ""
)