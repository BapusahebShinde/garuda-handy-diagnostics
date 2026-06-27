package com.itek.rftaar.data.model

data class BarcodeRefMap(val oldBarcode:String,val newBarcode:String, val epc:String, val tid:String, val timeStamp:String, val status: String? = null){
  override fun equals(other: Any?): Boolean {
    return other is BarcodeRefMap && other.oldBarcode.trim().equals(oldBarcode.trim(),true) && other.newBarcode.trim().equals(newBarcode.trim(),true)
  }
}