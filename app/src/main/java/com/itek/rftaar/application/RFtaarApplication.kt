package com.itek.rftaar.application

import android.app.Application
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.BaseUtils
import com.itek.rftaar.core.common.utils.CrashUtils
import com.itek.rftaar.core.common.utils.FileUtils
import com.itek.rftaar.core.common.utils.JsonUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.mqtt.NotificationHelper
import com.itek.rftaar.network.NetworkMonitor
import com.itek.rftaar.receiver.AppBroadcastReceiver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@HiltAndroidApp
class RFtaarApplication : Application() {
  private val appBroadcastReceiver: AppBroadcastReceiver = AppBroadcastReceiver()
  private val networkMonitor: NetworkMonitor = NetworkMonitor()

  override fun onCreate() {
    super.onCreate()
    val start = System.currentTimeMillis()
    DataStoreManager.init(this)
    CoroutineScope(Dispatchers.IO).launch {
      ToastUtils.init(this@RFtaarApplication)
      BaseUtils.init(this@RFtaarApplication)
      JsonUtils.init(this@RFtaarApplication)
      LogUtils.init(this@RFtaarApplication)
      FileUtils.init(this@RFtaarApplication)
      CrashUtils.init(this@RFtaarApplication)
      networkMonitor.startMonitoring(this@RFtaarApplication)
      appBroadcastReceiver.register(this@RFtaarApplication)
      val brokerUrl = DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL, "")
      MqttManager.initialize(this@RFtaarApplication, brokerUrl)
      NotificationHelper.createNotificationChannel(this@RFtaarApplication)
    }
    LogUtils.showLog("APP_START", "GarudaVigil started in ${System.currentTimeMillis() - start} ms")

  }

  override fun onTerminate() {
    appBroadcastReceiver.unRegister()
    networkMonitor.stopMonitoring()
    super.onTerminate()
  }
}