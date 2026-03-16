package com.touchstoneinstitute.learningcompanion.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.deepLinkDataStore: DataStore<Preferences> by preferencesDataStore(name = "deep_link_prefs")

/**
 * Manages pending deep links from push notifications.
 * Stores target screen for cold-start navigation and provides
 * consume-once semantics so the link is only used once.
 */
@Singleton
class DeepLinkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PENDING_SCREEN_KEY = stringPreferencesKey("pending_target_screen")
    }

    /**
     * Store a pending deep link target screen (e.g., "schedule", "home", "settings").
     * Called when a notification is tapped and the app may cold-start.
     */
    suspend fun setPendingScreen(targetScreen: String) {
        context.deepLinkDataStore.edit { prefs ->
            prefs[PENDING_SCREEN_KEY] = targetScreen
        }
    }

    /**
     * Consume and return the pending deep link target screen.
     * Returns null if no pending link. Clears after reading (consume-once).
     */
    suspend fun consumePendingScreen(): String? {
        val screen = context.deepLinkDataStore.data.map { prefs ->
            prefs[PENDING_SCREEN_KEY]
        }.first()
        if (screen != null) {
            context.deepLinkDataStore.edit { prefs ->
                prefs.remove(PENDING_SCREEN_KEY)
            }
        }
        return screen
    }
}

