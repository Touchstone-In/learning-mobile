package com.touchstoneinstitute.learningcompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.touchstoneinstitute.learningcompanion.ui.theme.TsinOnSuccessContainer
import com.touchstoneinstitute.learningcompanion.ui.theme.TsinOnWarningContainer
import com.touchstoneinstitute.learningcompanion.ui.theme.TsinSuccessContainer
import com.touchstoneinstitute.learningcompanion.ui.theme.TsinWarningContainer
import com.touchstoneinstitute.learningcompanion.ui.theme.spacing

enum class GuidanceTone {
    Brand,
    Neutral,
    Success,
    Warning,
    Critical,
}

@Composable
fun GuidanceSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            supportingText?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        action?.invoke()
    }
}

@Composable
fun GuidanceSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(spacing.cardPadding), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(10.dp)
                    ) {
                        Icon(leadingIcon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                GuidanceSectionHeader(
                    title = title,
                    supportingText = supportingText,
                    modifier = Modifier.weight(1f),
                    action = action,
                )
            }
            content()
        }
    }
}

@Composable
fun GuidanceStatusChip(label: String, tone: GuidanceTone = GuidanceTone.Brand, modifier: Modifier = Modifier) {
    val (container, content) = toneColors(tone)
    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun GuidanceBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    tone: GuidanceTone = GuidanceTone.Neutral,
) {
    val (container, content) = toneColors(tone)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(18.dp))
            .padding(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = content, fontWeight = FontWeight.SemiBold)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = content)
    }
}

@Composable
fun GuidanceStatePanel(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .padding(spacing.sm)
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun TsinSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: (@Composable () -> Unit)? = null,
) = GuidanceSectionHeader(
    title = title,
    modifier = modifier,
    supportingText = supportingText,
    action = action,
)

@Composable
fun TsinStatusChip(
    label: String,
    tone: GuidanceTone = GuidanceTone.Brand,
    modifier: Modifier = Modifier,
) = GuidanceStatusChip(label = label, tone = tone, modifier = modifier)

@Composable
fun TsinStateContainer(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) = GuidanceStatePanel(
    title = title,
    message = message,
    icon = icon,
    modifier = modifier,
    actionLabel = actionLabel,
    onAction = onAction,
)

@Composable
fun GuidanceLoadingState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun toneColors(tone: GuidanceTone): Pair<Color, Color> = when (tone) {
    GuidanceTone.Brand -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f) to MaterialTheme.colorScheme.onPrimaryContainer
    GuidanceTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f) to MaterialTheme.colorScheme.onSurface
    GuidanceTone.Success -> TsinSuccessContainer.copy(alpha = 0.95f) to TsinOnSuccessContainer
    GuidanceTone.Warning -> TsinWarningContainer.copy(alpha = 0.95f) to TsinOnWarningContainer
    GuidanceTone.Critical -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
}