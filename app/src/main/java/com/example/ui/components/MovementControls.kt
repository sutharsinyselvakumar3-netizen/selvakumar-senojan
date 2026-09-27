package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ManualGreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MovementControls(
    selectedSpeed: String,
    onSpeedSelected: (String) -> Unit,
    onStartMovement: (String) -> Unit,
    onStopMovement: () -> Unit,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Speed Selection Tap Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DRIVE SPEED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ManualGreen
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SLOW", "MEDIUM", "FAST").forEach { speed ->
                        val isSelected = selectedSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ManualGreen else Color(0xFFF0F4F1))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ManualGreen else Color(0xFFCFD8DC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSpeedSelected(speed) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("speed_${speed.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = speed,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF37474F)
                            )
                        }
                    }
                }
            }

            // Exactly 4 Movement Buttons (No STOP button; releases send STOP)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FORWARD (▲)
                DirectionButton(
                    command = "FORWARD",
                    label = "FORWARD",
                    icon = Icons.Default.ArrowUpward,
                    onPress = { onStartMovement("FORWARD") },
                    onRelease = onStopMovement,
                    tag = "btn_forward"
                )

                // LEFT (◀) & RIGHT (▶)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DirectionButton(
                        command = "LEFT",
                        label = "LEFT",
                        icon = Icons.Default.ArrowBack,
                        onPress = { onStartMovement("LEFT") },
                        onRelease = onStopMovement,
                        tag = "btn_left"
                    )

                    // Center Neutral Info
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HOLD\nDRIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ManualGreen
                        )
                    }

                    DirectionButton(
                        command = "RIGHT",
                        label = "RIGHT",
                        icon = Icons.Default.ArrowForward,
                        onPress = { onStartMovement("RIGHT") },
                        onRelease = onStopMovement,
                        tag = "btn_right"
                    )
                }

                // REVERSE (▼)
                DirectionButton(
                    command = "REVERSE",
                    label = "REVERSE",
                    icon = Icons.Default.ArrowDownward,
                    onPress = { onStartMovement("REVERSE") },
                    onRelease = onStopMovement,
                    tag = "btn_reverse"
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DirectionButton(
    command: String,
    label: String,
    icon: ImageVector,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    tag: String
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed) ManualGreen else Color(0xFFF1F8E9),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isPressed) ManualGreen else Color(0xFFC8E6C9)
        ),
        modifier = Modifier
            .size(width = 90.dp, height = 56.dp)
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPress()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onRelease()
                        true
                    }
                    else -> false
                }
            }
            .testTag(tag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) Color.White else ManualGreen,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) Color.White else ManualGreen
            )
        }
    }
}
