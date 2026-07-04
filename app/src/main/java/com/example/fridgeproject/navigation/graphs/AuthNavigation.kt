package com.example.fridgeproject.navigation.graphs

import com.example.fridgeproject.navigation.*
import com.example.fridgeproject.navigation.utils.*

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.fridgeproject.ui.screens.auth.LogInScreen
import com.example.fridgeproject.ui.screens.auth.OnboardingScreen
import com.example.fridgeproject.ui.screens.auth.RegistrationScreen
import com.example.fridgeproject.viewmodel.AuthEvent
import com.example.fridgeproject.viewmodel.auth.LogInViewModel
import com.example.fridgeproject.viewmodel.auth.OnboardingViewModel
import com.example.fridgeproject.viewmodel.auth.RegistrationViewModel
import kotlinx.coroutines.launch

fun NavGraphBuilder.authGraph(
    navActions: AppNavigationActions,
    exitRequestManager: ExitRequestManager,
    onShowSnackbar: (String) -> Unit = {}
) {
    navigation<AuthGraph>(startDestination = LogInRoute) {

        composable<LogInRoute> {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val vm: LogInViewModel = viewModel(factory = LogInViewModel.Factory)
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        is AuthEvent.LoggedIn -> onShowSnackbar("Logged in successfully")
                        else -> {}
                    }
                }
            }

            LogInScreen (
                uiState = uiState,
                onBackClick = { navActions.navigateBack() },
                onLogInClick = {
                    vm.startGoogleLogIn()
                    coroutineScope.launch {
                        SessionManagerFacade.signIn(context).fold(
                            onSuccess = {
                                vm.completeGoogleLogIn { profileExists ->
                                    if(profileExists) {
                                        val currentUserId = SessionManagerFacade.currentUserId.orEmpty()
                                        navActions.navigateToProfile(currentUserId)
                                    } else {
                                        navActions.navigateTo(RegistrationRoute)
                                    }
                                }
                            },
                            onFailure = { error ->
                                vm.failGoogleLogIn(error)
                            }
                        )
                    }
                }
            )
        }

        composable<RegistrationRoute> {
            val vm: RegistrationViewModel = viewModel(factory = RegistrationViewModel.Factory)

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        is AuthEvent.OnboardingSkipped -> onShowSnackbar("Welcome!")
                        else -> {}
                    }
                }
            }

            RegistrationScreen(
                onSkipClick = {
                    vm.skipRegistration()
                    val currentUserId = SessionManagerFacade.currentUserId.orEmpty()
                    navActions.navigateToProfile(currentUserId)
                },
                onWizardClick = { navActions.navigateTo(OnboardingRoute) }
            )
        }

        composable<OnboardingRoute> {
            val cameraActions = LocalCameraActions.current
            val vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        is AuthEvent.ProfileCreated -> onShowSnackbar("Profile created successfully")
                        is AuthEvent.OnboardingSkipped -> onShowSnackbar("Welcome!")
                        AuthEvent.ExitAllowed -> exitRequestManager.executePendingNavigation()
                        AuthEvent.ExitCancelled -> exitRequestManager.cancelPendingNavigation()
                        else -> {}
                    }
                }
            }
            DisposableEffect(Unit) {
                exitRequestManager.setCurrentFormExitRequest { vm.requestExit() }
                onDispose { exitRequestManager.clearCurrentFormExitRequest() }
            }
            BackHandler {
                exitRequestManager.requestExitBefore { navActions.navigateTo(HomeRoute) }
            }
            OnboardingScreen(
                uiState = uiState,
                onNextClick = {
                    if (vm.nextStep()) {
                        vm.saveInitialProfile {
                            navActions.navigateTo(HomeRoute)
                        }
                    }
                },
                onBackClick = { vm.previousStep() },
                onSkipClick = {
                    exitRequestManager.requestExitBefore {
                        vm.confirmSkip()
                        navActions.navigateTo(HomeRoute)
                    }
                },
                onConfirmExit = { vm.confirmExit() },
                onDismissExitDialog = { vm.dismissExitDialog() },
                onProfileCameraClick = {
                    cameraActions.onTakePhoto { uri ->
                        vm.updateProfileImage(ProfileImageSource.Local(LocalImageInput.Camera(uri)))
                        navActions.navigateBack()
                    }
                    navActions.navigateTo(CameraRoute)
                },
                onProfileGalleryClick = {
                    cameraActions.onPickFromGallery { uri ->
                        vm.updateProfileImage(ProfileImageSource.Local(LocalImageInput.Gallery(uri)))
                    }
                },
                onRemoveProfileImageClick = { vm.removeProfileImage() },
                onCookingRoleChange = { vm.updateCookingRole(it) },
                onShortBioChange = { vm.updateShortBio(it) },
                onSocialProfilesChange = { vm.updateSocialProfiles(it) },
                onDietChange = { vm.updateDiet(it) },
                onOpenAllergenWizard = { vm.openAllergenWizard() },
                onCloseAllergenWizard = { vm.closeAllergenWizard() },
                onBackAllergenWizard = { vm.backAllergenWizard() },
                onAllergenCategoryChange = { vm.updateAllergenCategory(it) },
                onAllergenCategoryNext = { vm.confirmAllergenCategory() },
                onAllergenNameChange = { vm.updateAllergenName(it) },
                onConfirmAllergen = { vm.addSelectedAllergen() },
                onRemoveAllergen = { vm.removeAllergen(it) }
            )
        }
    }
}
