package com.itek.rftaar.diagnostics

import java.io.FileWriter
import java.util.Locale

object DiagnosticSummaryWriter {
  fun write(session: DiagnosticSession, summary: DiagnosticLogger.Summary, rfid: RfidMetrics, mqtt: MqttMetrics) {
    FileWriter(session.summaryFile, false).use { writer ->
      writer.appendLine("diagnostic_build=${session.buildVersion}")
      writer.appendLine("session_id=${session.sessionId}")
      writer.appendLine("session_duration_seconds=${summary.durationSeconds}")
      writer.appendLine("max_battery_temp_c=${fmt(summary.maxBatteryTempC)}")
      writer.appendLine("max_battery_temp_timestamp=${summary.maxBatteryTempTimestamp}")
      writer.appendLine("first_temp_above_40_timestamp=${summary.firstTempAbove40Timestamp}")
      writer.appendLine("first_temp_above_45_timestamp=${summary.firstTempAbove45Timestamp}")
      writer.appendLine("peak_raw_reads_per_sec=${fmt(summary.peakRawReadsPerSec)}")
      writer.appendLine("lowest_raw_reads_per_sec_after_5_min=${fmt(summary.lowestRawReadsPerSecAfter5Min)}")
      writer.appendLine("peak_unique_reads_per_sec=${fmt(summary.peakUniqueReadsPerSec)}")
      writer.appendLine("lowest_unique_reads_per_sec_after_5_min=${fmt(summary.lowestUniqueReadsPerSecAfter5Min)}")
      writer.appendLine("total_unique_epcs=${rfid.uniqueEpcsTotal}")
      writer.appendLine("total_duplicates=${rfid.duplicateCallbacksTotal}")
      writer.appendLine("total_raw_callbacks=${rfid.rawCallbacksTotal}")
      writer.appendLine("max_process_cpu_percent=${fmt(summary.maxProcessCpuPercent)}")
      writer.appendLine("avg_process_cpu_percent=${fmt(summary.avgProcessCpuPercent())}")
      writer.appendLine("max_callback_ms=${fmt(rfid.callbackMaxMs)}")
      writer.appendLine("avg_callback_ms=${fmt(rfid.callbackAvgMs)}")
      writer.appendLine("max_callback_queue_depth=${summary.maxCallbackQueueDepth}")
      writer.appendLine("avg_rssi=${fmt(rfid.avgRssi)}")
      writer.appendLine("min_rssi=${fmt(rfid.minRssi)}")
      writer.appendLine("max_rssi=${fmt(rfid.maxRssi)}")
      writer.appendLine("sdk_error_count=${rfid.sdkErrorCount}")
      writer.appendLine("sdk_warning_count=${rfid.sdkWarningCount}")
      writer.appendLine("max_uhf_module_temp_c=${fmt(summary.maxUhfModuleTempC)}")
      writer.appendLine("max_battery_current_ma=${fmt(summary.maxBatteryCurrentMa)}")
      writer.appendLine("mqtt_connected_final=${mqtt.connected}")
      writer.appendLine("mqtt_reconnect_count=${mqtt.reconnectCount}")
      writer.appendLine("mqtt_publish_attempt_total=${mqtt.publishAttemptTotal}")
      writer.appendLine("mqtt_publish_success_total=${mqtt.publishSuccessTotal}")
      writer.appendLine("mqtt_publish_failure_total=${mqtt.publishFailureTotal}")
      val successPercent = if (mqtt.publishAttemptTotal > 0) mqtt.publishSuccessTotal * 100.0 / mqtt.publishAttemptTotal else 0.0
      writer.appendLine("mqtt_publish_success_percent=${fmt(successPercent)}")
      writer.appendLine("mqtt_max_publish_latency_ms=${mqtt.publishMaxLatencyMs}")
      writer.appendLine("mqtt_avg_publish_latency_ms=${fmt(mqtt.publishAvgLatencyMs)}")
      writer.appendLine("mqtt_max_pending_queue=${summary.maxMqttPendingQueue}")
      writer.appendLine("mqtt_dropped_message_count=${mqtt.droppedMessageCount}")
      writer.appendLine("mqtt_last_error=${mqtt.lastError}")
    }
  }
  private fun fmt(value: Double) = if (value < 0 || value.isNaN()) "" else String.format(Locale.US, "%.2f", value)
}
