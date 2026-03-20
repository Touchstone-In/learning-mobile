package com.touchstoneinstitute.learningcompanion.ui.screens.auth

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import com.touchstoneinstitute.learningcompanion.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenHiltTest {

    private val mainActivityRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: TestRule = RuleChain
        .outerRule(ClearSessionRule())
        .around(mainActivityRule)

    @Test
    fun appLaunch_showsRealLoginScreenWhenNoStoredSessionExists() {
        mainActivityRule.waitUntil(timeoutMillis = 5_000) {
            try {
                mainActivityRule.onNodeWithText("Sign in to your account").fetchSemanticsNode()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        mainActivityRule.onNodeWithText("Sign in to your account").assertExists()
        mainActivityRule.onNodeWithText("Email").assertExists()
        mainActivityRule.onNodeWithText("Password").assertExists()
        mainActivityRule.onNodeWithText("Sign in").assertExists()
    }
}

private class ClearSessionRule : ExternalResource() {
    override fun before() {
        clearTokens()
    }

    override fun after() {
        clearTokens()
    }

    private fun clearTokens() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        runBlocking {
            TokenManager(context).clearTokens()
        }
    }
}