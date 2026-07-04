package com.example.fridgeproject.ui.screens.recipe

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Cuisine
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure
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
import com.example.fridgeproject.viewmodel.recipe.EditRecipeUiState



@Composable
fun RecipeProposalEditScreen(
    modifier: Modifier = Modifier,

    uiState: EditRecipeUiState,

    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageGalleryClick: () -> Unit,
    onImageCameraClick: () -> Unit,

    onPrepTimeChange: (Long) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onDishTypeChange: (DishType) -> Unit,
    onCostRangeChange: (CostRange) -> Unit,

    onAddIngredient: (IngredientQuantity) -> Unit,
    onUpdateIngredient: (String) -> Unit,
    onRemoveIngredient: (IngredientQuantity) -> Unit,

    onInitAddIngredientProcedure: () -> Unit,
    onCancelAddIngredientProcedure: () -> Unit,
    onUpdateNewIngredientCategory: (IngredientCategory) -> Unit,
    onUpdateNewIngredientName: (String) -> Unit,
    onUpdateNewIngredientQuantity: (Float) -> Unit,
    onUpdateNewIngredientUnit: (UnitOfMeasure) -> Unit,
    onQuantityTextChange: (String) -> Unit,
    onIncrementQuantity: () -> Unit,
    onDecrementQuantity: () -> Unit,
    isConfirmEnabled: Boolean,
    isEditMode: Boolean,

    onPreviousIngredientWizardStep: () -> Unit,
    onNextIngredientWizardStep: () -> Unit,

    onAddStep: () -> Unit,
    onStepGalleryClick: () -> Unit,
    onStepCameraClick: () -> Unit,
    onRemoveStep: () -> Unit,
    onUpdateStepDescription: (String) -> Unit,
    onSwitchLeftStep: () -> Unit,
    onSwitchRightStep: () -> Unit,

    onDietToggle: (Diet) -> Unit,
    onCuisineToggle: (Cuisine) -> Unit,
    onDifficultyDialogOpen: () -> Unit,
    onTimeDialogOpen: () -> Unit,
    onCostDialogOpen: () -> Unit,
    onStatsDialogDismiss: () -> Unit,
    onDietDialogOpen: () -> Unit,
    onCuisineDialogOpen: () -> Unit,
    onTagsDialogDismiss: () -> Unit,

    onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    onBackClick: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onDismissErrorDialog: () -> Unit
) {
   val scrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    var wizardWasOpened by rememberSaveable { mutableStateOf(false) }
    var ingredientsSectionOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.addingIngredient) {
        if (uiState.addingIngredient) {
            wizardWasOpened = true
        } else if (wizardWasOpened) {
            scrollState.animateScrollTo(ingredientsSectionOffset)
        }
    }
    if (uiState.showExitDialog) {
        UnsavedChangesDialog(
            onConfirmExit = onConfirmExit,
            onDismiss = onDismissExitDialog
        )
    }

    LaunchedEffect(uiState.isSaved) { // per tornare indietro dopo il salvataggio
        if (uiState.isSaved) onBackClick()
    }

    when{
        uiState.addingIngredient -> {
            IngredientWizard(
                step = uiState.addingIngredientStep,
                selectedCategory = uiState.newIngredient.ingredient.category,
                ingredients = uiState.selectableIngredients,
                selectedIngredient = uiState.newIngredient.ingredient.name,
                quantityText = uiState.quantityInput,
                unit = uiState.newIngredient.unit,
                onClose = onCancelAddIngredientProcedure,
                onBack = onPreviousIngredientWizardStep,
                onCategorySelect = { onUpdateNewIngredientCategory(it) },
                onIngredientSelect = { onUpdateNewIngredientName(it) },
                onNext = onNextIngredientWizardStep,
                onConfirm = { onAddIngredient(uiState.newIngredient) },
                onQuantityTextChange = onQuantityTextChange,
                onIncrementQuantity = onIncrementQuantity,
                onDecrementQuantity = onDecrementQuantity,
                isConfirmEnabled = isConfirmEnabled,
                isEditMode = isEditMode,
                modifier = modifier
            )
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
                        horizontal = 25.dp,
                        vertical = 8.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row {
                        IconButton(onSaveClick) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                // ── Image Picker ──────────────────────────────────────────────────
                ImagePickerCard(
                    image = uiState.image ?: "",
                    emptyText = "Insert Recipe Image",
                    heightDp = 200,
                    isMenuExpanded = isRecipeImageMenuExpanded,
                    onMenuOpen = { isRecipeImageMenuExpanded = true },
                    onMenuDismiss = { isRecipeImageMenuExpanded = false },
                    onGalleryClick = onImageGalleryClick,
                    onCameraClick = onImageCameraClick,
                    errorText = uiState.errors.imageError
                )

                // ── Recipe Title ──────────────────────────────────────────────────
                LabeledSection(label = "Change Recipe Title", subLabel = "(max 40 characters)") {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { onTitleChange(it) },
                        placeholder = {
                            Text(
                                "Insert title ...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        isError = !uiState.errors.titleError.isNullOrBlank(),
                        supportingText = { uiState.errors.titleError?.let { Text(it) } },
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
                LabeledSection(label = "Change Category") {
                    RecipeDishTypeSelector(
                        selected = uiState.dishType,
                        onSelect = { onDishTypeChange(it) }
                    )

                }

                // ── Dietary Tags ──────────────────────────────────────────────────
                RecipeTagsRow(
                    dishType = uiState.dishType,
                    selectedDiets = uiState.suitableDiets,
                    selectedCuisines = uiState.cuisine,
                    edit = true,
                    showDietDialog = uiState.showDietDialog,
                    showCuisineDialog = uiState.showCuisineDialog,
                    onDietDialogOpen = onDietDialogOpen,
                    onCuisineDialogOpen = onCuisineDialogOpen,
                    onTagsDialogDismiss = onTagsDialogDismiss,
                    onDietToggle = onDietToggle,
                    onCuisineToggle = onCuisineToggle
                )

                // ── Stats Row (Difficulty / Time / Cost) ──────────────────────────
                LabeledSection(label = "Change Statistics", subLabel = "Click on a Statistic to change it") {
                    RecipeStatsRow(
                        difficulty = uiState.difficulty,
                        prepTime = uiState.preparationTimeSec,
                        costRange = uiState.costRange,
                        editable = true,
                        showDifficultyDialog = uiState.showDifficultyDialog,
                        showTimeDialog = uiState.showTimeDialog,
                        showCostDialog = uiState.showCostDialog,
                        onDifficultyDialogOpen = onDifficultyDialogOpen,
                        onTimeDialogOpen = onTimeDialogOpen,
                        onCostDialogOpen = onCostDialogOpen,
                        onStatsDialogDismiss = onStatsDialogDismiss,
                        onDifficultyChange = onDifficultyChange,
                        onTimeChange = onPrepTimeChange,
                        onCostChange = onCostRangeChange
                    )
                }

                // ── Description ───────────────────────────────────────────────────
                LabeledSection(label = "Change Description", subLabel = "(max 200 characters)") {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { onDescriptionChange(it) },
                        placeholder = {
                            Text(
                                "Insert a description ...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = false,
                        minLines = 3,
                        isError = !uiState.errors.descriptionError.isNullOrBlank(),
                        supportingText = { uiState.errors.descriptionError?.let { Text(it) }  },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp),
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
                        uiState.ingredients,
                        onInitAddIngredientProcedure,
                        onUpdateIngredient,
                        onRemoveIngredient
                    )
                }
                if (!uiState.errors.ingredientsError.isNullOrBlank()) {
                    Text(
                        uiState.errors.ingredientsError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                // ── Preparation ───────────────────────────────────────────────────
                PreparationSection(
                    uiState.preparationSteps,
                    uiState.currentEditedStepIndex,
                    uiState.errors.stepsImageError,
                    uiState.errors.stepsDescError,
                    isStepImageMenuExpanded,
                    { isStepImageMenuExpanded = true },
                    { isStepImageMenuExpanded = false },
                    {onAddStep()},
                    {onRemoveStep()},
                    {onSwitchLeftStep()},
                    {onSwitchRightStep()},
                    {onStepGalleryClick()},
                    {onStepCameraClick()},
                    {onUpdateStepDescription(it)},
                )
                if (!uiState.errors.stepsError.isNullOrBlank()) {
                    Text(
                        uiState.errors.stepsError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                // ── Cancel / Save buttons ─────────────────────────────────────────
                BottomActionRow(
                    cancel = onBackClick,
                    save = onSaveClick,
                    saveLabel = "Save Recipe"
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    if (uiState.showErrorDialog) {
        MultipleErrorsDialog(
            title = "Edit error",
            errors = listOf(
                uiState.errors.titleError,
                uiState.errors.descriptionError,
                uiState.errors.ingredientsError,
                uiState.errors.stepsError,
                uiState.errors.stepsImageError,
                uiState.errors.stepsDescError
            ),
            onDismiss = onDismissErrorDialog
        )
    }
}