package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_companion_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            hotspotName = prefs.getString("hotspot_name", "AI_GARDEN_HOTSPOT") ?: "AI_GARDEN_HOTSPOT",
            hotspotPassword = prefs.getString("hotspot_password", "onionprotect2026") ?: "onionprotect2026",
            esp32Ip = prefs.getString("esp32_ip", "192.168.1.50") ?: "192.168.1.50",
            esp32Port = prefs.getInt("esp32_port", 80),
            esp32TimeoutMs = prefs.getInt("esp32_timeout", 3000),
            retryCount = prefs.getInt("retry_count", 3),

            cameraIp = prefs.getString("camera_ip", "192.168.1.51") ?: "192.168.1.51",
            streamPort = prefs.getInt("stream_port", 81),
            streamEndpoint = prefs.getString("stream_endpoint", "/stream") ?: "/stream",
            captureEndpoint = prefs.getString("capture_endpoint", "/capture") ?: "/capture",
            brightness = prefs.getInt("brightness", 50),
            panCenter = prefs.getInt("pan_center", 90),
            tiltCenter = prefs.getInt("tilt_center", 90),
            panLimitMin = prefs.getInt("pan_limit_min", 0),
            panLimitMax = prefs.getInt("pan_limit_max", 180),
            tiltLimitMin = prefs.getInt("tilt_limit_min", 20),
            tiltLimitMax = prefs.getInt("tilt_limit_max", 160),
            homePan = prefs.getInt("home_pan", 90),
            homeTilt = prefs.getInt("home_tilt", 90),

            soilDryThreshold = prefs.getInt("soil_dry_thresh", 40),
            soilCriticalThreshold = prefs.getInt("soil_crit_thresh", 20),
            soilCalibration = prefs.getInt("soil_calib", 0),
            soilAlertSound = prefs.getBoolean("soil_alert_sound", true),
            soilPhoneVibration = prefs.getBoolean("soil_phone_vibe", true),
            soilNotification = prefs.getBoolean("soil_notif", true),
            soilRelay4Siren = prefs.getBoolean("soil_siren", false),

            waterSensorType = prefs.getString("water_sensor_type", "Conductive Analog") ?: "Conductive Analog",
            waterCalibration = prefs.getInt("water_calib", 0),
            waterLowThreshold = prefs.getInt("water_low_thresh", 30),
            waterCriticalThreshold = prefs.getInt("water_crit_thresh", 15),
            waterPumpLockout = prefs.getBoolean("water_pump_lockout", true),
            waterSiren = prefs.getBoolean("water_siren", true),
            waterNotifications = prefs.getBoolean("water_notif", true),

            vibrationThreshold = prefs.getInt("vib_thresh", 60),
            vibrationDebounceMs = prefs.getInt("vib_debounce", 200),
            vibrationConfirmationCount = prefs.getInt("vib_confirm_count", 3),
            vibrationCriticalDurationMs = prefs.getInt("vib_crit_dur", 1500),
            vibrationCooldownMs = prefs.getInt("vib_cooldown", 3000),
            vibrationSound = prefs.getBoolean("vib_sound", true),
            vibrationPhoneVibration = prefs.getBoolean("vib_phone_vibe", true),
            vibrationNotification = prefs.getBoolean("vib_notif", true),
            vibrationSiren = prefs.getBoolean("vib_siren", true),
            vibrationMotorStop = prefs.getBoolean("vib_motor_stop", true),
            vibrationAutoStop = prefs.getBoolean("vib_auto_stop", true),

            sirenRelay4Manual = prefs.getBoolean("siren_manual", true),
            sirenAutoAlarm = prefs.getBoolean("siren_auto", true),
            sirenAlarmDurationSec = prefs.getInt("siren_duration", 5),

            aiMinConfidence = prefs.getFloat("ai_min_confidence", 0.80f),
            aiConsecutiveDetections = prefs.getInt("ai_consec_detections", 3),

            cameraCalibrationScale = prefs.getFloat("cam_calib_scale", 0.05f),
            groundScaleX = prefs.getFloat("ground_scale_x", 1.0f),
            groundScaleY = prefs.getFloat("ground_scale_y", 1.0f),
            armOriginX = prefs.getFloat("arm_origin_x", 0.0f),
            armOriginY = prefs.getFloat("arm_origin_y", 14.0f),
            link1Length = prefs.getFloat("link1_len", 12.0f),
            link2Length = prefs.getFloat("link2_len", 10.0f),
            link3Length = prefs.getFloat("link3_len", 8.0f),
            servo1Offset = prefs.getInt("servo1_offset", 0),
            servo2Offset = prefs.getInt("servo2_offset", 0),
            servo3Offset = prefs.getInt("servo3_offset", 0),
            servo4MinLimit = prefs.getInt("servo4_min", 0),
            servo4MaxLimit = prefs.getInt("servo4_max", 45),

            lowVoltageThreshold = prefs.getFloat("low_voltage_thresh", 11.1f).toDouble(),
            criticalVoltageThreshold = prefs.getFloat("crit_voltage_thresh", 10.2f).toDouble(),
            mpuPitchLimit = prefs.getFloat("mpu_pitch_limit", 25.0f).toDouble(),
            mpuRollLimit = prefs.getFloat("mpu_roll_limit", 25.0f).toDouble(),
            maxDrillTimeSeconds = prefs.getInt("max_drill_time", 7),
            commandTimeoutMs = prefs.getInt("cmd_timeout", 2000),

            soundEnabled = prefs.getBoolean("sound_enabled", true),
            phoneVibrationEnabled = prefs.getBoolean("phone_vibe_enabled", true),
            notificationsEnabled = prefs.getBoolean("notif_enabled", true)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString("hotspot_name", newSettings.hotspotName)
            putString("hotspot_password", newSettings.hotspotPassword)
            putString("esp32_ip", newSettings.esp32Ip)
            putInt("esp32_port", newSettings.esp32Port)
            putInt("esp32_timeout", newSettings.esp32TimeoutMs)
            putInt("retry_count", newSettings.retryCount)

            putString("camera_ip", newSettings.cameraIp)
            putInt("stream_port", newSettings.streamPort)
            putString("stream_endpoint", newSettings.streamEndpoint)
            putString("capture_endpoint", newSettings.captureEndpoint)
            putInt("brightness", newSettings.brightness)
            putInt("pan_center", newSettings.panCenter)
            putInt("tilt_center", newSettings.tiltCenter)
            putInt("pan_limit_min", newSettings.panLimitMin)
            putInt("pan_limit_max", newSettings.panLimitMax)
            putInt("tilt_limit_min", newSettings.tiltLimitMin)
            putInt("tilt_limit_max", newSettings.tiltLimitMax)
            putInt("home_pan", newSettings.homePan)
            putInt("home_tilt", newSettings.homeTilt)

            putInt("soil_dry_thresh", newSettings.soilDryThreshold)
            putInt("soil_crit_thresh", newSettings.soilCriticalThreshold)
            putInt("soil_calib", newSettings.soilCalibration)
            putBoolean("soil_alert_sound", newSettings.soilAlertSound)
            putBoolean("soil_phone_vibe", newSettings.soilPhoneVibration)
            putBoolean("soil_notif", newSettings.soilNotification)
            putBoolean("soil_siren", newSettings.soilRelay4Siren)

            putString("water_sensor_type", newSettings.waterSensorType)
            putInt("water_calib", newSettings.waterCalibration)
            putInt("water_low_thresh", newSettings.waterLowThreshold)
            putInt("water_crit_thresh", newSettings.waterCriticalThreshold)
            putBoolean("water_pump_lockout", newSettings.waterPumpLockout)
            putBoolean("water_siren", newSettings.waterSiren)
            putBoolean("water_notif", newSettings.waterNotifications)

            putInt("vib_thresh", newSettings.vibrationThreshold)
            putInt("vib_debounce", newSettings.vibrationDebounceMs)
            putInt("vib_confirm_count", newSettings.vibrationConfirmationCount)
            putInt("vib_crit_dur", newSettings.vibrationCriticalDurationMs)
            putInt("vib_cooldown", newSettings.vibrationCooldownMs)
            putBoolean("vib_sound", newSettings.vibrationSound)
            putBoolean("vib_phone_vibe", newSettings.vibrationPhoneVibration)
            putBoolean("vib_notif", newSettings.vibrationNotification)
            putBoolean("vib_siren", newSettings.vibrationSiren)
            putBoolean("vib_motor_stop", newSettings.vibrationMotorStop)
            putBoolean("vib_auto_stop", newSettings.vibrationAutoStop)

            putBoolean("siren_manual", newSettings.sirenRelay4Manual)
            putBoolean("siren_auto", newSettings.sirenAutoAlarm)
            putInt("siren_duration", newSettings.sirenAlarmDurationSec)

            putFloat("ai_min_confidence", newSettings.aiMinConfidence)
            putInt("ai_consec_detections", newSettings.aiConsecutiveDetections)

            putFloat("cam_calib_scale", newSettings.cameraCalibrationScale)
            putFloat("ground_scale_x", newSettings.groundScaleX)
            putFloat("ground_scale_y", newSettings.groundScaleY)
            putFloat("arm_origin_x", newSettings.armOriginX)
            putFloat("arm_origin_y", newSettings.armOriginY)
            putFloat("link1_len", newSettings.link1Length)
            putFloat("link2_len", newSettings.link2Length)
            putFloat("link3_len", newSettings.link3Length)
            putInt("servo1_offset", newSettings.servo1Offset)
            putInt("servo2_offset", newSettings.servo2Offset)
            putInt("servo3_offset", newSettings.servo3Offset)
            putInt("servo4_min", newSettings.servo4MinLimit)
            putInt("servo4_max", newSettings.servo4MaxLimit)

            putFloat("low_voltage_thresh", newSettings.lowVoltageThreshold.toFloat())
            putFloat("crit_voltage_thresh", newSettings.criticalVoltageThreshold.toFloat())
            putFloat("mpu_pitch_limit", newSettings.mpuPitchLimit.toFloat())
            putFloat("mpu_roll_limit", newSettings.mpuRollLimit.toFloat())
            putInt("max_drill_time", newSettings.maxDrillTimeSeconds)
            putInt("cmd_timeout", newSettings.commandTimeoutMs)

            putBoolean("sound_enabled", newSettings.soundEnabled)
            putBoolean("phone_vibe_enabled", newSettings.phoneVibrationEnabled)
            putBoolean("notif_enabled", newSettings.notificationsEnabled)
            apply()
        }
        _settings.value = newSettings
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = AppSettings()
    }
}
