package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.WeedImageStorage
import com.example.data.local.WeedReferenceSlot
import com.example.data.model.AlertItem
import com.example.data.model.AppSettings
import com.example.data.model.ArmState
import com.example.data.model.DetectionHistoryItem
import com.example.data.model.DetectionResult
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.network.CameraApiClient
import com.example.network.Esp32ApiClient
import com.example.network.MjpegStreamReader
import com.example.service.AiVisionEngine
import com.example.service.InverseKinematicsCalculator
import com.example.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RobotViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val preferencesManager = PreferencesManager(application)
    val weedStorage = WeedImageStorage(application)
    val esp32ApiClient = Esp32ApiClient()
    val cameraApiClient = CameraApiClient()
    val notificationHelper = NotificationHelper(application, database, viewModelScope)
    val ikCalculator = InverseKinematicsCalculator()
    val aiVisionEngine = AiVisionEngine(database, weedStorage)
    val mjpegReader = MjpegStreamReader(viewModelScope)

    val settings: StateFlow<AppSettings> = preferencesManager.settings

    private val _robotStatus = MutableStateFlow(RobotStatus())
    val robotStatus: StateFlow<RobotStatus> = _robotStatus.asStateFlow()

    private val _cameraOnline = MutableStateFlow(false)
    val cameraOnline: StateFlow<Boolean> = _cameraOnline.asStateFlow()

    private val _isHardwareBrightness = MutableStateFlow(false)
    val isHardwareBrightness: StateFlow<Boolean> = _isHardwareBrightness.asStateFlow()

    private val _selectedSpeed = MutableStateFlow("SLOW")
    val selectedSpeed: StateFlow<String> = _selectedSpeed.asStateFlow()

    private val _autoModeBlockedReason = MutableStateFlow<String?>(null)
    val autoModeBlockedReason: StateFlow<String?> = _autoModeBlockedReason.asStateFlow()

    private val _showModeSwitchDialog = MutableStateFlow<RobotMode?>(null)
    val showModeSwitchDialog: StateFlow<RobotMode?> = _showModeSwitchDialog.asStateFlow()

    private val _latestNotice = MutableStateFlow<String?>(null)
    val latestNotice: StateFlow<String?> = _latestNotice.asStateFlow()

    private val _isTargetingSequenceActive = MutableStateFlow(false)
    val isTargetingSequenceActive: StateFlow<Boolean> = _isTargetingSequenceActive.asStateFlow()

    private val _targetingStepDescription = MutableStateFlow<String?>(null)
    val targetingStepDescription: StateFlow<String?> = _targetingStepDescription.asStateFlow()

    val weedSlots: StateFlow<List<WeedReferenceSlot>> = weedStorage.slotsState

    val alertHistory: StateFlow<List<AlertItem>> = database.alertDao().getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val detectionHistory: StateFlow<List<DetectionHistoryItem>> = database.detectionHistoryDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var pollingJob: Job? = null
    private var aiLoopJob: Job? = null
    private var targetingJob: Job? = null

    init {
        startStatusPolling()
        startAiLoop()
        // Initialize camera stream
        mjpegReader.startStream(settings.value.getStreamUrl())
    }

    private fun startStatusPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val currentSettings = settings.value
                val statusResult = esp32ApiClient.getStatus(currentSettings)
                if (statusResult.isSuccess) {
                    val status = statusResult.getOrNull()!!
                    _robotStatus.value = status

                    // Evaluate safety alerts
                    evaluateSafetyConditions(status, currentSettings)
                } else {
                    _robotStatus.value = _robotStatus.value.copy(
                        isConnected = false,
                        statusText = "OFFLINE"
                    )
                }

                // Periodic check for camera connection
                val camOnline = cameraApiClient.checkCameraStatus(currentSettings)
                _cameraOnline.value = camOnline

                delay(1000)
            }
        }
    }

    private fun evaluateSafetyConditions(status: RobotStatus, currentSettings: AppSettings) {
        if (status.emergencyStopActive) {
            notificationHelper.triggerAlert(
                "CRITICAL",
                "EMERGENCY STOP ACTIVE",
                "Robot emergency stop has been activated. Motors and drills stopped.",
                currentSettings
            )
        } else if (status.vibrationCritical) {
            notificationHelper.triggerAlert(
                "CRITICAL",
                "CRITICAL VIBRATION DETECTED",
                "Excessive chassis vibration threshold exceeded! Operations halted.",
                currentSettings
            )
        } else if (!status.mpuStable) {
            notificationHelper.triggerAlert(
                "CRITICAL",
                "MPU6050 UNSTABLE",
                "Robot tilt or terrain instability detected! Operations halted.",
                currentSettings
            )
        } else if (status.batteryVoltage != null && status.batteryVoltage <= currentSettings.criticalVoltageThreshold) {
            notificationHelper.triggerAlert(
                "CRITICAL",
                "CRITICAL BATTERY VOLTAGE",
                "Battery at ${status.batteryVoltage}V (Critical limit: ${currentSettings.criticalVoltageThreshold}V)!",
                currentSettings
            )
        } else if (status.soilMoisture != null && status.soilMoisture < currentSettings.soilDryThreshold) {
            notificationHelper.triggerAlert(
                "WARNING",
                "DRY SOIL DETECTED",
                "Soil moisture at ${status.soilMoisture}% (Threshold: ${currentSettings.soilDryThreshold}%)",
                currentSettings
            )
        } else if (status.waterLevel != null && status.waterLevel < currentSettings.waterLowThreshold) {
            notificationHelper.triggerAlert(
                "WARNING",
                "LOW WATER LEVEL",
                "Water reservoir level at ${status.waterLevel}%",
                currentSettings
            )
        }
    }

    private fun startAiLoop() {
        aiLoopJob?.cancel()
        aiLoopJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                if (aiVisionEngine.isAiRunning.value) {
                    val frame = mjpegReader.currentFrame.value
                    val detection = aiVisionEngine.analyzeFrame(frame, settings.value)

                    // In AUTO mode, handle autonomous weed targeting workflow
                    if (_robotStatus.value.mode == RobotMode.AUTO &&
                        detection != null &&
                        detection.targetStatus == "WEED CONFIRMED" &&
                        !_isTargetingSequenceActive.value
                    ) {
                        triggerAutonomousWeedRemoval(detection, frame?.width ?: 640, frame?.height ?: 480)
                    }
                }
                delay(200)
            }
        }
    }

    private fun triggerAutonomousWeedRemoval(detection: DetectionResult, imgW: Int, imgH: Int) {
        targetingJob?.cancel()
        targetingJob = viewModelScope.launch(Dispatchers.IO) {
            _isTargetingSequenceActive.value = true
            try {
                // 1. STOP ROBOT
                _targetingStepDescription.value = "1. STOPPING CHASSIS MOTORS"
                esp32ApiClient.sendMovement(settings.value, "STOP", _selectedSpeed.value)
                delay(300)

                // 2. Compute Inverse Kinematics
                _targetingStepDescription.value = "2. COMPUTING INVERSE KINEMATICS & TARGET"
                val ik = ikCalculator.calculateKinematics(
                    detection.centerX,
                    detection.centerY,
                    imgW,
                    imgH,
                    settings.value
                )

                // 3. Move Arm Servos 1-3
                _targetingStepDescription.value = "3. MOVING ARM TO (${ik.groundX.toInt()}cm, ${ik.groundY.toInt()}cm)"
                esp32ApiClient.sendAutoTargetDrill(
                    settings.value,
                    ik.servo1Angle,
                    ik.servo2Angle,
                    ik.servo3Angle,
                    false
                )
                delay(1200)

                // 4. Position Confirmation & DRILL ON (max 7s enforced by ESP32)
                _targetingStepDescription.value = "4. DRILLING WEED (MAX 7s ENFORCED)"
                notificationHelper.triggerAlert(
                    "INFO",
                    "WEED DRILLING ACTIVE",
                    "Weed targeted at [${detection.centerX}, ${detection.centerY}]. Drill engaged.",
                    settings.value
                )
                esp32ApiClient.sendAutoTargetDrill(
                    settings.value,
                    ik.servo1Angle,
                    ik.servo2Angle,
                    ik.servo3Angle,
                    true
                )
                // Wait up to max drill duration
                val drillTimeMs = (settings.value.maxDrillTimeSeconds.coerceAtMost(7) * 1000L)
                delay(drillTimeMs)

                // 5. DRILL OFF
                _targetingStepDescription.value = "5. DRILL OFF — RETURNING ARM HOME"
                esp32ApiClient.sendAutoTargetDrill(
                    settings.value,
                    90,
                    90,
                    90,
                    false
                )
                delay(1000)

                // 6. Resume Slow Search
                _targetingStepDescription.value = "6. RESUMING SEARCH"
                delay(500)
            } catch (e: Exception) {
                Log.e("RobotViewModel", "Targeting sequence error: ${e.message}")
            } finally {
                _isTargetingSequenceActive.value = false
                _targetingStepDescription.value = null
            }
        }
    }

    fun requestModeSwitch(targetMode: RobotMode) {
        _showModeSwitchDialog.value = targetMode
    }

    fun cancelModeSwitch() {
        _showModeSwitchDialog.value = null
        _autoModeBlockedReason.value = null
    }

    fun confirmModeSwitch() {
        val targetMode = _showModeSwitchDialog.value ?: return
        _showModeSwitchDialog.value = null

        viewModelScope.launch(Dispatchers.IO) {
            if (targetMode == RobotMode.AUTO) {
                // Safety checklist (Section 6 & 56)
                val status = _robotStatus.value
                val curSettings = settings.value

                if (!status.isConnected) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: ESP32 OFFLINE"
                    return@launch
                }
                if (!_cameraOnline.value) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: CAMERA OFFLINE"
                    return@launch
                }
                if (mjpegReader.currentFrame.value == null) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: STREAM / CAMERA FRAME NOT AVAILABLE"
                    return@launch
                }
                if (status.batteryVoltage != null && status.batteryVoltage <= curSettings.criticalVoltageThreshold) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: CRITICAL VOLTAGE (${status.batteryVoltage}V)"
                    return@launch
                }
                if (!status.mpuStable) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: MPU6050 UNSTABLE"
                    return@launch
                }
                if (status.vibrationCritical) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: CRITICAL VIBRATION"
                    return@launch
                }
                if (status.emergencyStopActive) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: EMERGENCY STOP ACTIVE"
                    return@launch
                }
                if (status.drillOn) {
                    _autoModeBlockedReason.value = "AUTO MODE BLOCKED: DRILL ALREADY ACTIVE"
                    return@launch
                }
            }

            // Checklist passed, send mode switch request to Main ESP32
            val resp = esp32ApiClient.setMode(settings.value, targetMode)
            if (resp.isSuccess && resp.getOrNull()?.success == true) {
                // Refresh status
                val refreshed = esp32ApiClient.getStatus(settings.value)
                if (refreshed.isSuccess) {
                    _robotStatus.value = refreshed.getOrNull()!!
                }
                _autoModeBlockedReason.value = null
                _latestNotice.value = "Mode switched to ${targetMode.name}"
            } else {
                val reason = resp.getOrNull()?.reason ?: "REJECTED BY MAIN ESP32"
                _autoModeBlockedReason.value = "AUTO MODE BLOCKED: $reason"
            }
        }
    }

    fun startMovement(command: String) {
        if (_robotStatus.value.mode != RobotMode.MANUAL) return
        viewModelScope.launch(Dispatchers.IO) {
            val resp = esp32ApiClient.sendMovement(settings.value, command, _selectedSpeed.value)
            if (resp.isSuccess && resp.getOrNull()?.success == false) {
                _latestNotice.value = "COMMAND REJECTED: ${resp.getOrNull()?.reason}"
            }
        }
    }

    fun stopMovement() {
        if (_robotStatus.value.mode != RobotMode.MANUAL) return
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendMovement(settings.value, "STOP", _selectedSpeed.value)
        }
    }

    fun setSpeed(speed: String) {
        _selectedSpeed.value = speed
    }

    fun setCameraPan(pan: Int) {
        val currentTilt = _robotStatus.value.cameraTilt
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendCameraPanTilt(settings.value, pan, currentTilt)
            _robotStatus.value = _robotStatus.value.copy(cameraPan = pan)
        }
    }

    fun setCameraTilt(tilt: Int) {
        val currentPan = _robotStatus.value.cameraPan
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendCameraPanTilt(settings.value, currentPan, tilt)
            _robotStatus.value = _robotStatus.value.copy(cameraTilt = tilt)
        }
    }

    fun setCameraHome() {
        val hPan = settings.value.homePan
        val hTilt = settings.value.homeTilt
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendCameraPanTilt(settings.value, hPan, hTilt)
            _robotStatus.value = _robotStatus.value.copy(cameraPan = hPan, cameraTilt = hTilt)
        }
    }

    fun setCameraCenter() {
        val cPan = settings.value.panCenter
        val cTilt = settings.value.tiltCenter
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendCameraPanTilt(settings.value, cPan, cTilt)
            _robotStatus.value = _robotStatus.value.copy(cameraPan = cPan, cameraTilt = cTilt)
        }
    }

    fun setServo4Angle(angle: Int) {
        if (_robotStatus.value.mode != RobotMode.MANUAL) return
        val clamped = angle.coerceIn(0, 45) // Strictly 0° - 45°
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendServo4(settings.value, clamped)
            _robotStatus.value = _robotStatus.value.copy(servo4Angle = clamped)
        }
    }

    fun setServo4Home() {
        setServo4Angle(0)
    }

    fun setServo4Center() {
        setServo4Angle(22)
    }

    fun toggleRelay(relayNumber: Int) {
        if (_robotStatus.value.mode != RobotMode.MANUAL) return
        // Relay 1 AUTO DRILL has NO manual control
        if (relayNumber == 1) return

        val currentState = when (relayNumber) {
            2 -> _robotStatus.value.relay2Soil
            3 -> _robotStatus.value.relay3Pump
            4 -> _robotStatus.value.relay4Siren
            else -> false
        }
        val newState = !currentState

        viewModelScope.launch(Dispatchers.IO) {
            val resp = esp32ApiClient.sendRelay(settings.value, relayNumber, newState)
            if (resp.isSuccess && resp.getOrNull()?.success == true) {
                // Refresh status
                val refreshed = esp32ApiClient.getStatus(settings.value)
                if (refreshed.isSuccess) {
                    _robotStatus.value = refreshed.getOrNull()!!
                }
            } else {
                _latestNotice.value = "RELAY $relayNumber REJECTED: ${resp.getOrNull()?.reason}"
            }
        }
    }

    fun triggerEmergencyStop() {
        viewModelScope.launch(Dispatchers.IO) {
            esp32ApiClient.sendEmergencyStop(settings.value, true)
            // Immediately stop any autonomous targeting
            targetingJob?.cancel()
            _isTargetingSequenceActive.value = false
            val refreshed = esp32ApiClient.getStatus(settings.value)
            if (refreshed.isSuccess) {
                _robotStatus.value = refreshed.getOrNull()!!
            } else {
                _robotStatus.value = _robotStatus.value.copy(
                    emergencyStopActive = true,
                    mode = RobotMode.MANUAL,
                    drillOn = false
                )
            }
            notificationHelper.triggerAlert(
                "CRITICAL",
                "EMERGENCY STOP TRIGGERED",
                "All operations aborted. Physical e-stop sent to Main ESP32.",
                settings.value
            )
        }
    }

    fun setBrightness(brightness: Int) {
        val updated = settings.value.copy(brightness = brightness)
        preferencesManager.updateSettings(updated)
        viewModelScope.launch(Dispatchers.IO) {
            val hwSuccess = cameraApiClient.setHardwareBrightness(updated, brightness)
            _isHardwareBrightness.value = hwSuccess
        }
    }

    fun resetBrightness() {
        setBrightness(50)
    }

    fun startAi() {
        aiVisionEngine.setAiRunning(true)
    }

    fun stopAi() {
        aiVisionEngine.setAiRunning(false)
    }

    fun captureFrameNow() {
        viewModelScope.launch(Dispatchers.IO) {
            val res = cameraApiClient.captureFrame(settings.value)
            if (res.isSuccess) {
                _latestNotice.value = "Frame captured successfully (${res.getOrNull()?.width}x${res.getOrNull()?.height})"
                aiVisionEngine.analyzeFrame(res.getOrNull(), settings.value)
            } else {
                _latestNotice.value = "Capture failed: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun uploadWeedReference(slot: Int, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = weedStorage.saveReferenceImageFromUri(slot, uri)
            if (result.isSuccess) {
                _latestNotice.value = "Weed Reference $slot saved successfully"
            } else {
                _latestNotice.value = "Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun uploadWeedReferenceBitmap(slot: Int, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = weedStorage.saveReferenceBitmap(slot, bitmap)
            if (result.isSuccess) {
                _latestNotice.value = "Weed Reference $slot saved successfully"
            } else {
                _latestNotice.value = "Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteWeedReference(slot: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            weedStorage.deleteReferenceImage(slot)
            _latestNotice.value = "Weed Reference $slot removed"
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        preferencesManager.updateSettings(newSettings)
        mjpegReader.startStream(newSettings.getStreamUrl())
        _latestNotice.value = "Settings updated"
    }

    fun resetSettings() {
        preferencesManager.resetToDefaults()
        mjpegReader.startStream(settings.value.getStreamUrl())
        _latestNotice.value = "Settings reset to defaults"
    }

    fun clearNotice() {
        _latestNotice.value = null
    }

    fun clearAutoBlockedReason() {
        _autoModeBlockedReason.value = null
    }
}
