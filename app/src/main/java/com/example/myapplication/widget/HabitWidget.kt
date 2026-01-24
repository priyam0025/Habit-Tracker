package com.example.myapplication.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.data.entity.DailyStatus
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlinx.coroutines.flow.first

class HabitWidget : GlanceAppWidget() {
    
    override val stateDefinition = PreferencesGlanceStateDefinition

    companion object {
        val HABIT_ID_KEY = intPreferencesKey("habit_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val habitId = prefs[HABIT_ID_KEY] ?: -1
        
        var habit: Hitmaker? = null
        var statuses: List<DailyStatus> = emptyList()
        
        if (habitId != -1) {
            val db = AppDatabase.getDatabase(context)
            habit = db.hitmakerDao().getHitmakerById(habitId)
            if (habit != null) {
                statuses = db.hitmakerDao().getAllDailyStatuses().first()
                    .filter { it.hitmakerId == habitId }
            }
        }

        provideContent {
            WidgetContent(habit, statuses)
        }
    }

    @Composable
    private fun WidgetContent(habit: Hitmaker?, statuses: List<DailyStatus>) {
        if (habit == null) {
            Box(
                modifier = GlanceModifier.fillMaxSize().background(Color.Black), 
                contentAlignment = Alignment.Center
            ) {
                Text("Habit not found", style = TextStyle(color = ColorProvider(Color.White)))
            }
            return
        }

        val habitColor = Color(habit.color)
        val today = LocalDate.now()
        val yearMonth = YearMonth.now()
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfMonth = yearMonth.atDay(1)
        val offset = firstDayOfMonth.dayOfWeek.value - 1

        val statusMap = statuses.associate { 
            java.time.Instant.ofEpochMilli(it.date).atOffset(java.time.ZoneOffset.UTC).toLocalDate() to it.isDone 
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0B0B0B))
                .padding(4.dp), // Reduced padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = habit.name,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp, // Slightly smaller text to allow more room for grid
                    fontWeight = androidx.glance.text.FontWeight.Bold
                )
            )
            Text(
                text = yearMonth.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()), // Short month
                style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 10.sp),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )
            
            val totalCells = offset + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(rows) { rowIndex ->
                    Row(
                        modifier = GlanceModifier.padding(vertical = 1.dp)
                    ) {
                        repeat(7) { colIndex ->
                            val dayIndex = rowIndex * 7 + colIndex
                            val dayOfMonth = dayIndex - offset + 1
                            
                            if (dayOfMonth in 1..daysInMonth) {
                                val date = yearMonth.atDay(dayOfMonth)
                                val isDone = statusMap[date] ?: false
                                val isFuture = date.isAfter(today)
                                
                                val color = when {
                                    isDone -> habitColor
                                    isFuture -> Color(0xFF1A1A1A)
                                    else -> Color(0xFF2A2A2A)
                                }

                                Box(
                                    modifier = GlanceModifier
                                        .size(18.dp) // Larger boxes
                                        .padding(1.dp)
                                        .background(color)
                                ) {}
                            } else {
                                Spacer(modifier = GlanceModifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}
