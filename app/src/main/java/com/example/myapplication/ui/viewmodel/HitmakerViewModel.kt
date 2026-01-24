package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import com.example.myapplication.data.repository.HitmakerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import com.example.myapplication.widget.HabitWidget
import androidx.glance.appwidget.updateAll

class HitmakerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HitmakerRepository
    val allHitmakers: Flow<List<Hitmaker>>
    private val notificationHelper: com.example.myapplication.reminder.NotificationHelper

    init {
        val dao = AppDatabase.getDatabase(application).hitmakerDao()
        repository = HitmakerRepository(dao)
        allHitmakers = repository.allHitmakers
        notificationHelper = com.example.myapplication.reminder.NotificationHelper(application)
    }

    fun addHitmaker(
        name: String, 
        color: Long, 
        icon: String, 
        reminderTime: String? = null, 
        reminderDays: String? = null
    ) {
        viewModelScope.launch {
            val count = allHitmakers.first().size
            val hitmaker = Hitmaker(
                name = name,
                color = color,
                startDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000,
                icon = icon,
                priority = count,
                reminderTime = reminderTime,
                reminderDays = reminderDays
            )
            val id = repository.insertHitmaker(hitmaker).toInt()
            
            if (reminderTime != null) {
                notificationHelper.scheduleReminder(hitmaker.copy(id = id))
            }
            HabitWidget().updateAll(getApplication())
        }
    }

    fun updateHitmaker(hitmaker: Hitmaker) {
        viewModelScope.launch {
            repository.updateHitmaker(hitmaker)
            if (hitmaker.reminderTime != null) {
                notificationHelper.scheduleReminder(hitmaker)
            } else {
                notificationHelper.cancelReminder(hitmaker.id)
            }
            HabitWidget().updateAll(getApplication())
        }
    }

    fun reorderHitmakers(hitmakers: List<Hitmaker>) {
        viewModelScope.launch {
            val reordered = hitmakers.mapIndexed { index, hitmaker ->
                hitmaker.copy(priority = index)
            }
            repository.updateHitmakersOrder(reordered)
        }
    }

    fun moveUp(id: Int) {
        viewModelScope.launch {
            val currentList = allHitmakers.first().sortedBy { it.priority }.toMutableList()
            val index = currentList.indexOfFirst { it.id == id }
            if (index > 0) {
                val item = currentList.removeAt(index)
                currentList.add(index - 1, item)
                reorderHitmakers(currentList)
            }
        }
    }

    fun moveDown(id: Int) {
        viewModelScope.launch {
            val currentList = allHitmakers.first().sortedBy { it.priority }.toMutableList()
            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1 && index < currentList.size - 1) {
                val item = currentList.removeAt(index)
                currentList.add(index + 1, item)
                reorderHitmakers(currentList)
            }
        }
    }

    fun markAsDone(hitmakerId: Int, isDone: Boolean = true, progress: Float = 1.0f) {
        viewModelScope.launch {
            val today = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val existing = repository.getDailyStatusByDate(hitmakerId, today)
            
            repository.insertDailyStatus(
                existing?.copy(isDone = isDone, progress = progress) ?: DailyStatus(
                    hitmakerId = hitmakerId,
                    date = today,
                    isDone = isDone,
                    progress = progress
                )
            )
            HabitWidget().updateAll(getApplication())
        }
    }
    
    val allDailyStatuses = repository.allDailyStatuses

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
            notificationHelper.cancelReminder(id)
            repository.deleteHitmaker(id)
            HabitWidget().updateAll(getApplication())
        }
    }
}
