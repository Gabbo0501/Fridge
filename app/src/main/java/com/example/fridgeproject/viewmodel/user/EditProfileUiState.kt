package com.example.fridgeproject.viewmodel.user

import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.model.enums.CookingRole

data class EditProfileErrors(
    val globalError: String = "",
    val nickname: String = "",
    val shortBio: String = "",
    val phoneNumber: String = "",
    val socialProfiles: String = "")

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val profileImage : ProfileImageSource = ProfileImageSource.Monogram,
    val nickname: String = "",
    val email: String = "",
    val shortBio: String? = null,
    val phoneNumber: String? = null,
    val socialProfiles: List<SocialProfile> = emptyList(),
    val cookingRole: CookingRole = CookingRole.AMATEUR,
    val success: Boolean = false,
    val isLoading: Boolean = false,
    val showExitDialog: Boolean = false,
    val errors: EditProfileErrors = EditProfileErrors()
)

fun EditProfileUiState.canCheckUnsavedChanges(): Boolean =
    !isLoading && !success && errors.globalError.isBlank()

fun EditProfileUiState.hasChangesFrom(original: UserProfile): Boolean =
    profileImage != original.profileImage ||
            nickname != original.nickname ||
            shortBio != original.shortBio ||
            phoneNumber != original.phoneNumber ||
            socialProfiles != original.socialProfiles ||
            cookingRole != original.cookingRole