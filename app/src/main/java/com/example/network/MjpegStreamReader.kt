package com.example.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MjpegStreamReader(private val scope: CoroutineScope) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var streamJob: Job? = null

    private val _currentFrame = MutableStateFlow<Bitmap?>(null)
    val currentFrame: StateFlow<Bitmap?> = _currentFrame.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastFrameTimeMs = MutableStateFlow(0L)
    val lastFrameTimeMs: StateFlow<Long> = _lastFrameTimeMs.asStateFlow()

    private var activeStreamUrl: String = ""

    fun startStream(streamUrl: String) {
        activeStreamUrl = streamUrl
        stopStream()

        streamJob = scope.launch(Dispatchers.IO) {
            _isStreaming.value = true
            _hasError.value = false
            _errorMessage.value = null

            var response: Response? = null
            var inputStream: BufferedInputStream? = null

            try {
                val request = Request.Builder()
                    .url(streamUrl)
                    .addHeader("Accept", "multipart/x-mixed-replace, image/jpeg")
                    .build()

                response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    _hasError.value = true
                    _errorMessage.value = "STREAM NOT AVAILABLE (HTTP ${response.code})"
                    _isStreaming.value = false
                    return@launch
                }

                val body = response.body
                if (body == null) {
                    _hasError.value = true
                    _errorMessage.value = "STREAM NOT AVAILABLE (No content)"
                    _isStreaming.value = false
                    return@launch
                }

                inputStream = BufferedInputStream(body.byteStream())
                val buffer = ByteArray(4096)
                val frameStream = ByteArrayOutputStream()

                var prevByte = -1
                var insideJpeg = false

                while (isActive) {
                    val read = inputStream.read()
                    if (read == -1) break

                    val currentByte = read and 0xFF

                    if (!insideJpeg) {
                        // Check for Start of Image (SOI): 0xFF, 0xD8
                        if (prevByte == 0xFF && currentByte == 0xD8) {
                            insideJpeg = true
                            frameStream.reset()
                            frameStream.write(0xFF)
                            frameStream.write(0xD8)
                        }
                    } else {
                        frameStream.write(currentByte)
                        // Check for End of Image (EOI): 0xFF, 0xD9
                        if (prevByte == 0xFF && currentByte == 0xD9) {
                            insideJpeg = false
                            val jpegBytes = frameStream.toByteArray()
                            if (jpegBytes.size > 200) {
                                val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                                if (bitmap != null) {
                                    _currentFrame.value = bitmap
                                    _lastFrameTimeMs.value = System.currentTimeMillis()
                                    _hasError.value = false
                                }
                            }
                            frameStream.reset()
                        }
                    }
                    prevByte = currentByte
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e("MjpegStreamReader", "Stream error: ${e.message}")
                    _hasError.value = true
                    _errorMessage.value = "STREAM NOT AVAILABLE (${e.localizedMessage ?: "Connection error"})"
                }
            } finally {
                _isStreaming.value = false
                try {
                    inputStream?.close()
                    response?.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopStream() {
        streamJob?.cancel()
        streamJob = null
        _isStreaming.value = false
    }

    fun toggleStream() {
        if (_isStreaming.value) {
            stopStream()
        } else if (activeStreamUrl.isNotBlank()) {
            startStream(activeStreamUrl)
        }
    }

    fun retry() {
        if (activeStreamUrl.isNotBlank()) {
            startStream(activeStreamUrl)
        }
    }
}
