package com.example.myapplication.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.datastore.preferences.core.edit
import com.example.myapplication.widget.HabitWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PinWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val habitId = intent.getIntExtra("habit_id", -1)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && habitId != -1) {
            MainScope().launch {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[HabitWidget.HABIT_ID_KEY] = habitId
                }
                HabitWidget().update(context, glanceId)
            }
        }
    }
}
