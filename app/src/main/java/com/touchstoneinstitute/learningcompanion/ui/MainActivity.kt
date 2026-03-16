package com.touchstoneinstitute.learningcompanion.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.touchstoneinstitute.learningcompanion.data.local.DeepLinkManager
import com.touchstoneinstitute.learningcompanion.ui.navigation.AppNavHost
import com.touchstoneinstitute.learningcompanion.ui.theme.TSINLearningCompanionTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var deepLinkManager: DeepLinkManager

    /** Holds the deep-link target screen from a notification tap. */
    private val pendingDeepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for deep link from notification tap (cold start)
        handleDeepLinkIntent(intent)

        // Also check DataStore for deep links persisted by background FCM
        CoroutineScope(Dispatchers.IO).launch {
            val stored = deepLinkManager.consumePendingScreen()
            if (stored != null) {
                pendingDeepLink.value = stored
            }
        }

        setContent {
            TSINLearningCompanionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(deepLinkTarget = pendingDeepLink.value)
                }
            }
        }
    }

    /** Handle deep link when app is resumed from background via notification. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val target = intent?.getStringExtra("target_screen")
        if (target != null) {
            pendingDeepLink.value = target
            // Clear so we don't re-navigate on config change
            intent.removeExtra("target_screen")
        }
    }
}