package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hitmakers")
data class Hitmaker(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Long, // Hex color as Long (e.g. 0xFF22C55EL)
    val startDate: Long // Timestamp
)
