package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.TaskWithDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TaskNotificationHelper {
    private const val CHANNEL_ID = "due_today_tasks_channel"
    private const val CHANNEL_NAME = "Tasks Due Today"
    private const val NOTIFICATION_ID = 2001
    private const val PREFS_NAME = "task_notification_prefs"
    private const val KEY_LAST_DATE = "last_notification_date"
    private const val KEY_COUNT_TODAY = "notification_count_today"
    private const val KEY_LAST_TIMESTAMP = "last_notification_timestamp"

    private const val MAX_NOTIFICATIONS_PER_DAY = 2
    // Minimum 3 hours interval between automatic notifications
    private const val MIN_INTERVAL_MS = 3 * 60 * 60 * 1000L

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

    /**
     * Schedules 2 automatic daily alarms (e.g. 9:00 AM and 3:00 PM)
     */
    fun scheduleTwiceDailyAlarms(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // Alarm 1: 9:00 AM
            scheduleAlarmAtHour(context, alarmManager, requestCode = 101, hour = 9, minute = 0)
            // Alarm 2: 3:00 PM (15:00)
            scheduleAlarmAtHour(context, alarmManager, requestCode = 102, hour = 15, minute = 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleAlarmAtHour(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        hour: Int,
        minute: Int
    ) {
        val intent = Intent(context, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sends due today notification if tasks exist and limit (< 2 notifications/day) allows.
     */
    fun sendDueTodayNotification(
        context: Context,
        dueTodayTasks: List<TaskWithDetails>,
        checkLimit: Boolean = true
    ) {
        val pendingTasks = dueTodayTasks.filter { !it.isCompletedByCurrentUser }
        if (pendingTasks.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nowMs = System.currentTimeMillis()

        if (checkLimit) {
            val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
            var countToday = if (lastDate == todayStr) prefs.getInt(KEY_COUNT_TODAY, 0) else 0
            val lastTimeMs = if (lastDate == todayStr) prefs.getLong(KEY_LAST_TIMESTAMP, 0L) else 0L

            // Enforce max 2 times per day limit
            if (countToday >= MAX_NOTIFICATIONS_PER_DAY) {
                return
            }

            // Enforce minimum time gap between the 2 daily notifications
            if (countToday > 0 && (nowMs - lastTimeMs) < MIN_INTERVAL_MS) {
                return
            }

            // Record this notification dispatch
            countToday += 1
            prefs.edit()
                .putString(KEY_LAST_DATE, todayStr)
                .putInt(KEY_COUNT_TODAY, countToday)
                .putLong(KEY_LAST_TIMESTAMP, nowMs)
                .apply()
        }

        createNotificationChannel(context)

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
