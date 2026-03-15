package com.example.shiftsync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedules shift reminders after device reboot so they keep firing daily.
 */
class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (prefs.getBoolean(KEY_REMINDER_CLOCK_IN_ENABLED, false)) {
            val h = prefs.getInt(KEY_REMINDER_CLOCK_IN_HOUR, 9)
            val m = prefs.getInt(KEY_REMINDER_CLOCK_IN_MINUTE, 0)
            val days = prefs.getStringSet(KEY_REMINDER_CLOCK_IN_DAYS, DEFAULT_REMINDER_DAYS) ?: DEFAULT_REMINDER_DAYS
            ShiftReminderReceiver.schedule(context, ShiftReminderReceiver.TYPE_CLOCK_IN, h, m, days)
        }

        if (prefs.getBoolean(KEY_REMINDER_CLOCK_OUT_ENABLED, false)) {
            val h = prefs.getInt(KEY_REMINDER_CLOCK_OUT_HOUR, 17)
            val m = prefs.getInt(KEY_REMINDER_CLOCK_OUT_MINUTE, 0)
            val days = prefs.getStringSet(KEY_REMINDER_CLOCK_OUT_DAYS, DEFAULT_REMINDER_DAYS) ?: DEFAULT_REMINDER_DAYS
            ShiftReminderReceiver.schedule(context, ShiftReminderReceiver.TYPE_CLOCK_OUT, h, m, days)
        }
    }
}


