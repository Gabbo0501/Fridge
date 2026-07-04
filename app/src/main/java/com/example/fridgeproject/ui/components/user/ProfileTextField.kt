package com.example.fridgeproject.ui.components.user
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTextField(
    text: String, value: String?,
    onValueChange: (String) -> Unit = {},
    error: String = "",
    isChangeable: Boolean = true
){
    val borderColors = if (error.isNotEmpty()) {
        MaterialTheme.colorScheme.error
    } else if (!isChangeable) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val textColor = if (isChangeable) Color.Black else Color.Gray

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (error.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        BasicTextField(
            value = value ?: "",
            onValueChange = onValueChange,
            enabled = isChangeable,
            maxLines = Int.MAX_VALUE,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Next
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = textColor
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = if (error.isNotEmpty()) 2.dp else 1.dp,
                            color = borderColors,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    innerTextField()
                }
            }
        )

        if (error.isNotEmpty()) {
            Text(
                text = error,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(top = 4.dp, start = 12.dp)
            )
        }
    }
}