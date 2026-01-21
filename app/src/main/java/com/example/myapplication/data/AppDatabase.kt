package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.HitmakerDao
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker

@Database(entities = [Hitmaker::class, DailyStatus::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hitmakerDao(): HitmakerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hitmaker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
