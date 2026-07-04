package com.example.fridgeproject.ui.screens.recipe
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fridgeproject.model.enums.DishType
import coil.compose.AsyncImage
import com.example.fridgeproject.ui.components.LoadingComponent
import com.example.fridgeproject.ui.theme.PageBottomPadding
import com.example.fridgeproject.ui.components.responsiveGridColumns

@Composable
fun ExploreCategoriesScreen(
    onCategoryClick: (DishType) -> Unit,
    isLoading: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val columns = responsiveGridColumns()

    if(isLoading){
        LoadingComponent()
    } else {

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 25.dp)
                .background(colorScheme.background),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 25.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Discovery by Category",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    textAlign = TextAlign.Center
                )
            }

            val categories = DishType.entries
            val lastRowStart = ((categories.lastIndex) / columns) * columns
            itemsIndexed(categories) { index, dishType ->
                CategoryCard(
                    title = dishType.desc,
                    imageUrl = dishType.image,
                    onClick = { onCategoryClick(dishType) },
                    modifier = Modifier.padding(
                        bottom = if (index >= lastRowStart) PageBottomPadding else 0.dp
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Text(
                text = title,
                modifier = Modifier.padding(bottom = PageBottomPadding, top = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colorScheme.onSurface
            )
        }
    }
}