package com.touchstoneinstitute.learningcompanion.ui.screens.notifications

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.touchstoneinstitute.learningcompanion.ui.theme.TSINLearningCompanionTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun alertsScreen_rendersGuidanceStateAndPreviewContent() {
        composeRule.setContent {
            TSINLearningCompanionTheme {
                NotificationsScreen()
            }
        }

        composeRule.onNodeWithText("Alerts").assertExists()
        composeRule.onNodeWithText("You’re all caught up").assertExists()
        composeRule.onNodeWithText("What alerts will look like").assertExists()
        composeRule.onNodeWithText("Open alert preferences").assertExists()
    }

    @Test
    fun openAlertPreferences_invokesCallback() {
        var openSettingsCount = 0

        composeRule.setContent {
            TSINLearningCompanionTheme {
                NotificationsScreen(onOpenSettings = { openSettingsCount += 1 })
            }
        }

        composeRule.onNodeWithText("Open alert preferences").performClick()

        composeRule.runOnIdle {
            assertEquals(1, openSettingsCount)
        }
    }
}