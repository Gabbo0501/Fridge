package com.example.fridgeproject.ui.screens.recipe

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Cuisine
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.IngredientWizardStep
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.MultipleErrorsDialog
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.components.recipe.BottomActionRow
import com.example.fridgeproject.ui.components.recipe.IngredientsSection
import com.example.fridgeproject.ui.components.recipe.LabeledSection
import com.example.fridgeproject.ui.components.recipe.PreparationSection
import com.example.fridgeproject.ui.components.recipe.RecipeDishTypeSelector
import com.example.fridgeproject.ui.components.recipe.ImagePickerCard
import com.example.fridgeproject.ui.components.recipe.RecipeStatsRow
import com.example.fridgeproject.ui.components.recipe.RecipeTagsRow
import com.example.fridgeproject.ui.components.wizard.IngredientWizard
import com.example.fridgeproject.viewmodel.recipe.CreateRecipeErrors

@Composable
fun CreateRecipeScreen(
    newRecipe: Recipe,
    addingIngredient: Boolean,
    addingIngredientStep: IngredientWizardStep,
    selectableIngredients: List<Ingredient>,
    newIngredient: IngredientQuantity,
    quantityInput: String,
    currentEditedStepIndex: Int,
    errors: CreateRecipeErrors,
    showErrorDialog: Boolean,
    success: Boolean,
    isLoading: Boolean,
    showExitDialog: Boolean,
    showDifficultyDialog: Boolean,
    showTimeDialog: Boolean,
    showCostDialog: Boolean,
    showDietDialog: Boolean,
    showCuisineDialog: Boolean,
    onDifficultyDialogOpen: () -> Unit,
    onTimeDialogOpen: () -> Unit,
    onCostDialogOpen: () -> Unit,
    onStatsDialogDismiss: () -> Unit,
    onDietDialogOpen: () -> Unit,
    onCuisineDialogOpen: () -> Unit,
    onTagsDialogDismiss: () -> Unit,
    onImageGalleryClick: () -> Unit,
    onImageCameraClick: () -> Unit,
    updateTitle: (String) -> Unit ,
    updateDishType: (DishType) -> Unit ,
    toggleDiet: (Diet) -> Unit ,
    toggleCuisine: (Cuisine) -> Unit ,
    updateDifficulty: (Difficulty) -> Unit ,
    updatePrepTime: (Long) -> Unit ,
    updateCostRange: (CostRange) -> Unit ,
    updateDescription: (String) -> Unit ,
    initAddIngredientProcedure: () -> Unit ,
    cancelAddIngredientProcedure: () -> Unit ,
    updateNewIngredientCategory: (IngredientCategory) -> Unit ,
    updateNewIngredientName: (String) -> Unit ,
    updateNewIngredientQuantity: (Float) -> Unit ,
    onPreviousIngredientWizardStep: () -> Unit,
    onNextIngredientWizardStep: () -> Unit,
    addIngredient: (IngredientQuantity) -> Unit ,
    updateIngredient: (String) -> Unit,
    removeIngredient: (IngredientQuantity) -> Unit ,
    addStep: () -> Unit ,
    updateStepDescription: (String) -> Unit ,
    onStepGalleryClick: () -> Unit,
    onStepCameraClick: () -> Unit,
    removeStep: () -> Unit ,
    switchLeftStep: () -> Unit ,
    switchRightStep: () -> Unit ,
    onSaveRecipe: () -> Unit ,
    onNavigateToHome: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onDismissErrorDialog: () -> Unit,
    onResetCreteState: () -> Unit,
    onQuantityTextChange: (String) -> Unit,
    onIncrementQuantity: () -> Unit,
    onDecrementQuantity: () -> Unit,
    isConfirmEnabled: Boolean,
    isEditMode: Boolean
) {
    val scrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    var wizardWasOpened by rememberSaveable { mutableStateOf(false) }
    var ingredientsSectionOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(addingIngredient) {
        if (addingIngredient) {
            wizardWasOpened = true
        } else if (wizardWasOpened) {
            scrollState.animateScrollTo(ingredientsSectionOffset)
        }
    }
    if (showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    LaunchedEffect(success) {
        if (success) {
            onResetCreteState()
            onNavigateToHome()
        }
    }

    when{
        addingIngredient -> {
            IngredientWizard(
                step = addingIngredientStep,
                selectedCategory = newIngredient.ingredient.category,
                ingredients = selectableIngredients,
                selectedIngredient = newIngredient.ingredient.name,
                quantityText = quantityInput,
                unit = newIngredient.unit,
                onClose = cancelAddIngredientProcedure,
                onBack = onPreviousIngredientWizardStep,
                onCategorySelect = { updateNewIngredientCategory(it) },
                onIngredientSelect = { updateNewIngredientName(it) },
                onNext = onNextIngredientWizardStep,
                onConfirm = { addIngredient(newIngredient) },
                onQuantityTextChange = onQuantityTextChange,
                onIncrementQuantity = onIncrementQuantity,
                onDecrementQuantity = onDecrementQuantity,
                isConfirmEnabled = isConfirmEnabled,
                isEditMode = isEditMode
            )
        }
        isLoading -> {
            LoadingComponent()
        }
        else -> {
            var isRecipeImageMenuExpanded by remember { mutableStateOf(false) }
            var isStepImageMenuExpanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(
                        top = 25.dp,
                        start = 25.dp,
                        end = 25.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Image Picker ──────────────────────────────────────────────────
                ImagePickerCard(
                    image = newRecipe.image.orEmpty(),
                    emptyText = "Insert Recipe Image",
                    heightDp = 200,
                    isMenuExpanded = isRecipeImageMenuExpanded,
                    onMenuOpen = { isRecipeImageMenuExpanded = true },
                    onMenuDismiss = { isRecipeImageMenuExpanded = false },
                    onGalleryClick = onImageGalleryClick,
                    onCameraClick = onImageCameraClick,
                    errorText = errors.image.takeIf { it.isNotBlank() }
                )

                // ── Recipe Title ──────────────────────────────────────────────────
                LabeledSection(label = "Recipe Title") {
                    OutlinedTextField(
                        value = newRecipe.title,
                        onValueChange = { updateTitle(it) },
                        placeholder = {
                            Text(
                                "e.g. Grandma's Spiced Pumpkin Soup",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        isError = errors.title.isNotEmpty(),
                        supportingText = { if (errors.title.isNotEmpty()) Text(errors.title) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )
                }

                // ── Category ──────────────────────────────────────────────────────
                LabeledSection(label = "Category") {
                    RecipeDishTypeSelector(
                        selected = newRecipe.dishType,
                        onSelect = { updateDishType(it) }
                    )

                }

                // ── Dietary Tags ──────────────────────────────────────────────────
                RecipeTagsRow(
                    dishType = newRecipe.dishType,
                    selectedDiets = newRecipe.suitableDiets,
                    selectedCuisines = newRecipe.cuisine,
                    edit = true,
                    showDietDialog = showDietDialog,
                    showCuisineDialog = showCuisineDialog,
                    onDietDialogOpen = onDietDialogOpen,
                    onCuisineDialogOpen = onCuisineDialogOpen,
                    onTagsDialogDismiss = onTagsDialogDismiss,
                    onDietToggle = {  toggleDiet(it) },
                    onCuisineToggle = {  toggleCuisine(it) }
                )

                // ── Stats Row (Difficulty / Time / Cost) ──────────────────────────
                LabeledSection(label = "Statistics") {
                    RecipeStatsRow(
                        difficulty = newRecipe.difficulty,
                        prepTime = newRecipe.preparationTimeSec,
                        costRange = newRecipe.costRange,
                        editable = true,
                        showDifficultyDialog = showDifficultyDialog,
                        showTimeDialog = showTimeDialog,
                        showCostDialog = showCostDialog,
                        onDifficultyDialogOpen = onDifficultyDialogOpen,
                        onTimeDialogOpen = onTimeDialogOpen,
                        onCostDialogOpen = onCostDialogOpen,
                        onStatsDialogDismiss = onStatsDialogDismiss,
                        onDifficultyChange = { updateDifficulty(it) },
                        onTimeChange = { updatePrepTime(it) },
                        onCostChange = { updateCostRange(it) }
                    )
                }

                // ── Description ───────────────────────────────────────────────────
                LabeledSection(label = "Description") {
                    OutlinedTextField(
                        value = newRecipe.description,
                        onValueChange = { updateDescription(it) },
                        placeholder = {
                            Text(
                                "Tell a story about this recipe...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        isError = errors.description.isNotEmpty(),
                        supportingText = { if (errors.description.isNotEmpty()) Text(errors.description) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        minLines = 3,
                        maxLines = 10,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // ── Ingredients ───────────────────────────────────────────────────
                Box(modifier = Modifier.onGloballyPositioned { coordinates ->
                    ingredientsSectionOffset = coordinates.positionInParent().y.toInt()
                }) {
                    IngredientsSection(
                        ingredients = newRecipe.ingredients,
                        initAddIngredientProcedure = initAddIngredientProcedure,
                        onUpdateIngredient = updateIngredient,
                        onRemoveIngredient = removeIngredient
                    )
                }

                if (errors.ingredients.isNotEmpty()) {
                    Text(
                        errors.ingredients,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                // ── Preparation ───────────────────────────────────────────────────
                PreparationSection(
                    steps = newRecipe.preparationSteps,
                    currentEditedStepIndex = currentEditedStepIndex,
                    imageError = errors.stepImage,
                    descError = errors.stepDescription,
                    isStepImageMenuExpanded = isStepImageMenuExpanded,
                    onStepImageMenuOpen = { isStepImageMenuExpanded = true },
                    onStepImageMenuDismiss = { isStepImageMenuExpanded = false },
                    onAddStep = addStep,
                    onRemoveStep = { removeStep() },
                    onSwitchLeft = switchLeftStep,
                    onSwitchRight = switchRightStep,
                    onStepGalleryClick = onStepGalleryClick,
                    onStepCameraClick = onStepCameraClick,
                    onDescriptionChange = { updateStepDescription(it)}
                )
                if (errors.steps.isNotEmpty()) {
                    Text(
                        errors.steps,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                // ── Cancel / Save buttons ─────────────────────────────────────────
                BottomActionRow(
                    cancel = onNavigateToHome,
                    save = onSaveRecipe,
                    saveLabel = "Save Recipe"
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showErrorDialog) {
        MultipleErrorsDialog(
            title = "Creation error",
            errors = listOf(
                errors.image,
                errors.title,
                errors.description,
                errors.ingredients,
                errors.steps,
                errors.stepImage,
                errors.stepDescription
            ),
            onDismiss = onDismissErrorDialog
        )
    }
}