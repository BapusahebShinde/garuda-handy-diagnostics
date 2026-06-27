package com.itek.rftaar.diagnostics

import android.content.Context
import android.content.Intent
import java.io.File

object DiagnosticShare {
  fun shareLatest(context: Context) {
    val file: File = DiagnosticFileManager.latestDiagnosticsFiles(context).firstOrNull() ?: return
    val uri = DiagnosticFileManager.uriFor(context, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/*"
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share diagnostics"))
  }
}
