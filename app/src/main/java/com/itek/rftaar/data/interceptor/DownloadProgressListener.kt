package com.itek.rftaar.data.interceptor

interface DownloadProgressListener {
  fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}