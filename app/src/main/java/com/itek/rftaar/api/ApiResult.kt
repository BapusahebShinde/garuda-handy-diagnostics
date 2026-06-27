package com.itek.rftaar.api

import android.os.Bundle
import org.json.JSONObject

data class ApiResult(
  val url: String,
  val appendData: String?=null,
  val jsonRequest: JSONObject?=null,
  val isSuccess: Boolean=false,
  val response: JSONObject?=null,
  val responseCode:Int=0,
  val errMsg:String?=null,
  val args:Bundle?=null
)