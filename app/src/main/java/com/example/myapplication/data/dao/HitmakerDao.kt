package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import kotlinx.coroutines.flow.Flow

@Dao
interface HitmakerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHitmaker(hitmaker: Hitmaker): Long

    @Update
    suspend fun updateHitmaker(hitmaker: Hitmaker)

    @Update
    suspend fun updateHitmakers(hitmakers: List<Hitmaker>)

    @Query("SELECT * FROM hitmakers ORDER BY priority ASC")
    fun getAllHitmakers(): Flow<List<Hitmaker>>

    @Query("SELECT * FROM hitmakers WHERE id = :id")
    suspend fun getHitmakerById(id: Int): Hitmaker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStatus(status: DailyStatus)

    @Query("SELECT * FROM daily_status WHERE hitmakerId = :hitmakerId")
    fun getDailyStatusesForHitmaker(hitmakerId: Int): Flow<List<DailyStatus>>
    
    @Query("SELECT * FROM daily_status")
    fun getAllDailyStatuses(): Flow<List<DailyStatus>>

    @Query("SELECT * FROM daily_status WHERE hitmakerId = :hitmakerId AND date = :date LIMIT 1")
    suspend fun getDailyStatusByDate(hitmakerId: Int, date: Long): DailyStatus?
    
    @Query("UPDATE hitmakers SET name = :newName WHERE id = :id")
    suspend fun updateHitmakerName(id: Int, newName: String)

    @Query("DELETE FROM hitmakers WHERE id = :id")
    suspend fun deleteHitmaker(id: Int)
}
