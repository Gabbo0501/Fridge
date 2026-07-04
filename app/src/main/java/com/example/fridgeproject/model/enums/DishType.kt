package com.example.fridgeproject.model.enums

import androidx.compose.ui.graphics.Color
import com.example.fridgeproject.ui.theme.DishTypeAppetizerTagBg
import com.example.fridgeproject.ui.theme.DishTypeAppetizerTagBorder
import com.example.fridgeproject.ui.theme.DishTypeAppetizerTagFont
import com.example.fridgeproject.ui.theme.DishTypeDessertTagBg
import com.example.fridgeproject.ui.theme.DishTypeDessertTagBorder
import com.example.fridgeproject.ui.theme.DishTypeDessertTagFont
import com.example.fridgeproject.ui.theme.DishTypeDrinkTagBg
import com.example.fridgeproject.ui.theme.DishTypeDrinkTagBorder
import com.example.fridgeproject.ui.theme.DishTypeDrinkTagFont
import com.example.fridgeproject.ui.theme.DishTypeFirstCourseTagBg
import com.example.fridgeproject.ui.theme.DishTypeFirstCourseTagBorder
import com.example.fridgeproject.ui.theme.DishTypeFirstCourseTagFont
import com.example.fridgeproject.ui.theme.DishTypeSecondCourseTagBg
import com.example.fridgeproject.ui.theme.DishTypeSecondCourseTagBorder
import com.example.fridgeproject.ui.theme.DishTypeSecondCourseTagFont
import com.example.fridgeproject.ui.theme.DishTypeSideDishTagBg
import com.example.fridgeproject.ui.theme.DishTypeSideDishTagBorder
import com.example.fridgeproject.ui.theme.DishTypeSideDishTagFont
import com.example.fridgeproject.ui.theme.DishTypeSnackTagBg
import com.example.fridgeproject.ui.theme.DishTypeSnackTagBorder
import com.example.fridgeproject.ui.theme.DishTypeSnackTagFont

private const val STORAGE_BASE_URL = "https://firebasestorage.googleapis.com/v0/b/fridge-c1c87.firebasestorage.app/o"

enum class DishType(val desc: String, val icon: String, val image : String,  val tagBgColor: Color, val tagFontColor:Color,val tagBorderColor: Color) {
    APPETIZER("Appetizer", "🥗", "$STORAGE_BASE_URL/categories%2Fappetizer.jpg?alt=media&token=11111111-1111-4111-8111-111111111111", DishTypeAppetizerTagBg, DishTypeAppetizerTagFont, DishTypeAppetizerTagBorder),
    FIRST_COURSE("First Course", "🍝", "$STORAGE_BASE_URL/categories%2Ffirst_course.jpg?alt=media&token=22222222-2222-4222-8222-222222222222", DishTypeFirstCourseTagBg, DishTypeFirstCourseTagFont, DishTypeFirstCourseTagBorder),
    SECOND_COURSE("Second Course", "🍖", "$STORAGE_BASE_URL/categories%2Fsecond_course.jpg?alt=media&token=33333333-3333-4333-8333-333333333333", DishTypeSecondCourseTagBg, DishTypeSecondCourseTagFont, DishTypeSecondCourseTagBorder),
    SIDE_DISH("Side Dish", "🥦", "$STORAGE_BASE_URL/categories%2Fside_dish.jpg?alt=media&token=44444444-4444-4444-8444-444444444444", DishTypeSideDishTagBg, DishTypeSideDishTagFont, DishTypeSideDishTagBorder),
    DESSERT("Dessert", "🍰", "$STORAGE_BASE_URL/categories%2Fdessert.jpg?alt=media&token=55555555-5555-4555-8555-555555555555", DishTypeDessertTagBg, DishTypeDessertTagFont, DishTypeDessertTagBorder),
    DRINK("Drink", "🥤", "$STORAGE_BASE_URL/categories%2Fdrink.jpg?alt=media&token=66666666-6666-4666-8666-666666666666", DishTypeDrinkTagBg, DishTypeDrinkTagFont, DishTypeDrinkTagBorder),
    SNACK("Snack", "🍿", "$STORAGE_BASE_URL/categories%2Fsnack.jpg?alt=media&token=77777777-7777-4777-8777-777777777777",DishTypeSnackTagBg, DishTypeSnackTagFont, DishTypeSnackTagBorder)
}
