package com.example.shiftsync

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

class ClockForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (canPostNotifications()) {
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(currentStartMillis)
                )
            }
            handler.postDelayed(this, UPDATE_INTERVAL_MS)
        }
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService<NotificationManager>() as NotificationManager
    }

    private val prefs by lazy {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    private var currentStartMillis: Long = NO_ACTIVE_SHIFT

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startClock()
            ACTION_STOP -> stopClock()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    private fun startClock() {
        createNotificationChannelIfNeeded()

        val persistedStart = prefs.getLong(KEY_ACTIVE_START_MILLIS, NO_ACTIVE_SHIFT)
        currentStartMillis = if (persistedStart > 0L) persistedStart else System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, createNotification(currentStartMillis))
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS)
    }

    private fun stopClock() {
        handler.removeCallbacks(updateRunnable)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotification(startMillis: Long): Notification {
        val elapsedMinutes = ((System.currentTimeMillis() - startMillis) / 60_000L).coerceAtLeast(0L)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("ShiftSync Live Clock")
            .setContentText("Elapsed: ${formatDuration(elapsedMinutes)}")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Live Clock",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active shift tracking in the notification area"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.example.shiftsync.action.START_CLOCK"
        const val ACTION_STOP = "com.example.shiftsync.action.STOP_CLOCK"

        private const val CHANNEL_ID = "shift_sync_live_clock"
        private const val NOTIFICATION_ID = 1107
        private const val UPDATE_INTERVAL_MS = 60_000L
    }
}
