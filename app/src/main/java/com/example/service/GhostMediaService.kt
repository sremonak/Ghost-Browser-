package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class GhostMediaService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val mediaTitle = intent?.getStringExtra(EXTRA_MEDIA_TITLE) ?: "Ghost Browser • Media Playing"
        val mediaUrl = intent?.getStringExtra(EXTRA_MEDIA_URL) ?: "Background Audio Active"

        when (action) {
            ACTION_START -> {
                val notification = buildNotification(mediaTitle, mediaUrl, isPlaying = true)
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_PAUSE -> {
                val notification = buildNotification(mediaTitle, mediaUrl, isPlaying = false)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ghost Media & Background Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows media controls and notification when audio plays in Ghost Browser"
                setShowBadge(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, subtitle: String, isPlaying: Boolean): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, GhostMediaService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Playing in Background" else "Paused",
                pendingOpen
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                pendingStop
            )
            .build()
    }

    companion object {
        const val CHANNEL_ID = "ghost_media_channel_v1"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "com.example.ghostbrowser.ACTION_MEDIA_START"
        const val ACTION_PAUSE = "com.example.ghostbrowser.ACTION_MEDIA_PAUSE"
        const val ACTION_STOP = "com.example.ghostbrowser.ACTION_MEDIA_STOP"
        const val EXTRA_MEDIA_TITLE = "extra_media_title"
        const val EXTRA_MEDIA_URL = "extra_media_url"

        fun startService(context: Context, title: String, url: String) {
            val intent = Intent(context, GhostMediaService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MEDIA_TITLE, title)
                putExtra(EXTRA_MEDIA_URL, url)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, GhostMediaService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
