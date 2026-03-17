package com.touchstoneinstitute.learningcompanion.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object MfaChallenge : Screen("mfa_challenge/{email}/{mfaMethod}") {
        fun createRoute(email: String, mfaMethod: String) = "mfa_challenge/$email/$mfaMethod"
    }
    data object Home : Screen("home")
    data object Schedule : Screen("schedule")
    data object Notifications : Screen("notifications")
    data object Settings : Screen("settings")
}

