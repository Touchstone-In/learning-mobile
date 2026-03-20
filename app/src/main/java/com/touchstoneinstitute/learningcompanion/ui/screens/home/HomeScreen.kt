package com.touchstoneinstitute.learningcompanion.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.touchstoneinstitute.learningcompanion.data.remote.dto.KeyDateSummary
import com.touchstoneinstitute.learningcompanion.data.remote.dto.LearnerOverviewResponse
import com.touchstoneinstitute.learningcompanion.data.remote.dto.NextSessionSummary
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceBanner
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceLoadingState
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionCard
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionHeader
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceStatePanel
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceStatusChip
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceTone
import com.touchstoneinstitute.learningcompanion.ui.theme.spacing

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenSchedule: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing
    val hasContent = uiState.user != null || uiState.overview != null

    if (!hasContent && uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            GuidanceLoadingState(message = "Getting your learning snapshot ready...")
            HomeLoadingCard("Preparing your next step", "Checking your next session and best action.")
            HomeLoadingCard("Summarizing your program", "Program overview and key dates coming up.")
        }
        return
    }

    if (!hasContent && uiState.errorMessage != null) {
        GuidanceStatePanel(
            title = "Home is not available right now",
            message = "${uiState.errorMessage} Try again to load your current overview.",
            icon = Icons.Outlined.School,
            modifier = modifier.fillMaxSize()
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
            actionLabel = "Retry", onAction = viewModel::loadHome,
        )
        return
    }

    val greeting = uiState.user?.firstName?.let { "Welcome back, $it" } ?: "Welcome back"

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        GuidanceSectionHeader(
            title = greeting,
            supportingText = if (uiState.isRefreshing) "Refreshing…" else "Here's your current snapshot.",
            action = { IconButton(onClick = viewModel::loadHome) { Icon(Icons.Outlined.Refresh, "Refresh") } },
        )

        if (uiState.isCached) {
            GuidanceBanner("Offline view", "Last saved snapshot. Refresh when back online.", tone = GuidanceTone.Warning)
        } else if (uiState.errorMessage != null) {
            GuidanceBanner("Refresh incomplete", uiState.errorMessage.orEmpty(), tone = GuidanceTone.Warning)
        }

        val nextSession = uiState.overview?.nextSession
        if (nextSession != null) {
            NextSessionHero(session = nextSession, onOpenSchedule = onOpenSchedule)
        } else {
            NoSessionCard(onOpenSchedule)
        }

        uiState.overview?.let { ProgramOverviewCard(it) }

        val keyDates = uiState.overview?.keyDates.orEmpty()
        if (keyDates.isNotEmpty()) { KeyDatesCard(keyDates) }
    }
}

// ── Helper composables ──────────────────────────────────────────────────────

@Composable
private fun HomeLoadingCard(title: String, subtitle: String) {
    GuidanceSectionCard(
        title = title,
        supportingText = subtitle,
        leadingIcon = Icons.Outlined.School,
    ) { /* Skeleton placeholder — content will appear once loaded */ }
}

@Composable
private fun NextSessionHero(session: NextSessionSummary, onOpenSchedule: () -> Unit) {
    GuidanceSectionCard(
        title = "Next session",
        supportingText = session.sessionName ?: "Details below",
        leadingIcon = Icons.Outlined.CalendarMonth,
        action = {
            FilledTonalButton(onClick = onOpenSchedule) {
                Text("Full schedule")
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            if (session.day != null || session.period != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    session.day?.let { GuidanceStatusChip(label = it, tone = GuidanceTone.Brand) }
                    session.period?.let { GuidanceStatusChip(label = it, tone = GuidanceTone.Neutral) }
                }
            }
            if (session.track != null || session.group != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    session.track?.let { GuidanceStatusChip(label = it, tone = GuidanceTone.Neutral) }
                    session.group?.let { GuidanceStatusChip(label = it, tone = GuidanceTone.Neutral) }
                }
            }
        }
    }
}

@Composable
private fun NoSessionCard(onOpenSchedule: () -> Unit) {
    GuidanceSectionCard(
        title = "No upcoming session",
        supportingText = "There are no scheduled sessions right now.",
        leadingIcon = Icons.Outlined.CalendarMonth,
        action = {
            FilledTonalButton(onClick = onOpenSchedule) {
                Text("View schedule")
            }
        },
    ) {
        Text(
            text = "Check the full schedule or wait for new sessions to be published.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgramOverviewCard(overview: LearnerOverviewResponse) {
    GuidanceSectionCard(
        title = overview.programName ?: "Your program",
        supportingText = overview.programType,
        leadingIcon = Icons.Outlined.School,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            overview.applicationStatus?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Application", style = MaterialTheme.typography.bodyMedium)
                    GuidanceStatusChip(label = it, tone = GuidanceTone.Neutral)
                }
            }
            overview.registrationStatus?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Registration", style = MaterialTheme.typography.bodyMedium)
                    GuidanceStatusChip(label = it, tone = GuidanceTone.Neutral)
                }
            }
        }
    }
}

@Composable
private fun KeyDatesCard(dates: List<KeyDateSummary>) {
    GuidanceSectionCard(
        title = "Key dates",
        supportingText = "${dates.size} upcoming",
        leadingIcon = Icons.Outlined.Event,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            dates.forEach { kd ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = kd.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = kd.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

