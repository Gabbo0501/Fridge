package com.example.fridgeproject.model.enums

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Facebook
import compose.icons.fontawesomeicons.brands.Instagram
import compose.icons.fontawesomeicons.brands.Tiktok
import compose.icons.fontawesomeicons.brands.Twitter
import compose.icons.fontawesomeicons.brands.Youtube
import com.example.fridgeproject.ui.theme.SocialFacebookBackground
import com.example.fridgeproject.ui.theme.SocialFacebookText
import com.example.fridgeproject.ui.theme.SocialInstagramBackground
import com.example.fridgeproject.ui.theme.SocialInstagramText
import com.example.fridgeproject.ui.theme.SocialTikTokBackground
import com.example.fridgeproject.ui.theme.SocialTikTokText
import com.example.fridgeproject.ui.theme.SocialTwitterBackground
import com.example.fridgeproject.ui.theme.SocialTwitterText
import com.example.fridgeproject.ui.theme.SocialYouTubeBackground
import com.example.fridgeproject.ui.theme.SocialYouTubeText

enum class SocialPlatform(
    val baseUrl: String,
    val textColor: Color,
    val backgroundColor: Color,
    val icon: ImageVector
) {
    INSTAGRAM("https://www.instagram.com/", SocialInstagramText, SocialInstagramBackground, FontAwesomeIcons.Brands.Instagram),
    TWITTER("https://www.twitter.com/",     SocialTwitterText, SocialTwitterBackground, FontAwesomeIcons.Brands.Twitter),
    FACEBOOK("https://www.facebook.com/",   SocialFacebookText, SocialFacebookBackground, FontAwesomeIcons.Brands.Facebook),
    TIKTOK("https://www.tiktok.com/@",      SocialTikTokText, SocialTikTokBackground, FontAwesomeIcons.Brands.Tiktok),
    YOUTUBE("https://www.youtube.com/",     SocialYouTubeText, SocialYouTubeBackground, FontAwesomeIcons.Brands.Youtube);

    fun buildUrl(username: String): String = baseUrl + username
}
