package com.iammuksith.batterypulse.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.iammuksith.batterypulse.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_CRITICAL = "battery_critical_alerts"
        const val CHANNEL_GENERAL = "battery_general_alerts"
        const val NOTIF_ID_LOW_BATTERY = 2001
        const val NOTIF_ID_TEMP_ALERT = 2002
        const val NOTIF_ID_CHARGE_LIMIT = 2003
        const val NOTIF_ID_TEST = 2004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Critical alerts channel (Low battery and Overheating)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "Battery Safety & Warning Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications when battery level drops below 20% or temperature exceeds safe limits."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setShowBadge(true)
            }

            // General alerts channel (80% charge reminders)
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Battery Pulse Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Helpful reminders such as 80% charge limit notifications."
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showLowBatteryAlert(level: Int, threshold: Int = 20) {
        if (!hasNotificationPermission()) return

        // Tap action: open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            101,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action button: Open Android Battery Saver Settings
        val saverIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val saverPendingIntent = PendingIntent.getActivity(
            context,
            102,
            saverIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("⚠️ Low Battery Alert ($level%)")
            .setContentText("Battery dropped below $threshold%. Plug in your device or enable Battery Saver now.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Battery level is now at $level% (below the $threshold% safety threshold). Connect your charger or activate Battery Saver to prevent shutdown."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_preferences, "Turn On Saver", saverPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_LOW_BATTERY, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun showHighTemperatureAlert(tempCelsius: Float, threshold: Float = 40.0f) {
        if (!hasNotificationPermission()) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            201,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(String.format("🔥 Battery Overheat Warning (%.1f°C)", tempCelsius))
            .setContentText(String.format("Battery temperature exceeds safe limit of %.1f°C! Let device cool down.", threshold))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                String.format(
                    "Battery temperature is currently %.1f°C, exceeding your safe threshold of %.1f°C. High heat accelerates cell degradation. Please unplug rapid charger and close heavy apps.",
                    tempCelsius,
                    threshold
                )
            ))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_TEMP_ALERT, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun showChargeLimitAlert(level: Int) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            301,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("🔋 80% Target Charge Reached")
            .setContentText("Battery is at $level%. Unplug charger now to prolong battery lifespan.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_CHARGE_LIMIT, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun showTestAlert(isTempTest: Boolean) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            401,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isTempTest) "🔥 [TEST] High Temperature Warning (41.5°C)" else "⚠️ [TEST] Low Battery Alert (18%)"
        val body = if (isTempTest) {
            "Simulated alert: Safe temperature limit exceeded! Battery Pulse alert system is active and functioning."
        } else {
            "Simulated alert: Battery level dropped below 20%! Battery Pulse alert system is active and functioning."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CRITICAL)
            .setSmallIcon(if (isTempTest) android.R.drawable.stat_notify_error else android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }
}
