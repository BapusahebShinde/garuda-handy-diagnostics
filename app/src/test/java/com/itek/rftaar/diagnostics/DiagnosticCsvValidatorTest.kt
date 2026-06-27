package com.itek.rftaar.diagnostics

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticCsvValidatorTest {
  @Test
  fun csvHeaderHasExpectedColumns() {
    assertTrue(DiagnosticCsvValidator.headerColumnCount() >= 70)
  }

  @Test
  fun csvRowValuesMatchHeaderColumns() {
    val session = DiagnosticSession(
      sessionId = "session-1",
      buildVersion = "1.0.0",
      startedAtMs = 1_000L,
      csvFile = File("RetailDiag_1.0.0_test.csv"),
      summaryFile = File("RetailDiag_1.0.0_test_summary.txt")
    )
    val system = SystemMetrics(
      batteryTempC = 34.4,
      batteryLevel = 88,
      chargingStatus = "DISCHARGING",
      batteryVoltageMv = 3900,
      batteryCurrentMa = 125.5,
      thermalStatus = "NONE",
      thermalThrottlingLevel = 0,
      screenBrightness = "100",
      memoryUsedMb = 42,
      memoryFreeMb = 512,
      processCpuPercent = 23.5,
      cpuFrequencies = "cpu0:1200000",
      gcCount = 3,
      gcTimeMs = 7,
      threadCount = 21
    )
    val rfid = RfidMetrics(
      inventoryRunning = true,
      rawCallbacksTotal = 10,
      duplicateCallbacksTotal = 4,
      uniqueEpcsTotal = 6,
      callbackAvgMs = 1.5,
      callbackMaxMs = 9.2,
      callbackQueueDepth = 0,
      avgRssi = -54.4,
      minRssi = -60.0,
      maxRssi = -48.0,
      uhfModuleTempC = -1.0,
      rfPowerDbm = "27",
      inventorySession = "S1",
      inventoryTarget = "A",
      qValue = "",
      dynamicQEnabled = "",
      antennaState = "ACTIVE_OR_CALLBACKS_RECEIVED",
      readerConnected = true,
      sdkErrorCount = 0,
      sdkLastError = "",
      sdkWarningCount = 0,
      dbPendingQueue = 0,
      dbLastFlushMs = 12,
      knownUnreadExpected = -1,
      knownUnreadFound = -1
    )
    val mqtt = MqttMetrics(
      connected = true,
      brokerHost = "tcp://broker",
      clientId = "client-1",
      connectionState = "Connected",
      lastConnectTimestamp = 2_000L,
      lastDisconnectTimestamp = 0L,
      reconnectCount = 1,
      lastError = "",
      publishAttemptTotal = 5,
      publishSuccessTotal = 4,
      publishFailureTotal = 1,
      publishPendingQueue = 0,
      publishRetryQueue = 0,
      publishAvgLatencyMs = 25.0,
      publishMaxLatencyMs = 40,
      subscribeMessageTotal = 2,
      lastMessageTimestamp = 3_000L,
      inboundQueueDepth = 0,
      outboundQueueDepth = 0,
      qos = 1,
      offlineBufferSize = 0,
      droppedMessageCount = 0,
      duplicateMessageCount = 0
    )

    val row = DiagnosticLogger.buildRowValues(session, system, rfid, mqtt, now = 6_000L, elapsedSeconds = 5L, eventType = "SAMPLE")

    assertEquals(DiagnosticLogger.CSV_COLUMNS.size, row.size)
    assertEquals("0", row[column("callback_queue_depth")])
    assertEquals("0", row[column("db_pending_queue")])
    assertEquals("", row[column("known_unread_expected")])
    assertEquals("", row[column("known_unread_found")])
    assertEquals("true", row[column("reader_connected")])
    assertEquals("-54.40", row[column("avg_rssi")])
    assertEquals("5", row[column("mqtt_publish_attempt_total")])
    assertEquals("4", row[column("mqtt_publish_success_total")])
    assertEquals("1", row[column("mqtt_publish_failure_total")])
  }

  private fun column(name: String): Int = DiagnosticLogger.CSV_COLUMNS.indexOf(name).also {
    assertTrue("Missing CSV column $name", it >= 0)
  }
}
