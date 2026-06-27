package com.itek.rftaar.reader.chainway

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.reader.ReaderRepository

class ChainwayRepository : ReaderRepository {
  private var homeKeyEventBroadCastReceiver: HomeKeyEventBroadCastReceiver? =null

  constructor(context: CommonActivity,isSetupBarcodeReader:Boolean=true):super(context,isSetupBarcodeReader){
    rfidHandler = ChainwayRFIDHandler(context,errMsg)
    rfidHandler?.onCreate()
    if(isSetupBarcodeReader) {
      barcodeHandler = ChainwayBarcodeHandler(context,errMsg)
      barcodeHandler?.init()
    }
    setHomeKeyEventBroadCastReceiver()
  }

  private fun setHomeKeyEventBroadCastReceiver() {
    if (homeKeyEventBroadCastReceiver == null) {
      homeKeyEventBroadCastReceiver = HomeKeyEventBroadCastReceiver(this)
      val intentFilter1 = IntentFilter()
      intentFilter1.addAction("com.rscja.android.KEY_DOWN")
      context.registerReceiver(homeKeyEventBroadCastReceiver, intentFilter1)
    }
  }

  override fun unregisterReceiver(){
    if (homeKeyEventBroadCastReceiver != null){
      context.unregisterReceiver(homeKeyEventBroadCastReceiver)
      homeKeyEventBroadCastReceiver = null
    }
  }


  class HomeKeyEventBroadCastReceiver(val repository: ChainwayRepository) : BroadcastReceiver() {
    private val triggerKeyCodes = arrayOf(139, 280, 293)
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (action.equals("com.rscja.android.KEY_DOWN",true)) {
        repository.showLog("TRIGGER", "PRESSED")
        val keyCode = intent.getIntExtra("keycode", 0)
        repository.showLog("TRIGGER keyCode", "" + keyCode)
        val isPressed = intent.getBooleanExtra("Pressed", false)
        repository.showLog("TRIGGER isPressed", "" + isPressed)
        // home key处理点
        if (triggerKeyCodes.isNotEmpty() && triggerKeyCodes.contains(keyCode))
          repository.rfidHandler?.setTriggerPressed()
          //repository.rfidHandler?.isTriggerPressed?.postValue(true)
      }
    }
  }
}