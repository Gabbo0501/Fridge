package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.viewmodel.user.EditProfileErrors

@Composable
fun UserTextualForm(
    nickname : String,
    email : String,
    phoneNumber : String?,
    errors: EditProfileErrors,
    onNicknameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ProfileTextField("Name", nickname, onNicknameChange, errors.nickname)
        Spacer(modifier = Modifier.height(25.dp))
        ProfileTextField("Email", email, isChangeable = false)
        Spacer(modifier = Modifier.height(25.dp))
        ProfileTextField("Phone Number", phoneNumber,onPhoneNumberChange, errors.phoneNumber)
        Spacer(modifier = Modifier.height(25.dp))
    }
}