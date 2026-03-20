package com.touchstoneinstitute.learningcompanion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TsinSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val screenHorizontal: Dp = 20.dp,
    val screenVertical: Dp = 16.dp,
    val cardPadding: Dp = 18.dp,
    val touchTarget: Dp = 48.dp,
)

val LocalTsinSpacing = staticCompositionLocalOf { TsinSpacing() }

val MaterialTheme.spacing: TsinSpacing
    @Composable
    get() = LocalTsinSpacing.current