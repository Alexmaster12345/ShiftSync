package com.example.shiftsync

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import java.util.Calendar

class ShiftReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if today is an enabled day for this reminder
        val daysKey = if (type == TYPE_CLOCK_IN) KEY_REMINDER_CLOCK_IN_DAYS else KEY_REMINDER_CLOCK_OUT_DAYS
        val enabledDays = prefs.getStringSet(daysKey, DEFAULT_REMINDER_DAYS) ?: DEFAULT_REMINDER_DAYS
        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString()

        // Re-schedule for the next enabled day regardless
        val hourKey = if (type == TYPE_CLOCK_IN) KEY_REMINDER_CLOCK_IN_HOUR else KEY_REMINDER_CLOCK_OUT_HOUR
        val minKey = if (type == TYPE_CLOCK_IN) KEY_REMINDER_CLOCK_IN_MINUTE else KEY_REMINDER_CLOCK_OUT_MINUTE
        val h = prefs.getInt(hourKey, if (type == TYPE_CLOCK_IN) 9 else 17)
        val m = prefs.getInt(minKey, 0)
        scheduleNextEnabledDay(context, type, h, m, enabledDays)

        // Only fire notification if today is an enabled day
        if (todayDow !in enabledDays) return

        val title: String
        val body: String
        val notifId: Int

        when (type) {
            TYPE_CLOCK_IN -> {
                title = "Time to Clock In! ⏰"
                body = "Your shift is about to start. Open ShiftSync to clock in."
                notifId = NOTIF_ID_REMINDER_IN
            }
            TYPE_CLOCK_OUT -> {
                title = "Time to Clock Out! 🏁"
                body = "Your shift is ending. Open ShiftSync to clock out."
                notifId = NOTIF_ID_REMINDER_OUT
            }
            else -> return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val nm = context.getSystemService<NotificationManager>() ?: return
        ensureChannel(nm)

        val useSound = prefs.getBoolean(KEY_NOTIFY_SOUND, true)
        val useVibrate = prefs.getBoolean(KEY_NOTIFY_VIBRATE, true)

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!useSound) builder.setSilent(true)
        if (useVibrate) builder.setVibrate(longArrayOf(0, 400, 200, 400))

        nm.notify(notifId, builder.build())
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val ch = NotificationChannel(
            CHANNEL_ID, "Shift Reminders", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to clock in or clock out"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
        }
        nm.createNotificationChannel(ch)
    }

    companion object {
        const val EXTRA_TYPE = "reminder_type"
        const val TYPE_CLOCK_IN = "clock_in"
        const val TYPE_CLOCK_OUT = "clock_out"

        private const val CHANNEL_ID = "shift_reminders"
        private const val NOTIF_ID_REMINDER_IN = 3001
        private const val NOTIF_ID_REMINDER_OUT = 3002
        private const val REQUEST_CLOCK_IN = 3001
        private const val REQUEST_CLOCK_OUT = 3002

        /**
         * Schedule an alarm at [hour]:[minute] for the next occurrence of an enabled day.
         * [enabledDays] contains Calendar.DAY_OF_WEEK values as strings (1=Sun…7=Sat).
         */
        fun schedule(context: Context, type: String, hour: Int, minute: Int, enabledDays: Set<String> = DEFAULT_REMINDER_DAYS) {
            if (enabledDays.isEmpty()) return
            scheduleNextEnabledDay(context, type, hour, minute, enabledDays)
        }

        /**
         * Find the next calendar day that is in [enabledDays] and schedule the alarm.
         */
        fun scheduleNextEnabledDay(context: Context, type: String, hour: Int, minute: Int, enabledDays: Set<String>) {
            if (enabledDays.isEmpty()) return

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
                putExtra(EXTRA_TYPE, type)
            }
            val requestCode = if (type == TYPE_CLOCK_IN) REQUEST_CLOCK_IN else REQUEST_CLOCK_OUT
            val pi = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If the time already passed today, start searching from tomorrow
                if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
            }

            // Search up to 7 days ahead for the next enabled day
            for (i in 0..6) {
                val dow = target.get(Calendar.DAY_OF_WEEK).toString()
                if (dow in enabledDays) break
                target.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, target.timeInMillis, pi)
            }
        }

        /**
         * Cancel a scheduled reminder for the given [type].
         */
        fun cancel(context: Context, type: String) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
                putExtra(EXTRA_TYPE, type)
            }
            val requestCode = if (type == TYPE_CLOCK_IN) REQUEST_CLOCK_IN else REQUEST_CLOCK_OUT
            val pi = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }
}



