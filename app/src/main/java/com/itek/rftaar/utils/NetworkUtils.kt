package com.itek.rftaar.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Looper
import android.provider.Settings
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.itek.rftaar.R
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import java.io.IOException
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {
  @SuppressLint("MissingPermission")
  fun isInternetConnected(
    context: Context?,
    isShowErrDialog: Boolean = false,
    isShowErrToast: Boolean = false
  ): Boolean {
    var isNetConnected = false

    if (context != null && (context !is Activity || !context.isFinishing)) {
      val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      val isAirplaneMode = isAirplaneModeOn(context)

      if (connectivityManager != null && !isAirplaneMode) {
        val activeNetwork = connectivityManager.activeNetworkInfo
        if (activeNetwork != null) {
          isNetConnected = activeNetwork.isConnected
        } else {
          connectivityManager.allNetworkInfo?.forEach { networkInfo ->
            if (networkInfo.isConnected) {
              isNetConnected = true
              return@forEach
            }
          }
        }
      }

      /*    if (!isNetConnected && isShowErrToast) {
  Toast.makeText(
    context,
    context.getString(
      if (isAirplaneMode) R.string.err_airplane_mode else R.string.err_internet_no_connect
    ),
    Toast.LENGTH_SHORT
  ).show()
          }*/
    }

    if (!isNetConnected) {
      if(isShowErrToast) ToastUtils.showLongToast(R.string.err_internet_no_connect)
    }

    return isNetConnected
  }

  private fun isAirplaneModeOn(context: Context): Boolean {
    return Settings.Global.getInt(
      context.contentResolver,
      Settings.Global.AIRPLANE_MODE_ON,
      0
    ) != 0
  }


  fun getIpAddress(context: Context): String? {
    if (isInternetConnected(context, false, false)) {
      try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
          val addrs = intf.inetAddresses
          for (addr in addrs) {
            if (!addr.isLoopbackAddress && addr is Inet4Address) {
              // log IP address (replace showLog with your own logging util)
              LogUtils.showLog("local ip", addr.hostAddress ?: "")
              return addr.hostAddress
            }
          }
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
      }
    }
    return null
  }

  @SuppressLint("MissingPermission")
  fun getCurrentLatLng(
    context: Context,
    intervalMillis: Long = 5000L,
    callback: (String) -> Unit
  ): LocationCallback {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest = LocationRequest.Builder(
      Priority.PRIORITY_HIGH_ACCURACY,
      intervalMillis
    ).build()

    val locationCallback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        for (location in result.locations) {
          val latLng = "${location.latitude},${location.longitude}"
          callback(latLng) // pass the value asynchronously
        }
      }
    }

    fusedLocationClient.requestLocationUpdates(
      locationRequest,
      locationCallback,
      Looper.getMainLooper()
    )

    return locationCallback
  }

  fun getDeviceMacAddress(context: Context): String {
    try {
      val devMacAddress = DataStoreManager.readFromPreferences(ParameterConstants.MAC_ADDRESS,"")
      if(devMacAddress.isNotEmpty()) return devMacAddress
      val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
      for (networkInterface in interfaces) {
        if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
          val macBytes = networkInterface.hardwareAddress ?: return ""
          val macAddress =  macBytes.joinToString(":") { String.format("%02X", it) }
          if(macAddress.isNotEmpty()) DataStoreManager.saveToPreferences(ParameterConstants.MAC_ADDRESS,macAddress)
          return macAddress
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return ""
  }

  fun getLocalMacAddress6Above(): String {
    return try {
      val all = Collections.list(NetworkInterface.getNetworkInterfaces())
      for (nif in all) {
        if (!nif.name.equals("wlan0", ignoreCase = true)) continue
        val macBytes = nif.hardwareAddress ?: return ""
        return macBytes.joinToString(":") { String.format("%02X", it) }
      }
      "02:00:00:00:00:00"
    } catch (ex: Exception) {
      "02:00:00:00:00:00"
    }
  }

  fun getMac(): String? {
    var macSerial: String? = null
    var ir: InputStreamReader? = null
    var input: LineNumberReader? = null
    try {
      val pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address")
      ir = InputStreamReader(pp.inputStream)
      input = LineNumberReader(ir)
      var str: String?
      while (input.readLine().also { str = it } != null) {
        macSerial = str!!.trim()
        break
      }
    } catch (ex: IOException) {
      ex.printStackTrace()
    } finally {
      try {
        ir?.close()
        input?.close()
      } catch (_: Exception) {
      }
    }
    return macSerial
  }

  private fun getImei_mtk(context: Context, imei2: Array<String>?): String? {
    val imei1 = Settings.Global.getString(context.contentResolver, "Imei1")
    val imei2 = Settings.Global.getString(context.contentResolver, "Imei2")
    if (imei1.isNotEmpty()) return imei1;
    else if (imei2.isNotEmpty()) return imei2;
    else return "";
  }

  private fun getImeiC60_smd450(context: Context, imei2: Array<String>?): String? {
    val imei1 = Settings.System.getString(context.contentResolver, "cw_imei_one")
    val imei2Str = Settings.System.getString(context.contentResolver, "cw_imei_two")
    if (imei2Str != null && imei2 != null && imei2.isNotEmpty()) {
      imei2[0] = imei2Str
    }
    LogUtils.showLog("getImeiC60_smd450", "getImeiC60_smd450 imei1=$imei1 imei2=${imei2?.getOrNull(0)}")
    return imei1
  }

  private fun getImeiC60(context: Context, imei2: Array<String>?): String? {
    val imei1 = getSystemProperties("persist.quectel.imei1")
    val imei2Str = getSystemProperties("persist.quectel.imei2")
    if (imei2Str != null && imei2 != null && imei2.isNotEmpty()) {
      imei2[0] = imei2Str
    }
    return imei1
  }

  fun getSystemProperties(properties: String): String {
    var strState = ""
    try {
      val systemProperties = Class.forName("android.os.SystemProperties")
      val get = systemProperties.getDeclaredMethod("get", String::class.java)
      strState = get.invoke(null, properties) as String
    } catch (e: Exception) {
      e.printStackTrace()
    }
    LogUtils.showLog("getSystemProperties", "getSystemProperties $properties = $strState")
    return strState
  }


}