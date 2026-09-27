package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detection_history")
data class DetectionHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val classification: String,
    val confidence: Float,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val centerX: Int,
    val centerY: Int,
    val referenceImage: String? = null,
    val result: String // WEED CONFIRMED, ONION PROTECTED, UNKNOWN IGNORED, LOW CONFIDENCE
)
