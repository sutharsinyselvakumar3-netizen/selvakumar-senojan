package com.example.network

import com.example.data.model.AppSettings
import com.example.data.model.ArmState
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class Esp32ApiClient {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun getStatus(settings: AppSettings): Result<RobotStatus> = withContext(Dispatchers.IO) {
        val url = "${settings.getEsp32BaseUrl()}/api/status"
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty status response"))
            val json = JSONObject(body)

            val modeStr = json.optString("mode", "UNKNOWN")
            val mode = RobotMode.fromString(modeStr)

            val batteryVoltage = if (json.has("batteryVoltage") && !json.isNull("batteryVoltage")) json.optDouble("batteryVoltage") else null
            val batteryPercent = if (json.has("batteryPercent") && !json.isNull("batteryPercent")) json.optInt("batteryPercent") else null

            val soilMoisture = if (json.has("soilMoisture") && !json.isNull("soilMoisture")) json.optInt("soilMoisture") else null
            val soilSensorValid = json.optBoolean("soilSensorValid", soilMoisture != null)

            val waterLevel = if (json.has("waterLevel") && !json.isNull("waterLevel")) json.optInt("waterLevel") else null
            val waterSensorValid = json.optBoolean("waterSensorValid", waterLevel != null)

            val mpuStable = json.optBoolean("mpuStable", true)
            val pitch = if (json.has("pitch") && !json.isNull("pitch")) json.optDouble("pitch") else null
            val roll = if (json.has("roll") && !json.isNull("roll")) json.optDouble("roll") else null

            val vibrationSensorValid = json.optBoolean("vibrationSensorValid", true)
            val vibrationDetected = json.optBoolean("vibrationDetected", false)
            val vibrationCritical = json.optBoolean("vibrationCritical", false)
            val vibrationAlert = json.optBoolean("vibrationAlert", false)

            val armReady = json.optBoolean("armReady", true)
            val armStateStr = json.optString("armState", "IDLE")
            val armState = ArmState.fromString(armStateStr)

            val servo1 = json.optInt("servo1", 90)
            val servo2 = json.optInt("servo2", 90)
            val servo3 = json.optInt("servo3", 90)
            val servo4 = json.optInt("servo4", 22)

            val targetX = if (json.has("targetX") && !json.isNull("targetX")) json.optDouble("targetX") else null
            val targetY = if (json.has("targetY") && !json.isNull("targetY")) json.optDouble("targetY") else null
            val armX = if (json.has("armX") && !json.isNull("armX")) json.optDouble("armX") else null
            val armY = if (json.has("armY") && !json.isNull("armY")) json.optDouble("armY") else null
            val armZ = if (json.has("armZ") && !json.isNull("armZ")) json.optDouble("armZ") else null

            val drillOn = json.optBoolean("drillOn", false)
            val relay2Soil = json.optBoolean("relay2", false)
            val relay3Pump = json.optBoolean("relay3", false)
            val relay4Siren = json.optBoolean("relay4", false)
            val sirenOn = json.optBoolean("sirenOn", relay4Siren)
            val alarmState = json.optBoolean("alarmState", false)
            val alarmReason = if (json.has("alarmReason") && !json.isNull("alarmReason")) json.optString("alarmReason") else null

            val cameraPan = json.optInt("cameraPan", 90)
            val cameraTilt = json.optInt("cameraTilt", 90)
            val safety = json.optString("safety", "SAFE")
            val emergencyStop = json.optBoolean("emergencyStop", false)

            val status = RobotStatus(
                isConnected = true,
                mode = mode,
                batteryVoltage = batteryVoltage,
                batteryPercent = batteryPercent,
                soilMoisture = soilMoisture,
                soilSensorValid = soilSensorValid,
                waterLevel = waterLevel,
                waterSensorValid = waterSensorValid,
                mpuStable = mpuStable,
                pitch = pitch,
                roll = roll,
                vibrationSensorValid = vibrationSensorValid,
                vibrationDetected = vibrationDetected,
                vibrationCritical = vibrationCritical,
                vibrationAlert = vibrationAlert,
                armReady = armReady,
                armState = armState,
                servo1Angle = servo1,
                servo2Angle = servo2,
                servo3Angle = servo3,
                servo4Angle = servo4,
                targetX = targetX,
                targetY = targetY,
                armX = armX,
                armY = armY,
                armZ = armZ,
                drillOn = drillOn,
                relay2Soil = relay2Soil,
                relay3Pump = relay3Pump,
                relay4Siren = relay4Siren,
                sirenOn = sirenOn,
                alarmState = alarmState,
                alarmReason = alarmReason,
                cameraPan = cameraPan,
                cameraTilt = cameraTilt,
                safety = safety,
                emergencyStopActive = emergencyStop,
                lastUpdatedMillis = System.currentTimeMillis(),
                statusText = "ONLINE"
            )

            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setMode(settings: AppSettings, targetMode: RobotMode): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val url = "${settings.getEsp32BaseUrl()}/api/mode"
        val payload = JSONObject().apply {
            put("mode", targetMode.name)
        }
        postJson(url, payload)
    }

    suspend fun sendMovement(settings: AppSettings, command: String, speed: String): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val url = "${settings.getEsp32BaseUrl()}/api/robot"
        val payload = JSONObject().apply {
            put("command", command)
            put("speed", speed)
        }
        postJson(url, payload)
    }

    suspend fun sendServo4(settings: AppSettings, angle: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val clamped = angle.coerceIn(0, 45) // Strict hardware enforcement: 0° to 45°
        val url = "${settings.getEsp32BaseUrl()}/api/servo4"
        val payload = JSONObject().apply {
            put("angle", clamped)
        }
        postJson(url, payload)
    }

    suspend fun sendCameraPanTilt(settings: AppSettings, pan: Int, tilt: Int): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val url = "${settings.getEsp32BaseUrl()}/api/camera/pantilt"
        val payload = JSONObject().apply {
            put("pan", pan.coerceIn(settings.panLimitMin, settings.panLimitMax))
            put("tilt", tilt.coerceIn(settings.tiltLimitMin, settings.tiltLimitMax))
        }
        postJson(url, payload)
    }

    suspend fun sendRelay(settings: AppSettings, relayNumber: Int, state: Boolean): Result<ApiResponse> = withContext(Dispatchers.IO) {
        // Relay 1 (AUTO DRILL) must NEVER be manually activated from app
        if (relayNumber == 1) {
            return@withContext Result.failure(IllegalStateException("Relay 1 (AUTO DRILL) cannot be manually controlled"))
        }
        val url = "${settings.getEsp32BaseUrl()}/api/relay"
        val payload = JSONObject().apply {
            put("relay", relayNumber)
            put("state", state)
        }
        postJson(url, payload)
    }

    suspend fun sendEmergencyStop(settings: AppSettings, active: Boolean = true): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val url = "${settings.getEsp32BaseUrl()}/api/emergency_stop"
        val payload = JSONObject().apply {
            put("active", active)
        }
        postJson(url, payload)
    }

    suspend fun sendAutoTargetDrill(
        settings: AppSettings,
        servo1: Int,
        servo2: Int,
        servo3: Int,
        drill: Boolean
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val url = "${settings.getEsp32BaseUrl()}/api/target_arm"
        val payload = JSONObject().apply {
            put("servo1", servo1)
            put("servo2", servo2)
            put("servo3", servo3)
            put("drill", drill)
            put("maxDrillSeconds", settings.maxDrillTimeSeconds.coerceAtMost(7))
        }
        postJson(url, payload)
    }

    private fun postJson(url: String, payload: JSONObject): Result<ApiResponse> {
        return try {
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val respStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val reason = try {
                    JSONObject(respStr).optString("reason", "HTTP_${response.code}")
                } catch (e: Exception) {
                    "HTTP_${response.code}"
                }
                return Result.success(
                    ApiResponse(
                        success = false,
                        message = "Command failed with code ${response.code}",
                        reason = reason
                    )
                )
            }

            val json = try {
                JSONObject(respStr)
            } catch (e: Exception) {
                JSONObject().put("success", true).put("message", "OK")
            }

            Result.success(
                ApiResponse(
                    success = json.optBoolean("success", true),
                    message = json.optString("message", "Success"),
                    reason = if (json.has("reason") && !json.isNull("reason")) json.optString("reason") else null
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
