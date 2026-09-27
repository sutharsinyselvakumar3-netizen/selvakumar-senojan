package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DetectionHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionHistoryDao {
    @Query("SELECT * FROM detection_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<DetectionHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: DetectionHistoryItem)

    @Query("DELETE FROM detection_history")
    suspend fun clearHistory()
}
