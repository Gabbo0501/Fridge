package com.example.fridgeproject.ui.screens.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader

@Composable
fun NotificationSettingsScreen(
    receiveNotification: Boolean,
    receiveLikeNotification: Boolean,
    receiveRemixNotification: Boolean,
    receiveNewFollowerNotification: Boolean,
    receiveNewRecipeNotification: Boolean,
    receiveReviewNotification: Boolean,
    receiveTipNotification: Boolean,
    error: String,
    toggleReceiveNotification: () -> Unit,
    toggleReceiveLikeNotification: () -> Unit,
    toggleReceiveRemixNotification: () -> Unit,
    toggleReceiveNewFollowerNotification: () -> Unit,
    toggleReceiveNewRecipeNotification: () -> Unit,
    toggleReceiveReviewNotification: () -> Unit,
    toggleReceiveTipNotification : () -> Unit,
    saveNotificationSettings: () -> Unit,
    onBackClick: () -> Unit
){
    if (error.isNotBlank()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        return
    }else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 25.dp, top = 8.dp, end = 25.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PageHeader(title = "Notifications Settings", onBackClick = onBackClick)

            val switchColors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Receive notifications",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enable or disable all push notifications",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = receiveNotification,
                        onCheckedChange = { toggleReceiveNotification() },
                        colors = switchColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "PREFERENZE ATTIVITÀ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            val subItemsEnabled = receiveNotification

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = PageBottomPadding),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subItemsEnabled) 1f else 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (subItemsEnabled) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Receive notifications for new recipes by followed author",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (subItemsEnabled) 1f else 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = receiveNewRecipeNotification && subItemsEnabled,
                        onCheckedChange = { toggleReceiveNewRecipeNotification() },
                        enabled = subItemsEnabled,
                        colors = switchColors
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subItemsEnabled) 1f else 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (subItemsEnabled) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Receive like notifications",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (subItemsEnabled) 1f else 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = receiveLikeNotification && subItemsEnabled,
                        onCheckedChange = { toggleReceiveLikeNotification() },
                        enabled = subItemsEnabled,
                        colors = switchColors
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subItemsEnabled) 1f else 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (subItemsEnabled) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Receive notifications for duplicated recipes",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (subItemsEnabled) 1f else 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = receiveRemixNotification && subItemsEnabled,
                        onCheckedChange = { toggleReceiveRemixNotification() },
                        enabled = subItemsEnabled,
                        colors = switchColors
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subItemsEnabled) 1f else 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (subItemsEnabled) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Receive notifications for new follower",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (subItemsEnabled) 1f else 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = receiveNewFollowerNotification && subItemsEnabled,
                        onCheckedChange = { toggleReceiveNewFollowerNotification() },
                        enabled = subItemsEnabled,
                        colors = switchColors
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subItemsEnabled) 1f else 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (subItemsEnabled) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Receive notifications for new reviews",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (subItemsEnabled) 1f else 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = receiveReviewNotification && subItemsEnabled,
                        onCheckedChange = { toggleReceiveReviewNotification() },
                        enabled = subItemsEnabled,
                        colors = switchColors
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subItemsEnabled) 1f else 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (subItemsEnabled) 1f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Receive notifications for new tips",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (subItemsEnabled) 1f else 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = receiveTipNotification && subItemsEnabled,
                        onCheckedChange = { toggleReceiveTipNotification() },
                        enabled = subItemsEnabled,
                        colors = switchColors
                    )
                }
            }
        }
    }
}