package com.stephanmeijer.fileconverter.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ConversionForegroundService : Service() {

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CANCEL_CONVERSION) {
                onCancelRequested?.invoke()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ContextCompat.registerReceiver(
            this,
            cancelReceiver,
            IntentFilter(ACTION_CANCEL_CONVERSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        startForeground(NOTIFICATION_ID, buildNotification(this, -1f))
    }

    override fun onDestroy() {
        unregisterReceiver(cancelReceiver)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "File Conversion",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "conversion_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_CANCEL_CONVERSION = "com.stephanmeijer.fileconverter.CANCEL_CONVERSION"

        var onCancelRequested: (() -> Unit)? = null

        fun start(context: Context) {
            val intent = Intent(context, ConversionForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ConversionForegroundService::class.java))
        }

        fun update(context: Context, progress: Float) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, buildNotification(context, progress))
        }

        private fun buildNotification(context: Context, progress: Float): Notification {
            val cancelIntent = Intent(ACTION_CANCEL_CONVERSION).apply {
                setPackage(context.packageName)
            }
            val cancelPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Converting file...")
                .setContentText("Please wait while the document is being converted")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "Cancel", cancelPendingIntent)

            if (progress < 0f) {
                builder.setProgress(0, 0, true)
            } else {
                builder.setProgress(100, (progress * 100).toInt(), false)
            }

            return builder.build()
        }
    }
}
