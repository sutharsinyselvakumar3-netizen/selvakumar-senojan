package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.RobotMode
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    robotMode: RobotMode,
    onSaveSettings: (AppSettings) -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAuto = robotMode == RobotMode.AUTO
    val accentColor = if (isAuto) AutoNavy else ManualGreen

    var settings by remember(currentSettings) { mutableStateOf(currentSettings) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GARDEN ROBOT SETTINGS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = "Network endpoints, sensors, calibration & safety limits",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { onSaveSettings(settings) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_settings_top_button")
            ) {
                Text("SAVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 1. NETWORK SECTION
        SettingsSectionCard(title = "1. NETWORK & HOTSPOT CONFIGURATION", accentColor = accentColor) {
            OutlinedTextField(
                value = settings.hotspotName,
                onValueChange = { settings = settings.copy(hotspotName = it) },
                label = { Text("Mobile Hotspot SSID") },
                modifier = Modifier.fillMaxWidth().testTag("setting_hotspot_name")
            )
            OutlinedTextField(
                value = settings.hotspotPassword,
                onValueChange = { settings = settings.copy(hotspotPassword = it) },
                label = { Text("Mobile Hotspot Password") },
                modifier = Modifier.fillMaxWidth().testTag("setting_hotspot_password")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.esp32Ip,
                    onValueChange = { settings = settings.copy(esp32Ip = it) },
                    label = { Text("Main ESP32 IP") },
                    modifier = Modifier.weight(2f).testTag("setting_esp32_ip")
                )
                OutlinedTextField(
                    value = settings.esp32Port.toString(),
                    onValueChange = { settings = settings.copy(esp32Port = it.toIntOrNull() ?: 80) },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("setting_esp32_port")
                )
            }
        }

        // 2. CAMERA SECTION
        SettingsSectionCard(title = "2. ESP32-CAM STREAM & GIMBAL", accentColor = accentColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.cameraIp,
                    onValueChange = { settings = settings.copy(cameraIp = it) },
                    label = { Text("Camera IP") },
                    modifier = Modifier.weight(2f).testTag("setting_camera_ip")
                )
                OutlinedTextField(
                    value = settings.streamPort.toString(),
                    onValueChange = { settings = settings.copy(streamPort = it.toIntOrNull() ?: 81) },
                    label = { Text("Stream Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("setting_stream_port")
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.panCenter.toString(),
                    onValueChange = { settings = settings.copy(panCenter = it.toIntOrNull() ?: 90) },
                    label = { Text("Pan Center (°)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.tiltCenter.toString(),
                    onValueChange = { settings = settings.copy(tiltCenter = it.toIntOrNull() ?: 90) },
                    label = { Text("Tilt Center (°)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. SOIL & WATER SECTION
        SettingsSectionCard(title = "3. SOIL & WATER SENSOR THRESHOLDS", accentColor = accentColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.soilDryThreshold.toString(),
                    onValueChange = { settings = settings.copy(soilDryThreshold = it.toIntOrNull() ?: 40) },
                    label = { Text("Soil Dry Thresh (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.waterLowThreshold.toString(),
                    onValueChange = { settings = settings.copy(waterLowThreshold = it.toIntOrNull() ?: 30) },
                    label = { Text("Water Low (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            SettingsSwitchRow(
                label = "Water Pump Lockout on Critical Level",
                checked = settings.waterPumpLockout,
                onCheckedChange = { settings = settings.copy(waterPumpLockout = it) },
                accentColor = accentColor
            )
            SettingsSwitchRow(
                label = "Trigger Siren (Relay 4) on Dry Soil",
                checked = settings.soilRelay4Siren,
                onCheckedChange = { settings = settings.copy(soilRelay4Siren = it) },
                accentColor = accentColor
            )
        }

        // 4. VIBRATION & MPU SAFETY SECTION
        SettingsSectionCard(title = "4. VIBRATION (GPIO 33) & MPU6050 LIMITS", accentColor = accentColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.vibrationThreshold.toString(),
                    onValueChange = { settings = settings.copy(vibrationThreshold = it.toIntOrNull() ?: 60) },
                    label = { Text("Vibration Thresh") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.vibrationDebounceMs.toString(),
                    onValueChange = { settings = settings.copy(vibrationDebounceMs = it.toIntOrNull() ?: 200) },
                    label = { Text("Debounce (ms)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.mpuPitchLimit.toString(),
                    onValueChange = { settings = settings.copy(mpuPitchLimit = it.toDoubleOrNull() ?: 25.0) },
                    label = { Text("Max Pitch (°)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.mpuRollLimit.toString(),
                    onValueChange = { settings = settings.copy(mpuRollLimit = it.toDoubleOrNull() ?: 25.0) },
                    label = { Text("Max Roll (°)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            SettingsSwitchRow(
                label = "Halt Motors on Critical Vibration",
                checked = settings.vibrationMotorStop,
                onCheckedChange = { settings = settings.copy(vibrationMotorStop = it) },
                accentColor = accentColor
            )
        }

        // 5. AI & INVERSE KINEMATICS SECTION
        SettingsSectionCard(title = "5. AI DETECTION & INVERSE KINEMATICS", accentColor = accentColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = (settings.aiMinConfidence * 100).toInt().toString(),
                    onValueChange = { settings = settings.copy(aiMinConfidence = (it.toFloatOrNull() ?: 80f) / 100f) },
                    label = { Text("Min Confidence (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.aiConsecutiveDetections.toString(),
                    onValueChange = { settings = settings.copy(aiConsecutiveDetections = it.toIntOrNull() ?: 3) },
                    label = { Text("Consecutive Frames") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.link1Length.toString(),
                    onValueChange = { settings = settings.copy(link1Length = it.toFloatOrNull() ?: 12f) },
                    label = { Text("Arm Link 1 (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.link2Length.toString(),
                    onValueChange = { settings = settings.copy(link2Length = it.toFloatOrNull() ?: 10f) },
                    label = { Text("Arm Link 2 (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 6. PHYSICAL SAFETY & DRILL TIMEOUT
        SettingsSectionCard(title = "6. SAFETY WATCHDOG & DRILL PROTECTION", accentColor = accentColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = settings.criticalVoltageThreshold.toString(),
                    onValueChange = { settings = settings.copy(criticalVoltageThreshold = it.toDoubleOrNull() ?: 10.2) },
                    label = { Text("Critical Volt (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = settings.maxDrillTimeSeconds.toString(),
                    onValueChange = { settings = settings.copy(maxDrillTimeSeconds = (it.toIntOrNull() ?: 7).coerceAtMost(7)) },
                    label = { Text("Max Drill (s ≤ 7)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Note: Main ESP32 enforces independent 7-second hardware cut-off for Relay 1 AUTO DRILL.",
                fontSize = 10.sp,
                color = StatusCritical
            )
        }

        // 7. GENERAL NOTIFICATIONS & RESET
        SettingsSectionCard(title = "7. GENERAL & ALERT PREFERENCES", accentColor = accentColor) {
            SettingsSwitchRow(
                label = "Phone Vibration on Critical Events",
                checked = settings.phoneVibrationEnabled,
                onCheckedChange = { settings = settings.copy(phoneVibrationEnabled = it) },
                accentColor = accentColor
            )
            SettingsSwitchRow(
                label = "Android Status Bar Notifications",
                checked = settings.notificationsEnabled,
                onCheckedChange = { settings = settings.copy(notificationsEnabled = it) },
                accentColor = accentColor
            )
            SettingsSwitchRow(
                label = "Sound Alerts",
                checked = settings.soundEnabled,
                onCheckedChange = { settings = settings.copy(soundEnabled = it) },
                accentColor = accentColor
            )
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onSaveSettings(settings) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("save_settings_bottom_button")
            ) {
                Text("SAVE SETTINGS", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onResetSettings,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCritical),
                modifier = Modifier.weight(1f).testTag("reset_settings_button")
            ) {
                Text("RESET DEFAULTS", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF263238), modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
        )
    }
}
