package com.touchstoneinstitute.learningcompanion.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TsinPrimary,
    onPrimary = TsinOnPrimary,
    primaryContainer = TsinPrimaryContainer,
    onPrimaryContainer = TsinOnPrimaryContainer,
    secondary = TsinSecondary,
    onSecondary = TsinOnSecondary,
    secondaryContainer = TsinSecondaryContainer,
    onSecondaryContainer = TsinOnSecondaryContainer,
    tertiary = TsinTertiary,
    onTertiary = TsinOnTertiary,
    tertiaryContainer = TsinTertiaryContainer,
    onTertiaryContainer = TsinOnTertiaryContainer,
    error = TsinError,
    onError = TsinOnError,
    errorContainer = TsinErrorContainer,
    onErrorContainer = TsinOnErrorContainer,
    background = ConsoleBackground,
    onBackground = ConsoleOnSurface,
    surface = ConsoleSurface,
    onSurface = ConsoleOnSurface,
    surfaceVariant = ConsoleSurfaceVariant,
    onSurfaceVariant = ConsoleOnSurfaceVariant,
    outline = ConsoleOutline,
    outlineVariant = ConsoleOutlineVariant,
    surfaceContainerLow = ConsoleSurfaceVariant,
    surfaceContainer = ConsoleSurfaceContainer,
    surfaceContainerHigh = ConsoleSurfaceContainerHigh,
)

private val DarkColorScheme = darkColorScheme(
    primary = TsinPrimaryDark,
    onPrimary = TsinOnPrimaryDark,
    primaryContainer = TsinPrimaryContainerDark,
    onPrimaryContainer = TsinOnPrimaryContainerDark,
    secondary = TsinSecondary,
    onSecondary = TsinOnSecondary,
    secondaryContainer = TsinSecondaryContainer,
    onSecondaryContainer = TsinOnSecondaryContainer,
    tertiary = TsinTertiary,
    onTertiary = TsinOnTertiary,
    tertiaryContainer = TsinTertiaryContainer,
    onTertiaryContainer = TsinOnTertiaryContainer,
    error = TsinError,
    onError = TsinOnError,
    errorContainer = TsinErrorContainer,
    onErrorContainer = TsinOnErrorContainer,
    background = ConsoleBackgroundDark,
    onBackground = ConsoleOnSurfaceDark,
    surface = ConsoleSurfaceDark,
    onSurface = ConsoleOnSurfaceDark,
    surfaceVariant = ConsoleSurfaceVariantDark,
    onSurfaceVariant = ConsoleOnSurfaceVariantDark,
    outline = ConsoleOutlineVariant,
    outlineVariant = ConsoleOutline,
)

@Composable
fun TSINLearningCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TSINTypography,
        content = content
    )
}

