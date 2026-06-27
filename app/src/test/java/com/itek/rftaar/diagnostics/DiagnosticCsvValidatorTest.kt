package com.itek.rftaar.diagnostics

import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticCsvValidatorTest {
  @Test
  fun csvHeaderHasExpectedColumns() {
    assertTrue(DiagnosticCsvValidator.headerColumnCount() >= 70)
  }
}
