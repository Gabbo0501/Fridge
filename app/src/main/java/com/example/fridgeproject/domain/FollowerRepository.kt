package com.example.fridgeproject.domain

import com.example.fridgeproject.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface FollowerRepository {
    suspend fun followUser(followerId: String, followedId: String)
    suspend fun unfollowUser(followerId: String, followedId: String)
    suspend fun isFollowing(followerId: String, followedId: String): Boolean
    fun getFollowersFlow(userId: String): Flow<List<UserProfile>>
    fun getFollowingFlow(userId: String): Flow<List<UserProfile>>
}