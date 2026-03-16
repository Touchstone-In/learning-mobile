package com.touchstoneinstitute.learningcompanion.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.touchstoneinstitute.learningcompanion.R
import com.touchstoneinstitute.learningcompanion.data.local.DeepLinkManager
import com.touchstoneinstitute.learningcompanion.data.local.TokenManager
import com.touchstoneinstitute.learningcompanion.data.repository.NotificationRepository
import com.touchstoneinstitute.learningcompanion.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TSINFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var deepLinkManager: DeepLinkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "tsin_learning"
        const val CHANNEL_NAME = "Learning Updates"
        private var notificationId = 0
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token received")

        serviceScope.launch {
            if (tokenManager.isLoggedIn()) {
                notificationRepository.registerDeviceToken(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.data}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "TSIN Learning"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new update"

        val targetScreen = message.data["targetScreen"] ?: message.data["screen"]

        if (targetScreen != null) {
            serviceScope.launch {
                deepLinkManager.setPendingScreen(targetScreen)
            }
        }

        showNotification(title, body, targetScreen)
    }

    private fun showNotification(title: String, body: String, targetScreen: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            targetScreen?.let { putExtra("target_screen", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId++, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Training schedule updates and reminders"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}