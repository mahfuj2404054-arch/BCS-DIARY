package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.model.TaskEntity
import com.example.data.model.TaskWithDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.studyDao()

                val tasks: List<TaskEntity> = dao.getAllTasks().first()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val dueToday = tasks.filter { it.dueDate == todayStr }
                if (dueToday.isNotEmpty()) {
                    val taskDetailsList = dueToday.map { task ->
                        TaskWithDetails(
                            task = task,
                            subject = null,
                            topic = null,
                            isCompletedByCurrentUser = false
                        )
                    }
                    TaskNotificationHelper.sendDueTodayNotification(context, taskDetailsList, checkLimit = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
