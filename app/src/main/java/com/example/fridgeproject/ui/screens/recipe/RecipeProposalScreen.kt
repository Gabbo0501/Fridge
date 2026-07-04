package com.example.fridgeproject.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.example.fridgeproject.ui.components.AlertDialog
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.IngredientListItem
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.ErrorComponent
import com.example.fridgeproject.ui.components.recipe.*
import com.example.fridgeproject.ui.components.CustomHorizontalDivider
import com.example.fridgeproject.viewmodel.recipe.RecipeUiState


@Composable
fun RecipeProposalScreen(
    modifier: Modifier = Modifier,
    uiState: RecipeUiState,
    isLoggedIn: Boolean,
    onEditClick: () -> Unit,
    onRemixClick: () -> Unit,
    onExpandReviewList: () -> Unit,
    onAddReviewClick: () -> Unit,
    onExpandTipsList: () -> Unit,
    onAddTipClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteReviewClick: (String) -> Unit,
    onDeleteReviewConfirm: () -> Unit,
    onDeleteReviewDismiss: () -> Unit,
    onDeleteTipClick: (String) -> Unit,
    onDeleteTipConfirm: () -> Unit,
    onDeleteTipDismiss: () -> Unit,
    onLikeToggle: () -> Unit,
    onSaveClick: () -> Unit,
    addMissingIngredientsToGroceryList: () -> Unit
) {
    if (uiState.deleteDialog.showDeleteRecipeDialog) {
        AlertDialog(
            title = "Delete Recipe?",
            text = "Are you sure you want to delete this recipe? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = colorScheme.error,
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss
        )
    }

    if (uiState.deleteDialog.reviewToDeleteId != null) {
        AlertDialog(
            title = "Delete review?",
            text = "Are you sure you want to delete this review? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = colorScheme.error,
            onConfirm = onDeleteReviewConfirm,
            onDismiss = onDeleteReviewDismiss
        )
    }

    if (uiState.deleteDialog.tipToDeleteId != null) {
        AlertDialog(
            title = "Delete tip?",
            text = "Are you sure you want to delete this tip? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            confirmTextColor = colorScheme.error,
            onConfirm = onDeleteTipConfirm,
            onDismiss = onDeleteTipDismiss
        )
    }

    when {
        uiState.isLoading -> LoadingComponent()
        uiState.error != null -> ErrorComponent(uiState.error)

        uiState.content.recipe != null -> {
            val recipe = uiState.content.recipe

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(start = 25.dp, top = 15.dp, end = 25.dp)
            ) {
                item {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.primary
                            )
                        }
                        if (uiState.permissions.isOwner) {
                            Row {
                                IconButton(onEditClick) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = colorScheme.primary
                                    )
                                }
                                IconButton(onDeleteClick) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Header Image
                item {
                    recipe.image?.let {
                        RecipeHeaderImage(it)
                    }
                }

                // Titolo e Autore
                item {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = colorScheme.onSurfaceVariant)) { append("by ") }
                                withStyle(
                                    style = SpanStyle(
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(recipe.authorNickname)
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onAuthorClick(recipe.authorId) }
                                .padding(2.dp)
                        )
                    }
                }

                item { RecipeDescriptionSection(recipe.description) }

                item { CustomHorizontalDivider() }

                // Stats & Info
                item {
                    RecipeActionsBar(
                        rating = recipe.rating,
                        likes = recipe.likes,
                        isFavorite = uiState.content.isFavorite,
                        onRemixClick = onRemixClick,
                        onLikeToggle = onLikeToggle,
                        onSaveClick = onSaveClick
                    )
                }
                item {
                    RecipeTagsRow(
                        recipe.dishType,
                        recipe.suitableDiets,
                        recipe.cuisine,
                        false
                    )
                }
                item {
                    RecipeStatsRow(
                        recipe.difficulty,
                        recipe.preparationTimeSec,
                        recipe.costRange
                    )
                }

                // Sezione Frigo
                if(isLoggedIn) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "Fridge compatibility",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            FridgePercentageSection(
                                percentage = uiState.content.percentageFridge,
                                missingIngredients = uiState.content.missingIngredients,
                                missingQuantityIngredients = uiState.content.missingQuantityIngredients,
                                addMissingIngredientsToGroceryList = addMissingIngredientsToGroceryList
                            )
                        }
                    }
                }

                // Ingredienti
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        recipe.ingredients.forEach { ing ->
                            IngredientListItem(
                                ingredient = ing.ingredient,
                                quantity = ing.quantity,
                                unity = ing.unit
                            )
                        }
                    }
                }

                // Steps, Reviews, Tips
                item {
                    Column(modifier = Modifier.padding(bottom = PageBottomPadding)) {
                        Spacer(modifier = Modifier.height(24.dp))
                        RecipeStepsComponent(steps = recipe.preparationSteps)
                        Spacer(modifier = Modifier.height(32.dp))
                        RecipeReviewsComponent(
                            myReview = recipe.myReview,
                            otherReviews = recipe.otherReviews,
                            onExpandReviewList = { onExpandReviewList() },
                            showAddReviewButton = uiState.permissions.canAddReview,
                            onAddReviewClick = { onAddReviewClick() },
                            onDeleteReviewClick = onDeleteReviewClick,
                            onAuthorClick = onAuthorClick
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        RecipeTipsComponent(
                            myTip = recipe.myTip,
                            otherTips = recipe.otherTips,
                            onExpandTipsList = { onExpandTipsList() },
                            showAddTipButton = uiState.permissions.canAddTip,
                            onAddTipClick = { onAddTipClick() },
                            onDeleteTipClick = onDeleteTipClick,
                            onAuthorClick = onAuthorClick
                        )
                    }
                }

            }
        }

        else -> ErrorComponent("Recipe not found")
    }
}