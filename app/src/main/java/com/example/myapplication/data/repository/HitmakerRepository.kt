package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.HitmakerDao
import com.example.myapplication.data.entity.DailyStatus
import com.example.myapplication.data.entity.Hitmaker
import kotlinx.coroutines.flow.Flow

class HitmakerRepository(private val hitmakerDao: HitmakerDao) {
    val allHitmakers: Flow<List<Hitmaker>> = hitmakerDao.getAllHitmakers()

    suspend fun insertHitmaker(hitmaker: Hitmaker) {
        hitmakerDao.insertHitmaker(hitmaker)
    }

    suspend fun getHitmakerById(id: Int): Hitmaker? {
        return hitmakerDao.getHitmakerById(id)
    }

    suspend fun insertDailyStatus(status: DailyStatus) {
        hitmakerDao.insertDailyStatus(status)
    }

    fun getDailyStatusesForHitmaker(hitmakerId: Int): Flow<List<DailyStatus>> {
        return hitmakerDao.getDailyStatusesForHitmaker(hitmakerId)
    }

    suspend fun getDailyStatusByDate(hitmakerId: Int, date: Long): DailyStatus? {
        return hitmakerDao.getDailyStatusByDate(hitmakerId, date)
    }
    
    suspend fun updateHitmakerName(id: Int, newName: String) {
        hitmakerDao.updateHitmakerName(id, newName)
    }

    suspend fun updateHitmakersOrder(hitmakers: List<Hitmaker>) {
        hitmakerDao.updateHitmakers(hitmakers)
    }

    val allDailyStatuses: Flow<List<DailyStatus>> = hitmakerDao.getAllDailyStatuses()

    suspend fun deleteHitmaker(id: Int) {
        hitmakerDao.deleteHitmaker(id)
    }
}
