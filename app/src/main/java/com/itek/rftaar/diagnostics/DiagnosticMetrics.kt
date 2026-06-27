package com.itek.rftaar.diagnostics

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object RfidDiagnosticTracker {
  private val seenEpcs = ConcurrentHashMap.newKeySet<String>()
  private val rawCallbacks = AtomicLong(0)
  private val duplicateCallbacks = AtomicLong(0)
  private val uniqueEpcs = AtomicLong(0)
  private val callbackTotalNanos = AtomicLong(0)
  private val callbackMaxNanos = AtomicLong(0)
  private val queuedTags = AtomicLong(0)
  private val insertedTags = AtomicLong(0)
  private val dbLastFlushMs = AtomicLong(0)
  private val rssiCount = AtomicLong(0)
  private val rssiTotalMilli = AtomicLong(0)
  private val rssiMinMilli = AtomicLong(Long.MAX_VALUE)
  private val rssiMaxMilli = AtomicLong(Long.MIN_VALUE)
  private val sdkErrors = AtomicLong(0)
  private val sdkWarnings = AtomicLong(0)
  @Volatile var sdkLastError: String = ""
  @Volatile var inventoryRunning: Boolean = false
  @Volatile var readerConnected: Boolean = false
  @Volatile var rfPowerDbm: String = ""
  @Volatile var inventorySession: String = ""
  @Volatile var inventoryTarget: String = ""
  @Volatile var qValue: String = ""
  @Volatile var dynamicQEnabled: String = ""
  @Volatile var antennaState: String = ""
  @Volatile var uhfModuleTempC: Double = -1.0
  @Volatile var knownUnreadExpected: Int = -1
  @Volatile var knownUnreadFound: Int = -1
  @Volatile private var lastCallbackMs: Long = 0

  fun reset() {
    seenEpcs.clear(); rawCallbacks.set(0); duplicateCallbacks.set(0); uniqueEpcs.set(0)
    callbackTotalNanos.set(0); callbackMaxNanos.set(0); queuedTags.set(0); insertedTags.set(0); dbLastFlushMs.set(0)
    rssiCount.set(0); rssiTotalMilli.set(0); rssiMinMilli.set(Long.MAX_VALUE); rssiMaxMilli.set(Long.MIN_VALUE)
    sdkErrors.set(0); sdkWarnings.set(0); sdkLastError = ""; knownUnreadExpected = -1; knownUnreadFound = -1
    inventoryRunning = false; readerConnected = false; rfPowerDbm = ""; inventorySession = ""; inventoryTarget = ""
    qValue = ""; dynamicQEnabled = ""; antennaState = ""; uhfModuleTempC = -1.0; lastCallbackMs = 0
  }

  fun onCallback(epc: String?, rssi: String?, elapsedNanos: Long) {
    rawCallbacks.incrementAndGet()
    lastCallbackMs = System.currentTimeMillis()
    callbackTotalNanos.addAndGet(elapsedNanos.coerceAtLeast(0))
    updateMax(callbackMaxNanos, elapsedNanos.coerceAtLeast(0))
    if (!epc.isNullOrEmpty()) {
      if (seenEpcs.add(epc)) uniqueEpcs.incrementAndGet() else duplicateCallbacks.incrementAndGet()
    }
    rssi?.trim()?.toDoubleOrNull()?.let {
      val milli = (it * 1000.0).toLong()
      rssiCount.incrementAndGet(); rssiTotalMilli.addAndGet(milli); updateMin(rssiMinMilli, milli); updateMax(rssiMaxMilli, milli)
    }
  }

  fun onTagQueued() { queuedTags.incrementAndGet() }
  fun onDbInserted(count: Int, durationMs: Long) { insertedTags.addAndGet(count.toLong()); dbLastFlushMs.set(durationMs.coerceAtLeast(0)) }
  fun onSdkError(message: String) { sdkErrors.incrementAndGet(); sdkLastError = message }
  fun onSdkWarning() { sdkWarnings.incrementAndGet() }
  fun setKnownUnread(expected: Int, found: Int) { knownUnreadExpected = expected; knownUnreadFound = found }

  fun snapshot(): RfidMetrics {
    val raw = rawCallbacks.get()
    val rssiN = rssiCount.get()
    return RfidMetrics(
      inventoryRunning = inventoryRunning,
      rawCallbacksTotal = raw,
      duplicateCallbacksTotal = duplicateCallbacks.get(),
      uniqueEpcsTotal = uniqueEpcs.get(),
      callbackAvgMs = if (raw > 0) callbackTotalNanos.get() / raw / 1_000_000.0 else 0.0,
      callbackMaxMs = callbackMaxNanos.get() / 1_000_000.0,
      callbackQueueDepth = 0,
      avgRssi = if (rssiN > 0) (rssiTotalMilli.get().toDouble() / rssiN) / 1000.0 else Double.NaN,
      minRssi = rssiMinMilli.get().takeIf { it != Long.MAX_VALUE }?.let { it / 1000.0 } ?: Double.NaN,
      maxRssi = rssiMaxMilli.get().takeIf { it != Long.MIN_VALUE }?.let { it / 1000.0 } ?: Double.NaN,
      uhfModuleTempC = uhfModuleTempC,
      rfPowerDbm = rfPowerDbm,
      inventorySession = inventorySession,
      inventoryTarget = inventoryTarget,
      qValue = qValue,
      dynamicQEnabled = dynamicQEnabled,
      antennaState = if (!readerConnected && hasRecentCallbacks()) "ACTIVE_OR_CALLBACKS_RECEIVED" else antennaState,
      readerConnected = readerConnected || hasRecentCallbacks(),
      sdkErrorCount = sdkErrors.get(),
      sdkLastError = sdkLastError,
      sdkWarningCount = sdkWarnings.get(),
      dbPendingQueue = 0,
      dbLastFlushMs = dbLastFlushMs.get(),
      knownUnreadExpected = knownUnreadExpected,
      knownUnreadFound = knownUnreadFound
    )
  }

  private fun hasRecentCallbacks(): Boolean = lastCallbackMs > 0 && System.currentTimeMillis() - lastCallbackMs <= 10_000
  private fun updateMax(target: AtomicLong, value: Long) { while (true) { val old = target.get(); if (value <= old || target.compareAndSet(old, value)) return } }
  private fun updateMin(target: AtomicLong, value: Long) { while (true) { val old = target.get(); if (value >= old || target.compareAndSet(old, value)) return } }
}

