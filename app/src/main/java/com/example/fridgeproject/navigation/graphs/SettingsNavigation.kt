package com.example.fridgeproject.navigation.graphs

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import com.example.fridgeproject.navigation.*
import com.example.fridgeproject.navigation.utils.*

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.fridgeproject.camera.LocalCameraActions
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.ui.screens.user.DietManagementScreen
import com.example.fridgeproject.ui.screens.review_and_tip.EditReviewScreen
import com.example.fridgeproject.ui.screens.review_and_tip.EditTipScreen
import com.example.fridgeproject.ui.screens.auth.LogOutScreen
import com.example.fridgeproject.ui.screens.review_and_tip.MyReviewScreen
import com.example.fridgeproject.ui.screens.review_and_tip.MyTipsScreen
import com.example.fridgeproject.ui.screens.notification.NotificationSettingsScreen
import com.example.fridgeproject.ui.screens.settings.SettingsScreen
import com.example.fridgeproject.ui.screens.user.UserProfileEditScreen
import com.example.fridgeproject.viewmodel.user.EditProfileViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.EditReviewViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.EditTipViewModel
import com.example.fridgeproject.viewmodel.user.EditDietViewModel
import com.example.fridgeproject.viewmodel.auth.LogOutViewModel
import com.example.fridgeproject.viewmodel.notification.NotificationSettingViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.ProfileReviewViewModel
import com.example.fridgeproject.viewmodel.review_and_tip.ProfileTipsViewModel
import com.example.fridgeproject.viewmodel.ReviewEvent
import com.example.fridgeproject.viewmodel.SettingsEvent
import com.example.fridgeproject.viewmodel.settings.SettingsViewModel
import com.example.fridgeproject.viewmodel.TipEvent


