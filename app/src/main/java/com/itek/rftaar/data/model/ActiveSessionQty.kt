package com.itek.rftaar.data.model

data class ActiveSessionQty(
  val topic:String,
  val menuName:String,
  val parentCode:String,
  val menuPath:String,
  val menuCode:String,
  val transactionType: String,
  val scanCount:Int,
  val foundCount:Int=0,
)
