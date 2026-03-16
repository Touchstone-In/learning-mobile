package com.touchstoneinstitute.learningcompanion.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Schedule : Screen("schedule")
    data object Notifications : Screen("notifications")
    data object Settings : Screen("settings")
}

