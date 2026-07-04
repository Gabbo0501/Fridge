package com.example.fridgeproject.ui.components.wizard.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.user.UserBioEditor
import com.example.fridgeproject.ui.components.user.UserCookingRoleSelector
import com.example.fridgeproject.ui.components.user.UserProfileImage
import com.example.fridgeproject.ui.components.user.UserSocialSelector
import com.example.fridgeproject.ui.components.wizard.shared.WizardButton
import com.example.fridgeproject.ui.components.wizard.shared.WizardProgressHeader

@Composable
fun WizardProfileStep(
    firstName: String,
    lastName: String,
    profileImage: ProfileImageSource,
    cookingRole: CookingRole,
    shortBio: String,
    socialProfiles: List<SocialProfile>,
    onProfileCameraClick: () -> Unit,
    onProfileGalleryClick: () -> Unit,
    onRemoveProfileImageClick: () -> Unit,
    onCookingRoleChange: (CookingRole) -> Unit,
    onShortBioChange: (String) -> Unit,
    onSocialProfilesChange: (List<SocialProfile>) -> Unit,
    onActionClick: () -> Unit,
    stepCount: Int,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onSkipClick: (() -> Unit)? = null,
    isActionEnabled: Boolean = true
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues()
    ) {
        item {
            WizardProgressHeader(
                title = "Setup",
                stepIndex = 1,
                stepCount = stepCount,
                onBack = onBackClick,
                rightTextAction = if (onSkipClick != null) "Skip" else null,
                onRightTextAction = onSkipClick,
                sideSlotSize = 40.dp,
                horizontalPadding = 12.dp,
                verticalPadding = 10.dp
            )
        }

        item {
            Text(
                text = "Complete Profile",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        item {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            UserProfileImage(
                isOwner = true,
                isEditing = true,
                imageSource = profileImage,
                onCameraClick = onProfileCameraClick,
                onGalleryClick = onProfileGalleryClick,
                onRemoveClick = onRemoveProfileImageClick,
                firstName = firstName,
                lastName = lastName
            )
            }
        }
        item {
            UserCookingRoleSelector(
                selected = cookingRole,
                onSelect = onCookingRoleChange,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        item {
            UserBioEditor(
                value = shortBio,
                onValueChange = onShortBioChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .padding(horizontal = 24.dp),
            )
        }
        item {
            UserSocialSelector(
                socialProfiles = socialProfiles,
                onSocialProfilesChange = onSocialProfilesChange,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            WizardButton(
                enabled = isActionEnabled,
                onClick = onActionClick,
                text = "Next",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = PageBottomPadding)
            )
        }
    }
}