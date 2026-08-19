package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.TaskWithDetails

object TaskNotificationHelper {
    private const val CHANNEL_ID = "due_today_tasks_channel"
    private const val CHANNEL_NAME = "Tasks Due Today"
    private const val NOTIFICATION_ID = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for study tasks due today"
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendDueTodayNotification(context: Context, dueTodayTasks: List<TaskWithDetails>) {
        createNotificationChannel(context)

        val pendingTasks = dueTodayTasks.filter { !it.isCompletedByCurrentUser }
        if (pendingTasks.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val taskTitles = pendingTasks.take(3).joinToString(separator = "\n• ") { it.task.title }
        val contentText = if (pendingTasks.size == 1) {
            "Due today: ${pendingTasks.first().task.title}"
        } else {
            "You have ${pendingTasks.size} tasks due today!\n• $taskTitles"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ ${pendingTasks.size} Study Task${if (pendingTasks.size > 1) "s" else ""} Due Today!")
            .setContentText("Tap to view and complete your study goals.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
