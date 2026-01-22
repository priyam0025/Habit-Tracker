package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.entity.DailyStatus
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.ZoneOffset
import java.util.Locale

@Composable
fun WeekBar(
    themeColor: Color,
    statuses: List<DailyStatus>,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val today = LocalDate.now()
    // Calculate start of week (Monday)
    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    
    val statusMap = remember(statuses) {
        statuses.associate { 
            java.time.Instant.ofEpochMilli(it.date).atOffset(ZoneOffset.UTC).toLocalDate() to it 
        }
    }

    val animatedThemeColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "WeekBarThemeColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedThemeColor.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
            .padding(vertical = 20.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..6).forEach { offset ->
                val date = startOfWeek.plusDays(offset.toLong())
                val isToday = date == today
                val isFuture = date.isAfter(today)
                val status = statusMap[date]
                val progress = when {
                    status == null -> 0f
                    status.isDone -> 1f
                    else -> status.progress
                }

                DayRing(
                    date = date,
                    isToday = isToday,
                    isFuture = isFuture,
                    progress = progress,
                    themeColor = animatedThemeColor,
                    onClick = { onDayClick(date) }
                )
            }
        }
    }
}

@Composable
fun DayRing(
    date: LocalDate,
    isToday: Boolean,
    isFuture: Boolean,
    progress: Float,
    themeColor: Color,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumLow),
        label = "Progress"
    )
    
    val opacity = if (isFuture) 0.4f else 1f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .alpha(opacity)
            .clickable(enabled = true, onClick = onClick)
    ) {
        // Day Label (M, T, W...)
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) Color.White else Color.Gray,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp
        )

        // Ring Container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .let {
                    if (isToday) {
                        it.shadow(
                            elevation = 12.dp, 
                            shape = CircleShape, 
                            ambientColor = themeColor.copy(alpha = 0.5f),
                            spotColor = themeColor.copy(alpha = 0.5f)
                        )
                    } else it
                }
        ) {
            // Background Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF1A1A1A),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            
            // Progress Ring
            Canvas(modifier = Modifier.fillMaxSize().padding(1.5.dp)) {
                // Glow effect for today's ring
                if (isToday) {
                    drawCircle(
                        color = themeColor.copy(alpha = 0.15f),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }

                drawArc(
                    color = if (animatedProgress > 0f) themeColor else Color(0xFF2A2A2A),
                    startAngle = -90f,
                    sweepAngle = 360f * (if (animatedProgress > 0f) animatedProgress else 1f),
                    useCenter = false,
                    style = Stroke(
                        width = 3.2.dp.toPx(), 
                        cap = StrokeCap.Round
                    ),
                    alpha = if (animatedProgress > 0f) 1f else 0.5f
                )
                
                if (animatedProgress > 0f) {
                    // Inner "glow" for filled ring
                    drawArc(
                        color = themeColor.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(
                            width = 6.dp.toPx(), 
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            // Date Number
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) Color.White else Color.Gray,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }
    }
}
