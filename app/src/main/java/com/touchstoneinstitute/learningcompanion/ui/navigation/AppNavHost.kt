package com.touchstoneinstitute.learningcompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.touchstoneinstitute.learningcompanion.ui.screens.auth.LoginScreen
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
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(SHELL_ROUTE) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
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