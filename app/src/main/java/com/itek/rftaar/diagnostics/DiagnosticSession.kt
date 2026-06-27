package com.itek.rftaar.diagnostics

import java.io.File

data class DiagnosticSession(
  val sessionId: String,
  val buildVersion: String,
  val startedAtMs: Long,
  val csvFile: File,
  val summaryFile: File
)
