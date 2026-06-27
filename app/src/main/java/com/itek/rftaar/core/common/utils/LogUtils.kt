package com.itek.rftaar.core.common.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.itek.rftaar.utils.CommonUtils.isNonEmpty

object LogUtils {
  private lateinit var context: Context
  private var isDebugApp: Boolean=false

  fun init(context: Context) {
    this.context = context
    try {
      isDebugApp = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }catch (e:Exception){}
  }

  fun showLog(tag:String?, message:String?, isViewInRelease: Boolean=false){
    if((isDebugApp || isViewInRelease) && isNonEmpty(tag) && isNonEmpty(message)) Log.e(tag, message.toString())
  }
}