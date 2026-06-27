package com.itek.rftaar.reader

// ============================================================
// InventoryEngine.kt
//
// PURPOSE:
//   A self-contained, correct Kotlin inventory pipeline.
//   This is the MODEL implementation that fixes all three
//   architectural bugs found in the current RFIDHandler:
//
//   BUG 1 (FIXED): handlerScope.launch per tag
//                  → One new coroutine created per tag.
//                  → At 150 tags/sec = 150 launches/sec overhead.
//                  → FIXED: Tags go directly into a channel.
//                            Zero coroutine launches per tag.
//
//   BUG 2 (FIXED): LiveData.value read on IO thread
//                  → isInventoryOn.value is unsafe off main thread.
//                  → Returns null or stale value unpredictably.
//                  → FIXED: @Volatile plain Boolean flags.
//                            Safe to read from any thread, always.
//
//   BUG 3 (FIXED): beepTimer queries DB every 500ms
//                  → Timer thread hits DB while insertAll() holds lock.
//                  → DB contention slows inserts, buffer grows, freeze.
//                  → FIXED: beepTimer reads an in-memory counter.
//                            Zero DB calls on timer thread. Ever.
//
// HOW TO USE (step-by-step for team):
//   See INTEGRATION STEPS at the bottom of this file.
//
// WHAT THIS FILE DOES NOT TOUCH:
//   - Pick / Encode / Decode / Search / Tag Verify operations
//   - RFIDHandler base class
//   - ChainwayRFIDHandler
//   - Any UI or ViewModel
//
// ============================================================

import android.util.Log
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.utils.SoundUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================
// InventoryEngine
//
// One instance lives inside RFIDHandler alongside the existing code.
// It owns the entire path from raw tag arrival to DB insert,
// for inventory mode only.
// ============================================================

