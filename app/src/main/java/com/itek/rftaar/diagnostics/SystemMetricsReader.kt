package com.itek.rftaar.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.max

data class SystemMetrics(
  val batteryTempC: Double,
  val batteryLevel: Int,
  val chargingStatus: String,
  val batteryVoltageMv: Int,
  val batteryCurrentMa: Double,
  val thermalStatus: String,
  val thermalThrottlingLevel: Int,
  val screenBrightness: String,
  val memoryUsedMb: Long,
  val memoryFreeMb: Long,
  val processCpuPercent: Double,
  val cpuFrequencies: String,
  val gcCount: Long,
  val gcTimeMs: Long,
  val threadCount: Int
)

class SystemMetricsReader(private val context: Context) {
  private var lastProcessJiffies = -1L
  private var lastTotalJiffies = -1L
  private var lastElapsedCpuMs = Process.getElapsedCpuTime()
  private var lastElapsedWallMs = SystemClock.elapsedRealtime()

  fun read(): SystemMetrics {
    val battery = readBattery()
    val runtime = Runtime.getRuntime()
    val used = runtime.totalMemory() - runtime.freeMemory()
    val free = runtime.maxMemory() - used
    val thermal = readThermal()
    val gc = readGc()
    return SystemMetrics(
      batteryTempC = battery.tempC,
      batteryLevel = battery.levelPercent,
      chargingStatus = battery.status,
      batteryVoltageMv = battery.voltageMv,
      batteryCurrentMa = battery.currentMa,
      thermalStatus = thermal.first,
      thermalThrottlingLevel = thermal.second,
      screenBrightness = readScreenBrightness(),
      memoryUsedMb = used / (1024L * 1024L),
      memoryFreeMb = free / (1024L * 1024L),
      processCpuPercent = readCpuPercent(),
      cpuFrequencies = readCpuFrequencies(),
      gcCount = gc.first,
      gcTimeMs = gc.second,
      threadCount = readThreadCount()
    )
  }

  private fun readBattery(): BatteryInfo = try {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return BatteryInfo()
    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    BatteryInfo(
      tempC = if (temp == Int.MIN_VALUE) -1.0 else temp / 10.0,
      levelPercent = if (level >= 0 && scale > 0) Math.round(level * 100f / scale) else -1,
      status = chargingStatus(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)),
      voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1),
      currentMa = readBatteryCurrentMa()
    )
  } catch (_: Throwable) { BatteryInfo() }

  private fun readBatteryCurrentMa(): Double = try {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) -1.0 else {
      val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return -1.0
      val ua = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
      if (ua == Int.MIN_VALUE) -1.0 else ua / 1000.0
    }
  } catch (_: Throwable) { -1.0 }

  private fun readThermal(): Pair<String, Int> = try {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "" to -1 else {
      val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return "" to -1
      val status = pm.currentThermalStatus
      thermalName(status) to status
    }
  } catch (_: Throwable) { "" to -1 }

  /**
   * Returns process CPU percent normalized so approximately 100% means one fully used CPU core.
   * Uses /proc deltas first and falls back to Process.getElapsedCpuTime; no shell commands are run.
   */
  private fun readCpuPercent(): Double {
    val process = readProcessJiffies()
    val total = readTotalJiffies()
    var percent = -1.0
    if (process >= 0 && total > 0 && lastProcessJiffies >= 0 && lastTotalJiffies > 0) {
      val pDelta = max(0, process - lastProcessJiffies)
      val tDelta = max(0, total - lastTotalJiffies)
      if (tDelta > 0) percent = pDelta * Runtime.getRuntime().availableProcessors() * 100.0 / tDelta
    }
    if (process >= 0) lastProcessJiffies = process
    if (total > 0) lastTotalJiffies = total
    if (percent >= 0) return percent
    val cpuMs = Process.getElapsedCpuTime()
    val wallMs = SystemClock.elapsedRealtime()
    val cpuDelta = max(0, cpuMs - lastElapsedCpuMs)
    val wallDelta = max(0, wallMs - lastElapsedWallMs)
    lastElapsedCpuMs = cpuMs; lastElapsedWallMs = wallMs
    return if (wallDelta > 0) cpuDelta * 100.0 / wallDelta else -1.0
  }

  private fun readProcessJiffies() = readProcLine("/proc/self/stat") { line ->
    val end = line.lastIndexOf(')')
    if (end < 0) -1 else line.substring(end + 2).trim().split(Regex("\\s+")).let { if (it.size > 12) parseLong(it[11]) + parseLong(it[12]) else -1 }
  }
  private fun readTotalJiffies() = readProcLine("/proc/stat") { line -> if (!line.startsWith("cpu ")) -1 else line.trim().split(Regex("\\s+")).drop(1).sumOf { parseLong(it) } }
  private fun readProcLine(path: String, block: (String) -> Long): Long = try { BufferedReader(FileReader(path)).use { it.readLine()?.let(block) ?: -1 } } catch (_: Throwable) { -1 }
  private fun readCpuFrequencies(): String = try { (0 until Runtime.getRuntime().availableProcessors()).mapNotNull { cpu -> readFirstLine("/sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_cur_freq").takeIf { it.isNotEmpty() }?.let { "cpu$cpu:$it" } }.joinToString("|") } catch (_: Throwable) { "" }
  private fun readGc(): Pair<Long, Long> = try { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) -1L to -1L else Debug.getRuntimeStats().let { parseLong(it["art.gc.gc-count"], -1) to parseLong(it["art.gc.gc-time"], -1) } } catch (_: Throwable) { -1L to -1L }
  private fun readThreadCount(): Int = try { BufferedReader(FileReader("/proc/self/status")).useLines { lines -> lines.firstOrNull { it.startsWith("Threads:") }?.substringAfter(':')?.trim()?.toIntOrNull() ?: -1 } } catch (_: Throwable) { -1 }
  private fun readScreenBrightness(): String = try { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS).toString() } catch (_: Throwable) { "" }
  private fun readFirstLine(path: String) = try { BufferedReader(FileReader(path)).use { it.readLine()?.trim() ?: "" } } catch (_: Throwable) { "" }
  private fun parseLong(value: String?, fallback: Long = 0) = value?.trim()?.toLongOrNull() ?: fallback
  private fun chargingStatus(status: Int) = when (status) { BatteryManager.BATTERY_STATUS_CHARGING -> "CHARGING"; BatteryManager.BATTERY_STATUS_FULL -> "FULL"; BatteryManager.BATTERY_STATUS_DISCHARGING -> "DISCHARGING"; BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NOT_CHARGING"; else -> "" }
  private fun thermalName(status: Int) = when (status) { PowerManager.THERMAL_STATUS_NONE -> "NONE"; PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"; PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"; PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"; PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"; PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"; PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"; else -> "UNKNOWN_$status" }
  private data class BatteryInfo(val tempC: Double = -1.0, val levelPercent: Int = -1, val status: String = "", val voltageMv: Int = -1, val currentMa: Double = -1.0)
}
