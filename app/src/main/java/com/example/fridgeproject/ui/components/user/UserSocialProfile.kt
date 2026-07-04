package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.SocialProfile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun UserSocialProfiles(
    socialProfiles: List<SocialProfile>,
    modifier: Modifier = Modifier
) {
    if (socialProfiles.isEmpty()) return

    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            socialProfiles.forEach { social ->
                IconButton(
                    onClick = { uriHandler.openUri(social.platform.buildUrl(social.username)) }
                ) {
                    Icon(
                        imageVector = social.platform.icon,
                        contentDescription = social.platform.name,
                        tint = social.platform.textColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}