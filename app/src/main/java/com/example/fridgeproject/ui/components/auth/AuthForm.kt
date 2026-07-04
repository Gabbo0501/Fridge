package com.example.fridgeproject.ui.components.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AuthFormField(
    val label: String,
    val value: String,
    val onValueChange: (String) -> Unit,
    val error: String = "",
    val isPassword: Boolean = false
)

@Composable
fun AuthForm(
    fields: List<AuthFormField>,
    submitText: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        fields.forEachIndexed { index, field ->
            AuthTextField(
                label = field.label,
                value = field.value,
                onValueChange = field.onValueChange,
                error = field.error,
                isPassword = field.isPassword
            )
            if (index < fields.lastIndex) {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = submitText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}