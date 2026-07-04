package com.example.fridgeproject.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.FollowerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowViewModel(private val followerRepository: FollowerRepository, private val targetUserId: String): ViewModel() {
    private val _uiState = MutableStateFlow(FollowUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initialLoad()
    }

    private fun initialLoad() {
        val currentUserId = SessionManagerFacade.currentUserId.orEmpty()

        viewModelScope.launch {
            val targetFollowersFlow = followerRepository.getFollowersFlow(targetUserId)
            val targetFollowingFlow = followerRepository.getFollowingFlow(targetUserId)
            val myFollowingFlow = followerRepository.getFollowingFlow(currentUserId)

            val queryFlow = _uiState.map { it.searchAuthorQuery }.distinctUntilChanged()

            combine(
                targetFollowersFlow,
                targetFollowingFlow,
                myFollowingFlow,
                queryFlow
            ) { followers, following, loggedUserFollowing, query ->

                val loggedUserFollowingIdsSet = loggedUserFollowing.map { it.id }.toSet()

                val filteredFollowers = if (query.isBlank()) {
                    followers
                } else {
                    followers.filter { user ->
                        user.nickname.contains(query, ignoreCase = true) ||
                                user.firstName.contains(query, ignoreCase = true)
                    }
                }

                val filteredFollowing = if (query.isBlank()) {
                    following
                } else {
                    following.filter { user ->
                        user.nickname.contains(query, ignoreCase = true) ||
                                user.firstName.contains(query, ignoreCase = true)
                    }
                }

                Triple(filteredFollowers, filteredFollowing, loggedUserFollowingIdsSet)

            }.collect { (followersList, followingList, myFollowingIdsSet) ->
                _uiState.update {
                    it.copy(
                        followers = followersList,
                        followed = followingList,
                        loggedUserFollowingIds = myFollowingIdsSet,
                        loggedUserId = currentUserId,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleFollow(id: String, isFollowing: Boolean) {
        if(uiState.value.loggedUserId.isEmpty())
            return
        val currentUserId = uiState.value.loggedUserId
        viewModelScope.launch {
            if (isFollowing) {
                followerRepository.unfollowUser(currentUserId, id)
            } else {
                followerRepository.followUser(currentUserId, id)
            }
        }
    }

    fun onSearchRecipeQueryChanged(newQuery: String) {
        _uiState.update {
            it.copy(searchAuthorQuery = newQuery)
        }
    }

    companion object {
        fun provideFactory(
            userId: String
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                FollowViewModel(
                    followerRepository = app.container.followerRepository,
                    targetUserId = userId
                )
            }
        }
    }
}