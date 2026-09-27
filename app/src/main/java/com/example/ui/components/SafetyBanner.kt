package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotStatus
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning

@Composable
fun SafetyBanner(
    status: RobotStatus,
    autoBlockedReason: String?,
    onClearAutoBlocked: () -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Emergency Stop Trigger Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (status.emergencyStopActive) StatusCritical else Color(0xFFFBE9E7)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("emergency_stop_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "E-Stop Indicator",
                        tint = if (status.emergencyStopActive) Color.White else StatusCritical
                    )
                    Column {
                        Text(
                            text = if (status.emergencyStopActive) "EMERGENCY STOP ACTIVE" else "PHYSICAL SAFETY AUTHORITY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (status.emergencyStopActive) Color.White else StatusCritical
                        )
                        Text(
                            text = if (status.emergencyStopActive) "All motor & drill power halted by Main ESP32" else "Main ESP32 enforces independent safety watchdog",
                            fontSize = 11.sp,
                            color = if (status.emergencyStopActive) Color.White.copy(alpha = 0.9f) else Color(0xFF5D4037)
                        )
                    }
                }

                Button(
                    onClick = onEmergencyStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.emergencyStopActive) Color.White else StatusCritical,
                        contentColor = if (status.emergencyStopActive) StatusCritical else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("emergency_stop_button")
                ) {
                    Text(
                        text = if (status.emergencyStopActive) "RESET E-STOP" else "E-STOP",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Auto Mode Blocked Banner
        AnimatedVisibility(visible = autoBlockedReason != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("auto_blocked_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = StatusCritical
                        )
                        Text(
                            text = autoBlockedReason ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusCritical
                        )
                    }
                    IconButton(onClick = onClearAutoBlocked) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = StatusCritical
                        )
                    }
                }
            }
        }
    }
}
