package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.ui.viewmodel.HitmakerViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.HitmakerIcons
import java.time.LocalDate
import java.time.ZoneOffset

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.util.Locale
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.example.myapplication.widget.HabitWidgetReceiver
import com.example.myapplication.widget.PinWidgetReceiver

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HitmakerViewModel,
    onHitmakerClick: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hitmakers by viewModel.allHitmakers.collectAsState(initial = emptyList())
    val allStatuses by viewModel.allDailyStatuses.collectAsState(initial = emptyList())
    
    var showAddSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Hitmaker?>(null) }
    var habitToViewStats by remember { mutableStateOf<Hitmaker?>(null) }
    
    // Derived State for UI
    val sortedHitmakers = remember(hitmakers) { 
        hitmakers.sortedBy { it.priority } 
    }
    
    val topHitmaker = sortedHitmakers.firstOrNull()
    val themeColor = topHitmaker?.let { Color(it.color) } ?: Color(0xFF3B82F6) // Default Blue
    
    val topHitmakerStatuses = topHitmaker?.let { h -> 
        allStatuses.filter { it.hitmakerId == h.id } 
    } ?: emptyList()

    Scaffold(
        containerColor = Color(0xFF0B0B0B),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Title with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    tint = Color.Unspecified, // Keep original SVG colors (Black background + Green/Gray grid)
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hitmaker",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // 1. TOP WEEK BAR (Highest Priority)
            WeekBar(
                themeColor = themeColor,
                statuses = topHitmakerStatuses,
                modifier = Modifier.fillMaxWidth(),
                onDayClick = { date ->
                    // Optional: Navigate to day detail
                }
            )
            
            // 2. HABIT LIST
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Your Momentum",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                itemsIndexed(sortedHitmakers, key = { _, h -> h.id }) { index, hitmaker ->
                    HabitCard(
                        hitmaker = hitmaker,
                        statuses = allStatuses.filter { it.hitmakerId == hitmaker.id },
                        onCompleteClick = {
                            val today = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
                            val isDone = allStatuses.any { 
                                it.hitmakerId == hitmaker.id && it.date == today && it.isDone 
                            }
                            viewModel.markAsDone(hitmaker.id, !isDone)
                        },
                        onClick = { habitToEdit = hitmaker },
                        onDelete = { viewModel.deleteHitmaker(hitmaker.id) },
                        onRename = { habitToEdit = hitmaker },
                        onViewStats = { habitToViewStats = hitmaker },
                        onAddWidget = { pinHabitWidget(context, hitmaker.id) },
                        onMoveUp = { viewModel.moveUp(hitmaker.id) },
                        onMoveDown = { viewModel.moveDown(hitmaker.id) },
                        modifier = Modifier
                            .animateItem(
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                    )
                }
            }
        }
        
        if (showAddSheet) {
            HitmakerSheet(
                statuses = emptyList(), // No statuses for new habit
                onDismiss = { showAddSheet = false },
                onConfirm = { name, color, icon, time, days ->
                    viewModel.addHitmaker(name, color, icon, time, days)
                    showAddSheet = false
                }
            )
        }

        if (habitToEdit != null) {
            HitmakerSheet(
                initialHitmaker = habitToEdit,
                statuses = allStatuses.filter { it.hitmakerId == habitToEdit?.id },
                onDismiss = { habitToEdit = null },
                onConfirm = { name, color, icon, time, days ->
                    habitToEdit?.let { 
                        viewModel.updateHitmaker(it.copy(
                            name = name, 
                            color = color, 
                            icon = icon,
                            reminderTime = time,
                            reminderDays = days
                        )) 
                    }
                    habitToEdit = null
                }
            )
        }

        if (habitToViewStats != null) {
            StatsSheet(
                hitmaker = habitToViewStats!!,
                statuses = allStatuses.filter { it.hitmakerId == habitToViewStats?.id },
                onDismiss = { habitToViewStats = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSheet(
    hitmaker: Hitmaker,
    statuses: List<DailyStatus>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121212),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(hitmaker.color).copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = HitmakerIcons.getIcon(hitmaker.icon),
                        contentDescription = null,
                        tint = Color(hitmaker.color),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        hitmaker.name, 
                        style = MaterialTheme.typography.headlineSmall, 
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Habit Progress", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            YearHeatmap(
                year = LocalDate.now().year,
                dailyStatuses = statuses,
                activeColor = Color(hitmaker.color),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HitmakerSheet(
    initialHitmaker: Hitmaker? = null,
    statuses: List<DailyStatus>,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, String, String?, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initialHitmaker?.name ?: "") }
    var selectedColor by remember { mutableStateOf(initialHitmaker?.color ?: 0xFF3B82F6L) }
    var selectedIcon by remember { mutableStateOf(initialHitmaker?.icon ?: "Star") }
    var isPickingIcon by remember { mutableStateOf(false) }
    
    // Reminder state
    var showReminderOptions by remember { mutableStateOf(initialHitmaker?.reminderTime != null) }
    var reminderTime by remember { mutableStateOf(initialHitmaker?.reminderTime ?: "09:00") }
    var reminderDays by remember { mutableStateOf(initialHitmaker?.reminderDays ?: "EVERYDAY") }
    var showTimePicker by remember { mutableStateOf(false) }

    val colors = listOf(
        0xFF3B82F6L, // Blue
        0xFF22C55EL, // Green
        0xFFA855F7L, // Purple
        0xFFEAB308L, // Yellow
        0xFFF97316L, // Orange
        0xFFEF4444L, // Red
        0xFFEC4899L, // Pink
        0xFF06B6D4L, // Cyan
        0xFF10B981L  // Emerald
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121212),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        if (isPickingIcon) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.padding(16.dp)
                ) {
                    IconButton(onClick = { isPickingIcon = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.White
                        )
                    }
                    Text("Select Icon", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                IconPicker(
                    selectedIconName = selectedIcon,
                    habitColor = Color(selectedColor),
                    onIconSelected = { 
                        selectedIcon = it
                        isPickingIcon = false 
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    if (initialHitmaker == null) "New Habit" else "Edit Habit", 
                    style = MaterialTheme.typography.headlineSmall, 
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(selectedColor).copy(alpha = 0.1f))
                            .clickable { isPickingIcon = true }
                            .border(1.dp, Color(selectedColor).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            HitmakerIcons.getIcon(selectedIcon),
                            contentDescription = null,
                            tint = Color(selectedColor),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Habit Name", color = Color.DarkGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(selectedColor),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "Color", 
                    style = MaterialTheme.typography.labelLarge, 
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { selectedColor = color }
                                .padding(2.dp)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // Reminder Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Set Reminder", 
                        style = MaterialTheme.typography.titleMedium, 
                        color = Color.White
                    )
                    Switch(
                        checked = showReminderOptions,
                        onCheckedChange = { showReminderOptions = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(selectedColor),
                            checkedTrackColor = Color(selectedColor).copy(alpha = 0.3f)
                        )
                    )
                }

                if (showReminderOptions) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Time Picker Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { showTimePicker = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reminder Time", color = Color.Gray)
                        Text(reminderTime, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EVERYDAY", "WEEKENDS", "WEEKDAYS").forEach { type ->
                            val isSelected = reminderDays == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(selectedColor) else Color.White.copy(alpha = 0.05f))
                                    .clickable { reminderDays = type }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                    color = if (isSelected) Color.Black else Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = { 
                        if (name.isNotBlank()) {
                            onConfirm(
                                name, 
                                selectedColor, 
                                selectedIcon, 
                                if (showReminderOptions) reminderTime else null,
                                if (showReminderOptions) reminderDays else null
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        if (initialHitmaker == null) "Start Habit" else "Save Changes", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val initialHour = reminderTime.split(":")[0].toInt()
        val initialMinute = reminderTime.split(":")[1].toInt()
        val timePickerState = rememberTimePickerState(initialHour, initialMinute, is24Hour = true)

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK", color = Color(selectedColor)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E1E1E),
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

private fun pinHabitWidget(context: android.content.Context, habitId: Int) {
    val appWidgetManager = context.getSystemService(android.appwidget.AppWidgetManager::class.java)
    val myProvider = android.content.ComponentName(context, com.example.myapplication.widget.HabitWidgetReceiver::class.java)

    if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
        val successCallback = android.content.Intent(context, com.example.myapplication.widget.PinWidgetReceiver::class.java).apply {
            putExtra("habit_id", habitId)
        }
        
        val successPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            habitId,
            successCallback,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )

        appWidgetManager.requestPinAppWidget(myProvider, null, successPendingIntent)
    }
}
