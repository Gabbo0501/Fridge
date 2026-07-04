package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ReviewStars(
    stars: Int,
    filledStarColor: Color,
    emptyStarColor: Color
) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (index < stars) filledStarColor else emptyStarColor
            )
        }
    }
}
