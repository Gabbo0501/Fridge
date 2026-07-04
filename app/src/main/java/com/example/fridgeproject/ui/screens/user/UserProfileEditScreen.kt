package com.example.fridgeproject.ui.screens.user

import androidx.compose.foundation.background
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.PageHeader
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.user.UserCookingRoleSelector
import com.example.fridgeproject.ui.components.user.UserBioEditor
import com.example.fridgeproject.ui.components.user.UserHeaderEdit
import com.example.fridgeproject.ui.components.user.UserTextualForm
import com.example.fridgeproject.ui.components.user.UserSocialSelector
import com.example.fridgeproject.ui.components.recipe.BottomActionRow
import com.example.fridgeproject.viewmodel.user.EditProfileErrors


@Composable
fun UserProfileEditScreen(
    firstName: String,
    lastName: String,
    profileImage: ProfileImageSource,
    nickname: String,
    email: String,
    shortBio: String?,
    phoneNumber: String?,
    socialProfiles: List<SocialProfile>,
    cookingRole: CookingRole,
    errors: EditProfileErrors,
    isLoading: Boolean,
    success: Boolean,
    showExitDialog: Boolean,
    onBackClick: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onSaveClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onShortBioChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSocialProfilesChange: (List<SocialProfile>) -> Unit,
    onCookingRoleChange: (CookingRole) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveImageClick: () -> Unit
) {
    if (showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    val currentOrientation = LocalConfiguration.current.orientation
    val isHorizontal = currentOrientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(success) {
        if (success) {
            onBackClick()
        }
    }
    if (errors.globalError.isNotBlank()) {
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
            Text(errors.globalError, color = MaterialTheme.colorScheme.error)
        }
        return
    } else if (isLoading) {
        LoadingComponent()
    } else{

        if (isHorizontal) {
            UserProfileEditHorizontal(
                firstName = firstName,
                lastName = lastName,
                profileImage = profileImage,
                nickname = nickname,
                email = email,
                shortBio = shortBio,
                phoneNumber = phoneNumber,
                socialProfiles = socialProfiles,
                cookingRole = cookingRole,
                errors = errors,
                onBackClick = onBackClick,
                onSaveClick = onSaveClick,
                onNicknameChange = onNicknameChange,
                onShortBioChange = onShortBioChange,
                onPhoneNumberChange = onPhoneNumberChange,
                onSocialProfilesChange = onSocialProfilesChange,
                onCookingRoleChange = onCookingRoleChange,
                onCameraClick = onCameraClick,
                onGalleryClick = onGalleryClick,
                onRemoveImageClick = onRemoveImageClick,
            )
        } else {
            UserProfileEditVertical(
                firstName = firstName,
                lastName = lastName,
                profileImage = profileImage,
                nickname = nickname,
                email = email,
                shortBio = shortBio,
                phoneNumber = phoneNumber,
                socialProfiles = socialProfiles,
                cookingRole = cookingRole,
                errors = errors,
                onBackClick = onBackClick,
                onSaveClick = onSaveClick,
                onNicknameChange = onNicknameChange,
                onShortBioChange = onShortBioChange,
                onPhoneNumberChange = onPhoneNumberChange,
                onSocialProfilesChange = onSocialProfilesChange,
                onCookingRoleChange = onCookingRoleChange,
                onCameraClick = onCameraClick,
                onGalleryClick = onGalleryClick,
                onRemoveImageClick = onRemoveImageClick,
            )
        }
    }
}

@Composable
fun UserProfileEditVertical(
    firstName: String,
    lastName: String,
    profileImage: ProfileImageSource,
    nickname: String,
    email: String,
    shortBio: String?,
    phoneNumber: String?,
    socialProfiles: List<SocialProfile>,
    cookingRole: CookingRole,
    errors: EditProfileErrors,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onShortBioChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSocialProfilesChange: (List<SocialProfile>) -> Unit,
    onCookingRoleChange: (CookingRole) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveImageClick: () -> Unit
) {


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        item {
            PageHeader(onBackClick = onBackClick) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PageBottomPadding),
                horizontalAlignment = Alignment.Start
            ) {
                UserHeaderEdit(
                    imageSource = profileImage,
                    firstName = firstName,
                    lastName = lastName,
                    onCameraClick = onCameraClick,
                    onGalleryClick = onGalleryClick,
                    onRemoveImageClick = onRemoveImageClick
                )
                Spacer(modifier = Modifier.height(25.dp))
                UserTextualForm(
                    nickname = nickname,
                    email = email,
                    phoneNumber = phoneNumber,
                    errors = errors,
                    onNicknameChange = onNicknameChange,
                    onPhoneNumberChange = onPhoneNumberChange
                )
                UserBioEditor(
                    value = shortBio ?: "",
                    onValueChange = onShortBioChange,
                    error = errors.shortBio
                )
                Spacer(modifier = Modifier.height(25.dp))
                UserSocialSelector(
                    socialProfiles = socialProfiles,
                    onSocialProfilesChange = onSocialProfilesChange,
                    socialProfilesError = errors.socialProfiles
                )
                Spacer(modifier = Modifier.height(25.dp))
                UserCookingRoleSelector(
                    selected = cookingRole,
                    onSelect = onCookingRoleChange
                )
                Spacer(modifier = Modifier.height(25.dp))
                BottomActionRow(
                    cancel = onBackClick,
                    save = onSaveClick,
                    saveLabel = "Save Profile"
                )
            }
        }
    }
}

@Composable
fun UserProfileEditHorizontal(
    firstName: String,
    lastName: String,
    profileImage: ProfileImageSource,
    nickname: String,
    email: String,
    shortBio: String?,
    phoneNumber: String?,
    socialProfiles: List<SocialProfile>,
    cookingRole: CookingRole,
    errors: EditProfileErrors,
    onBackClick: ()-> Unit,
    onSaveClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onShortBioChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSocialProfilesChange: (List<SocialProfile>) -> Unit,
    onCookingRoleChange: (CookingRole) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveImageClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 25.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        item {
            PageHeader(onBackClick = onBackClick) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserHeaderEdit(
                        imageSource = profileImage,
                        firstName = firstName,
                        lastName = lastName,
                        onCameraClick = onCameraClick,
                        onGalleryClick = onGalleryClick,
                        onRemoveImageClick = onRemoveImageClick
                    )
                }
                Spacer(modifier = Modifier.width(30.dp))
                Column(modifier = Modifier.weight(1.5f)) {
                    UserTextualForm(
                        nickname = nickname,
                        email = email,
                        phoneNumber = phoneNumber,
                        errors = errors,
                        onNicknameChange = onNicknameChange,
                        onPhoneNumberChange = onPhoneNumberChange
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item {
            UserBioEditor(
                value = shortBio ?: "",
                onValueChange = onShortBioChange,
                error = errors.shortBio
            )
        }
        item { Spacer(modifier = Modifier.height(25.dp)) }
        item {
            UserSocialSelector(
                socialProfiles = socialProfiles,
                onSocialProfilesChange = onSocialProfilesChange,
                socialProfilesError = errors.socialProfiles
            )
        }
        item {Spacer(modifier = Modifier.height(25.dp))}
        item {
            Box(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                Column {
                    UserCookingRoleSelector(
                        selected = cookingRole,
                        onSelect = onCookingRoleChange
                    )
                    Spacer(modifier = Modifier.height(25.dp))
                    BottomActionRow(
                        cancel = onBackClick,
                        save = onSaveClick,
                        saveLabel = "Save Profile"
                    )
                }
            }
        }
    }
}