package com.example.fridgeproject.data

import android.content.Context
import com.example.fridgeproject.domain.AuthRepository
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.FollowerRepository
import com.example.fridgeproject.data.useCase.MoveIngredientsToFridgeUseCase
import com.example.fridgeproject.data.useCase.SaveMissingIngredientInGroceryList
import com.example.fridgeproject.domain.FridgeRepository
import com.example.fridgeproject.domain.ImageStorageRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.domain.GroceryListRepository
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.domain.TipRepository
import com.example.fridgeproject.domain.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

interface AppContainer {
    val userRepository: UserRepository
    val recipeRepository: RecipeRepository
    val recipeWithStatsRepository: RecipeWithStatsRepository
    val collectionRepository: CollectionRepository
    val reviewRepository: ReviewRepository
    val tipRepository: TipRepository
    val notificationRepository: NotificationRepository
    val imageStorageRepository: ImageStorageRepository
    val authRepository: AuthRepository
    val fridgeRepository: FridgeRepository
    val groceryListRepository: GroceryListRepository

    val ingredientRepository: IngredientRepository
    val followerRepository: FollowerRepository

    val moveIngredientsToFridgeUseCase: MoveIngredientsToFridgeUseCase
    val saveMissingIngredientInGroceryList : SaveMissingIngredientInGroceryList
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }


    override val userRepository: UserRepository by lazy {
        FirestoreUserRepository(
            firestore = firestore,
            imageStorageRepository = imageStorageRepository,
            collectionRepository = collectionRepository,
            ingredientRepository = ingredientRepository
        )
    }

    override val recipeRepository: RecipeRepository by lazy {
        FirestoreRecipeRepository(
            firestore = firestore,
            authRepository = SessionManagerFacade,
            imageStorageRepository = imageStorageRepository,
            notificationRepository = notificationRepository,
            ingredientRepository = ingredientRepository
        )
    }

    override val recipeWithStatsRepository: RecipeWithStatsRepository by lazy {
        FirestoreRecipeWithStatsRepository(
            recipeRepository = recipeRepository,
            reviewRepository = reviewRepository
        )
    }

    override val collectionRepository: CollectionRepository by lazy {
        FirestoreCollectionRepository(firestore, notificationRepository = notificationRepository)
    }

    override val reviewRepository: ReviewRepository by lazy {
        FirestoreReviewRepository(
            firestore = firestore,
            imageStorageRepository = imageStorageRepository,
            notificationRepository = notificationRepository
        )
    }

    override val tipRepository: TipRepository by lazy {
        FirestoreTipRepository(firestore,
            notificationRepository = notificationRepository)
    }

    override val notificationRepository: NotificationRepository by lazy {
        FirestoreNotificationRepository(firestore)
    }

    override val imageStorageRepository: ImageStorageRepository by lazy {
        FirebaseImageStorageRepository(context, storage)
    }

    override val followerRepository: FollowerRepository by lazy{
        FirestoreFollowerRepository(firestore, notificationRepository)
    }

    override val authRepository: AuthRepository
        get() = SessionManagerFacade

    override val fridgeRepository: FridgeRepository by lazy {
        FirestoreFridgeRepository(
            firestore = firestore,
            authRepository = authRepository,
            ingredientRepository = ingredientRepository
        )
    }
    override val groceryListRepository: GroceryListRepository by lazy {
        FirestoreGroceryListRepository(
            firestore = firestore,
            authRepository = authRepository,
            ingredientRepository = ingredientRepository
        )
    }

    override val ingredientRepository: IngredientRepository by lazy {
        FirestoreIngredientRepository(firestore)
    }

    override val moveIngredientsToFridgeUseCase: MoveIngredientsToFridgeUseCase by lazy {
        MoveIngredientsToFridgeUseCase(
            firestore = firestore,
            authRepository = authRepository
        )
    }
    override val saveMissingIngredientInGroceryList: SaveMissingIngredientInGroceryList by lazy {
        SaveMissingIngredientInGroceryList(
            firestore = firestore,
            authRepository = authRepository
        )
    }
}
