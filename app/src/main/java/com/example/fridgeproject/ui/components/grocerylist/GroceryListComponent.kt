package com.example.fridgeproject.ui.components.grocerylist

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fridgeproject.R
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.fridgeproject.ui.theme.FridgeBackground
import com.example.fridgeproject.ui.theme.FridgeTextMuted
import com.example.fridgeproject.ui.theme.FridgeTextPrimary

@Composable
fun GroceryListComponent(
    groceryListIngredients : List<IngredientQuantityWithTime>,
    selectedIngredients: List<IngredientQuantityWithTime>,
    addingIngredient: Boolean,
    isLoading : Boolean,
    clearGroceryList: () -> Unit,
    initAddIngredientProcedure: () -> Unit,
    removeIngredient: (String) -> Unit,
    updateIngredient: (String) -> Unit,
    selectIngredient: (IngredientQuantityWithTime) -> Unit,
    unselectIngredient: (IngredientQuantityWithTime) -> Unit,
    moveSelectedIngredientsToFridge: () -> Unit,
    showExitDialog: Boolean,
    confirmExit: () -> Unit,
    dismissExitDialog: () -> Unit,
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
            onConfirmExit = confirmExit,
            onDismiss = dismissExitDialog
        )
    }
    if (isLoading) {
        LoadingComponent()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 15.dp)
                .background(FridgeBackground)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            // HEADER
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "My Grocery List",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = FridgeTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Keep track of the ingredients you need and you have bought",
                    fontSize = 16.sp,
                    color = FridgeTextMuted,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = R.drawable.grocery_list,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                modifier = Modifier
                    .width(130.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape),
                thickness = 3.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(24.dp))


            // INGREDIENTS
            GroceryListIngredientsSection(
                ingredients = groceryListIngredients,
                selectedIngredients = selectedIngredients,
                initAddIngredientProcedure =  initAddIngredientProcedure ,
                updateIngredient = updateIngredient,
                onRemoveIngredient = removeIngredient,
                clearGroceryList = clearGroceryList,
                selectIngredient = selectIngredient,
                unselectIngredient = unselectIngredient,
                moveSelectedIngredientsToFridge = moveSelectedIngredientsToFridge,
            )
        }
    }
}