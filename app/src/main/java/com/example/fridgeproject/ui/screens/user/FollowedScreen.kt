package com.example.fridgeproject.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.ui.components.FilterInputField
import com.example.fridgeproject.ui.components.user.UserProfileImage

@Composable
fun FollowedScreen(
    followed: List<UserProfile>,
    loggedUserFollowingIds : Set<String>,
    loggedUserId : String,
    searchQuery : String,
    onBackClick: () -> Unit,
    onFollowedProfileClick: (String) -> Unit,
    onFollowClick : (String, Boolean) -> Unit,
    onSearch : (String) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 8.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            FilterInputField(
                value = searchQuery,
                onValueChange = {onSearch(it)},
                placeholder = "Search by first name/nickname",
                icon = Icons.Default.Person
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // lista dei profili seguiti
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = PageBottomPadding)
        ) {
            followed.forEach { user ->
                val isFollowing = loggedUserFollowingIds.contains(user.id)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFollowedProfileClick(user.id) },
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            UserProfileImage(
                                false, false, user.profileImage,
                                {}, {}, firstName = user.firstName, lastName = user.lastName
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = user.firstName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.nickname,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if(user.id != loggedUserId) {
                            ElevatedButton(
                                onClick = { onFollowClick(user.id, isFollowing) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 2.dp
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(
                                    text = if (isFollowing) "Unfollow" else "Follow",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}