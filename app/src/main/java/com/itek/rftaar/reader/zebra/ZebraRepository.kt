package com.itek.rftaar.reader.zebra

import com.itek.rftaar.CommonActivity
import com.itek.rftaar.reader.ReaderRepository

class ZebraRepository : ReaderRepository {

  constructor(context: CommonActivity, isSetupBarcodeReader: Boolean = true) : super(context, isSetupBarcodeReader) {
    rfidHandler = ZebraRFIDHandler(context,errMsg)
    rfidHandler?.onCreate()
    if(isSetupBarcodeReader) {
      barcodeHandler = ZebraBarcodeHandler(context,errMsg)
      barcodeHandler?.init()
    }
  }
}