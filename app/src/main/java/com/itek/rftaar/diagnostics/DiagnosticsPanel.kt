package com.itek.rftaar.diagnostics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DiagnosticsPanel(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  var rfid by remember { mutableStateOf(RfidDiagnosticTracker.snapshot()) }
  var mqtt by remember { mutableStateOf(MqttDiagnosticTracker.snapshot()) }
  LaunchedEffect(Unit) {
    while (true) {
      rfid = RfidDiagnosticTracker.snapshot()
      mqtt = MqttDiagnosticTracker.snapshot()
      delay(5_000)
    }
  }
  Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    Text("Session file: ${DiagnosticLogger.latestCsvPath}")
    Text("Inventory running: ${rfid.inventoryRunning}")
    Text("Raw callbacks: ${rfid.rawCallbacksTotal}")
    Text("Unique EPCs: ${rfid.uniqueEpcsTotal}")
    Text("Duplicates: ${rfid.duplicateCallbacksTotal}")
    Text("Callback avg/max ms: ${rfid.callbackAvgMs}/${rfid.callbackMaxMs}")
    Text("RSSI avg/min/max: ${rfid.avgRssi}/${rfid.minRssi}/${rfid.maxRssi}")
    Text("SDK errors/warnings: ${rfid.sdkErrorCount}/${rfid.sdkWarningCount}")
    Text("MQTT connected: ${mqtt.connected}")
    Text("MQTT reconnects: ${mqtt.reconnectCount}")
    Text("MQTT publish success/failure: ${mqtt.publishSuccessTotal}/${mqtt.publishFailureTotal}")
    Text("MQTT pending queue: ${mqtt.publishPendingQueue}")
    Text("MQTT last error: ${mqtt.lastError}")
    Spacer(Modifier.height(8.dp))
    Button(onClick = { DiagnosticShare.shareLatest(context) }) { Text("Share latest diagnostics") }
  }
}
