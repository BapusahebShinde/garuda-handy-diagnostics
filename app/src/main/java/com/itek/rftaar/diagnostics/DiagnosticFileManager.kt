package com.itek.rftaar.diagnostics

import android.content.Context
import androidx.core.content.FileProvider
import com.itek.rftaar.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticFileManager {
  fun createSession(context: Context): DiagnosticSession {
    val now = System.currentTimeMillis()
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))
    val baseName = "RetailDiag_${sanitize(BuildConfig.VERSION_NAME)}_$stamp"
    val dir = context.getExternalFilesDir("diagnostics") ?: File(context.filesDir, "diagnostics")
    if (!dir.exists()) dir.mkdirs()
    return DiagnosticSession(
      sessionId = "$baseName-${now}",
      buildVersion = BuildConfig.VERSION_NAME,
      startedAtMs = now,
      csvFile = File(dir, "$baseName.csv"),
      summaryFile = File(dir, "${baseName}_summary.txt")
    )
  }

  fun latestDiagnosticsFiles(context: Context): List<File> {
    val dir = context.getExternalFilesDir("diagnostics") ?: File(context.filesDir, "diagnostics")
    return dir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()
  }

  fun uriFor(context: Context, file: File) = FileProvider.getUriForFile(context, context.packageName, file)
  private fun sanitize(value: String) = value.replace(Regex("[^A-Za-z0-9_.-]"), "_")
}
