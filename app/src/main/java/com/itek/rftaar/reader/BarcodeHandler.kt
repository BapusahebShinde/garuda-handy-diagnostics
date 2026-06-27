package com.itek.rftaar.reader

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.R
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.isNullOrEmpty

abstract class BarcodeHandler{
  protected lateinit var context: CommonActivity
  val scanTimeOut = 2000L
  var isBarcodeOn: MutableLiveData<Boolean> = MutableLiveData(false)
  var barcodeData: MutableLiveData<String> = MutableLiveData("")
  var scanType:String=""
  var scannedData:String=""
  var isScanning:Boolean=false
  var errMsg: MutableLiveData<String> = MutableLiveData("")

  constructor(context: CommonActivity,errMsg: MutableLiveData<String>) {
    this.context=context;
    this.errMsg = errMsg
    init()
  }

  protected val scanTimer: CountDownTimer = object : CountDownTimer(scanTimeOut, scanTimeOut) {
    override fun onTick(l: Long) {}

    override fun onFinish() {
      //onTimerFinish()
      stopScan()
      if(isNullOrEmpty(scannedData)) validateScannedData("null")//barcodeData.postValue("null")
    }
  }

  protected abstract fun onTimerFinish()

  abstract fun init()

  abstract fun startScanning():Boolean

  internal fun startScan(scanType: String){
    if(isScanning) return
    this.scannedData=""
    barcodeData.value=""
    this.scanType=chkNull(scanType, DataStoreManager.getBarcodeLabel())
    showLog("startScan_scanType",""+scanType)
    if(startScanning()) onScanStarted()
  }

  private fun onScanStarted(){
    this.isBarcodeOn.postValue(true)
    this.isScanning=true
    scanTimer.start()
  }

  protected fun postData(scannedData:String){
    if(this.scannedData.isNullOrEmpty()) {
      this.scannedData=scannedData;
      showLog("postData_scanType",""+scanType)
      stopScan()
      if(validateScannedData(scannedData)) barcodeData.postValue(scannedData)
    }
  }

  private fun validateScannedData(scannedData:String):Boolean{
    showLog("method","validateScannedData_"+scannedData)
    //validate based on scanType
    if(scannedData.equals("null", true)){
      setError(String.format(context.getString(R.string.err_scan_fail),scanType));
      return false;
    }
    val validationResult =  validateScannedDataWithScanType(scanType,scannedData)
    showLog("validationResult",""+validationResult)
    if(!validationResult) {
      setError(String.format(context.getString(R.string.err_invalid_scan),scanType,scannedData));
      return false;
    }
    return true;
  }

  private fun validateScannedDataWithScanType(scanType:String,scannedData:String):Boolean{
    showLog("method","validateScannedDataWithScanType_"+scanType+"_"+scannedData)
    //validate based on scanType
    return when(scanType.lowercase()){
      "epc" -> scannedData.matches(Regex("[0-9A-Fa-f]{8,36}")) && scannedData.length%4==0
      "tid" -> scannedData.matches(Regex("[0-9A-Fa-f]{8,}")) && scannedData.length%4==0
      "barcode" -> return true
      else -> return true;
    }
  }

  protected fun setError(error: String) {
    showLog("barcode_error", error)
    errMsg.postValue(error)
  }

  abstract fun stopScanning()

  protected fun stopScan(){
    showLog("method","stopScan")
    isBarcodeOn.postValue(false)
    if(isScanning) {
      isScanning = false;
      //isBarcodeOn.postValue(false)
      scanTimer.cancel()
      stopScanning()
    }
  }

  /**
   * On resume.
   */
  abstract fun onResume()

  /**
   * On pause.
   */
  abstract fun onPause()

  /**
   * On destroy.
   */
  abstract fun onDestroy()

  protected fun showLog(tag:String,message:String){
    LogUtils.showLog(tag,message)
  }
}