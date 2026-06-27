package com.itek.rftaar.reader.zebra

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.reader.BarcodeHandler
import com.itek.rftaar.utils.CommonUtils.chkNull

class ZebraBarcodeHandler(context: CommonActivity,errMsg: MutableLiveData<String>):BarcodeHandler(context,errMsg) {
  companion object {
    private const val EXTRA_PROFILE_NAME: String = "Profile1"

    // DataWedge Extras
    private const val EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    private const val EXTRA_CREATE_PROFILE: String = "com.symbol.datawedge.api.CREATE_PROFILE"
    private const val EXTRA_KEY_APPLICATION_NAME: String = "com.symbol.datawedge.api.APPLICATION_NAME"
    private const val EXTRA_KEY_NOTIFICATION_TYPE: String = "com.symbol.datawedge.api.NOTIFICATION_TYPE"
    private const val EXTRA_SOFT_SCAN_TRIGGER: String = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER"
    private const val EXTRA_RESULT_NOTIFICATION: String = "com.symbol.datawedge.api.NOTIFICATION"
    private const val EXTRA_REGISTER_NOTIFICATION: String = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION"
    private const val EXTRA_UNREGISTER_NOTIFICATION: String = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION"
    private const val EXTRA_KEY_INPUT: String = "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN"
    private const val EXTRA_SET_CONFIG: String = "com.symbol.datawedge.api.SET_CONFIG"
    private const val EXTRA_RESULT_NOTIFICATION_TYPE: String = "NOTIFICATION_TYPE"
    private const val EXTRA_KEY_VALUE_SCANNER_STATUS: String = "SCANNER_STATUS"
    private const val EXTRA_KEY_VALUE_PROFILE_SWITCH: String = "PROFILE_SWITCH"
    private const val EXTRA_KEY_VALUE_CONFIGURATION_UPDATE: String = "CONFIGURATION_UPDATE"
    private const val EXTRA_KEY_VALUE_NOTIFICATION_STATUS: String = "STATUS"
    private const val EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME: String = "PROFILE_NAME"
    private const val EXTRA_SEND_RESULT: String = "SEND_RESULT"
    private const val EXTRA_PLUGIN_RESUME: String = "RESUME_PLUGIN"
    private const val EXTRA_PLUGIN_SUSPEND: String = "SUSPEND_PLUGIN"
    private const val EXTRA_EMPTY: String = ""
    private const val EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
    private const val EXTRA_RESULT: String = "RESULT"
    private const val EXTRA_RESULT_INFO: String = "RESULT_INFO"
    private const val EXTRA_COMMAND: String = "COMMAND"

    // DataWedge Actions
    private const val ACTION_DATAWEDGE: String = "com.symbol.datawedge.api.ACTION"
    private const val ACTION_RESULT_NOTIFICATION: String = "com.symbol.datawedge.api.NOTIFICATION_ACTION"
    private const val ACTION_RESULT: String = "com.symbol.datawedge.api.RESULT_ACTION"
  }

  private var bRequestSendResult:Boolean = false;
  private var okToSuspend:Boolean = false;
  private var isReceiverRegistered:Boolean = false;

  private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      val action = intent.getAction()

