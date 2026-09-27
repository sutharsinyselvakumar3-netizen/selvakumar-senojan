package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RobotMode
import com.example.ui.components.ModeSwitchDialog
import com.example.ui.screens.AiVisionScreen
import com.example.ui.screens.ArmScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RobotScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AiCompanionTheme
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.AutoNavyLight
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.ManualGreenLight
import com.example.ui.theme.RoyalWhite
import com.example.ui.viewmodel.RobotViewModel

enum class MainScreen(val label: String, val icon: ImageVector) {
    HOME("HOME", Icons.Default.Home),
    ROBOT("ROBOT", Icons.Default.SmartToy),
    AI_VISION("AI VISION", Icons.Default.Visibility),
    ARM("ARM", Icons.Default.PrecisionManufacturing),
    SETTINGS("SETTINGS", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: RobotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val status by viewModel.robotStatus.collectAsStateWithLifecycle()
            val cameraOnline by viewModel.cameraOnline.collectAsStateWithLifecycle()
            val isAiRunning by viewModel.aiVisionEngine.isAiRunning.collectAsStateWithLifecycle()
            val latestDetection by viewModel.aiVisionEngine.latestResult.collectAsStateWithLifecycle()
            val weedSlots by viewModel.weedSlots.collectAsStateWithLifecycle()
            val alertHistory by viewModel.alertHistory.collectAsStateWithLifecycle()
            val detectionHistory by viewModel.detectionHistory.collectAsStateWithLifecycle()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val currentFrame by viewModel.mjpegReader.currentFrame.collectAsStateWithLifecycle()
            val isStreaming by viewModel.mjpegReader.isStreaming.collectAsStateWithLifecycle()
            val hasStreamError by viewModel.mjpegReader.hasError.collectAsStateWithLifecycle()
            val streamErrorMessage by viewModel.mjpegReader.errorMessage.collectAsStateWithLifecycle()
            val isHwBrightness by viewModel.isHardwareBrightness.collectAsStateWithLifecycle()
            val selectedSpeed by viewModel.selectedSpeed.collectAsStateWithLifecycle()
            val autoBlockedReason by viewModel.autoModeBlockedReason.collectAsStateWithLifecycle()
            val modeSwitchPrompt by viewModel.showModeSwitchDialog.collectAsStateWithLifecycle()
            val noticeMessage by viewModel.latestNotice.collectAsStateWithLifecycle()
            val isTargetingActive by viewModel.isTargetingSequenceActive.collectAsStateWithLifecycle()
            val targetingStepDesc by viewModel.targetingStepDescription.collectAsStateWithLifecycle()

            val isAuto = status.mode == RobotMode.AUTO
            val accentColor = if (isAuto) AutoNavy else ManualGreen

            var currentScreen by remember { mutableStateOf(MainScreen.HOME) }
            val snackbarHostState = remember { SnackbarHostState() }

            // Handle back press to return to HOME screen from sub-screens
            if (currentScreen != MainScreen.HOME) {
                BackHandler {
                    currentScreen = MainScreen.HOME
                }
            }

            LaunchedEffect(noticeMessage) {
                if (noticeMessage != null) {
                    snackbarHostState.showSnackbar(noticeMessage!!)
                    viewModel.clearNotice()
                }
            }

            AiCompanionTheme(isAutoMode = isAuto) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .testTag("main_navigation_bar")
                        ) {
                            MainScreen.values().forEach { screen ->
                                val selected = currentScreen == screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 9.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = accentColor,
                                        selectedTextColor = accentColor,
                                        indicatorColor = if (isAuto) AutoNavyLight else ManualGreenLight,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    ),
                                    modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(RoyalWhite)
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            MainScreen.HOME -> HomeScreen(
                                status = status,
                                cameraOnline = cameraOnline,
                                isAiRunning = isAiRunning,
                                latestDetection = latestDetection,
                                latestAlerts = alertHistory,
                                autoBlockedReason = autoBlockedReason,
                                onClearAutoBlocked = { viewModel.clearAutoBlockedReason() },
                                onEmergencyStop = { viewModel.triggerEmergencyStop() },
                                onRequestModeSwitch = { target -> viewModel.requestModeSwitch(target) }
                            )

                            MainScreen.ROBOT -> RobotScreen(
                                status = status,
                                currentFrame = currentFrame,
                                isStreaming = isStreaming,
                                hasError = hasStreamError,
                                errorMessage = streamErrorMessage,
                                brightness = settings.brightness,
                                isHardwareBrightness = isHwBrightness,
                                selectedSpeed = selectedSpeed,
                                latestDetection = latestDetection,
                                isTargetingActive = isTargetingActive,
                                targetingStepDescription = targetingStepDesc,
                                autoBlockedReason = autoBlockedReason,
                                onClearAutoBlocked = { viewModel.clearAutoBlockedReason() },
                                onEmergencyStop = { viewModel.triggerEmergencyStop() },
                                onDoubleTapStream = { viewModel.mjpegReader.toggleStream() },
                                onRetryStream = { viewModel.mjpegReader.retry() },
                                onNavigateToSettings = { currentScreen = MainScreen.SETTINGS },
                                onBrightnessChanged = { b -> viewModel.setBrightness(b) },
                                onResetBrightness = { viewModel.resetBrightness() },
                                onSpeedSelected = { spd -> viewModel.setSpeed(spd) },
                                onStartMovement = { cmd -> viewModel.startMovement(cmd) },
                                onStopMovement = { viewModel.stopMovement() },
                                onPanChanged = { pan -> viewModel.setCameraPan(pan) },
                                onTiltChanged = { tilt -> viewModel.setCameraTilt(tilt) },
                                onPanTiltHome = { viewModel.setCameraHome() },
                                onPanTiltCenter = { viewModel.setCameraCenter() },
                                onServo4Changed = { s4 -> viewModel.setServo4Angle(s4) },
                                onServo4Home = { viewModel.setServo4Home() },
                                onServo4Center = { viewModel.setServo4Center() },
                                onToggleRelay = { r -> viewModel.toggleRelay(r) }
                            )

                            MainScreen.AI_VISION -> AiVisionScreen(
                                status = status,
                                currentFrame = currentFrame,
                                isStreaming = isStreaming,
                                hasError = hasStreamError,
                                errorMessage = streamErrorMessage,
                                brightness = settings.brightness,
                                isHardwareBrightness = isHwBrightness,
                                isAiRunning = isAiRunning,
                                latestDetection = latestDetection,
                                weedSlots = weedSlots,
                                detectionHistory = detectionHistory,
                                autoBlockedReason = autoBlockedReason,
                                onClearAutoBlocked = { viewModel.clearAutoBlockedReason() },
                                onEmergencyStop = { viewModel.triggerEmergencyStop() },
                                onStartAi = { viewModel.startAi() },
                                onStopAi = { viewModel.stopAi() },
                                onCaptureNow = { viewModel.captureFrameNow() },
                                onDoubleTapStream = { viewModel.mjpegReader.toggleStream() },
                                onRetryStream = { viewModel.mjpegReader.retry() },
                                onNavigateToSettings = { currentScreen = MainScreen.SETTINGS },
                                onBrightnessChanged = { b -> viewModel.setBrightness(b) },
                                onResetBrightness = { viewModel.resetBrightness() },
                                onUploadWeedUri = { slot, uri -> viewModel.uploadWeedReference(slot, uri) },
                                onUploadWeedBitmap = { slot, bmp -> viewModel.uploadWeedReferenceBitmap(slot, bmp) },
                                onDeleteWeed = { slot -> viewModel.deleteWeedReference(slot) }
                            )

                            MainScreen.ARM -> ArmScreen(
                                status = status,
                                autoBlockedReason = autoBlockedReason,
                                onClearAutoBlocked = { viewModel.clearAutoBlockedReason() },
                                onEmergencyStop = { viewModel.triggerEmergencyStop() },
                                onServo4Changed = { s4 -> viewModel.setServo4Angle(s4) },
                                onServo4Home = { viewModel.setServo4Home() },
                                onServo4Center = { viewModel.setServo4Center() }
                            )

                            MainScreen.SETTINGS -> SettingsScreen(
                                currentSettings = settings,
                                robotMode = status.mode,
                                onSaveSettings = { updated -> viewModel.updateSettings(updated) },
                                onResetSettings = { viewModel.resetSettings() }
                            )
                        }

                        // Global Mode Switch Confirmation Dialog
                        ModeSwitchDialog(
                            targetMode = modeSwitchPrompt,
                            onConfirm = { viewModel.confirmModeSwitch() },
                            onDismiss = { viewModel.cancelModeSwitch() }
                        )
                    }
                }
            }
        }
    }
}
