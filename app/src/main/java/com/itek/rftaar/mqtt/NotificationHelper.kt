package com.itek.rftaar.mqtt

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.itek.rftaar.MainActivity
import com.itek.rftaar.R
import com.itek.rftaar.presentation.navigation.Screen
import org.json.JSONObject

object NotificationHelper {

    private const val CHANNEL_ID = "mqtt_channel"
    private const val CHANNEL_NAME = "MQTT Notifications"

    /**
     * Call this once (App start)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from MQTT"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification from JSON
     */
    fun showNotification(context: Context, json: JSONObject) {

        val title = json.optString("title", "New Notification")
        val message = json.optString("message", "You have a new update")

        showNotification(context, title, message)
    }

    /**
     * Show notification (manual)
     */
    fun showNotification(context: Context, title: String, message: String) {

        //Permission check (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       /* val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            //This is the ONLY thing needed for navigation
            //putExtra("route", "notificationScreen")
            putExtra("targetRoute", Screen.NotificationScreen.route)
        }*/

        val intent = Intent(context, MainActivity::class.java).apply {
            //flags = Intent.FLAG_ACTIVITY_NEW_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("targetRoute", Screen.NotificationScreen.route)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,//1001,//System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.rftaar) //make sure icon exists
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }

    /**
     * Optional: Check if app is in background
     */
    fun isAppInBackground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return true

        val packageName = context.packageName

        for (process in runningProcesses) {
            if (process.processName == packageName) {
                return process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return true
    }
}