      showLog("onReceive_Action", action!!)
      if (action == "com.zebra.datacapture1.ACTION") {
        //  Received a barcode scan
        if (isScanning) {
          isScanning = false
          showLog("isScanning2", isScanning.toString())
          try {
            postData(chkNull(intent.getStringExtra("com.symbol.datawedge.data_string"),""))
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      } 
      else if (action == ACTION_RESULT) {
        // Register to receive the result code
        if ((intent.hasExtra(EXTRA_RESULT)) && (intent.hasExtra(EXTRA_COMMAND))) {
          val command = intent.getStringExtra(EXTRA_COMMAND)
          val result = intent.getStringExtra(EXTRA_RESULT)
          var info = ""
          //showLog("onReceive_command", command);
          //showLog("onReceive_result", result);
          if (intent.hasExtra(EXTRA_RESULT_INFO)) {
            val result_info = intent.getBundleExtra(EXTRA_RESULT_INFO)
            val keys = result_info!!.keySet()
            for (key in keys) {
              val `object` = result_info.get(key)
              if (`object` is String) {
                info += key + ": " + `object` + "\n"
              }
              else if (`object` is Array<*> && `object`.isArrayOf<String>()) {
                val codes = `object` as Array<String?>
                for (code in codes) {
                  info += key + ": " + code + "\n"
                }
              }
            }

            //showLog("onReceive_info", info);
            /*showLog(this.getClass().getSimpleName(), "Command: "+command+"\n" +
              "Result: " +result+"\n" +
              "Result Info: " + info + "\n");*/
          }
        }
      }
      else if (action == ACTION_RESULT_NOTIFICATION && intent.hasExtra(EXTRA_RESULT_NOTIFICATION)) {
          val extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION)
          val notificationType = extras!!.getString(EXTRA_RESULT_NOTIFICATION_TYPE)
          if (notificationType != null) {
            //showLog("onReceive_notificationType", notificationType);
            when (notificationType) {
              EXTRA_KEY_VALUE_SCANNER_STATUS -> {
                // Change in scanner status occurred
                val status = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS)
                showLog("onReceive_Scanner status", status + "_" + isScanning)
                okToSuspend = status.equals("WAITING", ignoreCase = true) || status.equals("SCANNING", ignoreCase = true)
                if (!status.equals("Scanning", ignoreCase = true)) { }
                else isScanning = true
              }
              EXTRA_KEY_VALUE_PROFILE_SWITCH -> {}
              EXTRA_KEY_VALUE_CONFIGURATION_UPDATE -> {}
              else -> {}
            }
          }
      }
    }
  }

  private fun createProfile() {
    try {
      val profileName: String = EXTRA_PROFILE_NAME

      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, profileName)


      // Configure created profile to apply to this app
      val profileConfig = Bundle()
      profileConfig.putString("PROFILE_NAME", EXTRA_PROFILE_NAME)
      profileConfig.putString("PROFILE_ENABLED", "true")
      profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST") // Create profile if it does not exist

      // Configure barcode input plugin
      val barcodeConfig = Bundle()
      barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
      barcodeConfig.putString("RESET_CONFIG", "true") //  This is the default

      val barcodeProps = Bundle()

      /*barcodeProps.putString("scanner_selection", "auto");
      barcodeProps.putString("scanner_input_enabled", "true");
      barcodeProps.putString("decoder_code128", "true");
      barcodeProps.putString("decoder_code39", "true");
      barcodeProps.putString("decoder_ean13", "true");
      barcodeProps.putString("decoder_upca", "true");*/
      barcodeConfig.putBundle("PARAM_LIST", barcodeProps)

      profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)


      // Associate profile with this app
      val appConfig = Bundle()
      appConfig.putString("PACKAGE_NAME", context.getPackageName())
      appConfig.putStringArray("ACTIVITY_LIST", arrayOf<String>("*"))
      profileConfig.putParcelableArray("APP_LIST", arrayOf<Bundle>(appConfig))
      profileConfig.remove("PLUGIN_CONFIG")


      // Apply configs
      // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)


      // Configure intent output for captured data to be sent to this app
      val intentConfig = Bundle()
      intentConfig.putString("PLUGIN_NAME", "INTENT")
      intentConfig.putString("RESET_CONFIG", "true")
      val intentProps = Bundle()
      intentProps.putString("intent_output_enabled", "true")
      intentProps.putString("intent_action", "com.zebra.datacapture1.ACTION")
      intentProps.putString("intent_delivery", "2")
      intentConfig.putBundle("PARAM_LIST", intentProps)
      profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)


      // Place "barcodeConfig" bundle within main "profileConfig" bundle
      profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)


      // Create APP_LIST bundle to associate app with profile
      profileConfig.putParcelableArray("APP_LIST", arrayOf<Bundle>(appConfig))
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)


      //registerReceivers();

      updateProfile()

    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  private fun updateProfile() {
    try {
      // Main bundle properties
      val profileConfig = Bundle()
      profileConfig.putString(
        "PROFILE_NAME",
        EXTRA_PROFILE_NAME
      )
      profileConfig.putString("PROFILE_ENABLED", "true")
      profileConfig.putString("CONFIG_MODE", "UPDATE") // Update specified settings in profile


      // PLUGIN_CONFIG bundle properties
      val barcodeConfig = Bundle()
      barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
      barcodeConfig.putString("RESET_CONFIG", "true")


      // PARAM_LIST bundle properties
      val barcodeProps = Bundle()
      barcodeProps.putString("scanner_selection", "auto")
      barcodeProps.putString("scanner_input_enabled", "true")
      barcodeProps.putString("decoder_code128", "true")
      barcodeProps.putString("decoder_code39", "true")
      barcodeProps.putString("decoder_ean13", "true")
      barcodeProps.putString("decoder_upca", "true")


      // Bundle "barcodeProps" within bundle "barcodeConfig"
      barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
      // Place "barcodeConfig" bundle within main "profileConfig" bundle
      profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)
      DataStoreManager.setIsZebraBarcodeProfileSet(true)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Send data wedge intent with extra.
   *
   * @param action   the action
   * @param extraKey the extra key
   * @param extras   the extras
   */
  private fun sendDataWedgeIntentWithExtra(action: String?, extraKey: String?, extras: Bundle?) {
    val dwIntent = Intent()
    dwIntent.setAction(action)
    dwIntent.putExtra(extraKey, extras)
    if (bRequestSendResult) dwIntent.putExtra(EXTRA_SEND_RESULT, "true")
    context.sendBroadcast(dwIntent)
  }

  /**
   * Send data wedge intent with extra.
   *
   * @param action     the action
   * @param extraKey   the extra key
   * @param extraValue the extra value
   */
  private fun sendDataWedgeIntentWithExtra(
    action: String?,
    extraKey: String?,
    extraValue: String?
  ) {
    val dwIntent = Intent()
    dwIntent.setAction(action)
    dwIntent.putExtra(extraKey, extraValue)
    if (bRequestSendResult) dwIntent.putExtra(
      EXTRA_SEND_RESULT,
      "true")
    context.sendBroadcast(dwIntent)
  }

  /**
   * Register receivers.
   */
  private fun registerReceivers() {
    try {
      showLog("registerReceivers", "registerReceivers()")
      val filter = IntentFilter()
      filter.addAction(ACTION_RESULT_NOTIFICATION) // for notification result
      filter.addAction(ACTION_RESULT) // for error code result
      //filter.addCategory(Intent.CATEGORY_DEFAULT);    // needed to get version info
      // register to received broadcasts via DataWedge scanning
      filter.addAction("com.zebra.datacapture1.ACTION")
      filter.addAction("com.zebra.datacapture1.service.ACTION")
      context.registerReceiver(myBroadcastReceiver, filter)
      isReceiverRegistered = true
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Un register scanner status.
   */
  private fun unRegisterScannerStatus() {
    try {
      if (isReceiverRegistered) {
        context.unregisterReceiver(myBroadcastReceiver)
        isReceiverRegistered = false
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    try {
      //showLog(this.getClass().getSimpleName(), "unRegisterScannerStatus()");
      val b = Bundle()
      b.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName())
      b.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS)
      val i = Intent()
      i.setAction(ContactsContract.Intents.Insert.ACTION)
      i.putExtra(EXTRA_UNREGISTER_NOTIFICATION, b)
      context.sendBroadcast(i)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  override fun onTimerFinish() {
    //if (chkTrue(isBarcodeOn.getValue()) && scannedData.isNullOrEmpty()) {
      //if(okToSuspend)
      //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_KEY_INPUT, EXTRA_PLUGIN_SUSPEND);
      //isScanning = false
      stopScan()
    //}
  }

  override fun init() {
    val b = Bundle()
    b.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName())
    b.putString(EXTRA_KEY_NOTIFICATION_TYPE, "SCANNER_STATUS") // register for changes in scanner status
    sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, b)
    if (!isReceiverRegistered) registerReceivers()
    if (!DataStoreManager.getIsZebraBarcodeProfileSet()) createProfile()
  }

  override fun startScanning(): Boolean {
      showLog("startScanning","zebra")
      try {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_KEY_INPUT, EXTRA_PLUGIN_RESUME);
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING")
        return true
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return false
      }
      return false
    }

  override fun stopScanning() {
    showLog("stopScanning","zebra")
    sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_KEY_INPUT, EXTRA_PLUGIN_SUSPEND);
    //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING")
  }

  override fun onResume() {
    //showLog("ZBHO", "onResume");
    try {
      registerReceivers()
    }
    catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  override fun onPause() {
    //showLog("ZBHO", "onPause");
    stopScan()
    unRegisterScannerStatus()
  }

  override fun onDestroy() {
    //showLog("ZBHO", "onDestroy");
    stopScan()
    unRegisterScannerStatus()
  }
}