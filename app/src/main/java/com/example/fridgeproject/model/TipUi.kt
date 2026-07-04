package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.TipType

data class RecipeTipUi(
    val id: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val type: TipType = TipType.DO,
    val date: String = "",
    val comment: String = "",
    val userAvatarUrl: String? = null
)

data class UserTipUi(
    val id: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val recipeAuthorId: String = "",
    val recipeAuthorUsername: String = "",
    val type: TipType = TipType.DO,
    val date: String = "",
    val comment: String = ""
)

fun Tip.toRecipeTipUi(
    userName: String = "",
    firstName: String = "",
    lastName: String = "",
    userAvatarUrl: String? = null
): RecipeTipUi = RecipeTipUi(
    id = id,
    recipeId = recipeId,
    userId = userId,
    userName = userName,
    firstName = firstName,
    lastName = lastName,
    type = type,
    date = date,
    comment = comment,
    userAvatarUrl = userAvatarUrl
)

fun Tip.toUserTipUi(
    recipeTitle: String = "",
    recipeAuthorId: String = "",
    recipeAuthorUsername: String = ""
): UserTipUi = UserTipUi(
    id = id,
    recipeId = recipeId,
    recipeTitle = recipeTitle,
    recipeAuthorId = recipeAuthorId,
    recipeAuthorUsername = recipeAuthorUsername,
    type = type,
    date = date,
    comment = comment
)
