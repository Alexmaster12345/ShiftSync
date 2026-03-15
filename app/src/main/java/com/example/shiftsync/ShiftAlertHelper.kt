package com.example.shiftsync

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

object ShiftAlertHelper {

    private const val CHANNEL_ID = "shift_alerts"
    private const val CHANNEL_NAME = "Shift Alerts"
    private const val NOTIFY_ID_START = 2001
    private const val NOTIFY_ID_END = 2002

    fun fireShiftStartAlert(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_NOTIFY_SHIFT_START, true)) return
        sendAlert(
            context = context,
            notificationId = NOTIFY_ID_START,
            title = "Shift Started ⏱",
            body = "You have clocked in. Your shift is now being tracked.",
            useSoundPref = prefs.getBoolean(KEY_NOTIFY_SOUND, true),
            useVibratePref = prefs.getBoolean(KEY_NOTIFY_VIBRATE, true)
        )
    }

    fun fireShiftEndAlert(context: Context, durationMinutes: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_NOTIFY_SHIFT_END, true)) return
        sendAlert(
            context = context,
            notificationId = NOTIFY_ID_END,
            title = "Shift Ended 🏁",
            body = "You have clocked out. Total time: ${formatDuration(durationMinutes)}.",
            useSoundPref = prefs.getBoolean(KEY_NOTIFY_SOUND, true),
            useVibratePref = prefs.getBoolean(KEY_NOTIFY_VIBRATE, true)
        )
    }

    private fun sendAlert(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        useSoundPref: Boolean,
        useVibratePref: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val nm = context.getSystemService<NotificationManager>() ?: return
        createChannelIfNeeded(nm)

        val contentIntent = PendingIntent.getActivity(
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
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!useSoundPref) builder.setSilent(true)
        if (useVibratePref) builder.setVibrate(longArrayOf(0, 300, 150, 300))

        nm.notify(notificationId, builder.build())
    }

    private fun createChannelIfNeeded(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you clock in or clock out of a shift"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
        }
        nm.createNotificationChannel(channel)
    }
}

