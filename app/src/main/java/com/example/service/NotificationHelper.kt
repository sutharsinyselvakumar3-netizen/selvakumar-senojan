package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.AlertItem
import com.example.data.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationHelper(
    private val context: Context,
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val channelId = "ai_companion_robot_alerts"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Garden Robot Safety & Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts for battery, sensors, MPU, and weed operations"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerAlert(
        level: String, // "INFO", "WARNING", "CRITICAL"
        title: String,
        message: String,
        settings: AppSettings
    ) {
        scope.launch(Dispatchers.IO) {
            // 1. Record to database
            try {
                database.alertDao().insertAlert(
                    AlertItem(
                        level = level,
                        title = title,
                        message = message
                    )
                )
            } catch (_: Exception) {}

            // 2. Phone Vibration
            if (settings.phoneVibrationEnabled) {
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = if (level == "CRITICAL") {
                            longArrayOf(0, 400, 200, 400, 200, 600)
                        } else {
                            longArrayOf(0, 250, 150, 250)
                        }
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(400)
                    }
                } catch (_: Exception) {}
            }

            // 3. Sound
            if (settings.soundEnabled) {
                try {
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, soundUri)
                    ringtone?.play()
                } catch (_: Exception) {}
            }

            // 4. Android System Notification
            if (settings.notificationsEnabled) {
                try {
                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("[$level] $title")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)

                    notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
                } catch (_: Exception) {}
            }
        }
    }
}
