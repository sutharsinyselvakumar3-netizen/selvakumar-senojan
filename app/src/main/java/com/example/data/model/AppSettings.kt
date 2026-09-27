package com.example.data.model

data class AppSettings(
    // Network
    val hotspotName: String = "AI_GARDEN_HOTSPOT",
    val hotspotPassword: String = "onionprotect2026",
    val esp32Ip: String = "192.168.1.50",
    val esp32Port: Int = 80,
    val esp32TimeoutMs: Int = 3000,
    val retryCount: Int = 3,

    // Camera
    val cameraIp: String = "192.168.1.51",
    val streamPort: Int = 81,
    val streamEndpoint: String = "/stream",
    val captureEndpoint: String = "/capture",
    val brightness: Int = 50,
    val panCenter: Int = 90,
    val tiltCenter: Int = 90,
    val panLimitMin: Int = 0,
    val panLimitMax: Int = 180,
    val tiltLimitMin: Int = 20,
    val tiltLimitMax: Int = 160,
    val homePan: Int = 90,
    val homeTilt: Int = 90,

    // Soil
    val soilDryThreshold: Int = 40,
    val soilCriticalThreshold: Int = 20,
    val soilCalibration: Int = 0,
    val soilAlertSound: Boolean = true,
    val soilPhoneVibration: Boolean = true,
    val soilNotification: Boolean = true,
    val soilRelay4Siren: Boolean = false,

    // Water
    val waterSensorType: String = "Conductive Analog",
    val waterCalibration: Int = 0,
    val waterLowThreshold: Int = 30,
    val waterCriticalThreshold: Int = 15,
    val waterPumpLockout: Boolean = true,
    val waterSiren: Boolean = true,
    val waterNotifications: Boolean = true,

    // Vibration
    val vibrationThreshold: Int = 60,
    val vibrationDebounceMs: Int = 200,
    val vibrationConfirmationCount: Int = 3,
    val vibrationCriticalDurationMs: Int = 1500,
    val vibrationCooldownMs: Int = 3000,
    val vibrationSound: Boolean = true,
    val vibrationPhoneVibration: Boolean = true,
    val vibrationNotification: Boolean = true,
    val vibrationSiren: Boolean = true,
    val vibrationMotorStop: Boolean = true,
    val vibrationAutoStop: Boolean = true,

    // Siren / Relay 4
    val sirenRelay4Manual: Boolean = true,
    val sirenAutoAlarm: Boolean = true,
    val sirenAlarmDurationSec: Int = 5,

    // AI
    val aiMinConfidence: Float = 0.80f,
    val aiConsecutiveDetections: Int = 3,

    // Arm Calibration
    val cameraCalibrationScale: Float = 0.05f,
    val groundScaleX: Float = 1.0f,
    val groundScaleY: Float = 1.0f,
    val armOriginX: Float = 0.0f,
    val armOriginY: Float = 14.0f,
    val link1Length: Float = 12.0f,
    val link2Length: Float = 10.0f,
    val link3Length: Float = 8.0f,
    val servo1Offset: Int = 0,
    val servo2Offset: Int = 0,
    val servo3Offset: Int = 0,
    val servo4MinLimit: Int = 0,
    val servo4MaxLimit: Int = 45,

    // Safety
    val lowVoltageThreshold: Double = 11.1,
    val criticalVoltageThreshold: Double = 10.2,
    val mpuPitchLimit: Double = 25.0,
    val mpuRollLimit: Double = 25.0,
    val maxDrillTimeSeconds: Int = 7,
    val commandTimeoutMs: Int = 2000,

    // General
    val soundEnabled: Boolean = true,
    val phoneVibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
) {
    fun getStreamUrl(): String {
        return "http://$cameraIp:$streamPort$streamEndpoint"
    }

    fun getCaptureUrl(): String {
        return "http://$cameraIp:$esp32Port$captureEndpoint"
    }

    fun getCameraStatusUrl(): String {
        return "http://$cameraIp:$esp32Port/api/status"
    }

    fun getEsp32BaseUrl(): String {
        return "http://$esp32Ip:$esp32Port"
    }
}
