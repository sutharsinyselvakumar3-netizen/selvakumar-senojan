package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.RobotMode
import com.example.ui.theme.AutoNavy
import com.example.ui.theme.ManualGreen

@Composable
fun ModeSwitchDialog(
    targetMode: RobotMode?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (targetMode == null) return

    val isAuto = targetMode == RobotMode.AUTO
    val title = if (isAuto) "Switch to AUTO mode?" else "Switch to MANUAL mode?"
    val description = if (isAuto) {
        "The robot may move automatically and perform weed-removal operations.\n\nPre-flight safety verification will inspect ESP32 link, camera stream, battery voltage, MPU stability, and drill state before engaging.\n\nContinue?"
    } else {
        "Automatic movement and weed-removal operations will stop.\n\nChassis and arm will halt safely under Main ESP32 supervision.\n\nContinue?"
    }

    val actionColor = if (isAuto) AutoNavy else ManualGreen

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                modifier = Modifier.testTag("confirm_mode_switch_button")
            ) {
                Text(text = "CONFIRM SWITCH")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_mode_switch_button")
            ) {
                Text(text = "CANCEL")
            }
        }
    )
}
