package com.example.fridgeproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Tag(
    label: String,
    fontColor: Color,
    fillColor: Color,
    borderColor: Color,
    cornerRadius: Dp
){
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(fillColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(horizontal = 5.dp)
    ){
        Text(
            text = label,
            fontSize = 10.sp,
            color = fontColor
        )
    }
}