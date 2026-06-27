package com.itek.rftaar.diagnostics

import android.content.Context
import com.itek.rftaar.BuildConfig
import java.io.BufferedWriter
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.max

object DiagnosticLogger {
  private const val SAMPLE_INTERVAL_SECONDS = 5L
  const val CSV_HEADER = "timestamp,elapsed_seconds,event_type,diagnostic_build,session_id,battery_temp_c,battery_level,charging_status,battery_voltage_mv,battery_current_ma,thermal_status,thermal_throttling_level,screen_brightness,inventory_running,raw_callbacks_total,duplicate_callbacks_total,unique_epcs_total,raw_callbacks_per_sec,unique_epcs_per_sec,duplicate_percent,callback_avg_ms,callback_max_ms,callback_queue_depth,avg_rssi,min_rssi,max_rssi,uhf_module_temp_c,rf_power_dbm,inventory_session,inventory_target,q_value,dynamic_q_enabled,antenna_state,reader_connected,sdk_error_count,sdk_last_error,sdk_warning_count,mqtt_connected,mqtt_broker_host,mqtt_client_id,mqtt_connection_state,mqtt_last_connect_timestamp,mqtt_last_disconnect_timestamp,mqtt_reconnect_count,mqtt_last_error,mqtt_publish_attempt_total,mqtt_publish_success_total,mqtt_publish_failure_total,mqtt_publish_pending_queue,mqtt_publish_retry_queue,mqtt_publish_avg_latency_ms,mqtt_publish_max_latency_ms,mqtt_subscribe_message_total,mqtt_last_message_timestamp,mqtt_inbound_queue_depth,mqtt_outbound_queue_depth,mqtt_qos,mqtt_offline_buffer_size,mqtt_dropped_message_count,mqtt_duplicate_message_count,db_pending_queue,db_last_flush_ms,ui_update_count,memory_used_mb,memory_free_mb,process_cpu_percent,cpu_frequencies,gc_count,gc_time_ms,thread_count,known_unread_expected,known_unread_found,warning"
  private val lock = Any()
  private var executor: ScheduledExecutorService? = null
  private var writer: BufferedWriter? = null
  private var systemReader: SystemMetricsReader? = null
  private var session: DiagnosticSession? = null
  private var lastSampleMs = 0L
  private var lastRaw = 0L
  private var lastUnique = 0L
  private val summary = Summary()
  @Volatile var latestCsvPath: String = ""
  @Volatile var latestSummaryPath: String = ""

