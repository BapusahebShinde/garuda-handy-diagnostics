package com.itek.rftaar.core.common.utils

import android.content.Context
import android.content.pm.ApplicationInfo

object BaseUtils {
  private lateinit var context: Context

  fun init(context: Context) {
    this.context = context
  }

  fun isDebuggable(): Boolean {
    return 0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
  }
}