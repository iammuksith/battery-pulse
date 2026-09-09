package com.iammuksith.batterypulse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.iammuksith.batterypulse.util.NotificationHelper

class BatteryAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val helper = NotificationHelper(context)

        if (action == Intent.ACTION_BATTERY_LOW) {
            // Read sticky intent to get exact level
            val batteryStatus: Intent? = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val rawLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 15) ?: 15
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val level = if (scale > 0) (rawLevel * 100) / scale else rawLevel

            helper.showLowBatteryAlert(level = level, threshold = 20)
        }
    }
}
