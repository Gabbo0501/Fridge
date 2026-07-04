package com.example.fridgeproject.model.utils

import com.example.fridgeproject.model.enums.PrepTime

fun Long.toPrepTime(): PrepTime = when {
    this < 1800 -> PrepTime.SHORT   // < 30 min
    this < 3600 -> PrepTime.MEDIUM  // 30-60 min
    else -> PrepTime.LONG           // > 60 min
}