  fun start(context: Context) {
    synchronized(lock) {
      stopLocked(false)
      RfidDiagnosticTracker.reset()
      MqttDiagnosticTracker.reset()
      val appContext = context.applicationContext
      val createdSession = DiagnosticFileManager.createSession(appContext)
      session = createdSession
      latestCsvPath = createdSession.csvFile.absolutePath
      latestSummaryPath = createdSession.summaryFile.absolutePath
      systemReader = SystemMetricsReader(appContext)
      writer = BufferedWriter(FileWriter(createdSession.csvFile, false)).also {
        it.write(CSV_HEADER); it.newLine(); it.flush()
      }
      lastSampleMs = createdSession.startedAtMs
      lastRaw = 0; lastUnique = 0; summary.reset(createdSession.startedAtMs)
      executor = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "diagnostic_sampler").apply { isDaemon = true } }
      executor?.scheduleAtFixedRate({ sample("SAMPLE") }, SAMPLE_INTERVAL_SECONDS, SAMPLE_INTERVAL_SECONDS, TimeUnit.SECONDS)
    }
  }

  fun stop() { synchronized(lock) { stopLocked(true) } }

  private fun stopLocked(writeStop: Boolean) {
    val activeSession = session
    if (writeStop && activeSession != null) {
      sampleLocked("STOP")
      DiagnosticSummaryWriter.write(activeSession, summary, RfidDiagnosticTracker.snapshot(), MqttDiagnosticTracker.snapshot())
    }
    executor?.shutdownNow(); executor = null
    writer?.flush(); writer?.close(); writer = null
    systemReader = null; session = null
  }

  private fun sample(eventType: String) { synchronized(lock) { sampleLocked(eventType) } }
  private fun sampleLocked(eventType: String) {
    val activeSession = session ?: return
    val out = writer ?: return
    val sys = systemReader?.read() ?: return
    val rfid = RfidDiagnosticTracker.snapshot()
    val mqtt = MqttDiagnosticTracker.snapshot()
    val now = System.currentTimeMillis()
    val elapsedSeconds = max(0, (now - activeSession.startedAtMs) / 1000)
    val deltaSeconds = max(1.0, (now - lastSampleMs) / 1000.0)
    val rawRate = (rfid.rawCallbacksTotal - lastRaw).coerceAtLeast(0) / deltaSeconds
    val uniqueRate = (rfid.uniqueEpcsTotal - lastUnique).coerceAtLeast(0) / deltaSeconds
    val duplicatePercent = if (rfid.rawCallbacksTotal > 0) rfid.duplicateCallbacksTotal * 100.0 / rfid.rawCallbacksTotal else 0.0
    val warning = buildWarning(sys, rfid, rawRate)
    summary.update(now, elapsedSeconds, sys, rfid, mqtt, rawRate, uniqueRate)
    out.write(csvLine(listOf(
      timestamp(now), elapsedSeconds.toString(), eventType, BuildConfig.VERSION_NAME, activeSession.sessionId,
      fmt(sys.batteryTempC), sys.batteryLevel.toString(), sys.chargingStatus, sys.batteryVoltageMv.toString(), fmt(sys.batteryCurrentMa), sys.thermalStatus, sys.thermalThrottlingLevel.toString(), sys.screenBrightness,
      rfid.inventoryRunning.toString(), rfid.rawCallbacksTotal.toString(), rfid.duplicateCallbacksTotal.toString(), rfid.uniqueEpcsTotal.toString(), fmt(rawRate), fmt(uniqueRate), fmt(duplicatePercent), fmt(rfid.callbackAvgMs), fmt(rfid.callbackMaxMs), rfid.callbackQueueDepth.toString(), fmt(rfid.avgRssi), fmt(rfid.minRssi), fmt(rfid.maxRssi), fmt(rfid.uhfModuleTempC), rfid.rfPowerDbm, rfid.inventorySession, rfid.inventoryTarget, rfid.qValue, rfid.dynamicQEnabled, rfid.antennaState, rfid.readerConnected.toString(), rfid.sdkErrorCount.toString(), rfid.sdkLastError, rfid.sdkWarningCount.toString(),
      mqtt.connected.toString(), mqtt.brokerHost, mqtt.clientId, mqtt.connectionState, tsOrBlank(mqtt.lastConnectTimestamp), tsOrBlank(mqtt.lastDisconnectTimestamp), mqtt.reconnectCount.toString(), mqtt.lastError, mqtt.publishAttemptTotal.toString(), mqtt.publishSuccessTotal.toString(), mqtt.publishFailureTotal.toString(), mqtt.publishPendingQueue.toString(), mqtt.publishRetryQueue.toString(), fmt(mqtt.publishAvgLatencyMs), mqtt.publishMaxLatencyMs.toString(), mqtt.subscribeMessageTotal.toString(), tsOrBlank(mqtt.lastMessageTimestamp), mqtt.inboundQueueDepth.toString(), mqtt.outboundQueueDepth.toString(), mqtt.qos.toString(), mqtt.offlineBufferSize.toString(), mqtt.droppedMessageCount.toString(), mqtt.duplicateMessageCount.toString(),
      rfid.dbPendingQueue.toString(), rfid.dbLastFlushMs.toString(), "0", sys.memoryUsedMb.toString(), sys.memoryFreeMb.toString(), fmt(sys.processCpuPercent), sys.cpuFrequencies, sys.gcCount.toString(), sys.gcTimeMs.toString(), sys.threadCount.toString(), rfid.knownUnreadExpected.toString(), rfid.knownUnreadFound.toString(), warning
    )))
    out.newLine(); out.flush()
    lastSampleMs = now; lastRaw = rfid.rawCallbacksTotal; lastUnique = rfid.uniqueEpcsTotal
  }

  private fun buildWarning(sys: SystemMetrics, rfid: RfidMetrics, rawRate: Double): String {
    val warnings = ArrayList<String>()
    if (sys.batteryTempC >= 40) warnings.add("TEMP_ABOVE_40")
    if (sys.batteryTempC >= 45) warnings.add("TEMP_ABOVE_45")
    if (rfid.dbLastFlushMs >= 500) warnings.add("DB_FLUSH_SLOW")
    if (sys.memoryFreeMb in 0..63) warnings.add("MEMORY_LOW")
    if (summary.durationSeconds >= 300 && summary.peakRawReadsPerSec > 0 && rawRate < summary.peakRawReadsPerSec * 0.5) warnings.add("READ_RATE_DROP")
    return warnings.joinToString("|")
  }

  private fun csvLine(values: List<String>) = values.joinToString(",") { csv(it) }
  private fun csv(value: String): String = if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) value else "\"${value.replace("\"", "\"\"")}\""
  private fun timestamp(ms: Long) = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(ms))
  private fun tsOrBlank(ms: Long) = if (ms > 0) timestamp(ms) else ""
  private fun fmt(value: Double) = if (value < 0 || value.isNaN()) "" else String.format(Locale.US, "%.2f", value)

  class Summary {
    var durationSeconds: Long = 0; var maxBatteryTempC = -1.0; var maxBatteryTempTimestamp = ""; var firstTempAbove40Timestamp = ""; var firstTempAbove45Timestamp = ""; var peakRawReadsPerSec = 0.0; var lowestRawReadsPerSecAfter5Min = -1.0; var peakUniqueReadsPerSec = 0.0; var lowestUniqueReadsPerSecAfter5Min = -1.0; var maxProcessCpuPercent = -1.0; var processCpuTotal = 0.0; var processCpuSamples = 0L; var maxCallbackQueueDepth = 0; var maxUhfModuleTempC = -1.0; var maxBatteryCurrentMa = -1.0; var maxMqttPendingQueue = 0
    fun reset(startedAtMs: Long) { durationSeconds = 0; maxBatteryTempC = -1.0; maxBatteryTempTimestamp = ""; firstTempAbove40Timestamp = ""; firstTempAbove45Timestamp = ""; peakRawReadsPerSec = 0.0; lowestRawReadsPerSecAfter5Min = -1.0; peakUniqueReadsPerSec = 0.0; lowestUniqueReadsPerSecAfter5Min = -1.0; maxProcessCpuPercent = -1.0; processCpuTotal = 0.0; processCpuSamples = 0; maxCallbackQueueDepth = 0; maxUhfModuleTempC = -1.0; maxBatteryCurrentMa = -1.0; maxMqttPendingQueue = 0 }
    fun update(now: Long, elapsed: Long, sys: SystemMetrics, rfid: RfidMetrics, mqtt: MqttMetrics, rawRate: Double, uniqueRate: Double) { durationSeconds = elapsed; if (sys.batteryTempC > maxBatteryTempC) { maxBatteryTempC = sys.batteryTempC; maxBatteryTempTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(now)) }; if (sys.batteryTempC >= 40 && firstTempAbove40Timestamp.isEmpty()) firstTempAbove40Timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(now)); if (sys.batteryTempC >= 45 && firstTempAbove45Timestamp.isEmpty()) firstTempAbove45Timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(now)); if (rawRate > peakRawReadsPerSec) peakRawReadsPerSec = rawRate; if (uniqueRate > peakUniqueReadsPerSec) peakUniqueReadsPerSec = uniqueRate; if (elapsed >= 300) { if (lowestRawReadsPerSecAfter5Min < 0 || rawRate < lowestRawReadsPerSecAfter5Min) lowestRawReadsPerSecAfter5Min = rawRate; if (lowestUniqueReadsPerSecAfter5Min < 0 || uniqueRate < lowestUniqueReadsPerSecAfter5Min) lowestUniqueReadsPerSecAfter5Min = uniqueRate }; if (sys.processCpuPercent >= 0) { processCpuTotal += sys.processCpuPercent; processCpuSamples++; if (sys.processCpuPercent > maxProcessCpuPercent) maxProcessCpuPercent = sys.processCpuPercent }; if (rfid.callbackQueueDepth > maxCallbackQueueDepth) maxCallbackQueueDepth = rfid.callbackQueueDepth; if (rfid.uhfModuleTempC > maxUhfModuleTempC) maxUhfModuleTempC = rfid.uhfModuleTempC; if (sys.batteryCurrentMa > maxBatteryCurrentMa) maxBatteryCurrentMa = sys.batteryCurrentMa; if (mqtt.publishPendingQueue > maxMqttPendingQueue) maxMqttPendingQueue = mqtt.publishPendingQueue }
    fun avgProcessCpuPercent() = if (processCpuSamples > 0) processCpuTotal / processCpuSamples else -1.0
  }
}
