package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WeedReferenceSlot
import com.example.data.model.DetectionClass
import com.example.data.model.DetectionHistoryItem
import com.example.data.model.DetectionResult
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.ui.components.CameraStreamCard
import com.example.ui.components.SafetyBanner
import com.example.ui.components.WeedReferenceSection
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning

@Composable
fun AiVisionScreen(
    status: RobotStatus,
    currentFrame: Bitmap?,
    isStreaming: Boolean,
    hasError: Boolean,
    errorMessage: String?,
    brightness: Int,
    isHardwareBrightness: Boolean,
    isAiRunning: Boolean,
    latestDetection: DetectionResult?,
    weedSlots: List<WeedReferenceSlot>,
    detectionHistory: List<DetectionHistoryItem>,
    autoBlockedReason: String?,
    onClearAutoBlocked: () -> Unit,
    onEmergencyStop: () -> Unit,
    onStartAi: () -> Unit,
    onStopAi: () -> Unit,
    onCaptureNow: () -> Unit,
    onDoubleTapStream: () -> Unit,
    onRetryStream: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBrightnessChanged: (Int) -> Unit,
    onResetBrightness: () -> Unit,
    onUploadWeedUri: (Int, Uri) -> Unit,
    onUploadWeedBitmap: (Int, Bitmap) -> Unit,
    onDeleteWeed: (Int) -> Unit,
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
        // Safety watchdog banner
        SafetyBanner(
            status = status,
            autoBlockedReason = autoBlockedReason,
            onClearAutoBlocked = onClearAutoBlocked,
            onEmergencyStop = onEmergencyStop
        )

        // Camera Feed with AI Bounding Box Overlay
        CameraStreamCard(
            currentFrame = currentFrame,
            isStreaming = isStreaming,
            hasError = hasError,
            errorMessage = errorMessage,
            brightness = brightness,
            isHardwareBrightness = isHardwareBrightness,
            detectionResult = latestDetection,
            showDetectionOverlay = true,
            onDoubleTapStream = onDoubleTapStream,
            onRetryStream = onRetryStream,
            onNavigateToSettings = onNavigateToSettings,
            onBrightnessChanged = onBrightnessChanged,
            onResetBrightness = onResetBrightness,
            accentColor = accentColor
        )

        // AI Control Buttons (START AI, STOP AI, CAPTURE, ANALYZE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = if (isAiRunning) onStopAi else onStartAi,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAiRunning) StatusCritical else accentColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("toggle_ai_button")
            ) {
                Icon(
                    imageVector = if (isAiRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(if (isAiRunning) "STOP AI" else "START AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCaptureNow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("capture_frame_button")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("CAPTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCaptureNow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("analyze_frame_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("ANALYZE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Real-Time Detection Telemetry Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("detection_telemetry_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI VISION TELEMETRY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor
                    )
                    if (latestDetection != null) {
                        val badgeColor = when (latestDetection.classification) {
                            DetectionClass.WEED -> if (latestDetection.targetStatus.startsWith("WEED CONFIRMED")) StatusCritical else StatusWarning
                            DetectionClass.ONION -> StatusSafe
                            DetectionClass.UNKNOWN -> Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .background(badgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = latestDetection.targetStatus,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (latestDetection != null && latestDetection.w > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Class: ${latestDetection.classification.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Confidence: ${(latestDetection.confidence * 100).toInt()}%", fontSize = 11.sp)
                            Text("Confirmations: ${latestDetection.confirmationCount}", fontSize = 11.sp)
                        }
                        Column {
                            Text("BBox: [${latestDetection.x}, ${latestDetection.y}]", fontSize = 11.sp)
                            Text("Size: ${latestDetection.w} x ${latestDetection.h} px", fontSize = 11.sp)
                            Text("Center: (${latestDetection.centerX}, ${latestDetection.centerY})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = if (!isAiRunning) "AI vision offline. Tap START AI to analyze video frames." else "Scanning camera feed for weeds and onions...",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Coordinate Transformation Pipeline Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FBF9), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "COORDINATE PIPELINE:\nBBox (X,Y,W,H) → Center (X,Y) → Perspective Calibration → Ground (X,Y) → Arm Frame → 3-DOF Inverse Kinematics → Servos 1, 2, 3",
                        fontSize = 9.sp,
                        color = Color(0xFF455A64),
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // Weed Reference Images (Exactly 3 slots, original colors preserved)
        WeedReferenceSection(
            slots = weedSlots,
            onUploadUri = onUploadWeedUri,
            onUploadBitmap = onUploadWeedBitmap,
            onDelete = onDeleteWeed,
            accentColor = accentColor
        )

        // Recent Detection Log
        if (detectionHistory.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RECENT DETECTION HISTORY LOG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )

                    detectionHistory.take(4).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FBF9), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${item.classification} (${(item.confidence * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.classification == "WEED") StatusCritical else if (item.classification == "ONION") StatusSafe else Color.Gray
                                )
                                Text(
                                    text = "Center (${item.centerX}, ${item.centerY}) • ${item.result}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Text(
                                text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date(item.timestamp)),
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
