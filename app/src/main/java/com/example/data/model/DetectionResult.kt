package com.example.data.model

enum class DetectionClass {
    WEED,
    ONION,
    UNKNOWN
}

data class DetectionResult(
    val classification: DetectionClass = DetectionClass.UNKNOWN,
    val confidence: Float = 0f,
    val x: Int = 0,
    val y: Int = 0,
    val w: Int = 0,
    val h: Int = 0,
    val confirmationCount: Int = 0,
    val targetStatus: String = "IDLE",
    val timestamp: Long = System.currentTimeMillis()
) {
    val centerX: Int get() = x + (w / 2)
    val centerY: Int get() = y + (h / 2)
}
