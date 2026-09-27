package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class WeedReferenceSlot(
    val slotNumber: Int, // 1, 2, or 3
    val file: File?,
    val isValid: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val errorMessage: String? = null
)

class WeedImageStorage(private val context: Context) {
    private val weedDir = File(context.filesDir, "weed_references").apply {
        if (!exists()) mkdirs()
    }

    private val _slotsState = MutableStateFlow(loadAllSlots())
    val slotsState: StateFlow<List<WeedReferenceSlot>> = _slotsState.asStateFlow()

    fun loadAllSlots(): List<WeedReferenceSlot> {
        return (1..3).map { slot ->
            val file = File(weedDir, "weed_$slot.png")
            if (file.exists() && file.length() > 0) {
                val bounds = getImageBounds(file)
                if (bounds != null) {
                    WeedReferenceSlot(
                        slotNumber = slot,
                        file = file,
                        isValid = true,
                        width = bounds.first,
                        height = bounds.second
                    )
                } else {
                    WeedReferenceSlot(
                        slotNumber = slot,
                        file = file,
                        isValid = false,
                        errorMessage = "INVALID IMAGE"
                    )
                }
            } else {
                WeedReferenceSlot(
                    slotNumber = slot,
                    file = null,
                    isValid = false
                )
            }
        }
    }

    private fun getImageBounds(file: File): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveReferenceImageFromUri(slot: Int, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        if (slot !in 1..3) {
            return@withContext Result.failure(IllegalArgumentException("Only slots 1, 2, and 3 are supported"))
        }

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open image stream"))

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
                return@withContext Result.failure(Exception("INVALID IMAGE: Image cannot be decoded"))
            }

            // Save without tinting, recoloring, or altering
            val targetFile = File(weedDir, "weed_$slot.png")
            val outputStream = FileOutputStream(targetFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            _slotsState.value = loadAllSlots()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReferenceBitmap(slot: Int, bitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO) {
        if (slot !in 1..3) {
            return@withContext Result.failure(IllegalArgumentException("Only slots 1, 2, and 3 are supported"))
        }

        try {
            if (bitmap.width <= 0 || bitmap.height <= 0) {
                return@withContext Result.failure(Exception("INVALID IMAGE: Bitmap dimensions invalid"))
            }
            val targetFile = File(weedDir, "weed_$slot.png")
            val outputStream = FileOutputStream(targetFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            _slotsState.value = loadAllSlots()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReferenceImage(slot: Int): Boolean = withContext(Dispatchers.IO) {
        if (slot !in 1..3) return@withContext false
        val targetFile = File(weedDir, "weed_$slot.png")
        val deleted = if (targetFile.exists()) targetFile.delete() else false
        _slotsState.value = loadAllSlots()
        deleted
    }

    fun getReferenceBitmap(slot: Int): Bitmap? {
        if (slot !in 1..3) return null
        val targetFile = File(weedDir, "weed_$slot.png")
        if (!targetFile.exists()) return null
        return try {
            BitmapFactory.decodeFile(targetFile.absolutePath)
        } catch (e: Exception) {
            null
        }
    }
}
