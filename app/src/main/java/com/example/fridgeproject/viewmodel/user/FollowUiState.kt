package com.example.fridgeproject.viewmodel.user

import com.example.fridgeproject.model.UserProfile

data class FollowUiState (
    val followers: List<UserProfile> = emptyList(),
    val followed: List<UserProfile> = emptyList(),
    val isLoading: Boolean = false,
    val loggedUserFollowingIds: Set<String> = emptySet(),
    val loggedUserId: String = "",
    val searchAuthorQuery: String = ""
)