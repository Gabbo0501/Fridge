package com.example.fridgeproject.ui.components.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fridgeproject.ui.components.StatsItem

@Composable
fun UserStatsSection(
    recipesNumber: Int,
    onFollowerClick: () -> Unit,
    onFollowedClick: () -> Unit,
    followersCount : Int,
    followedCount : Int
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatsItem("Recipe", recipesNumber)
        Box(
            modifier = Modifier.clickable(onClick = onFollowerClick)
        ){
            StatsItem("Follower", followersCount)
        }
        Box(
            modifier = Modifier.clickable(onClick = onFollowedClick)
        ){
            StatsItem("Followed", followedCount)
        }
    }
}
