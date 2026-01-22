package com.example.myapplication.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.ui.viewmodel.HitmakerViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.HitmakerIcons
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HitmakerViewModel,
    onHitmakerClick: (Int) -> Unit
) {
    val hitmakers by viewModel.allHitmakers.collectAsState(initial = emptyList())
    val allStatuses by viewModel.allDailyStatuses.collectAsState(initial = emptyList())
    
    var showAddSheet by remember { mutableStateOf(false) }
    
    // Derived State for UI
    // Sorting by priority ensure the top one is indeed the priority habit
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
                        onClick = { onHitmakerClick(hitmaker.id) },
                        modifier = Modifier
                            .animateItem(
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                            .combinedClickable(
                                onClick = { onHitmakerClick(hitmaker.id) },
                                onLongClick = {
                                    // Implementation of Priority move: Move to top
                                    val newList = sortedHitmakers.toMutableList()
                                    val item = newList.removeAt(index)
                                    newList.add(0, item)
                                    viewModel.reorderHitmakers(newList)
                                }
                            )
                    )
                }
            }
        }
        
        if (showAddSheet) {
            AddHitmakerSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { name, color, icon ->
                    viewModel.addHitmaker(name, color, icon)
                    showAddSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHitmakerSheet(onDismiss: () -> Unit, onAdd: (String, Long, String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF3B82F6L) } // Default Blue
    var selectedIcon by remember { mutableStateOf("Star") }
    var isPickingIcon by remember { mutableStateOf(false) }

    val colors = listOf(
        0xFF3B82F6L, // Blue
        0xFF22C55EL, // Green
        0xFFA855F7L, // Purple
        0xFFEAB308L, // Yellow
        0xFFF97316L, // Orange
        0xFFEF4444L  // Red
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
            ) {
                Text(
                    "New Habit", 
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
                        placeholder = { Text("Gym, Study, Meditate...", color = Color.DarkGray) },
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
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "Assign Color", 
                    style = MaterialTheme.typography.labelLarge, 
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { selectedColor = color }
                                .padding(2.dp)
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = { if (name.isNotBlank()) onAdd(name, selectedColor, selectedIcon) },
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
                    Text("Start Habit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
