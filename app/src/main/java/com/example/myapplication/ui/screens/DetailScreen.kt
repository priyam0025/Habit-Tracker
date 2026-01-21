package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.ui.components.YearHeatmap
import com.example.myapplication.ui.viewmodel.HitmakerViewModel
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    hitmakerId: Int,
    viewModel: HitmakerViewModel,
    onBack: () -> Unit
) {
    var hitmaker by remember { mutableStateOf<Hitmaker?>(null) }
    val statuses by viewModel.getDailyStatuses(hitmakerId).collectAsState(initial = emptyList())
    
    LaunchedEffect(hitmakerId) {
        hitmaker = viewModel.getHitmaker(hitmakerId)
    }

    val today = LocalDate.now()
    val todayStart = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
    val isDoneToday = statuses.any { it.date == todayStart && it.isDone }
    
    val completedDays = statuses.count { it.isDone }
    val longestStreak = calculateLongestStreak(statuses)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hitmaker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            hitmaker?.let { h ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = h.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Year ${today.year}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFB0B0B0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatItem(label = "Completed", value = "$completedDays / 365 days")
                    StatItem(label = "Longest Streak", value = "$longestStreak days")
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                YearHeatmap(
                    year = today.year,
                    dailyStatuses = statuses,
                    activeColor = Color(h.color),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.markAsDone(h.id) },
                    enabled = !isDoneToday,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(h.color),
                        disabledContainerColor = Color(0xFF1A1A1A),
                        contentColor = Color.White,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text(
                        if (isDoneToday) "Done for Today" else "Mark Today as Done",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFB0B0B0))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

fun calculateLongestStreak(statuses: List<DailyStatus>): Int {
    if (statuses.isEmpty()) return 0
    val dates = statuses.filter { it.isDone }
        .map { java.time.Instant.ofEpochMilli(it.date).atOffset(java.time.ZoneOffset.UTC).toLocalDate() }
        .distinct()
        .sorted()
    
    if (dates.isEmpty()) return 0
    
    var longest = 1
    var current = 1
    
    for (i in 1 until dates.size) {
        if (ChronoUnit.DAYS.between(dates[i-1], dates[i]) == 1L) {
            current++
        } else {
            longest = maxOf(longest, current)
            current = 1
        }
    }
    return maxOf(longest, current)
}
