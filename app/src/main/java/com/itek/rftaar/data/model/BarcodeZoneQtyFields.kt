package com.itek.rftaar.data.model

data class BarcodeZoneQtyFields(var barcode: String="", var field1: String="", var field2: String="", var name:String="", var path:String="", var totalQty:Int=0, val errMsg:String=""){
    fun isValid(): Boolean{
       return  barcode.isNotEmpty() && name.isNotEmpty() && path.isNotEmpty() && totalQty>0 && field1.isNotEmpty() && field2.isNotEmpty()
    }
}
