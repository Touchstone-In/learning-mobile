package com.touchstoneinstitute.learningcompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

    // Status bar color is managed by enableEdgeToEdge() in MainActivity

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TSINTypography,
        content = content
    )
}

