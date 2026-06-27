package com.itek.rftaar.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import com.itek.rftaar.core.common.constants.LoginConstants.IS_LOGGED_IN
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.mqtt.MqttManager


class NetworkMonitor {
    companion object{
      var isNetworkConnected:Boolean =  false
      fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
      }
    }

    private var connectivityManager: ConnectivityManager?=null
    private var context:Context?=null

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
      override fun onAvailable(network: Network) {
        isNetworkConnected=true
        //LogUtils.showLog("isNetworkConnected",""+isNetworkConnected)
        println("Network available: $network")
        if(DataStoreManager.readFromPreferences(IS_LOGGED_IN,false) && MqttManager.isInitialized()) MqttManager.connect()
      }



      override fun onLost(network: Network) {
        // Network connection lost
        isNetworkConnected=false
        //LogUtils.showLog("isNetworkConnected",""+isNetworkConnected)
        println("Network lost: $network")
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        // Network capabilities changed (e.g., speed, type)
        println("Network capabilities changed for: $network, capabilities: $networkCapabilities")
      }
    }

    fun startMonitoring(context: Context) {
      this.context=context
      this.connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val networkRequest = NetworkRequest.Builder()
        //.addCapability(NetworkCapabilities.NET_CAPABILITY_LOCAL_NETWORK)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
      connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun stopMonitoring() {
      connectivityManager?.unregisterNetworkCallback(networkCallback)
    }
}