object MqttDiagnosticTracker {
  private val publishAttempts = AtomicLong(0)
  private val publishSuccess = AtomicLong(0)
  private val publishFailure = AtomicLong(0)
  private val publishLatencyTotalMs = AtomicLong(0)
  private val publishLatencyMaxMs = AtomicLong(0)
  private val subscribeMessages = AtomicLong(0)
  private val reconnectCount = AtomicLong(0)
  private val droppedMessages = AtomicLong(0)
  private val duplicateMessages = AtomicLong(0)
  @Volatile var connected: Boolean = false
  @Volatile var brokerHost: String = ""
  @Volatile var clientId: String = ""
  @Volatile var connectionState: String = "Disconnected"
  @Volatile var lastConnectTimestamp: Long = 0
  @Volatile var lastDisconnectTimestamp: Long = 0
  @Volatile var lastError: String = ""
  @Volatile var lastMessageTimestamp: Long = 0
  @Volatile var qos: Int = 1

  fun reset() { publishAttempts.set(0); publishSuccess.set(0); publishFailure.set(0); publishLatencyTotalMs.set(0); publishLatencyMaxMs.set(0); subscribeMessages.set(0); reconnectCount.set(0); droppedMessages.set(0); duplicateMessages.set(0); lastError = "" }
  fun onConnected(status: String, broker: String = brokerHost) { connected = true; connectionState = status; brokerHost = broker; lastConnectTimestamp = System.currentTimeMillis() }
  fun onDisconnected(status: String, error: String? = null) { if (connected) reconnectCount.incrementAndGet(); connected = false; connectionState = status; lastDisconnectTimestamp = System.currentTimeMillis(); if (!error.isNullOrEmpty()) lastError = error }
  fun onPublishAttempt() { publishAttempts.incrementAndGet() }
  fun onPublishSuccess(latencyMs: Long) { publishSuccess.incrementAndGet(); publishLatencyTotalMs.addAndGet(latencyMs.coerceAtLeast(0)); updateMax(publishLatencyMaxMs, latencyMs.coerceAtLeast(0)) }
  fun onPublishFailure(error: String?, latencyMs: Long = 0) { publishFailure.incrementAndGet(); if (!error.isNullOrEmpty()) lastError = error; publishLatencyTotalMs.addAndGet(latencyMs.coerceAtLeast(0)); updateMax(publishLatencyMaxMs, latencyMs.coerceAtLeast(0)) }
  fun onMessageArrived() { subscribeMessages.incrementAndGet(); lastMessageTimestamp = System.currentTimeMillis() }
  fun onDroppedMessage() { droppedMessages.incrementAndGet() }
  fun onDuplicateMessage() { duplicateMessages.incrementAndGet() }
  fun snapshot(): MqttMetrics {
    val success = publishSuccess.get()
    return MqttMetrics(connected, brokerHost, clientId, connectionState, lastConnectTimestamp, lastDisconnectTimestamp, reconnectCount.get(), lastError, publishAttempts.get(), success, publishFailure.get(), 0, 0, if (success > 0) publishLatencyTotalMs.get().toDouble() / success else 0.0, publishLatencyMaxMs.get(), subscribeMessages.get(), lastMessageTimestamp, 0, 0, qos, 0, droppedMessages.get(), duplicateMessages.get())
  }
  private fun updateMax(target: AtomicLong, value: Long) { while (true) { val old = target.get(); if (value <= old || target.compareAndSet(old, value)) return } }
}

data class RfidMetrics(val inventoryRunning: Boolean, val rawCallbacksTotal: Long, val duplicateCallbacksTotal: Long, val uniqueEpcsTotal: Long, val callbackAvgMs: Double, val callbackMaxMs: Double, val callbackQueueDepth: Int, val avgRssi: Double, val minRssi: Double, val maxRssi: Double, val uhfModuleTempC: Double, val rfPowerDbm: String, val inventorySession: String, val inventoryTarget: String, val qValue: String, val dynamicQEnabled: String, val antennaState: String, val readerConnected: Boolean, val sdkErrorCount: Long, val sdkLastError: String, val sdkWarningCount: Long, val dbPendingQueue: Int, val dbLastFlushMs: Long, val knownUnreadExpected: Int, val knownUnreadFound: Int)
data class MqttMetrics(val connected: Boolean, val brokerHost: String, val clientId: String, val connectionState: String, val lastConnectTimestamp: Long, val lastDisconnectTimestamp: Long, val reconnectCount: Long, val lastError: String, val publishAttemptTotal: Long, val publishSuccessTotal: Long, val publishFailureTotal: Long, val publishPendingQueue: Int, val publishRetryQueue: Int, val publishAvgLatencyMs: Double, val publishMaxLatencyMs: Long, val subscribeMessageTotal: Long, val lastMessageTimestamp: Long, val inboundQueueDepth: Int, val outboundQueueDepth: Int, val qos: Int, val offlineBufferSize: Int, val droppedMessageCount: Long, val duplicateMessageCount: Long)
