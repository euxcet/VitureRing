package com.euxcet.viturering

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hcifuture.producer.detector.GestureDetector
import com.hcifuture.producer.detector.OrientationDetector
import com.hcifuture.producer.sensor.NuixSensorManager
import com.hcifuture.producer.sensor.NuixSensorState
import com.hcifuture.producer.sensor.data.RingTouchData
import com.hcifuture.producer.sensor.data.RingTouchEvent
import com.hcifuture.producer.sensor.external.ring.RingSpec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RingService : Service() {

    companion object {
        const val CHANNEL_ID = "RingService"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "前台服务通知",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("前台服务正在运行")
            .setContentText("这是前台服务的通知内容")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        connect()

        return START_NOT_STICKY
    }

    private fun connect() {
    }

    override fun onBind(intent: Intent): IBinder {
        return object: IRingService.Stub() {
            override fun toSettings() {
                val jumpIntent = Intent(Settings.ACTION_SETTINGS)
                jumpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(jumpIntent)
            }
        }
    }
}