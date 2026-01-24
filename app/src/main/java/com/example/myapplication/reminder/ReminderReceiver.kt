package com.example.myapplication.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import android.app.PendingIntent
import com.example.myapplication.R
import java.util.*

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra("habit_id", -1)
        val habitName = intent.getStringExtra("habit_name") ?: "Habit"
        val habitDays = intent.getStringExtra("habit_days") ?: "EVERYDAY"

        if (!shouldShowToday(habitDays)) {
            // Check if we need to reschedule for tomorrow
            // (Alarms are one-shot in our implementation for simplicity, so reschedule always)
            // But wait, the NotificationHelper will be called when we add/edit.
            // For daily repetition, we should reschedule it here.
            reschedule(context, intent)
            return
        }

        showNotification(context, habitId, habitName)
        reschedule(context, intent)
    }

    private fun shouldShowToday(days: String): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        return when (days) {
            "EVERYDAY" -> true
            "WEEKENDS" -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            "WEEKDAYS" -> dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
            else -> true
        }
    }

    private fun showNotification(context: Context, habitId: Int, habitName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            habitId, 
            contentIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
            .setContentTitle("Self Message: $habitName")
            .setContentText("Reminder: It's time to work on your habit!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(habitId, notification)
    }

    private fun reschedule(context: Context, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val habitId = intent.getIntExtra("habit_id", -1)
        val hour = intent.getIntExtra("habit_hour", -1)
        val minute = intent.getIntExtra("habit_minute", -1)

        if (habitId == -1 || hour == -1 || minute == -1) return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.DATE, 1) // Set for tomorrow
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
