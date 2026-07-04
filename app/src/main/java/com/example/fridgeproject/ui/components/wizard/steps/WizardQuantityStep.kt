package com.example.fridgeproject.ui.components.wizard.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.wizard.shared.WizardButton
import com.example.fridgeproject.ui.components.wizard.shared.WizardProgressHeader

@Composable
fun WizardQuantityStep(
    ingredient: String,
    category: IngredientCategory,
    quantityText: String,
    unit: UnitOfMeasure,
    onQuantityTextChange: (String) -> Unit,
    onIncrementClick: () -> Unit,
    onDecrementClick: () -> Unit,
    onActionClick: () -> Unit,
    stepCount: Int,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
    isConfirmEnabled: Boolean
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues()
    ) {
        item {
            WizardProgressHeader(
                title = "Quantity",
                stepIndex = 3,
                stepCount = stepCount,
                onBack = onBackClick,
                onClose = onCloseClick
            )
        }

        item {
            Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                Text(
                    category.displayName.uppercase(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
            }
        }

        item {
            Text(
                ingredient,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "AMOUNT TO ADD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .clickable(enabled = unit != UnitOfMeasure.QB) { onDecrementClick() },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "-",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unit == UnitOfMeasure.QB) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(min = 100.dp, max = 180.dp)
                        ) {
                            BasicTextField(
                                value = if (unit == UnitOfMeasure.QB) "/" else quantityText,
                                onValueChange = onQuantityTextChange,
                                enabled = unit != UnitOfMeasure.QB,
                                textStyle = TextStyle(
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.widthIn(min = 40.dp, max = 110.dp)
                            )

                            if (unit != UnitOfMeasure.QB) {
                                Text(
                                    text = " " + unit.name.lowercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .clickable(enabled = unit != UnitOfMeasure.QB) { onIncrementClick() },
                            shape = CircleShape,
                            color = if (unit == UnitOfMeasure.QB) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (unit == UnitOfMeasure.QB) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            WizardButton(
                enabled = isConfirmEnabled,
                onClick = onActionClick,
                text = "Confirm",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = PageBottomPadding)
            )
        }
    }
}
