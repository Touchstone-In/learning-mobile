package com.touchstoneinstitute.learningcompanion.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Support
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.touchstoneinstitute.learningcompanion.BuildConfig
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceBanner
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionCard
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceSectionHeader
import com.touchstoneinstitute.learningcompanion.ui.components.GuidanceTone
import com.touchstoneinstitute.learningcompanion.ui.theme.spacing
import kotlinx.coroutines.launch

private const val HelpDeskUrl = "https://tsin.atlassian.net/servicedesk/customer/portal/8"
private const val PrivacyPolicyUrl = "https://touchstoneinstitute.ca/wp-content/uploads/Pre-Residency-Program-Canadian-Medicine-Primer-Privacy-Policy.pdf"
private const val RegistrationPolicyUrl = "https://d1wnwyag9usc7m.cloudfront.net/PRP%20Registration%20Policy.pdf"

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val spacing = MaterialTheme.spacing
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.screenVertical),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        val displayName = if (uiState.firstName != null || uiState.lastName != null) {
            listOfNotNull(uiState.firstName, uiState.lastName).joinToString(" ")
        } else {
            "Learner"
        }

        GuidanceSectionHeader(
            title = "Settings",
            supportingText = "Manage alerts, support resources, and account details.",
        )

        GuidanceSectionCard(
            title = "Account",
            supportingText = displayName,
        ) {
            uiState.email?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            uiState.role?.let {
                Text(
                    text = "Role: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        GuidanceSectionCard(
            title = "Alerts",
            supportingText = "Choose how the app keeps you informed.",
            leadingIcon = Icons.Outlined.Notifications,
        ) {
            if (uiState.preferencesSaving) {
                GuidanceBanner(
                    title = "Saving changes",
                    message = "We’re updating your alert preferences now.",
                    tone = GuidanceTone.Neutral,
                )
            }

            uiState.preferenceSuccessMessage?.let {
                GuidanceBanner(
                    title = "Saved",
                    message = it,
                    tone = GuidanceTone.Success,
                )
            }

            uiState.preferenceErrorMessage?.let {
                GuidanceBanner(
                    title = "Couldn’t save changes",
                    message = it,
                    tone = GuidanceTone.Critical,
                )
            }

            if (uiState.preferencesLoading) {
                Text(
                    text = "Syncing your alert settings…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!uiState.pushEnabled && !uiState.preferencesLoading) {
                GuidanceBanner(
                    title = "Push notifications are off",
                    message = "Turn them on to receive schedule and orientation reminders on this device.",
                    tone = GuidanceTone.Neutral,
                )
            }

            ToggleSettingsRow(
                title = "Push notifications",
                subtitle = "Receive mobile alerts on this device.",
                checked = uiState.pushEnabled,
                onCheckedChange = viewModel::togglePushEnabled,
            )
            HorizontalDivider()
            ToggleSettingsRow(
                title = "Schedule reminders",
                subtitle = "Get a heads-up before upcoming sessions.",
                checked = uiState.scheduleReminders,
                enabled = uiState.pushEnabled,
                onCheckedChange = viewModel::toggleScheduleReminders,
            )
            HorizontalDivider()
            ToggleSettingsRow(
                title = "Orientation reminders",
                subtitle = "Stay on top of orientation events and updates.",
                checked = uiState.orientationReminders,
                enabled = uiState.pushEnabled,
                onCheckedChange = viewModel::toggleOrientationReminders,
            )
        }

        GuidanceSectionCard(
            title = "Support and policies",
            supportingText = "Quick links for help, privacy, and program terms.",
            leadingIcon = Icons.Outlined.Support,
        ) {
            LinkSettingsRow(
                title = "Help",
                subtitle = "Open the TSIN helpdesk for support requests.",
                icon = Icons.Outlined.Support,
                onClick = { uriHandler.openUri(HelpDeskUrl) },
            )
            HorizontalDivider()
            LinkSettingsRow(
                title = "Privacy policy",
                subtitle = "Read how Touchstone Institute handles your information.",
                icon = Icons.Outlined.PrivacyTip,
                onClick = { uriHandler.openUri(PrivacyPolicyUrl) },
            )
            HorizontalDivider()
            LinkSettingsRow(
                title = "Terms and registration policy",
                subtitle = "Review current program registration expectations and terms.",
                icon = Icons.Outlined.VerifiedUser,
                onClick = { uriHandler.openUri(RegistrationPolicyUrl) },
            )
            HorizontalDivider()
            StaticSettingsRow(
                title = "App version",
                subtitle = "Version ${BuildConfig.VERSION_NAME}",
            )
        }

        GuidanceSectionCard(
            title = "Danger zone",
            supportingText = "Use this when you need to leave the app securely.",
        ) {
            LinkSettingsRow(
                title = "Sign out",
                subtitle = "Remove your current session from this device.",
                icon = Icons.AutoMirrored.Outlined.Logout,
                isDestructive = true,
                onClick = {
                    scope.launch {
                        viewModel.logout()
                        onSignOut()
                    }
                },
            )
        }
    }
}

@Composable
private fun ToggleSettingsRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = spacing.touchTarget + spacing.xs)
            .clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun LinkSettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    val titleColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val iconTint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = spacing.touchTarget + spacing.xs)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = iconTint)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!isDestructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = "$title opens externally",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StaticSettingsRow(title: String, subtitle: String) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = spacing.touchTarget + spacing.xs)
            .padding(vertical = spacing.xs),
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}