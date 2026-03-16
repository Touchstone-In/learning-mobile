package com.touchstoneinstitute.learningcompanion.ui.screens.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.touchstoneinstitute.learningcompanion.ui.navigation.Screen
import com.touchstoneinstitute.learningcompanion.ui.screens.home.HomeScreen
import com.touchstoneinstitute.learningcompanion.ui.screens.notifications.NotificationsScreen
import com.touchstoneinstitute.learningcompanion.ui.screens.schedule.ScheduleScreen
import com.touchstoneinstitute.learningcompanion.ui.screens.settings.SettingsScreen

data class NavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    NavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Screen.Schedule, "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    NavItem(Screen.Notifications, "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    NavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(onSignOut: () -> Unit, initialDeepLink: String? = null) {
    // Resolve deep link target to a valid route, defaulting to Home
    val startRoute = when (initialDeepLink) {
        "schedule" -> Screen.Schedule.route
        "notifications" -> Screen.Notifications.route
        "settings" -> Screen.Settings.route
        "home" -> Screen.Home.route
        else -> Screen.Home.route
    }
    var currentRoute by rememberSaveable { mutableStateOf(startRoute) }
    val currentTitle = navItems.find { it.screen.route == currentRoute }?.label ?: "TSIN Learning"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            ConsoleBottomNav(
                currentRoute = currentRoute,
                onNavigate = { screen -> currentRoute = screen.route }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentRoute) {
            Screen.Home.route -> HomeScreen(modifier)
            Screen.Schedule.route -> ScheduleScreen(modifier)
            Screen.Notifications.route -> NotificationsScreen(modifier)
            Screen.Settings.route -> SettingsScreen(modifier = modifier, onSignOut = onSignOut)
        }
    }
}

@Composable
private fun ConsoleBottomNav(currentRoute: String, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    }
}