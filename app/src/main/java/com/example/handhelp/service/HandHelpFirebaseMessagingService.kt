package com.example.handhelp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.handhelp.MainActivity
import com.example.handhelp.R
import com.example.handhelp.data.model.Notification
import com.example.handhelp.repository.AuthRepository
import com.example.handhelp.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HandHelpFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var authRepository: AuthRepository

    companion object {
        const val CHANNEL_ID = "handhelp_notifications"
        const val CHANNEL_NAME = "HandHelp Notifications"

        // Récupérer le token FCM actuel
        fun getToken(onToken: (String) -> Unit) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                onToken(token)
            }
        }
    }

    // ─────────────────────────────────────────
    // Appelé quand l'app reçoit un message FCM
    // ─────────────────────────────────────────
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "HandHelp"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val missionId = remoteMessage.data["missionId"] ?: ""

        // Afficher la notification système
        showSystemNotification(title, body)

        // Sauvegarder dans Firestore
        val userId = authRepository.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.createNotification(
                Notification(
                    userId = userId,
                    title = title,
                    body = body,
                    missionId = missionId
                )
            )
        }
    }

    // ─────────────────────────────────────────
    // Appelé quand le token FCM change
    // ─────────────────────────────────────────
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = authRepository.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.saveFcmToken(userId, token)
        }
    }

    // ─────────────────────────────────────────
    // Afficher la notification système Android
    // ─────────────────────────────────────────
    private fun showSystemNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications HandHelp"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent pour ouvrir l'app au clic
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}