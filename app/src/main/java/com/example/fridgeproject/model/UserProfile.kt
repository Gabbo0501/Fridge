package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.enums.Diet

data class UserProfile(
val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profileImage: ProfileImageSource = ProfileImageSource.Monogram,
    val nickname: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val socialProfiles: List<SocialProfile> = emptyList(),
    val shortBio: String? = null,
    val cookingRole: CookingRole = CookingRole.AMATEUR,
    val allergens: List<Ingredient> = emptyList(),
    val diet: Diet = Diet.OMNIVORE,
    val receiveNotification: Boolean =  false,
    val receiveLikeNotification: Boolean =  false,
    val receiveRemixNotification: Boolean =  false,
    val receiveNewFollowerNotification: Boolean =  false,
    val receiveNewRecipeNotification: Boolean =  false,
    val receiveReviewNotification: Boolean = false,
    val receiveTipNotification: Boolean = false
)