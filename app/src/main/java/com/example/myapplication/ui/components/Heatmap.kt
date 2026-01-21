package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.entity.DailyStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearHeatmap(
    year: Int,
    dailyStatuses: List<DailyStatus>,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val statusMap = dailyStatuses.associate { 
        java.time.Instant.ofEpochMilli(it.date).atOffset(java.time.ZoneOffset.UTC).toLocalDate() to it.isDone 
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val months = (1..12).chunked(3)
        months.forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMonths.forEach { month ->
                    MonthView(
                        year = year,
                        month = month,
                        statusMap = statusMap,
                        activeColor = activeColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthView(
    year: Int,
    month: Int,
    statusMap: Map<LocalDate, Boolean>,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val yearMonth = YearMonth.of(year, month)
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Mon if we use Mon-Sun, but let's align with user: Mon-Sun
    // The user requested Mon-Sun. LocalDate.DayOfWeek.value is 1 (Mon) to 7 (Sun).
    // So offset for Mon is 0, Tue is 1, ..., Sun is 6.
    val offset = firstDayOfMonth.dayOfWeek.value - 1 

    val today = LocalDate.now()

    Column(modifier = modifier) {
        Text(
            text = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFB0B0B0),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(rows) { rowIndex ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(7) { colIndex ->
                        val dayIndex = rowIndex * 7 + colIndex
                        val dayOfMonth = dayIndex - offset + 1
                        
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayOfMonth)
                            val isDone = statusMap[date] ?: false
                            val isFuture = date.isAfter(today)
                            
                            val color = when {
                                isDone -> activeColor
                                isFuture -> Color(0xFF1A1A1A) // Very faint
                                else -> Color(0xFF2A2A2A) // Not done
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(color, shape = RoundedCornerShape(1.dp))
                            )
                        } else {
                            // Empty space before or after the month
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}
