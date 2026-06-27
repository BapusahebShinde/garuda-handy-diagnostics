package com.itek.rftaar
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.permissions.PermissionUtils
import com.itek.rftaar.sensors.HardwareChecker
import com.itek.rftaar.utils.CommonUtils.isNonEmpty
import com.itek.rftaar.utils.NetworkUtils
import com.itek.rftaar.utils.UUIDV5

open class CommonActivity : ComponentActivity() {

   val tag = "CommonActivity"//this.localClassName
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    showLog(tag,"onCreate")
    PermissionUtils.init(this)

    saveDeviceId()
    setLocationCallback()
    initMQTT()
    checkHardware()
  }

  fun initMQTT(){
   MqttManager.initialize(context = this, serverUrl = DataStoreManager.readFromPreferences(ParameterConstants.BROKER_URL,"").trim())
  }

  private fun checkHardware(){

    if (!DataStoreManager.readFromPreferences("IS_SENSOR_CHECKED",false)) {
      try {
        HardwareChecker.checkHardware((getSystemService(SENSOR_SERVICE) as SensorManager))
        DataStoreManager.saveToPreferences("IS_SENSOR_CHECKED",true)
      }
      catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
  }

  private fun setLocationCallback(){
    if(PermissionUtils.checkPermissions(2)) {
      NetworkUtils.getCurrentLatLng(this){latLng->
        DataStoreManager.saveToPreferences(ParameterConstants.LAT_LNG,latLng);
      }
    }
  }

  private fun saveDeviceId() {
    if (isNonEmpty(DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))) return
    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.Q || PermissionUtils.checkPermissions(1)) {
      var serial = if(Build.VERSION.SDK_INT>Build.VERSION_CODES.Q) Settings.Global.getString(contentResolver, "Serial") else "";//chkNull(Build.SERIAL,"")
      showLog("serial", "--" + serial)
      if(serial.isNullOrEmpty() || serial.equals("unknown",true)) {
        try {
          serial = Build.SERIAL
          showLog("serial1", "--" + serial)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      if (serial.isNullOrEmpty() || serial.equals("unknown", true)) {
        try {
          val props = listOf(
            "ro.serialno",
            "ro.boot.serialno",
            "persist.sys.serialno",
            "ro.ril.oem.sno"
          )
          for (prop in props) {
            val process = Runtime.getRuntime().exec("getprop $prop")
            val value = process.inputStream.bufferedReader().readLine() ?: ""
            showLog("serial3", value)
            if (!value.isNullOrEmpty() && !value.equals("unknown", true)) {
              serial = value
              break
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      if(serial.isNullOrEmpty() || serial.equals("unknown",true)) {
        try {
          serial = NetworkUtils.getDeviceMacAddress(this)
          showLog("serial3", "--" + serial)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      if (isNonEmpty(serial)) {
        DataStoreManager.saveToPreferences(ParameterConstants.SERIAL,serial)
        val deviceId = UUIDV5.generateDeviceId(serial).toString()
        showLog("deviceId", "" + deviceId)
        //Commented for now
        //String imei = Settings.Global.getString(context.getContentResolver(), "Imei1");
        //String imei2 = Settings.Global.getString(context.getContentResolver(), "Imei2");
        if (isNonEmpty(deviceId)) DataStoreManager.saveToPreferences(ParameterConstants.DEVICE_ID, deviceId)
      }
    }
  }

  /**
   * Show short toast.
   *
   * @param res the res
   */
  fun showShortToast(res: Int) {
    ToastUtils.showShortToast(res)
  }

  /**
   * Show long toast.
   *
   * @param res the res
   */
  fun showLongToast(res: Int) {
    ToastUtils.showLongToast(res)
  }

  /**
   * Show short toast.
   *
   * @param msg the msg
   */
  fun showShortToast(msg: String?) {
    ToastUtils.showShortToast(msg)
  }

  /**
   * Show long toast.
   *
   * @param msg the msg
   */
  fun showLongToast(msg: String?) {
    ToastUtils.showLongToast(msg)
  }

  override fun onRestart() {
    showLog(tag,"onRestart")
    super.onRestart()
  }

  override fun onStart() {
    showLog(tag,"onStart")
    super.onStart()
  }

  override fun onPause() {
    showLog(tag,"onPause")
    super.onPause()
  }

  override fun onStop() {
    showLog(tag,"onStop")
    super.onStop()
  }

  override fun onDestroy() {
    showLog(tag,"onDestroy")
    MqttManager.disconnect()
    super.onDestroy()
  }

  public fun showLog(tag:String,message:String){
    LogUtils.showLog(tag,message)
  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    DataStoreManager.setIsChkRationale(true)
    var isGranted = true
    for (permission in permissions) if (!PermissionUtils.isGranted(permission)) {
      isGranted = false
      break
    }
    if (!isGranted) PermissionUtils.checkPermissions(requestCode)
    else if (isGranted) {
      if (requestCode == 1) saveDeviceId()
      if (requestCode == 2) setLocationCallback()
    }
  }


}