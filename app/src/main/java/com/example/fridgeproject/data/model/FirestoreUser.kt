package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.SocialPlatform
import com.example.fridgeproject.model.enums.UnitOfMeasure

data class FirestoreUser(
    val firstName: String = "",
    val lastName: String = "",
    val profileImage: FirestoreProfileImageSource = FirestoreProfileImageSource(),
    val nickname: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val socialProfiles: List<FirestoreSocialProfile> = emptyList(),
    val shortBio: String? = null,
    val cookingRole: String = CookingRole.AMATEUR.name,
    val allergens: List<String> = emptyList(),
    val diet: String = Diet.OMNIVORE.name,
    val receiveNotification: Boolean = false,
    val receiveLikeNotification: Boolean = false,
    val receiveRemixNotification: Boolean = false,
    val receiveNewFollowerNotification: Boolean = false,
    val receiveNewRecipeNotification: Boolean = false,
    val receiveReviewNotification : Boolean = false,
    val receiveTipNotification: Boolean = false
)

data class FirestoreSocialProfile(
    val platform: String = SocialPlatform.INSTAGRAM.name,
    val username: String = ""
)

fun FirestoreUser.toDomain(id: String, ingredientsMap: Map<String, Ingredient>): UserProfile =
    UserProfile(
        id = id,
        firstName = firstName,
        lastName = lastName,
        profileImage = profileImage.toDomain(),
        nickname = nickname,
        email = email,
        phoneNumber = phoneNumber,
        socialProfiles = socialProfiles.map { it.toDomain() },
        shortBio = shortBio,
        cookingRole = cookingRole.toCookingRole(),
        allergens = allergens.map { name ->
            ingredientsMap[name] ?: Ingredient(
                name = name,
                category = IngredientCategory.OTHERS,
                defaultUnit = UnitOfMeasure.G
            )
        },
        diet = diet.toDiet(),
        receiveNotification = receiveNotification,
        receiveLikeNotification = receiveLikeNotification,
        receiveRemixNotification = receiveRemixNotification,
        receiveNewFollowerNotification = receiveNewFollowerNotification,
        receiveNewRecipeNotification = receiveNewRecipeNotification,
        receiveReviewNotification = receiveReviewNotification,
        receiveTipNotification = receiveTipNotification
    )

fun UserProfile.toFirestore(): FirestoreUser =
    FirestoreUser(
        firstName = firstName,
        lastName = lastName,
        profileImage = profileImage.toFirestore(),
        nickname = nickname,
        email = email,
        phoneNumber = phoneNumber,
        socialProfiles = socialProfiles.map { it.toFirestore() },
        shortBio = shortBio,
        cookingRole = cookingRole.name,
        allergens = allergens.map { it.name },
        diet = diet.name,
        receiveNotification = receiveNotification,
        receiveLikeNotification = receiveLikeNotification,
        receiveRemixNotification = receiveRemixNotification,
        receiveNewFollowerNotification = receiveNewFollowerNotification,
        receiveNewRecipeNotification = receiveNewRecipeNotification,
        receiveReviewNotification = receiveReviewNotification,
        receiveTipNotification = receiveTipNotification
    )

private fun FirestoreSocialProfile.toDomain(): SocialProfile =
    SocialProfile(
        platform = platform.toSocialPlatform(),
        username = username
    )

private fun SocialProfile.toFirestore(): FirestoreSocialProfile =
    FirestoreSocialProfile(
        platform = platform.name,
        username = username
    )

private fun String.toCookingRole(): CookingRole =
    try {
        CookingRole.valueOf(uppercase())
    } catch (e: Exception) {
        CookingRole.AMATEUR
    }

private fun String.toDiet(): Diet =
    try {
        Diet.valueOf(uppercase())
    } catch (e: Exception) {
        Diet.OMNIVORE
    }

private fun String.toSocialPlatform(): SocialPlatform =
    try {
        SocialPlatform.valueOf(uppercase())
    } catch (e: Exception) {
        SocialPlatform.INSTAGRAM
    }
