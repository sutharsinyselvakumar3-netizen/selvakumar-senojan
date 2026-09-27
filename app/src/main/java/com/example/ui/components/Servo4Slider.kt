package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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

@Composable
fun Servo4Slider(
    currentAngle: Int,
    onAngleChanged: (Int) -> Unit,
    onHomeClicked: () -> Unit,
    onCenterClicked: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SERVO 4 (MANUAL AUXILIARY)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ManualGreen
                    )
                    Text(
                        text = "Allowed range strictly enforced: 0°–45°",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onHomeClicked,
                        modifier = Modifier.height(30.dp).testTag("servo4_home_button")
                    ) {
                        Text("HOME (0°)", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = onCenterClicked,
                        modifier = Modifier.height(30.dp).testTag("servo4_center_button")
                    ) {
                        Text("CENTER (22°)", fontSize = 10.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FBF9), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0° ─────────────── 45°",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Current: $currentAngle°",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ManualGreen
                    )
                }

                Slider(
                    value = currentAngle.coerceIn(0, 45).toFloat(),
                    onValueChange = { onAngleChanged(it.toInt().coerceIn(0, 45)) },
                    valueRange = 0f..45f,
                    colors = SliderDefaults.colors(
                        thumbColor = ManualGreen,
                        activeTrackColor = ManualGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("servo4_slider")
                )
            }
        }
    }
}
