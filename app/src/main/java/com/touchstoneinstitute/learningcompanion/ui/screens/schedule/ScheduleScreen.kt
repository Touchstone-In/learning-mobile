package com.touchstoneinstitute.learningcompanion.ui.screens.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleDay
import com.touchstoneinstitute.learningcompanion.data.remote.dto.ScheduleWeek
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceBanner
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceLoadingState
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionCard
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionHeader
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceStatePanel
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceStatusChip
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceTone
import com.touchstoneinstitute.learningcompanion.ui.theme.spacing

@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing
    val hasContent = uiState.weeks.isNotEmpty()

    if (!hasContent && uiState.isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            GuidanceLoadingState(message = "Loading your schedule...")
            GuidanceSectionCard(
                title = "Getting this week ready",
                supportingText = "Organizing your sessions into an easy-to-scan view.",
                leadingIcon = Icons.Outlined.CalendarMonth,
            ) {
                Text(
                    text = "Your day-by-day session details will appear here once the sync finishes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    if (!hasContent && uiState.errorMessage != null) {
        GuidanceStatePanel(
            title = "Schedule unavailable",
            message = "${uiState.errorMessage} Retry to load the latest session plan.",
            icon = Icons.Outlined.CalendarMonth,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
            actionLabel = "Retry",
            onAction = viewModel::loadSchedule,
        )
        return
    }

    if (!hasContent) {
        GuidanceStatePanel(
            title = "No sessions scheduled",
            message = "When your training calendar is ready, it will appear here organized by week.",
            icon = Icons.Outlined.CalendarMonth,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
            actionLabel = "Refresh",
            onAction = viewModel::loadSchedule,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = spacing.screenHorizontal,
            vertical = spacing.screenVertical,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item {
            GuidanceSectionHeader(
                title = uiState.weekLabel ?: "Your schedule",
                supportingText = when {
                    uiState.isRefreshing -> "Refreshing your schedule..."
                    uiState.lastUpdated != null -> "Last updated ${uiState.lastUpdated}"
                    else -> "Scan your upcoming sessions at a glance."
                },
                action = {
                    IconButton(onClick = viewModel::loadSchedule) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh schedule")
                    }
                },
            )
        }

        if (uiState.isCached || uiState.errorMessage != null) {
            item {
                GuidanceBanner(
                    title = if (uiState.isCached) "Offline schedule" else "Refresh incomplete",
                    message = uiState.errorMessage
                        ?: "Showing the last saved schedule while you're offline.",
                    tone = GuidanceTone.Warning,
                )
            }
        }

        items(uiState.weeks, key = { it.weekName }) { week ->
            WeekCard(week = week)
        }
    }
}

@Composable
private fun WeekCard(week: ScheduleWeek) {
    GuidanceSectionCard(
        title = week.weekName,
        supportingText = if (week.days.size == 1) "1 session" else "${week.days.size} sessions",
        leadingIcon = Icons.Outlined.CalendarMonth,
    ) {
        if (week.days.isEmpty()) {
            Text(
                text = "No sessions scheduled for this week.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            week.days.forEachIndexed { index, day ->
                SessionRow(day = day)
                if (index < week.days.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs))
                }
            }
        }
    }
}

@Composable
private fun SessionRow(day: ScheduleDay) {
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
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.xxs),
            ) {
                Text(
                    text = day.session.sessionName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${day.day} · ${day.period}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                GuidanceStatusChip(label = day.session.track, tone = GuidanceTone.Neutral)
                GuidanceStatusChip(label = day.session.group, tone = GuidanceTone.Neutral)
            }
        }
    }
}

