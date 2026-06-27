package com.itek.rftaar.data.model

data class TagTime(val tagInfoId:Int,val barcode:String,val epc:String,val tid:String, val timeStamp:String, val status: String? = null)