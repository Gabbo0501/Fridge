package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Cuisine
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.PrepTime
import com.example.fridgeproject.model.utils.toPrepTime

data class RecipeUi(
    val id: String = "",
    val authorId: String = "",
    val authorNickname: String = "",
    val remixedFromRecipeId: String? = null,
    val remixedFromRecipeTitle: String? = null,
    val remixedFromAuthorNickname: String? = null,
    val title: String = "",
    val description: String = "",
    val image: String? = "",
    val dishType: DishType = DishType.APPETIZER,
    val suitableDiets: List<Diet> = emptyList(),
    val cuisine: List<Cuisine> = emptyList(),
    val costRange: CostRange = CostRange.FIVE,
    val difficulty: Difficulty = Difficulty.ONE,
    val preparationTimeSec: Long = 0L,
    val rating: Float = 0f,
    val likes: Int = 0,
    val ingredients: List<IngredientQuantity> = emptyList(),
    val preparationSteps: List<RecipeStep> = emptyList(),
    val myReview: RecipeReviewUi? = null,
    val otherReviews: List<RecipeReviewUi> = emptyList(),
    val myTip: RecipeTipUi? = null,
    val otherTips: List<RecipeTipUi> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class RecipeShortUi(
    val id: String = "",
    val authorId: String = "",
    val authorNickname: String = "",
    val title: String = "",
    val image: String? = "",
    val dishType: DishType = DishType.APPETIZER,
    val suitableDiets: List<Diet> = emptyList(),
    val costRange: CostRange = CostRange.FIVE,
    val difficulty: Difficulty = Difficulty.ONE,
    val preparationTimeSec: Long = 0L,
    val cookingTimeCategory: PrepTime = PrepTime.SHORT,
    val rating: Float = 0f,
    val likes: Int = 0,
    val createdAt: Long = 0L
)

fun Recipe.toRecipeShortUi(authorNickname: String = author): RecipeShortUi {
    return RecipeShortUi(
        id = id,
        authorId = authorId,
        title = title,
        authorNickname = authorNickname,
        image = image,
        preparationTimeSec = preparationTimeSec,
        suitableDiets = suitableDiets,
        likes = likes,
        difficulty = difficulty,
        costRange = costRange,
        dishType = dishType,
        cookingTimeCategory = preparationTimeSec.toPrepTime()
    )
}

fun RecipeWithStats.toRecipeUi(
    authorNickname: String,
    remixedFromRecipeTitle: String?,
    remixedFromAuthorNickname: String?,
    myReview: RecipeReviewUi? = null,
    otherReviews: List<RecipeReviewUi> = emptyList(),
    myTip: RecipeTipUi? = null,
    otherTips: List<RecipeTipUi> = emptyList()
): RecipeUi = RecipeUi(
    id = recipe.id,
    authorId = recipe.authorId,
    authorNickname = authorNickname,
    remixedFromRecipeId = recipe.remixedFromRecipeId,
    remixedFromRecipeTitle = remixedFromRecipeTitle,
    remixedFromAuthorNickname = remixedFromAuthorNickname,
    title = recipe.title,
    description = recipe.description,
    image = recipe.image,
    dishType = recipe.dishType,
    suitableDiets = recipe.suitableDiets,
    cuisine = recipe.cuisine,
    costRange = recipe.costRange,
    difficulty = recipe.difficulty,
    preparationTimeSec = recipe.preparationTimeSec,
    rating = rating,
    likes = recipe.likes,
    ingredients = recipe.ingredients,
    preparationSteps = recipe.preparationSteps,
    myReview = myReview,
    otherReviews = otherReviews,
    myTip = myTip,
    otherTips = otherTips,
    createdAt = recipe.createdAt,
    updatedAt = recipe.updatedAt
)

fun RecipeWithStats.toRecipeShortUi(authorNickname: String = recipe.author): RecipeShortUi {
    return RecipeShortUi(
        id = recipe.id,
        authorId = recipe.authorId,
        title = recipe.title,
        authorNickname = authorNickname,
        image = recipe.image,
        rating = rating,
        preparationTimeSec = recipe.preparationTimeSec,
        suitableDiets = recipe.suitableDiets,
        likes = recipe.likes,
        difficulty = recipe.difficulty,
        costRange = recipe.costRange,
        dishType = recipe.dishType,
        cookingTimeCategory = recipe.preparationTimeSec.toPrepTime(),
        createdAt = recipe.createdAt
    )
}