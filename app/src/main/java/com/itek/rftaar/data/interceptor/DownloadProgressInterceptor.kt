package com.itek.rftaar.data.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class DownloadProgressInterceptor() : Interceptor {
  private var listener: DownloadProgressListener? = null

  constructor(listener: DownloadProgressListener?) : this() {
    this.listener = listener
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalResponse: Response = chain.proceed(chain.request())

    return originalResponse.newBuilder()
      .body(DownloadProgressResponseBody(originalResponse.body, listener))
      .build()
  }
}