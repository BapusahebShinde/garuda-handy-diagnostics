package com.itek.rftaar.data.interceptor

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException

class DownloadProgressResponseBody() : ResponseBody() {
  private var responseBody: ResponseBody? = null
  private var progressListener: DownloadProgressListener? = null
  private var bufferedSource: BufferedSource? = null

  constructor(
    responseBody: ResponseBody?,
    progressListener: DownloadProgressListener?
  ) : this() {
    this.responseBody = responseBody
    this.progressListener = progressListener
  }

  override fun contentType(): MediaType? {
    return responseBody!!.contentType()
  }

  override fun contentLength(): Long {
    return responseBody!!.contentLength()
  }

  override fun source(): BufferedSource {
    if (bufferedSource == null) {
      bufferedSource = source(responseBody!!.source()).buffer()
    }
    return bufferedSource!!
  }

  private fun source(source: Source): Source {
    return object : ForwardingSource(source) {
      var totalBytesRead: Long = 0L

      @Throws(IOException::class)
      override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        // read() returns the number of bytes read, or -1 if this source is exhausted.
        totalBytesRead += if (bytesRead != -1L) bytesRead else 0

        if (null != progressListener) {
          progressListener!!.update(
            totalBytesRead,
            responseBody!!.contentLength(),
            bytesRead == -1L
          )
        }
        return bytesRead
      }
    }
  }
}