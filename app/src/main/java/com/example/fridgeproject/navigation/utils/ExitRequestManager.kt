package com.example.fridgeproject.navigation.utils

class ExitRequestManager {
    private var currentFormExitRequest: (() -> Unit)? = null
    private var navigationAfterExitConfirmation: (() -> Unit)? = null

    fun setCurrentFormExitRequest(requestExit: () -> Unit) {
        currentFormExitRequest = requestExit
    }

    fun clearCurrentFormExitRequest() {
        currentFormExitRequest = null
    }

    fun requestExitBefore(navigate: () -> Unit) {
        navigationAfterExitConfirmation = navigate

        val requestExit = currentFormExitRequest
        if (requestExit == null) {
            executePendingNavigation()
        } else {
            requestExit()
        }
    }

    fun executePendingNavigation() {
        val navigate = navigationAfterExitConfirmation
        navigationAfterExitConfirmation = null
        navigate?.invoke()
    }

    fun cancelPendingNavigation() {
        navigationAfterExitConfirmation = null
    }
}
