package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.enums.SocialPlatform


@Composable
fun UserSocialSelector(
    modifier: Modifier = Modifier,
    socialProfiles: List<SocialProfile>,
    onSocialProfilesChange: (List<SocialProfile>) -> Unit,
    socialProfilesError: String = "",

) {
    // piattaforme già usate
    val usedPlatforms = socialProfiles.map { it.platform }.toSet()
    // piattaforme ancora disponibili
    val availablePlatforms = SocialPlatform.entries.filter { it !in usedPlatforms }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Social Networks",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // riga per ogni social già aggiunto
        socialProfiles.forEach { social ->
            SocialRow(
                social = social,
                error = socialProfilesError,
                onUsernameChange = { newUsername ->
                    onSocialProfilesChange(
                        socialProfiles.map {
                            if (it.platform == social.platform) it.copy(username = newUsername) else it
                        }
                    )
                },
                onRemove = {
                    onSocialProfilesChange(socialProfiles.filter { it.platform != social.platform })
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // bottone aggiungi (visibile solo se ci sono piattaforme disponibili)
        if (availablePlatforms.isNotEmpty()) {
            Box {
                TextButton(
                    onClick = { dropdownExpanded = !dropdownExpanded }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add social",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Social")
                }
                DropdownMenu(
                    modifier = Modifier.background(Color.White),
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }

                ) {
                    availablePlatforms.forEach { platform ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = platform.icon,
                                        contentDescription = platform.name,
                                        tint = platform.textColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(platform.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            },
                            onClick = {
                                onSocialProfilesChange(
                                    socialProfiles + SocialProfile(platform, "")
                                )
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (socialProfilesError.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = socialProfilesError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SocialRow(
    social: SocialProfile,
    error: String,
    onUsernameChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = social.platform.icon,
            contentDescription = social.platform.name,
            tint = social.platform.textColor,
            modifier = Modifier.size(28.dp)
        )
        BasicTextField(
            value = social.username,
            onValueChange = onUsernameChange,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = if (error.isNotEmpty()) 2.dp else 1.dp,
                            color = if (error.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    innerTextField()
                }
            }
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}