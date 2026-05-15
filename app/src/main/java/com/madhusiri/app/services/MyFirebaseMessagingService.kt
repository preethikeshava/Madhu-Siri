package com.madhusiri.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.app.PendingIntent
import android.content.Intent
import com.madhusiri.app.MainActivity

import com.madhusiri.app.core.notifications.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")

        // Handle data payload (Preferred for background processing)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message Data Payload: ${remoteMessage.data}")
            val type = remoteMessage.data["type"] ?: "daily"
            val title = remoteMessage.data["title"] ?: "New Alert"
            val message = remoteMessage.data["message"] ?: "You have a new message."
            
            showNotification(title, message, type)
        }

        // Handle notification payload (Fallback for basic messages)
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            showNotification(it.title ?: "Alert", it.body ?: "New message", "daily")
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // TODO: Send token to server
    }

    private fun showNotification(title: String, message: String, type: String) {
        val channelId = if (type == "emergency") {
            NotificationHelper.CHANNEL_EMERGENCY_ID
        } else {
            NotificationHelper.CHANNEL_DAILY_ID
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(if (type == "emergency") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        if (type == "emergency") {
            notificationBuilder.addAction(android.R.drawable.ic_dialog_map, "View on Map", pendingIntent)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}
