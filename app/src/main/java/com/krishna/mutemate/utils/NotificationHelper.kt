package com.krishna.mutemate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.krishna.mutemate.R

object NotificationHelper {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mute Schedule Notifications"
            val descriptionText = "Notifies when a mute schedule is active"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPersistentNotification(context: Context, title: String, message: String, notificationId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return // Don't show the notification if permission is denied
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.mute_mate)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority to keep it visible
            .setOngoing(true) // This makes the notification persistent
            .setAutoCancel(false) // Prevents the user from dismissing it
            .build()

        NotificationManagerCompat.from(context).notify(notificationId.toInt(), notification)
    }

    fun dismissNotification(context: Context, notificationId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId.toInt())
    }
}