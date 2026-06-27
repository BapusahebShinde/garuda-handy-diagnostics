package com.itek.rftaar.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager

object PermissionUtils {

  private lateinit var context: Activity
  private val isUseInAppStorage: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
  private val isUseDeviceIDForIMEI: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
  private val isUseBluetoothScanConnect: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

  fun init(context: Activity) {
    PermissionUtils.context = context
  }

  fun isGranted(permission: PermissionUtils.PERMISSION): Boolean{
    return when(permission){
      PERMISSION.REQUEST_PERMISSION_LOCATION -> {
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
      }
      PERMISSION.REQUEST_PERMISSION_CAMERA -> {
        context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
      }
      PERMISSION.REQUEST_PERMISSION_STORAGE -> {
        context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
      }
      PERMISSION.REQUEST_PERMISSION_IMEI -> {
        context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED}
      PERMISSION.REQUEST_PERMISSION_BLUETOOTH -> {
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
      }
      else ->{false}
    }
  }
  
  fun isGranted(permission: String): Boolean{
    return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
  }

  enum class PERMISSION(val value: Int){
    REQUEST_PERMISSION_IMEI(1),REQUEST_PERMISSION_LOCATION(2),REQUEST_PERMISSION_BLUETOOTH(4),REQUEST_PERMISSION_STORAGE(8), REQUEST_PERMISSION_CAMERA(16);
    companion object {
      /**
       * Get epc non-std mode.
       *
       * @param value the value
       * @return the mode
       */
      fun get(value: Int): PERMISSION? {
        return PERMISSION.entries[value]
      }
    }
  }

  /**
   * Check permissions boolean.
   *
   * @param requestPermissionCode the request permission code
   * @return the boolean
   */
  fun checkPermissions(requestPermissionCode: Int): Boolean {
    val isCheckIMEIPermission = !isUseDeviceIDForIMEI && !isGranted(PERMISSION.REQUEST_PERMISSION_IMEI) && requestPermissionCode >= PERMISSION.REQUEST_PERMISSION_IMEI.value
    val isCheckLocationPermission = !isGranted(PERMISSION.REQUEST_PERMISSION_LOCATION) && requestPermissionCode >= PERMISSION.REQUEST_PERMISSION_LOCATION.value
    val isCheckBluetoothPermission = isUseBluetoothScanConnect && !isGranted(PERMISSION.REQUEST_PERMISSION_BLUETOOTH) && requestPermissionCode >= PERMISSION.REQUEST_PERMISSION_BLUETOOTH.value
    val isCheckStoragePermission = !isUseInAppStorage && !isGranted(PERMISSION.REQUEST_PERMISSION_STORAGE) && requestPermissionCode >= PERMISSION.REQUEST_PERMISSION_STORAGE.value
    val isCheckCameraPermission = !isGranted(PERMISSION.REQUEST_PERMISSION_CAMERA) && requestPermissionCode>= PERMISSION.REQUEST_PERMISSION_CAMERA.value

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      var isShowRationale = true
      val listRequestPermissions: MutableList<String?> = ArrayList<String?>(0)
      var requestCode = 0
      if (isShowRationale && isCheckIMEIPermission) {
        isShowRationale = !DataStoreManager.getIsChkRationale() || context.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)
        listRequestPermissions.add(Manifest.permission.READ_PHONE_STATE)
        requestCode += PERMISSION.REQUEST_PERMISSION_IMEI.value
      }
      if (isShowRationale && isCheckLocationPermission) {
        isShowRationale = !DataStoreManager.getIsChkRationale() || (context.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && context.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
        LogUtils.showLog("isShowRationale", "" + isShowRationale)
        listRequestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        listRequestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        requestCode += PERMISSION.REQUEST_PERMISSION_LOCATION.value
      }
      if (isShowRationale && isCheckCameraPermission) {
        isShowRationale = !DataStoreManager.getIsChkRationale() || context.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
        listRequestPermissions.add(Manifest.permission.CAMERA)
        requestCode += PERMISSION.REQUEST_PERMISSION_CAMERA.value
      }
      if (isShowRationale && isCheckStoragePermission) {
        isShowRationale = !DataStoreManager.getIsChkRationale() || ((Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2 || context.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) || (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || context.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) listRequestPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) listRequestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestCode += PERMISSION.REQUEST_PERMISSION_STORAGE.value
      }
      if (isShowRationale && isCheckBluetoothPermission) {
        isShowRationale = !DataStoreManager.getIsChkRationale() || (context.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) || context.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN))
        listRequestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        listRequestPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        requestCode += PERMISSION.REQUEST_PERMISSION_BLUETOOTH.value
      }
      if (listRequestPermissions.size > 0) {
        if (!isShowRationale) { //Show Dialog if a permission is Always/Permanently Denied (i.e. Deny with Don't Ask)
          /*showCustomAlertDialog(
            "",
            R.string.err_grant_app_permissions,
            R.string.btn_ok,
            object : DialogInterface.OnClickListener {
              override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                if (CommonActivity.appPermissionsResultLauncher != null) CommonActivity.appPermissionsResultLauncher.launch(
                  Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null)
                  )
                )
              }
            })*/
        }
        else context.requestPermissions(listRequestPermissions.toTypedArray<String?>(), requestCode)
        return false
      } else return true
    }
    else return true
  }
}