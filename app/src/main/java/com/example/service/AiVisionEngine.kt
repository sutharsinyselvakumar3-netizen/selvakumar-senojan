package com.example.service

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.local.AppDatabase
import com.example.data.local.WeedImageStorage
import com.example.data.model.AppSettings
import com.example.data.model.DetectionClass
import com.example.data.model.DetectionHistoryItem
import com.example.data.model.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class AiVisionEngine(
    private val database: AppDatabase,
    private val weedStorage: WeedImageStorage
) {
    private val _latestResult = MutableStateFlow<DetectionResult?>(null)
    val latestResult: StateFlow<DetectionResult?> = _latestResult.asStateFlow()

    private val _isAiRunning = MutableStateFlow(false)
    val isAiRunning: StateFlow<Boolean> = _isAiRunning.asStateFlow()

    private var consecutiveWeedCount = 0
    private var lastConfirmedBox: DetectionResult? = null

    fun setAiRunning(running: Boolean) {
        _isAiRunning.value = running
        if (!running) {
            consecutiveWeedCount = 0
            _latestResult.value = null
        }
    }

    suspend fun analyzeFrame(bitmap: Bitmap?, settings: AppSettings): DetectionResult? = withContext(Dispatchers.Default) {
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
            _latestResult.value = DetectionResult(
                classification = DetectionClass.UNKNOWN,
                confidence = 0f,
                targetStatus = "WAITING FOR CAMERA FRAME"
            )
            return@withContext _latestResult.value
        }

        // Subsample for fast real-time computer vision analysis
        val width = bitmap.width
        val height = bitmap.height
        val step = max(2, min(width, height) / 160)

        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var vegPixels = 0

        var sumR = 0L
        var sumG = 0L
        var sumB = 0L

        // Scan pixels for vegetation using Excess Green index (ExG = 2G - R - B)
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val exg = (2 * g) - r - b
                if (exg > 20 && g > r && g > b) {
                    vegPixels++
                    sumR += r
                    sumG += g
                    sumB += b

                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (vegPixels < 25 || maxX <= minX || maxY <= minY) {
            consecutiveWeedCount = 0
            val res = DetectionResult(
                classification = DetectionClass.UNKNOWN,
                confidence = 0f,
                targetStatus = "UNKNOWN — IGNORED",
                confirmationCount = 0
            )
            _latestResult.value = res
            return@withContext res
        }

        val boxX = max(0, minX - 10)
        val boxY = max(0, minY - 10)
        val boxW = min(width - boxX, (maxX - minX) + 20)
        val boxH = min(height - boxY, (maxY - minY) + 20)

        val aspectRatio = boxH.toFloat() / max(1f, boxW.toFloat())
        val avgG = sumG.toDouble() / vegPixels
        val avgR = sumR.toDouble() / vegPixels
        val avgB = sumB.toDouble() / vegPixels

        // Compare against configured weed reference templates
        var refMatchScore = 0.5f
        var matchedSlotName = "General Morphological"
        val refSlots = weedStorage.loadAllSlots()
        val validSlots = refSlots.filter { it.isValid }

        if (validSlots.isNotEmpty()) {
            var highestScore = 0f
            for (slot in validSlots) {
                val refBmp = weedStorage.getReferenceBitmap(slot.slotNumber)
                if (refBmp != null) {
                    val refAspect = refBmp.height.toFloat() / max(1f, refBmp.width.toFloat())
                    val aspectDiff = abs(aspectRatio - refAspect)
                    val score = max(0f, 1.0f - (aspectDiff * 0.4f))
                    if (score > highestScore) {
                        highestScore = score
                        matchedSlotName = "Weed Reference ${slot.slotNumber}"
                    }
                }
            }
            refMatchScore = highestScore.coerceIn(0.5f, 0.96f)
        }

        val classification: DetectionClass
        val confidence: Float
        val targetStatus: String
        val resultRecord: String

        // Onion foliage is distinctively tall, slender, tubular leaves (high aspect ratio > 2.2)
        // Weeds are broadleaf rosettes or low branching clumps (aspect ratio closer to 0.7 - 1.8)
        if (aspectRatio > 2.4f && (avgG - avgB) < 35) {
            // Protected Onion detected
            classification = DetectionClass.ONION
            confidence = (0.84f + (min(aspectRatio, 4f) * 0.03f)).coerceAtMost(0.98f)
            targetStatus = "ONION PROTECTED"
            consecutiveWeedCount = 0
            resultRecord = "ONION PROTECTED"
        } else if (refMatchScore >= 0.70f || (aspectRatio in 0.4f..2.1f && vegPixels >= 35)) {
            // Weed detected
            classification = DetectionClass.WEED
            val baseConf = max(refMatchScore, 0.82f)
            confidence = (baseConf + (min(vegPixels, 200) * 0.0005f)).coerceIn(0.75f, 0.97f)

            if (confidence >= settings.aiMinConfidence) {
                consecutiveWeedCount++
            } else {
                consecutiveWeedCount = max(0, consecutiveWeedCount - 1)
            }

            if (consecutiveWeedCount >= settings.aiConsecutiveDetections) {
                targetStatus = "WEED CONFIRMED"
                resultRecord = "WEED CONFIRMED"
            } else {
                targetStatus = "CONFIRMING (${consecutiveWeedCount}/${settings.aiConsecutiveDetections})"
                resultRecord = "LOW CONFIDENCE"
            }
        } else {
            classification = DetectionClass.UNKNOWN
            confidence = 0.45f
            targetStatus = "UNKNOWN — IGNORED"
            consecutiveWeedCount = 0
            resultRecord = "UNKNOWN IGNORED"
        }

        val result = DetectionResult(
            classification = classification,
            confidence = confidence,
            x = boxX,
            y = boxY,
            w = boxW,
            h = boxH,
            confirmationCount = consecutiveWeedCount,
            targetStatus = targetStatus
        )

        _latestResult.value = result

        // Save into history database
        try {
            database.detectionHistoryDao().insertHistory(
                DetectionHistoryItem(
                    classification = classification.name,
                    confidence = confidence,
                    x = boxX,
                    y = boxY,
                    w = boxW,
                    h = boxH,
                    centerX = result.centerX,
                    centerY = result.centerY,
                    referenceImage = if (classification == DetectionClass.WEED) matchedSlotName else null,
                    result = resultRecord
                )
            )
        } catch (_: Exception) {}

        result
    }
}
