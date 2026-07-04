package com.example.fridgeproject.ui.components.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.enums.DishType

@Composable
fun RecipeDishTypeSelector(
    selected: DishType,
    onSelect: (DishType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var boxWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = modifier
        .fillMaxWidth()
        .onGloballyPositioned { coordinates ->
        boxWidth = with(density) { coordinates.size.width.toDp() }
    }) {

        OutlinedTextField(
            value = selected.desc,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colorScheme.outline,
                focusedBorderColor = colorScheme.primary,
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(boxWidth),
            containerColor = colorScheme.surface,
        ) {
            DishType.entries.forEach { dishType ->
                DropdownMenuItem(
                    text = {
                        Text( "${dishType.icon}    ${dishType.desc
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }}")
                    },

                    onClick = {
                        onSelect(dishType)
                        expanded = false
                    }
                )
            }
        }
    }
}