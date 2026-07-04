package com.example.fridgeproject.ui.components.user

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserBioEditor(
    value: String,
    onValueChange: (String) -> Unit,
    error: String = "",
    modifier: Modifier = Modifier
) {
    ProfileTextField("Bio", value, onValueChange, error)
}