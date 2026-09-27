package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetectionClass
import com.example.data.model.DetectionResult
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning

@Composable
fun CameraStreamCard(
    currentFrame: Bitmap?,
    isStreaming: Boolean,
    hasError: Boolean,
    errorMessage: String?,
    brightness: Int,
    isHardwareBrightness: Boolean,
    detectionResult: DetectionResult? = null,
    showDetectionOverlay: Boolean = false,
    onDoubleTapStream: () -> Unit,
    onRetryStream: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBrightnessChanged: (Int) -> Unit,
    onResetBrightness: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (isStreaming && !hasError && currentFrame != null) StatusSafe else StatusCritical,
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Text(
                        text = "LIVE ESP32-CAM STREAM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor
                    )
                }

                Text(
                    text = if (isStreaming) "DOUBLE-TAP TO PAUSE" else "PAUSED",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            // Stream Frame / Error Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { onDoubleTapStream() }
                        )
                    }
                    .testTag("camera_preview_container"),
                contentAlignment = Alignment.Center
            ) {
                if (currentFrame != null && !hasError) {
                    // Preview brightness adjustment matrix (contrast/exposure multiplier)
                    // If hardware brightness is supported, factor is 1.0 (natural), else adjusts software preview
                    val brightnessFactor = if (isHardwareBrightness) 1.0f else (brightness / 50.0f)
                    val colorMatrix = ColorMatrix(
                        floatArrayOf(
                            brightnessFactor, 0f, 0f, 0f, 0f,
                            0f, brightnessFactor, 0f, 0f, 0f,
                            0f, 0f, brightnessFactor, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )

                    // Render camera image in NATURAL original color (BoxFit.contain / ContentScale.Fit)
                    Image(
                        bitmap = currentFrame.asImageBitmap(),
                        contentDescription = "Natural ESP32-CAM Video Stream",
                        contentScale = ContentScale.Fit,
                        colorFilter = if (isHardwareBrightness) null else ColorFilter.colorMatrix(colorMatrix),
                        modifier = Modifier.fillMaxSize()
                    )

                    // Optional Detection Overlay Box
                    if (showDetectionOverlay && detectionResult != null && detectionResult.w > 0) {
                        val boxBorderColor = when (detectionResult.classification) {
                            DetectionClass.WEED -> if (detectionResult.targetStatus.startsWith("WEED CONFIRMED")) StatusCritical else StatusWarning
                            DetectionClass.ONION -> StatusSafe
                            DetectionClass.UNKNOWN -> Color.Gray
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth(0.5f)
                                    .height(90.dp)
                                    .border(2.dp, boxBorderColor, RoundedCornerShape(8.dp))
                                    .background(boxBorderColor.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = "${detectionResult.targetStatus} (${(detectionResult.confidence * 100).toInt()}%)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(boxBorderColor, RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Stream Unavailable / Error State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "Stream Offline",
                            tint = Color.LightGray,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = errorMessage ?: "STREAM NOT AVAILABLE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Ensure ESP32-CAM is connected to garden Wi-Fi hotspot.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onRetryStream,
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier.testTag("retry_stream_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("RETRY", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onNavigateToSettings,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.testTag("camera_settings_button")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("SETTINGS", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Real Brightness Control Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FBF9), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CAMERA BRIGHTNESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = if (isHardwareBrightness) "HARDWARE BRIGHTNESS" else "PREVIEW BRIGHTNESS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isHardwareBrightness) StatusSafe else Color(0xFF616161)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$brightness%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        OutlinedButton(
                            onClick = onResetBrightness,
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier.height(30.dp).testTag("reset_brightness_button")
                        ) {
                            Text("RESET 50%", fontSize = 9.sp)
                        }
                    }
                }

                Slider(
                    value = brightness.toFloat(),
                    onValueChange = { onBrightnessChanged(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("brightness_slider")
                )
            }
        }
    }
}