fun NavGraphBuilder.settingsGraph(
    navActions: AppNavigationActions,
    exitRequestManager: ExitRequestManager,
    onShowSnackbar: (String) -> Unit = {}
) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {

        composable<SettingsRoute> {
            AuthenticatedRoute(navActions) {
                val context = LocalContext.current
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    tipsCount = uiState.tipsCount,
                    reviewsCount = uiState.reviewsCount,
                    onBackClick = { navActions.navigateToProfile(SessionManagerFacade.currentUserId!!) },
                    onUpdateProfileClick = { navActions.navigateTo(EditProfileRoute(SessionManagerFacade.currentUserId!!)) },
                    onDietManagementClick = { navActions.navigateTo(DietRoute(SessionManagerFacade.currentUserId!!)) },
                    onNotificationSettingsClick = { navActions.navigateTo(NotificationSettingsRoute(SessionManagerFacade.currentUserId!!)) },
                    onMyTipsClick = { navActions.navigateTo(MyTipsRoute(SessionManagerFacade.currentUserId!!)) },
                    onMyReviewClick = { navActions.navigateTo(MyReviewsRoute(SessionManagerFacade.currentUserId!!)) },
                    onContactUsClick = {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO,
                            "mailto:info@fridge.com".toUri()
                        )
                        try {
                            context.startActivity(emailIntent)
                        } catch (_: ActivityNotFoundException) {
                            onShowSnackbar("No email app available")
                        }
                    },
                    onLogOutClick = {navActions.navigateTo(LogOutRoute)}
                )
            }
        }

        composable<EditProfileRoute> {
            AuthenticatedRoute(navActions) {
                val cameraActions = LocalCameraActions.current
                val vm: EditProfileViewModel = viewModel(factory = EditProfileViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            SettingsEvent.ProfileUpdated -> onShowSnackbar("Profile updated successfully")
                            SettingsEvent.PreferencesUpdated -> onShowSnackbar("Preferences updated successfully")
                            SettingsEvent.LoggedOut -> onShowSnackbar("Logged out successfully")
                            SettingsEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            SettingsEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                UserProfileEditScreen(
                    firstName = uiState.firstName,
                    lastName = uiState.lastName,
                    profileImage = uiState.profileImage,
                    nickname = uiState.nickname,
                    email = uiState.email,
                    shortBio = uiState.shortBio,
                    phoneNumber = uiState.phoneNumber,
                    socialProfiles = uiState.socialProfiles,
                    cookingRole = uiState.cookingRole,
                    errors = uiState.errors,
                    isLoading = uiState.isLoading,
                    success = uiState.success,
                    showExitDialog = uiState.showExitDialog,
                    onBackClick = {
                        if (uiState.success) {
                            navActions.navigateBack()
                        } else {
                            exitRequestManager.requestExitBefore { navActions.navigateBack() }
                        }
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onSaveClick = { vm.saveProfile() },
                    onNicknameChange = { vm.updateNickname(it)},
                    onShortBioChange = { vm.updateShortBio(it)},
                    onPhoneNumberChange = { vm.updatePhoneNumber(it)},
                    onSocialProfilesChange = { vm.updateSocialProfiles(it)},
                    onCookingRoleChange = { vm.updateCookingRole(it)},
                    onCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.updateProfileImage(ProfileImageSource.Local(LocalImageInput.Camera(uri)))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    onGalleryClick = {
                        cameraActions.onPickFromGallery { uri ->
                            vm.updateProfileImage(ProfileImageSource.Local(LocalImageInput.Gallery(uri)))
                        }
                    },
                    onRemoveImageClick = { vm.removeProfileImage() },
                )
            }
        }

        composable<DietRoute> {
            AuthenticatedRoute(navActions) {
                val vm: EditDietViewModel = viewModel(factory = EditDietViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            SettingsEvent.PreferencesUpdated -> {
                                navActions.navigateBack()
                                onShowSnackbar("Preferences updated successfully")
                            }
                            SettingsEvent.ProfileUpdated -> onShowSnackbar("Profile updated successfully")
                            SettingsEvent.LoggedOut -> onShowSnackbar("Logged out successfully")
                            SettingsEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            SettingsEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                DietManagementScreen(
                    diet = uiState.diet,
                    allergens = uiState.allergens,
                    addingAllergen = uiState.addingAllergen,
                    allergenWizardStep = uiState.allergenWizardStep,
                    selectedAllergenCategory = uiState.selectedAllergenCategory,
                    selectableAllergens = uiState.selectableAllergens,
                    selectedAllergenName = uiState.selectedAllergenName,
                    error = uiState.globalError,
                    showExitDialog = uiState.showExitDialog,
                    onDietChange = {vm.updateDiet(it)},
                    OnOpenAllergenWizard= {vm.openAllergenWizard()},
                    onCloseAllergenWizard= {
                        exitRequestManager.requestExitBefore {
                            vm.closeAllergenWizard() 
                        }
                    },
                    onBackAllergenWizard= {vm.backAllergenWizard()},
                    onUpdateAllergenCategory= {vm.updateAllergenCategory(it)},
                    onConfirmAllergenCategory= {vm.confirmAllergenCategory()},
                    onUpdateAllergenName= {vm.updateAllergenName(it)},
                    onAddSelectedAllergen= {vm.addSelectedAllergen()},
                    onRemoveAllergens = {vm.removeAllergens(it)},
                    onSavePreferences = { vm.savePreference() },
                    onBackClick = { 
                        exitRequestManager.requestExitBefore {
                            navActions.navigateBack() 
                        } 
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                )
            }
        }

        composable<NotificationSettingsRoute> {
            AuthenticatedRoute(navActions) {
                val vm: NotificationSettingViewModel = viewModel(factory = NotificationSettingViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            SettingsEvent.PreferencesUpdated -> {
                                navActions.navigateBack()
                                onShowSnackbar("Preferences updated successfully")
                            }
                            SettingsEvent.ProfileUpdated -> onShowSnackbar("Profile updated successfully")
                            SettingsEvent.LoggedOut -> onShowSnackbar("Logged out successfully")
                            SettingsEvent.ExitAllowed -> Unit
                            SettingsEvent.ExitCancelled -> Unit
                        }
                    }
                }
                NotificationSettingsScreen(
                    receiveNotification = uiState.receiveNotification,
                    receiveLikeNotification = uiState.receiveLikeNotification,
                    receiveRemixNotification = uiState.receiveRemixNotification,
                    receiveNewFollowerNotification = uiState.receiveNewFollowerNotification,
                    receiveNewRecipeNotification = uiState.receiveNewRecipeNotification,
                    receiveReviewNotification = uiState.receiveReviewNotification,
                    receiveTipNotification = uiState.receiveTipNotification,
                    error = uiState.globalError,
                    toggleReceiveNotification = {vm.toggleReceiveNotification()},
                    toggleReceiveLikeNotification = {vm.toggleReceiveLikeNotification()},
                    toggleReceiveRemixNotification = {vm.toggleReceiveRemixNotification()},
                    toggleReceiveNewFollowerNotification = {vm.toggleReceiveNewFollowerNotification()},
                    toggleReceiveNewRecipeNotification = {vm.toggleReceiveNewRecipeNotification()},
                    saveNotificationSettings = { vm.saveNotificationSettings() },
                    onBackClick = { navActions.navigateBack() },
                    toggleReceiveReviewNotification = {vm.toggleReceiveReviewNotification()},
                    toggleReceiveTipNotification = {vm.toggleReceiveTipNotification()}
                )
            }
        }

        composable<MyTipsRoute> {
            AuthenticatedRoute(navActions) {
                val vm: ProfileTipsViewModel = viewModel(factory = ProfileTipsViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            TipEvent.Deleted -> onShowSnackbar("Tip deleted successfully")
                            TipEvent.Published -> onShowSnackbar("Tip published successfully")
                            TipEvent.Updated -> onShowSnackbar("Tip updated successfully")
                            TipEvent.ExitAllowed -> Unit
                            TipEvent.ExitCancelled -> Unit
                        }
                    }
                }
                MyTipsScreen(
                    tips = uiState.tips,
                    error = uiState.globalError,
                    onBackClick = { navActions.navigateBack() },
                    onRecipeClick = { navActions.navigateToRecipeDetails(it) },
                    onRecipeAuthorClick = { navActions.navigateToProfile(it) },
                    onEditTipClick = { navActions.navigateTo(EditTipRoute(it)) },
                    tipToDeleteId = uiState.tipToDeleteId,
                    onDeleteTipClick = { vm.requestDeleteTip(it) },
                    onDeleteTipConfirm = { vm.confirmDeleteTip() },
                    onDeleteTipDismiss = { vm.dismissDeleteTipDialog() }
                )
            }
        }

        composable<EditTipRoute> {
            AuthenticatedRoute(navActions) {
                val vm: EditTipViewModel = viewModel(factory = EditTipViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            TipEvent.Updated -> onShowSnackbar("Tip updated successfully")
                            TipEvent.Published -> onShowSnackbar("Tip published successfully")
                            TipEvent.Deleted -> onShowSnackbar("Tip deleted successfully")
                            TipEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            TipEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                EditTipScreen(
                    uiState = uiState,
                    onTypeChange = { vm.updateType(it) },
                    onTipTextChange = { vm.updateTipText(it) },
                    onSaveClick = { vm.saveTip() },
                    onBackRequest = { 
                        exitRequestManager.requestExitBefore {
                            navActions.navigateBack() 
                        } 
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onNavigateBack = { navActions.navigateBack() },
                    onResetEditState = { vm.resetEditState() }
                )
            }
        }

        composable<MyReviewsRoute> {
            AuthenticatedRoute(navActions) {
                val vm: ProfileReviewViewModel = viewModel(factory = ProfileReviewViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            ReviewEvent.Deleted -> onShowSnackbar("Review deleted successfully")
                            ReviewEvent.Published -> onShowSnackbar("Review published successfully")
                            ReviewEvent.Updated -> onShowSnackbar("Review updated successfully")
                            ReviewEvent.ExitAllowed -> Unit
                            ReviewEvent.ExitCancelled -> Unit
                        }
                    }
                }
                MyReviewScreen(
                    reviews = uiState.reviews,
                    error = uiState.globalError,
                    onBackClick = { navActions.navigateBack() },
                    onRecipeClick = { navActions.navigateToRecipeDetails(it) },
                    onRecipeAuthorClick = { navActions.navigateToProfile(it) },
                    onEditReviewClick = { navActions.navigateTo(EditReviewRoute(it)) },
                    reviewToDeleteId = uiState.reviewToDeleteId,
                    onDeleteReviewClick = { vm.requestDeleteReview(it) },
                    onDeleteReviewConfirm = { vm.confirmDeleteReview() },
                    onDeleteReviewDismiss = { vm.dismissDeleteReviewDialog() }
                )
            }
        }

        composable<EditReviewRoute> {
            AuthenticatedRoute(navActions) {
                val cameraActions = LocalCameraActions.current
                val vm: EditReviewViewModel = viewModel(factory = EditReviewViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            ReviewEvent.Updated -> onShowSnackbar("Review updated successfully")
                            ReviewEvent.Published -> onShowSnackbar("Review published successfully")
                            ReviewEvent.Deleted -> onShowSnackbar("Review deleted successfully")
                            ReviewEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                            ReviewEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        }
                    }
                }
                DisposableEffect(Unit) {
                    exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                    onDispose { exitRequestManager.clearCurrentFormExitRequest() }
                }
                BackHandler {
                    exitRequestManager.requestExitBefore { navActions.navigateBack() }
                }
                EditReviewScreen(
                    uiState = uiState,
                    onRatingChange = { vm.updateRating(it) },
                    onReviewTextChange = { vm.updateReviewText(it) },
                    onRemoveImage = { vm.removeReviewImage(it) },
                    onRemovePendingImage = { vm.removePendingReviewImage(it) },
                    onSaveClick = { vm.saveReview() },
                    onBackRequest = { 
                        exitRequestManager.requestExitBefore {
                            navActions.navigateBack() 
                        } 
                    },
                    onConfirmExit = { vm.confirmExit() },
                    onDismissExitDialog = { vm.dismissExitDialog() },
                    onNavigateBack = { navActions.navigateBack() },
                    onResetEditState = { vm.resetEditState() },
                    onCameraClick = {
                        cameraActions.onTakePhoto { uri ->
                            vm.addReviewImage(LocalImageInput.Camera(uri))
                            navActions.navigateBack()
                        }
                        navActions.navigateTo(CameraRoute)
                    },
                    onGalleryClick = {
                        cameraActions.onPickFromGallery { uri -> vm.addReviewImage(LocalImageInput.Gallery(uri)) }
                    },
                )
            }
        }

        composable<LogOutRoute> {
            AuthenticatedRoute(navActions) {
                val vm: LogOutViewModel = viewModel(factory = LogOutViewModel.Factory)
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            SettingsEvent.LoggedOut -> {
                                navActions.navigateHome()
                                onShowSnackbar("Logged out successfully")
                            }
                            SettingsEvent.ProfileUpdated -> onShowSnackbar("Profile updated successfully")
                            SettingsEvent.PreferencesUpdated -> onShowSnackbar("Preferences updated successfully")
                            SettingsEvent.ExitAllowed -> Unit
                            SettingsEvent.ExitCancelled -> Unit
                        }
                    }
                }

                LogOutScreen(
                    uiState = uiState,
                    onBackClick = { navActions.navigateBack() },
                    onLogOutClick = { vm.logOut() }
                )
            }
        }
    }
}
