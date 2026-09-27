package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
fun PanTiltSliders(
    panAngle: Int,
    tiltAngle: Int,
    onPanChanged: (Int) -> Unit,
    onTiltChanged: (Int) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAMERA GIMBAL (SERVO 5 & 6)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ManualGreen
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onHomeClicked,
                        modifier = Modifier.height(30.dp).testTag("pan_tilt_home_button")
                    ) {
                        Text("HOME", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = onCenterClicked,
                        modifier = Modifier.height(30.dp).testTag("pan_tilt_center_button")
                    ) {
                        Text("CENTER", fontSize = 10.sp)
                    }
                }
            }

            // PAN Slider
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
                        text = "PAN (SERVO 5)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = "$panAngle°",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ManualGreen
                    )
                }
                Slider(
                    value = panAngle.toFloat(),
                    onValueChange = { onPanChanged(it.toInt()) },
                    valueRange = 0f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = ManualGreen,
                        activeTrackColor = ManualGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("camera_pan_slider")
                )
            }

            // TILT Slider
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
                        text = "TILT (SERVO 6)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = "$tiltAngle°",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ManualGreen
                    )
                }
                Slider(
                    value = tiltAngle.toFloat(),
                    onValueChange = { onTiltChanged(it.toInt()) },
                    valueRange = 20f..160f,
                    colors = SliderDefaults.colors(
                        thumbColor = ManualGreen,
                        activeTrackColor = ManualGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("camera_tilt_slider")
                )
            }
        }
    }
}
