package com.example.fridgeproject.ui.components.review_and_tip

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fridgeproject.model.enums.TipType

@Composable
fun TipTypeSelector(
    selectedType: TipType?,
    isError: Boolean,
    onTypeChange: (TipType) -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isError) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(32.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TipTypeOption(
            type = TipType.DONT,
            isSelected = selectedType == TipType.DONT,
            shape = shape,
            modifier = Modifier.weight(1f),
            onClick = { onTypeChange(TipType.DONT) }
        )

        TipTypeOption(
            type = TipType.DO,
            isSelected = selectedType == TipType.DO,
            shape = shape,
            modifier = Modifier.weight(1f),
            onClick = { onTypeChange(TipType.DO) }
        )
    }
}