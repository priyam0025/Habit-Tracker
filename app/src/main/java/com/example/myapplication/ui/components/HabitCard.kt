package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.ui.theme.HitmakerIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun HabitCard(
    hitmaker: Hitmaker,
    statuses: List<DailyStatus>,
    onCompleteClick: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onRename: () -> Unit = {},
    onViewStats: () -> Unit = {},
    onAddWidget: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(hitmaker.color)
    val today = LocalDate.now()
    val todayStatus = statuses.find { 
        Instant.ofEpochMilli(it.date).atOffset(ZoneOffset.UTC).toLocalDate() == today 
    }
    val isDone = todayStatus?.isDone == true

    val streak = calculateStreak(statuses)

    val surfaceColor by animateColorAsState(
        targetValue = if (isDragging) Color(0xFF222222) else Color(0xFF121212),
        label = "CardBg"
    )
    
    val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "Elevation")

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(elevation, RoundedCornerShape(20.dp))
            .border(
                width = 1.dp, 
                color = if (isDragging) habitColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with soft background
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(habitColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = HitmakerIcons.getIcon(hitmaker.icon),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hitmaker.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocalFireDepartment, 
                        contentDescription = null,
                        tint = if (streak > 0) habitColor else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streak day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Completion Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) habitColor else Color(0xFF1E1E1E)
                    )
                    .clickable { onCompleteClick() }
                    .border(
                        width = 1.dp,
                        color = if (isDone) habitColor else Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                if (isDone) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Done",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // More Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = Color.Gray
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = Color.White) },
                        onClick = { onRename(); showMenu = false },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.White) }
                    )
                    DropdownMenuItem(
                        text = { Text("View Progress", color = Color.White) },
                        onClick = { onViewStats(); showMenu = false },
                        leadingIcon = { Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = habitColor) }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Widget", color = Color.White) },
                        onClick = { onAddWidget(); showMenu = false },
                        leadingIcon = { Icon(Icons.Rounded.Widgets, contentDescription = null, tint = Color.White) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move Up", color = Color.White) },
                        onClick = { onMoveUp(); showMenu = false },
                        leadingIcon = { Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = Color.White) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move Down", color = Color.White) },
                        onClick = { onMoveDown(); showMenu = false },
                        leadingIcon = { Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = Color.White) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { onDelete(); showMenu = false },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

fun calculateStreak(statuses: List<DailyStatus>): Int {
    val today = LocalDate.now()
    val dateSet = statuses.filter { it.isDone }
        .map { Instant.ofEpochMilli(it.date).atOffset(ZoneOffset.UTC).toLocalDate() }
        .toSet()

    if (dateSet.isEmpty()) return 0

    var streak = 0
    var date = if (dateSet.contains(today)) today else today.minusDays(1)

    while (dateSet.contains(date)) {
        streak++
        date = date.minusDays(1)
    }
    
    return streak
}
