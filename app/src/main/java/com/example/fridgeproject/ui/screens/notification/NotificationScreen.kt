package com.example.fridgeproject.ui.screens.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.NotificationUi
import com.example.fridgeproject.model.enums.NotificationType
import com.example.fridgeproject.ui.components.EmptyStateComponent
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.review_and_tip.AuthorAvatar
import com.example.fridgeproject.viewmodel.notification.NotificationUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(
    uiState: NotificationUiState,
    onBackClick: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onDeleteNotification: (String) -> Unit,
    onClearAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                title = "Notifications",
                onBackClick = onBackClick,
                bottomPadding = 12.dp
            ) {
                if (uiState.unreadNotifications.isNotEmpty() || uiState.readNotifications.isNotEmpty()) {
                    IconButton(onClick = onClearAll) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        when {
            uiState.globalError.isNotBlank() -> item {
                Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                    ErrorComponent(uiState.globalError)
                }
            }
            uiState.isLoading -> item {
                Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                    LoadingComponent()
                }
            }
            uiState.unreadNotifications.isEmpty() && uiState.readNotifications.isEmpty() -> {
                item {
                    EmptyStateComponent(
                        message = "You don't have notifications yet.",
                        icon = Icons.Default.Notifications,
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = PageBottomPadding)
                    )
                }
            }
            else -> {
                itemsIndexed(
                    items = uiState.unreadNotifications,
                    key = { _, notification -> notification.id }
                ) { index, notification ->
                    Box(
                        modifier = Modifier.padding(
                            bottom = if (uiState.readNotifications.isEmpty() && index == uiState.unreadNotifications.lastIndex) {
                                PageBottomPadding
                            } else {
                                0.dp
                            }
                        )
                    ) {
                        NotificationItem(
                            notification = notification,
                            isRead = false,
                            onMarkAsRead = { onMarkAsRead(notification.id) },
                            onUserClick = { onUserClick(notification.triggerUserId) },
                            onRecipeClick = { notification.recipeId?.let { onRecipeClick(it) } },
                            onDelete = { onDeleteNotification(notification.id) }
                        )
                    }
                }
                itemsIndexed(
                    items = uiState.readNotifications,
                    key = { _, notification -> notification.id }
                ) { index, notification ->
                    Box(
                        modifier = Modifier.padding(
                            bottom = if (index == uiState.readNotifications.lastIndex) PageBottomPadding else 0.dp
                        )
                    ) {
                        NotificationItem(
                            notification = notification,
                            isRead = true,
                            onMarkAsRead = { onMarkAsRead(notification.id) },
                            onUserClick = { onUserClick(notification.triggerUserId) },
                            onRecipeClick = { notification.recipeId?.let { onRecipeClick(it) } },
                            onDelete = { onDeleteNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationUi,
    isRead: Boolean,
    onMarkAsRead: () -> Unit,
    onUserClick: () -> Unit,
    onRecipeClick: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (!isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val tagUser = "user"
    val tagRecipe = "recipe"

    val actionText = when (notification.type) {
        NotificationType.LIKE -> " liked your recipe: "
        NotificationType.REMIX -> " remixed your recipe: "
        NotificationType.REVIEW -> " reviewed your recipe: "
        NotificationType.TIP -> " added a tip to your recipe: "
        NotificationType.NEW_FOLLOWER -> " started following you"
        NotificationType.NEW_RECIPE -> " shared a new recipe: "
        NotificationType.FAVORITE_UPDATED -> " updated a recipe you like: "
        NotificationType.FAVORITE_REMOVED -> " removed a recipe you liked: "
        NotificationType.RECOMMENDED_RECIPE -> " published a new recipe you might like: "
    }

    val annotatedText = buildAnnotatedString {

        pushStringAnnotation(tag = tagUser, annotation = tagUser)
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)) {
            append(notification.triggerUsername)
        }
        pop()

        withStyle(SpanStyle(color = onSurfaceVariantColor)) {
            append(actionText)
        }

        if (notification.recipeTitle != null) {
            val recipeColor = if (notification.type == NotificationType.FAVORITE_REMOVED)
                onSurfaceColor else primaryColor

            if (notification.type != NotificationType.FAVORITE_REMOVED) {
                pushStringAnnotation(tag = tagRecipe, annotation = tagRecipe)
            }
            withStyle(SpanStyle(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = recipeColor
            )) {
                append(notification.recipeTitle)
            }
            if (notification.type != NotificationType.FAVORITE_REMOVED) {
                pop()
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isRead) onMarkAsRead() },
        shape = RoundedCornerShape(28.dp),
        color = backgroundColor,
        border = if (!isRead) BorderStroke(1.dp, primaryColor) else null,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthorAvatar(
                    avatarUrl = notification.triggerUserAvatarUrl,
                    firstName = notification.triggerFirstName,
                    lastName = notification.triggerLastName,
                    size = 56.dp,
                    contentDescription = "User avatar",
                    modifier = Modifier.clickable { onUserClick() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tagUser, offset, offset)
                                .firstOrNull()?.let { onUserClick() }
                            annotatedText.getStringAnnotations(tagRecipe, offset, offset)
                                .firstOrNull()?.let { onRecipeClick() }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariantColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!isRead) {
                    TextButton(
                        onClick = onMarkAsRead,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mark as read",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete notification",
                        modifier = Modifier.size(18.dp),
                        tint = onSurfaceVariantColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> if (minutes == 1L) "1 min ago" else "$minutes mins ago"
        hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
