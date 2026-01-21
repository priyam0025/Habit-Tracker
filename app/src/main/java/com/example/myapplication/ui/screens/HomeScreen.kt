package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.ui.viewmodel.HitmakerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HitmakerViewModel,
    onHitmakerClick: (Int) -> Unit
) {
    val hitmakers by viewModel.allHitmakers.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var hitmakerToRename by remember { mutableStateOf<Hitmaker?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Hitmaker",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Hitmaker")
            }
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hitmakers) { hitmaker ->
                HitmakerCard(
                    hitmaker = hitmaker,
                    onClick = { onHitmakerClick(hitmaker.id) },
                    onRename = { hitmakerToRename = hitmaker },
                    onDelete = { viewModel.deleteHitmaker(hitmaker.id) }
                )
            }
        }

        if (showAddDialog) {
            AddHitmakerDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, color ->
                    viewModel.addHitmaker(name, color)
                    showAddDialog = false
                }
            )
        }

        hitmakerToRename?.let { hitmaker ->
            RenameHitmakerDialog(
                currentName = hitmaker.name,
                onDismiss = { hitmakerToRename = null },
                onRename = { newName ->
                    viewModel.renameHitmaker(hitmaker.id, newName)
                    hitmakerToRename = null
                }
            )
        }
    }
}

@Composable
fun HitmakerCard(
    hitmaker: Hitmaker,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(hitmaker.color), CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                hitmaker.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Gray
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF2A2A2A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = Color.White) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RenameHitmakerDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Hitmaker", color = Color.White, style = MaterialTheme.typography.headlineSmall) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("New Name", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onRename(name) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF0D0D0D)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHitmakerDialog(onDismiss: () -> Unit, onAdd: (String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF22C55EL) }
    
    val colors = listOf(
        0xFF22C55EL, // Green
        0xFF3B82F6L, // Blue
        0xFFA855F7L, // Purple
        0xFFF97316L, // Orange
        0xFFEF4444L, // Red
        0xFFEAB308L  // Yellow
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Hitmaker", color = Color.White, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Habit Name", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(color), CircleShape)
                                .clickable { selectedColor = color }
                                .let { 
                                    if (selectedColor == color) {
                                        it.padding(4.dp).background(Color.White, CircleShape).padding(2.dp).background(Color(color), CircleShape)
                                    } else it
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, selectedColor) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Create")
            }
        },
        containerColor = Color(0xFF0D0D0D)
    )
}
