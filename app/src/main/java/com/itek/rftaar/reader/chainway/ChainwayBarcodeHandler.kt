package com.itek.rftaar.reader.chainway

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.reader.BarcodeHandler
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.rscja.barcode.BarcodeUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChainwayBarcodeHandler(context:CommonActivity,errMsg: MutableLiveData<String>) : BarcodeHandler(context,errMsg) {
  private var barcodeUtility: BarcodeUtility = BarcodeUtility.getInstance()
  private var barcodeDataReceiver: BarcodeDataReceiver? = null

  override fun startScanning():Boolean {
    showLog("startScanning",""+(barcodeUtility != null))
    if (barcodeUtility != null) {
      barcodeUtility.startScan(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION)
      return true;
    }
    return false
  }

  override fun stopScanning() {
    showLog("stopScanning",""+(barcodeUtility != null))
    if (barcodeUtility != null) barcodeUtility.stopScan(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION)
  }

  override fun onTimerFinish() {
    stopScan()
  }

  override fun init() {
    CoroutineScope(Dispatchers.IO).launch {
      if(barcodeUtility==null) barcodeUtility=BarcodeUtility.getInstance()
      showLog("init",""+(barcodeUtility!=null))
      if (barcodeUtility != null) {
        //0 => Scan Content on cursor, 1=> Clipboard, 2=> BroadcastReceiver, 3=> Keyboard input, 4=> Overlay cursor position
        barcodeUtility.setOutputMode(context, 2) //设置广播接收数据
        barcodeUtility.setScanResultBroadcast(context, "com.scanner.broadcast", "data") //设置接收数据的广播
        barcodeUtility.open(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION) //打开2D

        barcodeUtility.setReleaseScan(context, false) //设置松开扫描按键，不停止扫描
        barcodeUtility.setScanOutTime(context,scanTimeOut.toInt());
        barcodeUtility.setScanFailureBroadcast(context, true) //扫描失败也发送广播
        barcodeUtility.enableContinuousScan(context, false) //关闭键盘助手连续扫描
        barcodeUtility.enablePlayFailureSound(context, false) //关闭键盘助手 扫描失败的声音
        //关闭键盘助手 扫描成功的声音
        barcodeUtility.enableEnter(context, false) //关闭回车//false
        //0 => default, 1=> ASCII, 2=> GB2312, 3=> UTF8, 4=> Unicode, 5=> GBK, 6=> GB18030, 7=> SHIFT_JIS, 8=> AutoDetect
        barcodeUtility.setBarcodeEncodingFormat(context, 1) //1
        registerReceiver()
      }
    }
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  private fun registerReceiver(){
    val chainwayBarcodeHandler = this
    CoroutineScope(Dispatchers.IO).launch {
      if (barcodeDataReceiver == null) {
        barcodeDataReceiver = BarcodeDataReceiver(chainwayBarcodeHandler)
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.scanner.broadcast")
        context.registerReceiver(barcodeDataReceiver, intentFilter)
        showLog("registerReceiver", "ok")
      }
    }
  }

  override fun onResume() {
    if (barcodeUtility != null) {
      barcodeUtility.open(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION)
      //registerReceiver()
    }
  }

  override fun onPause() {
    stopScan()
    /*if (barcodeUtility != null) {
      barcodeUtility.close(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION) //关闭2D
      if (barcodeDataReceiver != null) {
        context.unregisterReceiver(barcodeDataReceiver)
        barcodeDataReceiver = null
      }
    }*/
  }

  override fun onDestroy() {
    if (barcodeUtility != null) {
      isBarcodeOn.postValue(false)
      barcodeUtility.close(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION) //关闭2D
      if (barcodeDataReceiver != null) {
        context.unregisterReceiver(barcodeDataReceiver)
        barcodeDataReceiver = null
      }
    }
  }

  class BarcodeDataReceiver(val chainwayBarcodeHandler: ChainwayBarcodeHandler) : BroadcastReceiver() {
    override fun onReceive(context1: Context, intent: Intent) {
      val scannedBarcodeData = intent.getStringExtra("data")
      val status = intent.getStringExtra("SCAN_STATE")
      //showLog("status", chkNull(status, "-"))
     // showLog("_barcodeData_Result", chkNull(scannedBarcodeData, "fail"))
      /*if(status != null && (status.equals("cancel"))){
        barcodeData.postValue("");
      }
      else */
      if (status != null && (status == "failuer")) {
        chainwayBarcodeHandler.postData(chkNull(scannedBarcodeData, "").trim())
        /*scanTimer.cancel()
        if (isBarcodeOn.getValue()) {
          //((MainActivity) ChainwayBarcodeHandler.this.context).showCustomErrDialog(String.format(context.getString(R.string.err_scan_fail), getTypeCharCode(), scanType));
          setProgressMessage(false)
          isBarcodeOn.postValue(false)
        }*/
      }
      else {
        chainwayBarcodeHandler.postData(chkNull(scannedBarcodeData, "").trim())
      }
    }
  }
}