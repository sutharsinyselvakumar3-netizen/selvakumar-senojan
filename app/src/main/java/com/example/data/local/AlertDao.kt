package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AlertItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT 50")
    fun getAllAlerts(): Flow<List<AlertItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertItem)

    @Query("DELETE FROM alerts")
    suspend fun clearAlerts()
}
