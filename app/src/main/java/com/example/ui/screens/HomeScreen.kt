package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertItem
import com.example.data.model.DetectionResult
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.ui.components.SafetyBanner
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.AutoNavyLight
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.ManualGreenLight
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning

@Composable
fun HomeScreen(
    status: RobotStatus,
    cameraOnline: Boolean,
    isAiRunning: Boolean,
    latestDetection: DetectionResult?,
    latestAlerts: List<AlertItem>,
    autoBlockedReason: String?,
    onClearAutoBlocked: () -> Unit,
    onEmergencyStop: () -> Unit,
    onRequestModeSwitch: (RobotMode) -> Unit,
    modifier: Modifier = Modifier
) {
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
        // App Header & Tagline
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI COMPANION",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "“See the Weed. Protect the Onion. Work Smart.”",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF555555)
                    )
                }

                // Global Mode Badge & Switch Action
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAuto) AutoNavyLight else ManualGreenLight)
                        .border(1.5.dp, accentColor, RoundedCornerShape(12.dp))
                        .clickable {
                            val nextMode = if (isAuto) RobotMode.MANUAL else RobotMode.AUTO
                            onRequestModeSwitch(nextMode)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("mode_switch_header_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Mode",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "[ ${status.mode.name} ]",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = accentColor
                        )
                    }
                }
            }
        }

        // Safety Watchdog Banner & E-Stop
        SafetyBanner(
            status = status,
            autoBlockedReason = autoBlockedReason,
            onClearAutoBlocked = onClearAutoBlocked,
            onEmergencyStop = onEmergencyStop
        )

        // Primary Connection & AI Status Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusCard(
                title = "MAIN ESP32",
                value = status.getDisplayStatus(),
                isGood = status.isConnected && !status.isStale(),
                isWarning = status.isStale(),
                accentColor = accentColor,
                modifier = Modifier.weight(1f),
                tag = "home_esp32_status"
            )
            StatusCard(
                title = "ESP32-CAM",
                value = if (cameraOnline) "ONLINE" else "OFFLINE",
                isGood = cameraOnline,
                accentColor = accentColor,
                modifier = Modifier.weight(1f),
                tag = "home_camera_status"
            )
            StatusCard(
                title = "AI VISION",
                value = if (isAiRunning) "ACTIVE" else "READY",
                isGood = isAiRunning,
                accentColor = accentColor,
                modifier = Modifier.weight(1f),
                tag = "home_ai_status"
            )
        }

        // Battery, Soil, Water Telemetry Cards
        Text(
            text = "SOIL & POWER TELEMETRY",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = accentColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Battery Card
            TelemetryMetricCard(
                title = "BATTERY",
                icon = if (status.batteryVoltage != null && status.batteryVoltage < 10.5) Icons.Default.BatteryAlert else Icons.Default.BatteryChargingFull,
                primaryText = if (status.batteryVoltage != null) "${String.format("%.1f", status.batteryVoltage)} V" else "UNKNOWN",
                subText = if (status.batteryPercent != null) "${status.batteryPercent}%" else "OFFLINE",
                statusColor = when {
                    status.batteryVoltage == null -> Color.Gray
                    status.batteryVoltage < 10.5 -> StatusCritical
                    status.batteryVoltage < 11.2 -> StatusWarning
                    else -> StatusSafe
                },
                modifier = Modifier.weight(1f),
                tag = "home_metric_battery"
            )

            // Soil Moisture Card
            TelemetryMetricCard(
                title = "SOIL MOISTURE",
                icon = Icons.Default.Opacity,
                primaryText = if (status.soilSensorValid && status.soilMoisture != null) "${status.soilMoisture}%" else if (!status.isConnected) "OFFLINE" else "SOIL ERROR",
                subText = if (status.soilSensorValid && status.soilMoisture != null) {
                    if (status.soilMoisture < 25) "CRITICAL DRY" else if (status.soilMoisture < 40) "DRY" else "OPTIMAL"
                } else "UNKNOWN",
                statusColor = when {
                    !status.soilSensorValid || status.soilMoisture == null -> Color.Gray
                    status.soilMoisture < 25 -> StatusCritical
                    status.soilMoisture < 40 -> StatusWarning
                    else -> StatusSafe
                },
                modifier = Modifier.weight(1f),
                tag = "home_metric_soil"
            )

            // Water Level Card
            TelemetryMetricCard(
                title = "WATER LEVEL",
                icon = Icons.Default.CloudQueue,
                primaryText = if (status.waterSensorValid && status.waterLevel != null) "${status.waterLevel}%" else if (!status.isConnected) "OFFLINE" else "SENSOR ERROR",
                subText = if (status.waterSensorValid && status.waterLevel != null) {
                    if (status.waterLevel < 15) "LOCKOUT" else if (status.waterLevel < 30) "LOW" else "NORMAL"
                } else "UNKNOWN",
                statusColor = when {
                    !status.waterSensorValid || status.waterLevel == null -> Color.Gray
                    status.waterLevel < 15 -> StatusCritical
                    status.waterLevel < 30 -> StatusWarning
                    else -> StatusSafe
                },
                modifier = Modifier.weight(1f),
                tag = "home_metric_water"
            )
        }

        // MPU6050 & Vibration Safety Metrics
        Text(
            text = "KINEMATIC STABILITY & VIBRATION",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = accentColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).testTag("home_mpu_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MPU6050 (I2C)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        Box(
                            modifier = Modifier
                                .background(if (status.mpuStable) StatusSafe else StatusCritical, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (!status.isConnected) "UNKNOWN" else if (status.mpuStable) "STABLE" else "UNSTABLE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Pitch: ${if (status.pitch != null) String.format("%.1f°", status.pitch) else "UNKNOWN"}",
                        fontSize = 11.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Roll: ${if (status.roll != null) String.format("%.1f°", status.roll) else "UNKNOWN"}",
                        fontSize = 11.sp,
                        color = Color(0xFF333333)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f).testTag("home_vibration_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VIBRATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        !status.isConnected -> Color.Gray
                                        status.vibrationCritical -> StatusCritical
                                        status.vibrationDetected -> StatusWarning
                                        else -> StatusSafe
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    !status.isConnected -> "UNKNOWN"
                                    status.vibrationCritical -> "CRITICAL"
                                    status.vibrationDetected -> "DETECTED"
                                    else -> "NORMAL"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Sensor: GPIO 33",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = if (status.vibrationCritical) "HALT ENFORCED" else "Nominal threshold",
                        fontSize = 10.sp,
                        color = if (status.vibrationCritical) StatusCritical else Color.Gray
                    )
                }
            }
        }

        // Arm & Actuator Summary
        Card(
            modifier = Modifier.fillMaxWidth().testTag("home_actuators_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ROBOT ARM & RELAY ACTUATOR STATES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Arm State: ${status.armState.name}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Drill Relay 1: ${if (status.drillOn) "ON (DRILLING)" else "OFF"}", fontSize = 11.sp, color = if (status.drillOn) StatusCritical else Color.DarkGray)
                        Text("Aux Servo 4: ${status.servo4Angle}°", fontSize = 11.sp, color = Color.DarkGray)
                    }
                    Column {
                        Text("Soil Relay 2: ${if (status.relay2Soil) "ON" else "OFF"}", fontSize = 11.sp, color = if (status.relay2Soil) StatusSafe else Color.DarkGray)
                        Text("Pump Relay 3: ${if (status.relay3Pump) "ON" else "OFF"}", fontSize = 11.sp, color = if (status.relay3Pump) StatusSafe else Color.DarkGray)
                        Text("Siren Relay 4: ${if (status.relay4Siren) "ON" else "OFF"}", fontSize = 11.sp, color = if (status.relay4Siren) StatusCritical else Color.DarkGray)
                    }
                    Column {
                        Text("Cam Pan: ${status.cameraPan}°", fontSize = 11.sp)
                        Text("Cam Tilt: ${status.cameraTilt}°", fontSize = 11.sp)
                        Text("Safety: ${status.safety}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (status.safety == "SAFE") StatusSafe else StatusCritical)
                    }
                }
            }
        }

        // Latest Alerts Stream
        if (latestAlerts.isNotEmpty()) {
            Text(
                text = "LATEST SAFETY ALERTS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accentColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                latestAlerts.take(3).forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (alert.level == "CRITICAL") Color(0xFFFFEBEE) else Color(0xFFFFF8E1),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (alert.level == "CRITICAL") Icons.Default.Warning else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (alert.level == "CRITICAL") StatusCritical else StatusWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = alert.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alert.level == "CRITICAL") StatusCritical else Color(0xFF5D4037)
                            )
                            Text(
                                text = alert.message,
                                fontSize = 10.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    isGood: Boolean,
    isWarning: Boolean = false,
    accentColor: Color,
    modifier: Modifier = Modifier,
    tag: String
) {
    Card(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when {
                                isWarning -> StatusWarning
                                isGood -> StatusSafe
                                else -> StatusCritical
                            },
                            shape = CircleShape
                        )
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = when {
                        isWarning -> StatusWarning
                        isGood -> accentColor
                        else -> StatusCritical
                    }
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    title: String,
    icon: ImageVector,
    primaryText: String,
    subText: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    tag: String
) {
    Card(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
            }
            Text(
                text = primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF202020)
            )
            Text(
                text = subText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}
