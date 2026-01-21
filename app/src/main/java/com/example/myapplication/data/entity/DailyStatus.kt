package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_status",
    foreignKeys = [
        ForeignKey(
            entity = Hitmaker::class,
            parentColumns = ["id"],
            childColumns = ["hitmakerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hitmakerId"])]
)
data class DailyStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hitmakerId: Int,
    val date: Long, // Start of day timestamp
    val isDone: Boolean
)
