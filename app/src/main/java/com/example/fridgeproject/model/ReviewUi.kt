package com.example.fridgeproject.model

data class RecipeReviewUi(
    val id: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val stars: Int = 0,
    val date: String = "",
    val comment: String = "",
    val images: List<StoredImage> = emptyList(),
    val userAvatarUrl: String? = null
)

data class UserReviewUi(
    val id: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val recipeAuthorId: String = "",
    val recipeAuthorUsername: String = "",
    val stars: Int = 0,
    val date: String = "",
    val comment: String = "",
    val images: List<StoredImage> = emptyList()
)

fun Review.toRecipeReviewUi(
    userName: String = "",
    firstName: String = "",
    lastName: String = "",
    userAvatarUrl: String? = null
): RecipeReviewUi = RecipeReviewUi(
    id = id,
    recipeId = recipeId,
    userId = userId,
    userName = userName,
    firstName = firstName,
    lastName = lastName,
    stars = stars,
    date = date,
    comment = comment,
    images = images,
    userAvatarUrl = userAvatarUrl
)

fun Review.toUserReviewUi(
    recipeTitle: String = "",
    recipeAuthorId: String = "",
    recipeAuthorUsername: String = ""
): UserReviewUi = UserReviewUi(
    id = id,
    recipeId = recipeId,
    recipeTitle = recipeTitle,
    recipeAuthorId = recipeAuthorId,
    recipeAuthorUsername = recipeAuthorUsername,
    stars = stars,
    date = date,
    comment = comment,
    images = images
)