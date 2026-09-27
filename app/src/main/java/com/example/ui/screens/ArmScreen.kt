package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.data.model.RobotStatus
import com.example.ui.components.SafetyBanner
import com.example.ui.components.Servo4Slider
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.ManualGreen
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArmScreen(
    status: RobotStatus,
    autoBlockedReason: String?,
    onClearAutoBlocked: () -> Unit,
    onEmergencyStop: () -> Unit,
    onServo4Changed: (Int) -> Unit,
    onServo4Home: () -> Unit,
    onServo4Center: () -> Unit,
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
        // Safety Banner
        SafetyBanner(
            status = status,
            autoBlockedReason = autoBlockedReason,
            onClearAutoBlocked = onClearAutoBlocked,
            onEmergencyStop = onEmergencyStop
        )

        // 3-DOF Arm Schematic & Visualization Canvas
        Card(
            modifier = Modifier.fillMaxWidth().testTag("arm_visualization_card"),
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
                    Column {
                        Text(
                            text = "BOTTOM-MOUNTED 3-DOF ARM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = "Automatic weed targeting & vertical drill spindle",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = when (status.armState.name) {
                                    "DRILLING" -> StatusCritical
                                    "TARGETING", "MOVING" -> StatusWarning
                                    "SAFE", "HOME" -> StatusSafe
                                    else -> Color(0xFF607D8B)
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status.armState.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // 2D Kinematic Canvas rendering links and drill bit
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFF5F7F6), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val base = Offset(size.width * 0.3f, size.height * 0.8f)
                        val groundY = size.height * 0.85f

                        // Draw Ground line
                        drawLine(
                            color = Color(0xFF8D6E63),
                            start = Offset(0f, groundY),
                            end = Offset(size.width, groundY),
                            strokeWidth = 4f
                        )

                        // Link 1 (Shoulder)
                        val angle1Rad = Math.toRadians((180 - status.servo2Angle).toDouble())
                        val link1Len = 80f
                        val joint1 = Offset(
                            (base.x + link1Len * cos(angle1Rad)).toFloat(),
                            (base.y - link1Len * sin(angle1Rad)).toFloat()
                        )

                        // Link 2 (Elbow)
                        val angle2Rad = Math.toRadians((180 - status.servo2Angle + (status.servo3Angle - 90)).toDouble())
                        val link2Len = 65f
                        val joint2 = Offset(
                            (joint1.x + link2Len * cos(angle2Rad)).toFloat(),
                            (joint1.y - link2Len * sin(angle2Rad)).toFloat()
                        )

                        // Link 3 / Drill tool pointing down
                        val drillLen = 35f
                        val toolEnd = Offset(joint2.x, (joint2.y + drillLen))

                        // Draw Links
                        drawLine(Color(0xFF37474F), base, joint1, strokeWidth = 10f, cap = StrokeCap.Round)
                        drawLine(accentColor, joint1, joint2, strokeWidth = 8f, cap = StrokeCap.Round)
                        drawLine(
                            if (status.drillOn) StatusCritical else Color(0xFFD32F2F),
                            joint2,
                            toolEnd,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        // Draw Joints
                        drawCircle(Color.Black, radius = 9f, center = base)
                        drawCircle(Color(0xFF455A64), radius = 7f, center = joint1)
                        drawCircle(Color(0xFF455A64), radius = 6f, center = joint2)
                        drawCircle(if (status.drillOn) StatusCritical else Color.Black, radius = 4f, center = toolEnd)
                    }
                }
            }
        }

        // Automatic Servo States (1, 2, 3) — No manual sliders as per section 10
        Card(
            modifier = Modifier.fillMaxWidth().testTag("automatic_servos_card"),
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
                        text = "AUTOMATIC SERVOS 1–3 (SAFETY LOCKED)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Manual sliders are strictly forbidden for Servos 1–3 to prevent ground collisions. Position is governed by Autonomous Inverse Kinematics.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServoStatusBadge(
                        name = "SERVO 1",
                        sub = "Base (GPIO 13)",
                        angle = status.servo1Angle,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    ServoStatusBadge(
                        name = "SERVO 2",
                        sub = "Elbow (GPIO 14)",
                        angle = status.servo2Angle,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    ServoStatusBadge(
                        name = "SERVO 3",
                        sub = "Wrist (GPIO 16)",
                        angle = status.servo3Angle,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Coordinates & Drill State
        Card(
            modifier = Modifier.fillMaxWidth().testTag("arm_coordinates_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ARM SPATIAL COORDINATES & DRILL STATUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Target X: ${if (status.targetX != null) String.format("%.1f cm", status.targetX) else "IDLE"}", fontSize = 11.sp)
                        Text("Target Y: ${if (status.targetY != null) String.format("%.1f cm", status.targetY) else "IDLE"}", fontSize = 11.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Arm X: ${if (status.armX != null) String.format("%.1f cm", status.armX) else "0.0 cm"}", fontSize = 11.sp)
                        Text("Arm Y: ${if (status.armY != null) String.format("%.1f cm", status.armY) else "14.0 cm"}", fontSize = 11.sp)
                        Text("Arm Z: ${if (status.armZ != null) String.format("%.1f cm", status.armZ) else "5.0 cm"}", fontSize = 11.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "DRILL RELAY 1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (status.drillOn) StatusCritical else Color.DarkGray
                        )
                        Text(
                            text = if (status.drillOn) "ACTIVE (MAX 7s)" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (status.drillOn) StatusCritical else StatusSafe
                        )
                    }
                }
            }
        }

        // Servo 4: Separately Available in Manual Mode
        if (!isAuto) {
            Servo4Slider(
                currentAngle = status.servo4Angle,
                onAngleChanged = onServo4Changed,
                onHomeClicked = onServo4Home,
                onCenterClicked = onServo4Center
            )
        }
    }
}

@Composable
private fun ServoStatusBadge(
    name: String,
    sub: String,
    angle: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF9FBF9), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE2E7E4), RoundedCornerShape(10.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(sub, fontSize = 9.sp, color = Color.Gray)
            Text("$angle°", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF202020))
        }
    }
}
