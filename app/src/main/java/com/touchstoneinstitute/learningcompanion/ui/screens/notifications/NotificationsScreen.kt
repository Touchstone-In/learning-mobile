package com.touchstoneinstitute.learningcompanion.ui.screens.notifications


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerResultSummary
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceBanner
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceLoadingState
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionCard
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionHeader
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceStatePanel
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceStatusChip
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceTone
import com.touchstoneinstitute.learningcompanion.ui.theme.spacing



@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        GuidanceSectionHeader(
            title = "Your results",
            supportingText = if (uiState.isRefreshing) "Refreshing…"
                else "Published results and program updates.",
            action = {
                IconButton(onClick = viewModel::loadAlerts) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh results")
                }
            },
        )

        uiState.errorMessage?.let { error ->
            GuidanceBanner(
                title = "Couldn't load results",
                message = error,
                tone = GuidanceTone.Warning,
            )
        }

        when {
            uiState.isLoading -> {
                GuidanceLoadingState(message = "Loading your results…")
            }
            uiState.results.isEmpty() && uiState.errorMessage == null -> {
                GuidanceStatePanel(
                    title = "You're all caught up",
                    message = "When your results are released or important updates arrive, they'll appear here.",
                    icon = Icons.Outlined.NotificationsNone,
                    actionLabel = "Manage alert preferences",
                    onAction = onOpenSettings,
                )
            }
            else -> {
                if (uiState.results.isNotEmpty()) {
                    ResultsSection(uiState.results, spacing, onOpenSettings)
                }
            }
        }
    }
}

@Composable
private fun ResultsSection(
    results: List<LearnerResultSummary>,
    spacing: com.touchstoneinstitute.learningcompanion.ui.theme.TsinSpacing,
    onOpenSettings: () -> Unit,
) {
    GuidanceSectionCard(
        title = "Released results",
        supportingText = "Your published program results are shown below.",
        leadingIcon = Icons.Outlined.Assessment,
        action = {
            GuidanceStatusChip(
                label = "${results.size} result${if (results.size != 1) "s" else ""}",
                tone = GuidanceTone.Brand,
            )
        },
    ) {
        results.forEach { result -> ResultRow(result) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Log in to the portal at learn.tsin.ca to download your results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FilledTonalButton(
            onClick = onOpenSettings,
            modifier = Modifier.padding(top = spacing.xs),
        ) {
            Text("Manage alert preferences")
        }
    }
}


@Composable
private fun ResultRow(result: LearnerResultSummary) {
    val spacing = MaterialTheme.spacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.xxs),
                ) {
                    Text(
                        text = result.name ?: "Program Result",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    result.postgraduateTrainingProgram?.let { program ->
                        Text(
                            text = program,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                GuidanceStatusChip(
                    label = result.attendanceStatus ?: "Released",
                    tone = when (result.attendanceStatus?.uppercase()) {
                        "COMPLETE" -> GuidanceTone.Success
                        "INCOMPLETE" -> GuidanceTone.Warning
                        else -> GuidanceTone.Neutral
                    },
                )
            }

            result.comment?.let { comment ->
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            result.publishedAt?.let { date ->
                Text(
                    text = "Released: ${date.substringBefore("T")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
