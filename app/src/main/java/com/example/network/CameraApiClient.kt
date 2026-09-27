package com.example.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CameraApiClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(3500, TimeUnit.MILLISECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun checkCameraStatus(settings: AppSettings): Boolean = withContext(Dispatchers.IO) {
        val url = settings.getCameraStatusUrl()
        try {
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun captureFrame(settings: AppSettings): Result<Bitmap> = withContext(Dispatchers.IO) {
        val url = settings.getCaptureUrl()
        try {
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Capture failed with code ${response.code}"))
            }

            val bytes = response.body?.bytes()
                ?: return@withContext Result.failure(Exception("Empty capture response"))

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext Result.failure(Exception("Failed to decode camera JPEG"))

            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setHardwareBrightness(settings: AppSettings, brightness: Int): Boolean = withContext(Dispatchers.IO) {
        val url = "http://${settings.cameraIp}:${settings.esp32Port}/api/camera/brightness"
        try {
            val payload = JSONObject().apply {
                put("brightness", brightness)
            }
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
