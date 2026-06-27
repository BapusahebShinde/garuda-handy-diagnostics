package com.itek.rftaar.data.model

data class ProductZoneFoundQty(val barcode: String,val customField: String = "", val displayData: String,val name:String, val path:String, val totalQty:Int=0, val totalAvailableStockQty:Int=0, val stockQty:Int=0, val foundQty:Int=0, val decodeQty:Int=0,  val errMsg:String="")
