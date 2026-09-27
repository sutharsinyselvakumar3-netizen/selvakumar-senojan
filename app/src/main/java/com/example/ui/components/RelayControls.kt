package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe

@Composable
fun RelayControls(
    drillActive: Boolean,
    soilRelayActive: Boolean,
    waterPumpActive: Boolean,
    sirenActive: Boolean,
    waterCriticalLockout: Boolean,
    onToggleRelay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RELAY ACTUATORS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ManualGreen
            )

            // Relay 1: AUTO DRILL (Protected - No Manual Control)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEFF1), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Relay 1 Protected",
                        tint = if (drillActive) StatusCritical else Color.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "RELAY 1: AUTO DRILL (GPIO 18)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                        Text(
                            text = if (drillActive) "DRILLING IN PROGRESS (MAX 7s)" else "Protected: Auto weed logic only",
                            fontSize = 10.sp,
                            color = if (drillActive) StatusCritical else Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (drillActive) StatusCritical else Color(0xFFCFD8DC),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (drillActive) "ON" else "OFF (LOCKED)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (drillActive) Color.White else Color(0xFF455A64)
                    )
                }
            }

            // Relay 2: SOIL (GPIO 19)
            RelayRow(
                relayNumber = 2,
                name = "RELAY 2: SOIL (GPIO 19)",
                isActive = soilRelayActive,
                onToggle = { onToggleRelay(2) },
                tag = "relay2_soil"
            )

            // Relay 3: WATER PUMP (GPIO 5)
            RelayRow(
                relayNumber = 3,
                name = "RELAY 3: WATER PUMP (GPIO 5)",
                isActive = waterPumpActive,
                isLocked = waterCriticalLockout,
                lockReason = "LOCKED: CRITICAL WATER LEVEL",
                onToggle = { onToggleRelay(3) },
                tag = "relay3_pump"
            )

            // Relay 4: SIREN/ALARM (GPIO 15)
            RelayRow(
                relayNumber = 4,
                name = "RELAY 4: SIREN / ALARM (GPIO 15)",
                isActive = sirenActive,
                onToggle = { onToggleRelay(4) },
                tag = "relay4_siren"
            )
        }
    }
}

@Composable
private fun RelayRow(
    relayNumber: Int,
    name: String,
    isActive: Boolean,
    isLocked: Boolean = false,
    lockReason: String? = null,
    onToggle: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) Color(0xFFE8F5E9) else Color(0xFFF9FBF9), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263238)
            )
            if (isLocked && lockReason != null) {
                Text(
                    text = lockReason,
                    fontSize = 10.sp,
                    color = StatusCritical,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = if (isActive) "State: ENERGIZED" else "State: DE-ENERGIZED",
                    fontSize = 10.sp,
                    color = if (isActive) StatusSafe else Color.Gray
                )
            }
        }

        Button(
            onClick = onToggle,
            enabled = !isLocked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) ManualGreen else Color(0xFF78909C)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag(tag)
        ) {
            Icon(Icons.Default.Power, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = if (isActive) "ON" else "OFF",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
