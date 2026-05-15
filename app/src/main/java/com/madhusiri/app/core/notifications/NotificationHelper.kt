package com.madhusiri.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

object NotificationHelper {

    const val CHANNEL_EMERGENCY_ID = "emergency_alerts"
    const val CHANNEL_DAILY_ID = "daily_summaries"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Emergency Alerts Channel
            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY_ID,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts like active spraying nearby"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            // Daily Summaries Channel
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_ID,
                "Daily Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General updates and hive health reports"
                enableLights(true)
                lightColor = Color.BLUE
            }

            notificationManager.createNotificationChannels(listOf(emergencyChannel, dailyChannel))
        }
    }
}
