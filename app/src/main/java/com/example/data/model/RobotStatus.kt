package com.example.data.model

data class RobotStatus(
    val isConnected: Boolean = false,
    val mode: RobotMode = RobotMode.UNKNOWN,
    val batteryVoltage: Double? = null,
    val batteryPercent: Int? = null,
    val soilMoisture: Int? = null,
    val soilSensorValid: Boolean = false,
    val waterLevel: Int? = null,
    val waterSensorValid: Boolean = false,
    val mpuStable: Boolean = true,
    val pitch: Double? = null,
    val roll: Double? = null,
    val vibrationSensorValid: Boolean = false,
    val vibrationDetected: Boolean = false,
    val vibrationCritical: Boolean = false,
    val vibrationAlert: Boolean = false,
    val armReady: Boolean = false,
    val armState: ArmState = ArmState.IDLE,
    val servo1Angle: Int = 90,
    val servo2Angle: Int = 90,
    val servo3Angle: Int = 90,
    val servo4Angle: Int = 22,
    val targetX: Double? = null,
    val targetY: Double? = null,
    val armX: Double? = null,
    val armY: Double? = null,
    val armZ: Double? = null,
    val drillOn: Boolean = false,
    val relay2Soil: Boolean = false,
    val relay3Pump: Boolean = false,
    val relay4Siren: Boolean = false,
    val sirenOn: Boolean = false,
    val alarmState: Boolean = false,
    val alarmReason: String? = null,
    val cameraPan: Int = 90,
    val cameraTilt: Int = 90,
    val safety: String = "UNKNOWN",
    val emergencyStopActive: Boolean = false,
    val lastUpdatedMillis: Long = 0L,
    val statusText: String = "OFFLINE"
) {
    fun isStale(timeoutMillis: Long = 4000L): Boolean {
        if (!isConnected || lastUpdatedMillis == 0L) return true
        return (System.currentTimeMillis() - lastUpdatedMillis) > timeoutMillis
    }

    fun getDisplayStatus(): String {
        return when {
            emergencyStopActive -> "EMERGENCY STOP"
            !isConnected -> "OFFLINE"
            isStale() -> "DATA STALE"
            vibrationCritical -> "CRITICAL VIBRATION"
            !mpuStable -> "MPU UNSTABLE"
            batteryVoltage != null && batteryVoltage < 10.5 -> "CRITICAL VOLTAGE"
            else -> "ONLINE"
        }
    }
}
