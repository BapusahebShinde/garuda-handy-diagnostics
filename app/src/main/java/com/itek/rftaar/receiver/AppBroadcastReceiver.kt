package com.itek.rftaar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.isNonEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AppBroadcastReceiver(): BroadcastReceiver() {
  var context: Context? = null

  override fun onReceive(context: Context, intent: Intent?) {
    this.context=context
    val intentAction = if (intent != null) chkNull(intent.action, "") else ""
    if (intent != null && isNonEmpty(intentAction)) {
      LogUtils.showLog("intentAction",intentAction)
      when(intentAction){
        /*Intent.ACTION_TIME_TICK -> {
          //LogUtils.showLog("TIME_TICK", Calendar.getInstance().time.toString())
          MqttManager.removeUploadedAndDeleted();
          if(DataStoreManager.readFromPreferences(IS_LOGGED_IN,false))
            MqttManager.publishPending()
        }*/
        Intent.ACTION_TIME_TICK -> {
          LogUtils.showLog("TIME_TICK", Calendar.getInstance().time.toString())
          val brokerUrl = DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL, "").trim()
          LogUtils.showLog("MQTT_brokerUrl",brokerUrl)
          //MqttManager.removeUploadedAndDeleted()
          MqttManager.publishPending(context,brokerUrl)
        }

        Intent.ACTION_BATTERY_CHANGED -> {
          val batteryPercentage = intent.getIntExtra("level", 0)
          //LogUtils.showLog("ACTION_BATTERY_CHANGED"+"_BatteryPercentage", ""+batteryPercentage)
        }
        Intent.ACTION_BATTERY_LOW -> {
          val batteryPercentage = intent.getIntExtra("level", 0)
          LogUtils.showLog("ACTION_BATTERY_LOW"+"_BatteryPercentage", ""+batteryPercentage)
          ToastUtils.showShortToast(String.format(context.getString(R.string.err_device_battery_low),""+batteryPercentage))
        }
      }
    }
  }

  fun register(context: Context){
    this.context=context
    val broadcastReceiver = this;
    CoroutineScope(Dispatchers.IO).launch {
      val intentFilter = IntentFilter()
      intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
      intentFilter.addAction(Intent.ACTION_BATTERY_LOW)
      intentFilter.addAction(Intent.ACTION_TIME_TICK)
      context.registerReceiver(broadcastReceiver, intentFilter)
    }
  }

  fun unRegister(){
    val broadcastReceiver = this;
    CoroutineScope(Dispatchers.IO).launch {
      context?.unregisterReceiver(broadcastReceiver)
    }
  }
}