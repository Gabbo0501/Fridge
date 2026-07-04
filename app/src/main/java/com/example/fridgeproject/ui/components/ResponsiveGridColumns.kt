package com.example.fridgeproject.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

const val PortraitGridColumns = 2
const val LandscapeGridColumns = 4

@Composable
fun responsiveGridColumns(): Int =
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeGridColumns
    } else {
        PortraitGridColumns
    }