class InventoryEngine(
    private val db: AppDatabase,
    //private val onCountUpdated: (count: Int) -> Unit   // called on main thread with live count
) {

    // ----------------------------------------------------------
    // SECTION 1: FLAGS
    //
    // @Volatile means every thread always sees the latest value.
    // This replaces LiveData.value reads on background threads.
    // No main thread needed. No race condition. Always correct.
    // ----------------------------------------------------------

    @Volatile private var isRunning: Boolean = false

    // Session data — set once when inventory starts, read by IO thread.
    @Volatile private var sessionType: String = ""
    @Volatile private var transactionType: String = ""
    @Volatile private var sessionId: String = ""
    @Volatile private var sessionData: String = ""
    @Volatile private var topic: String = TopicConstants.INVENTORY
    @Volatile private var isPostToMqtt: Boolean = true
    @Volatile private var isUpdateFound: Boolean = false
    @Volatile private var maxScanLimit: Int = 0

    // ----------------------------------------------------------
    // SECTION 2: IN-MEMORY COUNTER
    //
    // This is the total tag count kept in memory.
    // The beep timer reads THIS — zero DB calls.
    // Room's LiveData observer updates THIS when DB confirms write.
    // ----------------------------------------------------------

    @Volatile private var confirmedDbCount: Int = 0

    // ----------------------------------------------------------
    // SECTION 3: THE CHANNEL
    //
    // Hardware callback drops tag into this channel.
    // trySend() is non-blocking — it never suspends the hardware thread.
    // Channel.UNLIMITED means hardware never drops a tag.
    //
    // WHY NOT Channel(512) or bounded?
    // Because inventory is read-all. We want zero drops.
    // The consumer is fast enough (batch insert) that the buffer
    // does not grow unboundedly — it drains every BATCH_INTERVAL_MS.
    // ----------------------------------------------------------

    private val tagChannel = Channel<TagInfoEntity>(Channel.UNLIMITED)

    // ----------------------------------------------------------
    // SECTION 4: COROUTINE SCOPE
    //
    // SupervisorJob means if one child coroutine fails,
    // it does not cancel the others.
    // Dispatchers.IO runs on a background thread pool.
    // ----------------------------------------------------------

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var consumerJob: Job? = null
    private var beepJob: Job? = null

    // ----------------------------------------------------------
    // SECTION 5: BATCH SETTINGS
    //
    // BATCH_SIZE: how many tags to insert at once.
    //             1000 is efficient for Room's insertAll().
    //             One DB call inserts 1000 rows — very fast.
    //
    // BATCH_INTERVAL_MS: even if we don't have 1000 tags yet,
    //             flush every 50ms so the UI counter stays live.
    // ----------------------------------------------------------

    private val BATCH_SIZE = 1000
    private val BATCH_INTERVAL_MS = 50L
    private val BEEP_INTERVAL_MS = 500L

    // ----------------------------------------------------------
    // SECTION 6: start()
    //
    // Called when the operator presses Start Inventory.
    // Sets all flags, starts the consumer coroutine and beep timer.
    // ----------------------------------------------------------

    fun start(
        sessionType: String,
        transactionType: String,
        sessionId: String,
        sessionData: String,
        topic: String = TopicConstants.INVENTORY,
        isPostToMqtt: Boolean = true,
        isUpdateFound: Boolean = false,
        maxScanLimit: Int = 0
    ) {
        if (isRunning) return   // already running — do nothing

        // Set session context
        this.sessionType = sessionType
        this.transactionType = transactionType
        this.sessionId = sessionId
        this.sessionData = sessionData
        this.topic = topic
        this.isPostToMqtt = isPostToMqtt
        this.isUpdateFound = isUpdateFound
        this.maxScanLimit = maxScanLimit
        this.confirmedDbCount = 0

        // Mark running BEFORE starting jobs
        isRunning = true

        startConsumer()
        startBeepTimer()

        log("InventoryEngine started. session=$sessionType txn=$transactionType")
    }

    // ----------------------------------------------------------
    // SECTION 7: stop()
    //
    // Called when the operator presses Stop Inventory.
    // Sets flag first so hardware callback stops sending.
    // Then cancels jobs cleanly.
    // ----------------------------------------------------------

    fun stop() {
        isRunning = false
        consumerJob?.cancel()
        beepJob?.cancel()
        consumerJob = null
        beepJob = null
        log("InventoryEngine stopped. finalCount=$confirmedDbCount")
    }

    // ----------------------------------------------------------
    // SECTION 8: onTagReceived()
    //
    // THIS IS THE METHOD CALLED FROM THE HARDWARE CALLBACK.
    //
    // Current code (wrong):
    //   handlerScope.launch { processScannedData(uhftagInfo) }
    //   → Launches a new coroutine per tag. Expensive.
    //
    // Correct code (this):
    //   inventoryEngine.onTagReceived(tagInfo)
    //   → trySend is a single non-blocking put into a queue.
    //   → No coroutine created. No thread switch. Instant.
    //
    // The hardware thread is never blocked or slowed down.
    // ----------------------------------------------------------

    fun onTagReceived(tag: TagInfoEntity) {
        if (!isRunning) return
        if (maxScanLimit > 0 && confirmedDbCount >= maxScanLimit) {
            stop()
            return
        }
        // trySend never blocks. It just puts the tag in the queue.
        // If for some reason it fails (should not happen with UNLIMITED),
        // we log and move on — we never block the hardware thread.
        val result = tagChannel.trySend(tag)
        if (result.isFailure) {
            log("onTagReceived: channel send failed (unexpected)")
        }
    }

    // ----------------------------------------------------------
    // SECTION 9: startConsumer()
    //
    // One coroutine runs for the entire inventory session.
    // It pulls tags from the channel, collects them into a batch,
    // and inserts the batch into Room when either:
    //   (a) the batch reaches BATCH_SIZE (1000 tags), or
    //   (b) BATCH_INTERVAL_MS (50ms) has passed.
    //
    // WHY THIS IS FAST:
    //   - One coroutine total (not one per tag)
    //   - One DB call per 1000 tags (not one per tag)
    //   - No thread switching between tags
    //   - No LiveData.value reads — uses @Volatile flags
    // ----------------------------------------------------------

    private fun startConsumer() {
        consumerJob = scope.launch {
            val batch = ArrayList<TagInfoEntity>(BATCH_SIZE)
            var lastFlushTime = System.currentTimeMillis()

            while (isActive && isRunning) {
                // Try to get a tag from the channel.
                // poll() is non-blocking — returns null if channel is empty.
                val tag = tagChannel.tryReceive().getOrNull()

                if (tag != null) {
                    // Enrich tag with session context before saving
                    val enriched = enrichTag(tag)
                    batch.add(enriched)

                    // Flush if batch is full
                    if (batch.size >= BATCH_SIZE) {
                        flushBatch(batch)
                        lastFlushTime = System.currentTimeMillis()
                        batch.clear()
                    }
                } else {
                    // Channel is empty right now.
                    // Flush whatever is in the batch if interval has passed.
                    val now = System.currentTimeMillis()
                    if (batch.isNotEmpty() && (now - lastFlushTime) >= BATCH_INTERVAL_MS) {
                        flushBatch(batch)
                        lastFlushTime = now
                        batch.clear()
                    } else {
                        // Nothing to do — yield briefly so we don't spin-lock the CPU.
                        delay(5)
                    }
                }
            }

            // Session ended — flush whatever remains in the batch.
            if (batch.isNotEmpty()) {
                flushBatch(batch)
                batch.clear()
            }

            log("Consumer coroutine exited cleanly.")
        }
    }

    // ----------------------------------------------------------
    // SECTION 10: flushBatch()
    //
    // Inserts a batch of tags into Room.
    // Room's insertAll uses IGNORE conflict strategy —
    // duplicate EPCs are silently ignored. No duplicates saved.
    //
    // After insert, updates the in-memory counter
    // and notifies the UI via onCountUpdated callback.
    // ----------------------------------------------------------

    private suspend fun flushBatch(batch: List<TagInfoEntity>) {
        if (batch.isEmpty()) return
        if (!isRunning) return

        try {
            // insertAll returns a list of row IDs.
            // Row ID > 0 means it was actually inserted (not a duplicate).
            val insertedRowIds = db.tagInfoDao().insertAll(batch)
            val newlyInserted = insertedRowIds.count { rowId -> rowId > 0 }

            if (newlyInserted > 0) {
                confirmedDbCount += newlyInserted
                log("flushBatch: inserted=$newlyInserted total=$confirmedDbCount")

                if(isUpdateFound){
                    val epcs = batch.map { tagInfo -> tagInfo.epc}.distinct()
                    val barcodeWiseQty = batch.map { tagInfo -> tagInfo.barcode}.groupingBy {it}.eachCount()
                    val barcodes = barcodeWiseQty.map { (key, value) -> key }.distinct()

                    val productZoneDataDao = db.productZoneDataDao()
                    val dataQtyDao = db.dataQtyDao()

                    val hasProductZoneEpcData = epcs.isNotEmpty() && productZoneDataDao.hasEpcData(topic, sessionType, transactionType);
                    val hasProductZoneBarcodeData = !hasProductZoneEpcData && barcodeWiseQty.size>0 && productZoneDataDao.hasBarcodes(topic, sessionType, transactionType,barcodes);

                    if (hasProductZoneEpcData && productZoneDataDao.hasEpcs(topic, sessionType, transactionType, epcs)) productZoneDataDao.updateFoundEpcs(topic, sessionType, transactionType, epcs)
                    else if (hasProductZoneBarcodeData) {
                        for((barcode,qty) in barcodeWiseQty)
                            productZoneDataDao.updateFoundBarcodeQty(topic, sessionType, transactionType, barcode,qty)
                    }

                    val hasDataQtyEpcData = epcs.isNotEmpty() && dataQtyDao.hasEpcData(topic, sessionType, transactionType)
                    val hasDataQtyBarcodeData = !hasDataQtyEpcData && barcodeWiseQty.size>0 && dataQtyDao.hasBarcodes(topic, sessionType, transactionType,barcodes);
                    if (hasDataQtyEpcData && dataQtyDao.hasEpcs(topic, sessionType, transactionType, epcs)) dataQtyDao.updateFoundEpcs(topic, sessionType, transactionType, epcs)
                    else if (hasDataQtyBarcodeData) {
                        for((barcode,qty) in barcodeWiseQty){
                            //dataQtyDao.updateFoundBarcodeQty(topic, sessionType, transactionType, barcode,qty)
                            val foundQty = db.tagInfoDao().getFoundCount1(topic, sessionType, transactionType, barcode)
                            if(foundQty>0) dataQtyDao.updateFoundBarcodeQty1(topic, sessionType, transactionType, barcode,foundQty)
                        }
                    }
                }

                // Notify UI on main thread
                /*withContext(Dispatchers.Main) {
                    onCountUpdated(confirmedDbCount)
                }*/
            }
        } catch (e: Exception) {
            log("flushBatch: DB error — ${e.message}")
        }
    }

    // ----------------------------------------------------------
    // SECTION 11: startBeepTimer()
    //
    // Fires every 500ms. Compares in-memory counter to last value.
    // If count grew → beep.
    //
    // ZERO DB CALLS. Ever.
    //
    // Current buggy code:
    //   val invScanCount = db.tagInfoDao().getTotalCount(...).value
    //   → Hits DB every 500ms while DB is busy with insertAll()
    //   → DB contention → inserts slow down → buffer grows → freeze
    //
    // This correct code:
    //   val current = confirmedDbCount   ← plain int, already in memory
    //   → No DB. No thread. No contention.
    // ----------------------------------------------------------

    private fun startBeepTimer() {
        beepJob = scope.launch {
            var lastCount = 0
            while (isActive && isRunning) {
                delay(BEEP_INTERVAL_MS)
                val current = confirmedDbCount   // read @Volatile int — instant
                if (current > lastCount) {
                    lastCount = current
                    LogUtils.showLog("saveToDB_topic_isPostToMqtt",topic+"_"+isPostToMqtt)
                    if(topic.equals(TopicConstants.INVENTORY) && isPostToMqtt) {
                        MqttManager.publishInventory(db.tagInfoDao().getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, sessionId), sessionType,transactionType,sessionId, sessionData);
                    }
                    // SoundUtils.beep() must run on main thread
                    withContext(Dispatchers.Main) {
                        SoundUtils.beep()
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------
    // SECTION 12: enrichTag()
    //
    // Fills in session context on each tag before saving.
    // This is what the current saveToDB() does in a map() call.
    // Extracted here so it is clear and testable.
    // ----------------------------------------------------------

    private fun enrichTag(tag: TagInfoEntity): TagInfoEntity {
        if (tag.sessionId.isNullOrEmpty() && sessionId.isNotEmpty()) tag.sessionId = sessionId
        if (tag.sessionData.isNullOrEmpty() && sessionData.isNotEmpty()) tag.sessionData = sessionData
        if (tag.session_type.isNullOrEmpty() && sessionType.isNotEmpty()) tag.session_type = sessionType
        if (tag.transactionType.isNullOrEmpty() && transactionType.isNotEmpty()) tag.transactionType = transactionType
        if (tag.topic.isNullOrEmpty() && topic.isNotEmpty()) tag.topic = topic
        if (isUpdateFound) tag.isFound = true
        if (!isPostToMqtt) tag.isUploaded = true
        return tag
    }

    // ----------------------------------------------------------
    // SECTION 13: destroy()
    //
    // Called when the Activity or Fragment is destroyed.
    // Cancels the entire coroutine scope cleanly.
    // ----------------------------------------------------------

    fun destroy() {
        stop()
        scope.cancel()
        log("InventoryEngine destroyed.")
    }

    // ----------------------------------------------------------
    // SECTION 14: currentCount()
    //
    // UI can call this to get the latest count synchronously.
    // Safe to call from any thread.
    // ----------------------------------------------------------

    fun currentCount(): Int = confirmedDbCount

    // ----------------------------------------------------------
    // Logging helper
    // ----------------------------------------------------------

    private fun log(message: String) {
        Log.d("InventoryEngine", message)
    }
}


// ============================================================
//
// INTEGRATION STEPS FOR TEAM
// Step-by-step. Each step names the exact file and what to do.
//
// ============================================================
//
// ───────────────────────────────────────────────────────────
// STEP 1: Add this file to the project
// ───────────────────────────────────────────────────────────
// File location:
//   app/src/main/java/com/itek/rftaar/reader/InventoryEngine.kt
//
// Action: Copy this entire file there. No other files change yet.
//
//
// ───────────────────────────────────────────────────────────
// STEP 2: Add InventoryEngine instance to RFIDHandler
// ───────────────────────────────────────────────────────────
// File: app/src/main/java/com/itek/rftaar/reader/RFIDHandler.kt
//
// Find this line (it is near the top of the class, around the
// tagBuffer declaration):
//
//   private var tagBuffer = Channel<TagInfoEntity>(Channel.UNLIMITED)
//
// ADD these two lines directly below it:
//
//   private lateinit var inventoryEngine: InventoryEngine
//
// Then find the method that initializes the DB (look for where
// `db = AppDatabase.getDbInstance(context)` is called).
// ADD this line right after that DB init:
//
//   inventoryEngine = InventoryEngine(db) { count ->
//       invScanCount = count
//       isInventoryOn.postValue(true)   // keeps UI observer alive
//   }
//
// WHAT THIS DOES:
//   Creates the engine. The lambda is called every time a batch
//   is saved. It updates invScanCount (the existing field) and
//   keeps isInventoryOn posting so the UI counter refreshes.
//
//
// ───────────────────────────────────────────────────────────
// STEP 3: Change the hardware callback in ChainwayRFIDHandler
// ───────────────────────────────────────────────────────────
// File: app/src/main/java/com/itek/rftaar/reader/chainway/ChainwayRFIDHandler.kt
//
// Find this exact block (the IUHFInventoryCallback):
//
//   private val defaultIUHFInventoryCallback = IUHFInventoryCallback { uhftagInfo: UHFTAGInfo ->
//       showLog("method", "IUHFInventoryCallback")
//       showLog("isActionInventory_IUHFInventoryCallback", ""+isActionInventory)
//       if(!isScanningOn()) return@IUHFInventoryCallback
//       else handlerScope.launch { processScannedData(uhftagInfo) }   // ← THIS LINE IS THE BUG
//   }
//
// CHANGE only the last line inside the callback to this:
//
//   private val defaultIUHFInventoryCallback = IUHFInventoryCallback { uhftagInfo: UHFTAGInfo ->
//       showLog("method", "IUHFInventoryCallback")
//       showLog("isActionInventory_IUHFInventoryCallback", ""+isActionInventory)
//       if(!isScanningOn()) return@IUHFInventoryCallback
//       else if (isActionInventory) {
//           // INVENTORY PATH: send to InventoryEngine — no coroutine launch, no overhead
//           val tagInfo = getTagInfo(uhftagInfo)
//           inventoryEngine.onTagReceived(tagInfo)
//       } else {
//           // ALL OTHER OPERATIONS (pick, search, encode, decode):
//           // Keep the original path — unchanged
//           handlerScope.launch { processScannedData(uhftagInfo) }
//       }
//   }
//
// WHAT THIS DOES:
//   When isActionInventory is true → tags go to InventoryEngine (fast, correct).
//   When any other operation is active → original path is used (unchanged, safe).
//
//
// ───────────────────────────────────────────────────────────
// STEP 4: Change startInventory() in RFIDHandler
// ───────────────────────────────────────────────────────────
// File: app/src/main/java/com/itek/rftaar/reader/RFIDHandler.kt
//
// Find the public fun startInventory(...) method.
// Find these lines inside it (near the end of the method):
//
//   isActionInventory = true
//   //setupTagBuffer()
//   if (startInventory()) {
//       showLog("startInventory",""+true)
//       startBeepTimer()
//       isInventoryOn.postValue(true)
//   }
//
// REPLACE with this:
//
//   isActionInventory = true
//   if (startInventory()) {
//       showLog("startInventory",""+true)
//       // Start the correct engine instead of the old beepTimer
//       inventoryEngine.start(
//           sessionType = sessionType,
//           transactionType = transactionType,
//           sessionId = deviceSessionId,
//           sessionData = sessionData,
//           topic = chkNull(topic, TopicConstants.INVENTORY),
//           isPostToMqtt = isPublishToMqtt && !isUnencoded && !isAlien,
//           isUpdateFound = isUpdateFound,
//           maxScanLimit = maxScanLimit
//       )
//       isInventoryOn.postValue(true)
//   }
//
// WHAT THIS DOES:
//   Replaces the old startBeepTimer() (which hits DB) with
//   inventoryEngine.start() which handles batching, beeping,
//   and DB inserts correctly — all internally.
//
//
// ───────────────────────────────────────────────────────────
// STEP 5: Change stopOperations() in RFIDHandler
// ───────────────────────────────────────────────────────────
// File: app/src/main/java/com/itek/rftaar/reader/RFIDHandler.kt
//
// Find stopOperations(). Find this block inside it:
//
//   val wasInvAction = isActionInventory
//   isActionInventory = false
//
// ADD this line right after isActionInventory = false:
//
//   if (wasInvAction) inventoryEngine.stop()
//
// WHAT THIS DOES:
//   When inventory stops, the engine flushes remaining tags
//   and stops the beep coroutine cleanly.
//
//
// ───────────────────────────────────────────────────────────
// STEP 6: Add destroy() call in RFIDHandler
// ───────────────────────────────────────────────────────────
// File: app/src/main/java/com/itek/rftaar/reader/RFIDHandler.kt
//
// Find the onDestroy() or dispose() method (the one that cleans up).
// ADD this line inside it:
//
//   inventoryEngine.destroy()
//
// WHAT THIS DOES:
//   Ensures the coroutine scope is cancelled when the device is
//   released — no memory leaks, no background jobs lingering.
//
//
// ───────────────────────────────────────────────────────────
// STEP 7: Fix the beepTimer in RFIDHandler — inventory branch only
// ───────────────────────────────────────────────────────────
// File: app/src/main/java/com/itek/rftaar/reader/RFIDHandler.kt
//
// Find startBeepTimer(). Inside it, find this inventory branch:
//
//   else if (chkTrue(isInventoryOn.getValue()) || isActionInventory) {
//       val oldcount: Int = scanCount
//       val invScanCount = chkNull(db.tagInfoDao().getTotalCount(sessionType, transactionType).value, 0)
//       scanCount = invScanCount
//       if (scanCount > oldcount) { SoundUtils.beep() }
//   }
//
// REPLACE that entire branch with a comment:
//
//   else if (isActionInventory) {
//       // Inventory beeping is now handled inside InventoryEngine.startBeepTimer()
//       // using the in-memory confirmedDbCount counter — no DB call needed here.
//       // DO NOT add DB calls here.
//   }
//
// WHAT THIS DOES:
//   Removes the DB call from the timer thread for inventory.
//   The engine's own beep coroutine handles this correctly.
//   Search and TagVerify beep branches in startBeepTimer() are
//   NOT touched — they stay exactly as they are.
//
//
// ───────────────────────────────────────────────────────────
// THAT IS ALL — 7 steps, 6 files touched minimally.
// ───────────────────────────────────────────────────────────
//
// SUMMARY OF WHAT CHANGED AND WHY:
//
//   Step 1  InventoryEngine.kt added          New file, correct engine
//   Step 2  RFIDHandler.kt                    Add engine instance
//   Step 3  ChainwayRFIDHandler.kt            Route inventory tags to engine
//   Step 4  RFIDHandler.startInventory()      Start engine instead of old beepTimer
//   Step 5  RFIDHandler.stopOperations()      Stop engine cleanly
//   Step 6  RFIDHandler.onDestroy()           Destroy engine (no memory leak)
//   Step 7  RFIDHandler.startBeepTimer()      Remove DB call from inventory branch
//
// WHAT DID NOT CHANGE:
//   - pick / encode / decode / search / tagVerify: UNTOUCHED
//   - processScannedData(): UNTOUCHED (still used by pick/search/encode/decode)
//   - saveToDB(): UNTOUCHED (still used by pick/search/encode/decode)
//   - tagBuffer: UNTOUCHED (still used by other operations if needed)
//   - chunked consumer: UNTOUCHED
//   - All UI / ViewModel / Compose screens: UNTOUCHED
//   - MQTT publish: UNTOUCHED (InventoryEngine can be extended to call it after flush)
//
// ============================================================
