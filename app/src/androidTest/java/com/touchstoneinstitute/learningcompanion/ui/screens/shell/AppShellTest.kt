package com.touchstoneinstitute.learningcompanion.ui.screens.shell

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.touchstoneinstitute.learningcompanion.ui.theme.TSINLearningCompanionTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppShellTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun alertsDeepLink_opensAlertsContext() {
        composeRule.setContent {
            TSINLearningCompanionTheme {
                AppShell(
                    onSignOut = {},
                    initialDeepLink = "alerts",
                    homeContent = { modifier, _ -> StubRoute(modifier, "Home stub") },
                    scheduleContent = { modifier -> StubRoute(modifier, "Schedule stub") },
                    notificationsContent = { modifier, _ -> StubRoute(modifier, "Alerts stub") },
                    settingsContent = { modifier, _ -> StubRoute(modifier, "Settings stub") },
                )
            }
        }

        composeRule.onNodeWithText("Alerts stub").assertExists()
    }

    @Test
    fun bottomNav_navigatesBetweenInjectedRoutes() {
        composeRule.setContent {
            TSINLearningCompanionTheme {
                AppShell(
                    onSignOut = {},
                    initialDeepLink = "alerts",
                    homeContent = { modifier, _ -> StubRoute(modifier, "Home stub") },
                    scheduleContent = { modifier -> StubRoute(modifier, "Schedule stub") },
                    notificationsContent = { modifier, _ -> StubRoute(modifier, "Alerts stub") },
                    settingsContent = { modifier, _ -> StubRoute(modifier, "Settings stub") },
                )
            }
        }

        composeRule.onNodeWithText("Alerts stub").assertExists()
        composeRule.onNode(clickableText("Settings")).performClick()
        composeRule.onNodeWithText("Settings stub").assertExists()
        composeRule.onNode(clickableText("Schedule")).performClick()
        composeRule.onNodeWithText("Schedule stub").assertExists()
    }

    private fun clickableText(label: String): SemanticsMatcher {
        return androidx.compose.ui.test.hasText(label).and(androidx.compose.ui.test.hasClickAction())
    }
}

@Composable
private fun StubRoute(modifier: Modifier, label: String) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(text = label)
    }
}

