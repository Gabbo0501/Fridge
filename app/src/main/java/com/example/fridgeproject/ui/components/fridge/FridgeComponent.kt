package com.example.fridgeproject.ui.components.fridge

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fridgeproject.R
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.components.UnsavedChangesDialog
import com.example.fridgeproject.ui.theme.FridgeBackground
import com.example.fridgeproject.ui.theme.FridgeTextMuted
import com.example.fridgeproject.ui.theme.FridgeTextPrimary

@Composable
fun FridgeComponent(
    addingIngredient: Boolean,
    showExitDialog: Boolean,
    onConfirmExit: () -> Unit,
    onDismissExitDialog: () -> Unit,
    fridgeIngredients: List<IngredientQuantityWithTime>,
    onSearchRecipes: () -> Unit,
    isLoading: Boolean,
    onClearFridge: () -> Unit,
    initAddIngredientProcedure: () -> Unit,
    updateIngredient: (String) -> Unit,
    removeIngredient: (String) -> Unit
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
                    text = "My Fridge",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = FridgeTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage the ingredients you already have at home and discover suitable recipes",
                    fontSize = 16.sp,
                    color = FridgeTextMuted,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = R.drawable.fridge,
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
            IngredientsWithTimeSection(
                ingredients = fridgeIngredients,
                initAddIngredientProcedure = initAddIngredientProcedure,
                onUpdateIngredient = updateIngredient,
                onRemoveIngredient = removeIngredient,
                onClearFridge = onClearFridge,
                hideHeaderDisclaimer = true
            )

            Spacer(modifier = Modifier.height(32.dp))


            // SEARCH BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find suitable recipes for your Fridge",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    fontSize = 16.sp,
                    color = FridgeTextPrimary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSearchRecipes,
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SEARCH",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
