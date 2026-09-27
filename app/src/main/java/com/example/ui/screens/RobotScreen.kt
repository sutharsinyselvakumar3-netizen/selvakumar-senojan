package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetectionResult
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.ui.components.CameraStreamCard
import com.example.ui.components.MovementControls
import com.example.ui.components.PanTiltSliders
import com.example.ui.components.RelayControls
import com.example.ui.components.SafetyBanner
import com.example.ui.components.Servo4Slider
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.AutoNavyLight
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.ManualGreenLight
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusWarning

@Composable
fun RobotScreen(
    status: RobotStatus,
    currentFrame: Bitmap?,
    isStreaming: Boolean,
    hasError: Boolean,
    errorMessage: String?,
    brightness: Int,
    isHardwareBrightness: Boolean,
    selectedSpeed: String,
    latestDetection: DetectionResult?,
    isTargetingActive: Boolean,
    targetingStepDescription: String?,
    autoBlockedReason: String?,
    onClearAutoBlocked: () -> Unit,
    onEmergencyStop: () -> Unit,
    onDoubleTapStream: () -> Unit,
    onRetryStream: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBrightnessChanged: (Int) -> Unit,
    onResetBrightness: () -> Unit,
    onSpeedSelected: (String) -> Unit,
    onStartMovement: (String) -> Unit,
    onStopMovement: () -> Unit,
    onPanChanged: (Int) -> Unit,
    onTiltChanged: (Int) -> Unit,
    onPanTiltHome: () -> Unit,
    onPanTiltCenter: () -> Unit,
    onServo4Changed: (Int) -> Unit,
    onServo4Home: () -> Unit,
    onServo4Center: () -> Unit,
    onToggleRelay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeViewTab by remember { mutableStateOf("NORMAL") } // "NORMAL" or "AI_AUTO"
    val isAuto = status.mode == RobotMode.AUTO
    val accentColor = if (isAuto) AutoNavy else ManualGreen

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Safety watchdog banner
        SafetyBanner(
            status = status,
            autoBlockedReason = autoBlockedReason,
            onClearAutoBlocked = onClearAutoBlocked,
            onEmergencyStop = onEmergencyStop
        )

        // View Tabs: [ NORMAL STREAM ] [ AI AUTO ] (These tabs change internal view only)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val normalActive = activeViewTab == "NORMAL"
            val aiActive = activeViewTab == "AI_AUTO"

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (normalActive) accentColor else Color.Transparent)
                    .clickable { activeViewTab = "NORMAL" }
                    .padding(vertical = 10.dp)
                    .testTag("tab_normal_stream"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NORMAL STREAM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (normalActive) Color.White else Color.DarkGray
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (aiActive) accentColor else Color.Transparent)
                    .clickable { activeViewTab = "AI_AUTO" }
                    .padding(vertical = 10.dp)
                    .testTag("tab_ai_auto"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI AUTO VIEW",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (aiActive) Color.White else Color.DarkGray
                )
            }
        }

        // Live ESP32-CAM Stream Card with Real Brightness Control
        CameraStreamCard(
            currentFrame = currentFrame,
            isStreaming = isStreaming,
            hasError = hasError,
            errorMessage = errorMessage,
            brightness = brightness,
            isHardwareBrightness = isHardwareBrightness,
            detectionResult = latestDetection,
            showDetectionOverlay = activeViewTab == "AI_AUTO",
            onDoubleTapStream = onDoubleTapStream,
            onRetryStream = onRetryStream,
            onNavigateToSettings = onNavigateToSettings,
            onBrightnessChanged = onBrightnessChanged,
            onResetBrightness = onResetBrightness,
            accentColor = accentColor
        )

        // Autonomous Targeting Status in AI Auto view
        if (activeViewTab == "AI_AUTO" && isTargetingActive) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AutoNavyLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_targeting_status_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Targeting",
                        tint = AutoNavy
                    )
                    Column {
                        Text(
                            text = "AUTONOMOUS WEED DRILL WORKFLOW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AutoNavy
                        )
                        Text(
                            text = targetingStepDescription ?: "Executing inverse kinematics & drill sequence",
                            fontSize = 11.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }
        }

        // Manual controls section — only available when global mode is MANUAL
        if (!isAuto) {
            Text(
                text = "MANUAL TELEOPERATION CONTROLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ManualGreen
            )

            // 1. Movement Controls (FORWARD, LEFT, RIGHT, REVERSE; SLOW/MEDIUM/FAST)
            MovementControls(
                selectedSpeed = selectedSpeed,
                onSpeedSelected = onSpeedSelected,
                onStartMovement = onStartMovement,
                onStopMovement = onStopMovement
            )

            // 2. Camera PAN/TILT Sliders (Gimbal)
            PanTiltSliders(
                panAngle = status.cameraPan,
                tiltAngle = status.cameraTilt,
                onPanChanged = onPanChanged,
                onTiltChanged = onTiltChanged,
                onHomeClicked = onPanTiltHome,
                onCenterClicked = onPanTiltCenter
            )

            // 3. Servo 4 Manual Auxiliary Slider (Strictly 0°–45°)
            Servo4Slider(
                currentAngle = status.servo4Angle,
                onAngleChanged = onServo4Changed,
                onHomeClicked = onServo4Home,
                onCenterClicked = onServo4Center
            )

            // 4. Relay Actuators (Relay 2 SOIL, Relay 3 WATER PUMP, Relay 4 SIREN)
            RelayControls(
                drillActive = status.drillOn,
                soilRelayActive = status.relay2Soil,
                waterPumpActive = status.relay3Pump,
                sirenActive = status.relay4Siren,
                waterCriticalLockout = status.waterLevel != null && status.waterLevel < 15,
                onToggleRelay = onToggleRelay
            )
        } else {
            // AUTO Mode Indicator Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = AutoNavyLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("auto_locked_controls_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Auto Active",
                        tint = AutoNavy
                    )
                    Column {
                        Text(
                            text = "AUTO OPERATION IN PROGRESS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AutoNavy
                        )
                        Text(
                            text = "Robot is scanning and weeding autonomously. Manual drive controls are locked by physical safety authority.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }
        }
    }
}
