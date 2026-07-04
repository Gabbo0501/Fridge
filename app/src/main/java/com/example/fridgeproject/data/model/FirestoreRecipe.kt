package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.RecipeStep
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Cuisine
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure

data class FirestoreRecipe(
    val authorId: String = "",
    val remixedFromRecipeId: String? = null,
    val title: String = "",
    val description: String = "",
    val image: String? = null,
    val dishType: String = DishType.FIRST_COURSE.name,
    val suitableDiets: List<String> = emptyList(),
    val cuisine: List<String> = emptyList(),
    val costRange: String = CostRange.FIVE.name,
    val difficulty: String = Difficulty.ONE.name,
    val preparationTimeSec: Long = 0L,
    val likes: Int = 0,
    val ingredients: List<FirestoreIngredientQuantity> = emptyList(),
    val preparationSteps: List<FirestoreRecipeStep> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreIngredientQuantity(
    val name: String = "",
    val quantity: Float = 0f,
    val unit: String = UnitOfMeasure.G.name
)

data class FirestoreRecipeStep(
    val description: String = "",
    val image: String = ""
)

fun FirestoreRecipe.toDomain(id: String, authorNickname: String, ingredientsMap: Map<String, Ingredient>): Recipe =
    Recipe(
        id = id,
        authorId = authorId,
        author = authorNickname,
        remixedFromRecipeId = remixedFromRecipeId,
        title = title,
        description = description,
        image = image,
        dishType = dishType.toDishType(),
        suitableDiets = suitableDiets.map { it.toDiet() },
        cuisine = cuisine.map { it.toCuisine() },
        costRange = costRange.toCostRange(),
        difficulty = difficulty.toDifficulty(),
        preparationTimeSec = preparationTimeSec,
        likes = likes,
        ingredients = ingredients.map { it.toDomain(ingredientsMap) },
        preparationSteps = preparationSteps.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Recipe.toFirestore(): FirestoreRecipe =
    FirestoreRecipe(
        authorId = authorId,
        remixedFromRecipeId = remixedFromRecipeId,
        title = title,
        description = description,
        image = image,
        dishType = dishType.name,
        suitableDiets = suitableDiets.map { it.name },
        cuisine = cuisine.map { it.name },
        costRange = costRange.name,
        difficulty = difficulty.name,
        preparationTimeSec = preparationTimeSec,
        likes = likes,
        ingredients = ingredients.map { it.toFirestore() },
        preparationSteps = preparationSteps.map { it.toFirestore() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun FirestoreIngredientQuantity.toDomain(ingredientsMap: Map<String, Ingredient>): IngredientQuantity {
    val domainIngredient = ingredientsMap[name] ?: Ingredient(
        name = name,
        category = IngredientCategory.OTHERS,
        defaultUnit = unit.toUnitOfMeasure()
    )
    return IngredientQuantity(
        ingredient = domainIngredient,
        quantity = quantity,
        unit = unit.toUnitOfMeasure()
    )
}

private fun IngredientQuantity.toFirestore(): FirestoreIngredientQuantity =
    FirestoreIngredientQuantity(
        name = ingredient.name,
        quantity = quantity,
        unit = unit.name
    )

private fun FirestoreRecipeStep.toDomain(): RecipeStep =
    RecipeStep(
        description = description,
        image = image
    )

private fun RecipeStep.toFirestore(): FirestoreRecipeStep =
    FirestoreRecipeStep(
        description = description,
        image = image
    )

private fun String.toDishType(): DishType =
    try { DishType.valueOf(uppercase()) } catch (e: Exception) { DishType.FIRST_COURSE }

private fun String.toDiet(): Diet =
    try { Diet.valueOf(uppercase()) } catch (e: Exception) { Diet.OMNIVORE }

private fun String.toCuisine(): Cuisine =
    try { Cuisine.valueOf(uppercase()) } catch (e: Exception) { Cuisine.ITALIAN }

private fun String.toCostRange(): CostRange =
    try { CostRange.valueOf(uppercase()) } catch (e: Exception) { CostRange.FIVE }

private fun String.toDifficulty(): Difficulty =
    try { Difficulty.valueOf(uppercase()) } catch (e: Exception) { Difficulty.ONE }

private fun String.toUnitOfMeasure(): UnitOfMeasure =
    try { UnitOfMeasure.valueOf(uppercase()) } catch (e: Exception) { UnitOfMeasure.G }