package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.data.repository.HitmakerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

class HitmakerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HitmakerRepository
    val allHitmakers: Flow<List<Hitmaker>>

    init {
        val dao = AppDatabase.getDatabase(application).hitmakerDao()
        repository = HitmakerRepository(dao)
        allHitmakers = repository.allHitmakers
    }

    fun addHitmaker(name: String, color: Long) {
        viewModelScope.launch {
            val hitmaker = Hitmaker(
                name = name,
                color = color,
                startDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            )
            repository.insertHitmaker(hitmaker)
        }
    }

    fun markAsDone(hitmakerId: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val existing = repository.getDailyStatusByDate(hitmakerId, today)
            if (existing == null) {
                repository.insertDailyStatus(
                    DailyStatus(
                        hitmakerId = hitmakerId,
                        date = today,
                        isDone = true
                    )
                )
            }
        }
    }

    fun getDailyStatuses(hitmakerId: Int): Flow<List<DailyStatus>> {
        return repository.getDailyStatusesForHitmaker(hitmakerId)
    }
    
    suspend fun getHitmaker(id: Int): Hitmaker? {
        return repository.getHitmakerById(id)
    }

    fun renameHitmaker(id: Int, newName: String) {
        viewModelScope.launch {
            repository.updateHitmakerName(id, newName)
        }
    }

    fun deleteHitmaker(id: Int) {
        viewModelScope.launch {
            repository.deleteHitmaker(id)
        }
    }
}
