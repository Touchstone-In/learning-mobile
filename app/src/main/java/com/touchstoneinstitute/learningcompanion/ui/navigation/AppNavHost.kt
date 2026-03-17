package com.touchstoneinstitute.learningcompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.touchstoneinstitute.learningcompanion.ui.screens.auth.LoginScreen
import com.touchstoneinstitute.learningcompanion.ui.screens.auth.LoginViewModel
import com.touchstoneinstitute.learningcompanion.ui.screens.auth.MfaVerificationScreen
import com.touchstoneinstitute.learningcompanion.ui.screens.shell.AppShell

private const val SHELL_ROUTE = "shell"

@Composable
fun AppNavHost(deepLinkTarget: String? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val uiState by loginViewModel.uiState.collectAsState()

            // Navigate to MFA screen when required
            LaunchedEffect(uiState.requiresMfa) {
                if (uiState.requiresMfa && uiState.mfaEmail != null) {
                    navController.navigate(
                        Screen.MfaChallenge.createRoute(
                            email = uiState.mfaEmail!!,
                            mfaMethod = uiState.mfaMethod ?: "App"
                        )
                    )
                }
            }

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(SHELL_ROUTE) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.MfaChallenge.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("mfaMethod") { type = NavType.StringType }
            )
        ) {
            MfaVerificationScreen(
                onVerificationSuccess = {
                    navController.navigate(SHELL_ROUTE) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SHELL_ROUTE) {
            AppShell(
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(SHELL_ROUTE) { inclusive = true }
                    }
                },
                initialDeepLink = deepLinkTarget
            )
        }
    }
}