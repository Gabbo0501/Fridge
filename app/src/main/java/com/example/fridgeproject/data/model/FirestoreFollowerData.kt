package com.example.fridgeproject.data.model

data class FirestoreFollowerData(
    val followerId : String = "",
    val followedId : String = "",
    val timestamp : Long = System.currentTimeMillis()
)