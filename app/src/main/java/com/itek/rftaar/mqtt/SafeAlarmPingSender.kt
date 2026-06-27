package com.itek.rftaar.mqtt

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.itek.rftaar.core.common.utils.LogUtils
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttPingSender
import org.eclipse.paho.client.mqttv3.MqttToken
import org.eclipse.paho.client.mqttv3.internal.ClientComms
import java.util.concurrent.atomic.AtomicBoolean

class SafeAlarmPingSender(context: Context) : MqttPingSender {

    private val appContext = context.applicationContext

    private var comms: ClientComms? = null
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    private val started = AtomicBoolean(false)

    override fun init(comms: ClientComms) {
        this.comms = comms
        alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun start() {
        if (!started.compareAndSet(false, true)) return

        val action = PING_ACTION + comms!!.client.clientId
        val intent = Intent(action).apply {
            setPackage(appContext.packageName)
        }

        pendingIntent = PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        appContext.registerReceiver(alarmReceiver, IntentFilter(action))
        schedule(comms!!.keepAlive)
    }

    override fun stop() {
        if (!started.compareAndSet(true, false)) return

        try {
            pendingIntent?.let {
                alarmManager?.cancel(it)
            }
        } catch (e: Exception) {
            LogUtils.showLog("SafeAlarmPingSender", "Alarm cancel failed:"+ e.toString())
        }

        try {
            appContext.unregisterReceiver(alarmReceiver)
        } catch (e: IllegalArgumentException) {
            // already unregistered – safe
        } finally {
            pendingIntent = null
        }
    }

    override fun schedule(delayInMilliseconds: Long) {
        if (!started.get() || pendingIntent == null) return

        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delayInMilliseconds,
            pendingIntent!!
        )
    }

    fun isStarted(): Boolean = started.get()

    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!started.get() || comms == null) return

            try {
                comms!!.checkForActivity(object : MqttToken("Ping"), IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        // no-op
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        LogUtils.showLog("SafeAlarmPingSender", "Ping failed:"+exception.toString())
                    }
                })
            } catch (e: Exception) {
                LogUtils.showLog("SafeAlarmPingSender", "Ping exception:"+e.toString())
            }
        }
    }

    companion object {
        private const val PING_ACTION = "com.itek.rftaar.mqtt.PING."
    }
}



