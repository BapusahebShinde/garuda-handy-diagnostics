package com.itek.rftaar.diagnostics

object DiagnosticCsvValidator {
  fun headerColumnCount(): Int = DiagnosticLogger.CSV_HEADER.split(',').size
  fun isValidRow(row: String): Boolean = parseCsv(row).size == headerColumnCount()

  private fun parseCsv(row: String): List<String> {
    val values = ArrayList<String>()
    val current = StringBuilder()
    var quoted = false
    var i = 0
    while (i < row.length) {
      val c = row[i]
      when {
        c == '"' && quoted && i + 1 < row.length && row[i + 1] == '"' -> { current.append('"'); i++ }
        c == '"' -> quoted = !quoted
        c == ',' && !quoted -> { values.add(current.toString()); current.clear() }
        else -> current.append(c)
      }
      i++
    }
    values.add(current.toString())
    return values
  }
}
