package com.itek.rftaar.reader

import android.bluetooth.BluetoothAdapter
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.R
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.mqtt.MqttManager.publishInventory
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.reader.epcwrapper.EpcEncoderDecoderWrapper
import com.itek.rftaar.reader.epcwrapper.constants.BarcodeConstants
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.CommonUtils.isNonEmpty
import com.itek.rftaar.utils.CommonUtils.isNullOrEmpty
import com.itek.rftaar.utils.SoundUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import kotlin.concurrent.fixedRateTimer
import kotlin.math.abs

abstract class RFIDHandler {
  protected lateinit var context: CommonActivity

  //var listener: RFIDListener? = null
  private lateinit var db: AppDatabase
  protected var mBluetoothAdapter: BluetoothAdapter? = null
  //private lateinit var inventoryEngine: InventoryEngine
  protected val isBluetoothReader = false
  protected var restrictTriggerPress = false

  //Pick
  private val defaultPickPower: Int = 7
  private val defaultPickTimeInMilliis: Long = 1000
  protected var pickPower: Int = defaultPickPower
  protected var pickedEpcs: ArrayList<String> = ArrayList(0)
  protected var pickedTids: ArrayList<String> = ArrayList(0)
  private var isDecodeOnPick: Boolean = false
  private var isPostPicked: Boolean = false
  private var isSavePickedToDB: Boolean = false
  private var isAllowNonEncodedTags: Boolean = false
  private var isAllowDuplicateTagRePick: Boolean = false

  //TODO directly use buffer & fixRateTimer to check
  private var tagBuffer: Channel<TagInfoEntity>? = null
  //protected var tagBuffer = Channel<TagInfoEntity>(Channel.UNLIMITED)//1000)
  //tagWriteRetryLimit
  val tagWriteRetryLimit: Int = 2

  //Power
  val MIN_POWER_TO_SET = 5
  val MAX_POWER_TO_SET = 30

  //Beeper
  val BEEP_DELAY_TIME_MIN: Int = 0
  val BEEP_DELAY_TIME_MAX: Int = 300
  private var beepTimer: Timer? = null;
  //private var dbUpdateTimer: Timer? = null;
  //val buffer = ArrayList<TagInfoEntity>(5000)

  //Command Flags (Reading Tag Data)
  protected var readTid: Boolean = false
  protected var readRssi: Boolean = false
  protected var readPC: Boolean = false
  protected var readUser: Boolean = false
  protected var readType: Boolean = false
  protected var readEAN: Boolean = false

  //Inputs For Inventory
  private var listEans: List<String> = emptyList()
  private var listEpcs: List<String> = emptyList()
  private var listExcludedEans: List<String> = emptyList()
  private var listExcludedEpcs: List<String> = emptyList()
  private var isPublishToMqtt: Boolean = false
  private var isUpdateFound: Boolean = false
  private var isUnencoded: Boolean = false
  private var isAlien: Boolean = false
  protected var isMaxScanLimitReached = false
  protected var maxScanLimit = 0
  protected var scanCount = 0
  protected var invScanCount = 0
  //protected var inwScanCount = 0
  protected var verifyCount = 0
  protected var foundCount = 0
  protected var topic: String = ""
  protected var sessionType: String = ""

  //Extra Session Data
  protected var transactionType = ""
  protected var deviceSessionId = ""
  protected var sessionData = ""
  protected var userRemark = ""
  protected var decodeType = ""
  /*protected var zoneName = ""
  protected var zonePath = ""*/

  //Command Flags Search
  protected var isLockSearchEPC: Boolean = false
  protected var isActionSearch: Boolean = false
  protected var isActionEPCSearch: Boolean = false
  protected var isActionTIDSearch: Boolean = false

  //Inputs for Search
  protected var counter_for_threshold_percentage_to_sound_beep: Int = 0
  private val SOUND_THRESHOLD = 8
  protected var searchLockedEpc: String = ""
  protected var searchBarcode: String = ""
  protected var pickBarcode: String = ""
  protected var searchEpc: String = ""
  protected var searchTid: String = ""
  protected var scannedTids: ArrayList<String> = ArrayList(0)
  //protected var percent = 0.0f
  protected var listSearchPercent = CopyOnWriteArrayList<Float>()

  protected var phase = ""
  protected var rssi = ""

  //Command Flags Pick
  protected var isRePick: Boolean = false
  protected var isActionPick: Boolean = false
  protected var isActionTidPick: Boolean = false
  protected var isSinglePick: Boolean = true;//AppCommonMethods.isSinglePick

  //HashSets for Processing Pick
  protected var pickTags: HashSet<String> = HashSet(0)
  protected var pickTagData: Any = Any()

  //Inventory
  @Volatile
  protected var isActionInventory: Boolean = false
  @Volatile
  private var confirmedDbCount: Int = 0

  //Tag Verify
  protected var isActionTagVerify: Boolean = false

  //pickTimer
  private var pickCountDownTime: Long = defaultPickTimeInMilliis
  private var pickTimer: Timer? = null;
  /*private val pickTimerTask = object : TimerTask() {
    override fun run() {
      onPickTimerFinish();
    }
  }*/

  //Command Flags Encoding
  protected var multiWriteListSize: Int = 0
  protected var multiWriteCount: Int = 0
  protected var isMultiWriteDone: Boolean = false
  protected var multiWriteSuccessCount: Int = 0
  protected val handlerScope = CoroutineScope(Dispatchers.IO)



  //tids & password
  protected val NON_PASSWORD_TIDS: List<String> = mutableListOf("E2806995", "E2801160", "E2801190")
  protected val NON_128_BIT_TIDS: List<String> = mutableListOf("E2806995", "E2801171", "E2801160", "E2801190")
  protected val defaultTagZeroPassword: String = "00000000"

  //Mutable values to check if a process/operation is running
  var isReaderSet: MutableLiveData<Boolean?> = MutableLiveData(false)
  var isDeviceConfigured: MutableLiveData<Boolean> = MutableLiveData(false)
  var isSessionOn: MutableLiveData<Boolean> = MutableLiveData(false)
  var isInventoryOn: MutableLiveData<Boolean> = MutableLiveData(false)
  var isTagVerifyOn: MutableLiveData<Boolean> = MutableLiveData(false)
  var isSearchOn: MutableLiveData<Boolean> = MutableLiveData(false)
  var isPickOn: MutableLiveData<Boolean> = MutableLiveData(false)
  var isTagWriteOn: MutableLiveData<Boolean> = MutableLiveData(false)

  //temp code (for testing)
  var searchRssi: MutableLiveData<String> = MutableLiveData("")
  var searchPercent: MutableLiveData<Float> = MutableLiveData(0.0f)
  var searchPhase: MutableLiveData<String> = MutableLiveData("")

  var isTagWriteDone: MutableLiveData<Boolean> = MutableLiveData(false)

  var pickData: MutableLiveData<TagInfoEntity?> = MutableLiveData(null)
  var pickedListData: MutableLiveData<List<TagInfoEntity>?> = MutableLiveData<List<TagInfoEntity>?>(null)
  var isTriggerPressed: MutableLiveData<Boolean> = MutableLiveData(false)

  var readerPower: MutableLiveData<Int> = MutableLiveData(30)
  var readTagPassword: MutableLiveData<String> = MutableLiveData("")

  //Success
  var successMsg: MutableLiveData<String> = MutableLiveData("")

  //Error
  var errMsg: MutableLiveData<String> = MutableLiveData("")

  //var isAllVerified: Boolean = false
  //var invScanCount: Int = 0
  //var tagVerifyCount: Int = 0
  //var itemFoundCount: Int = 0



    /**init {
    handlerScope.launch(Dispatchers.IO) {

    val buffer = mutableListOf<TagInfoEntity>()

    while (isActive) {

    val tag = try {
    val immediate = tagBuffer.tryReceive().getOrNull()

    if (immediate != null) {
    immediate
    } else if (buffer.isNotEmpty()) {
    // 👇 check AGAIN before waiting
    val secondCheck = tagBuffer.tryReceive().getOrNull()
    if (secondCheck != null) {
    secondCheck
    } else {
    null // ✅ directly return null → triggers immediate flush
    }
    } else {
    null
    }
    } catch (e: Exception) {
    null
    }

    if (tag != null) {
    buffer.add(tag)
    }

    if (buffer.size >= 500 || (buffer.isNotEmpty() && tag == null)) {

    val tags = buffer.toList()
    buffer.clear()

    try {
    tags.forEach { tagInfo ->
    tagInfo.insertTime = getCurrentTime()

    if (tagInfo.sessionId.isNullOrEmpty() && deviceSessionId.isNotEmpty())
    tagInfo.sessionId = deviceSessionId

    if (tagInfo.sessionData.isNullOrEmpty() && sessionData.isNotEmpty())
    tagInfo.sessionData = sessionData

    if (tagInfo.session_type.isNullOrEmpty() && sessionType.isNotEmpty())
    tagInfo.session_type = sessionType

    if (tagInfo.transactionType.isNullOrEmpty() && transactionType.isNotEmpty())
    tagInfo.transactionType = transactionType

    if (tagInfo.topic.isNullOrEmpty() && topic.isNotEmpty())
    tagInfo.topic = topic

    if (isUpdateFound) tagInfo.isFound = true

    if (!(isPublishToMqtt && !isUnencoded && !isAlien))
    tagInfo.isUploaded = true
    }

    db.tagInfoDao().insertAll(tags)

    } catch (e: Exception) {
    LogUtils.showLog("DB_ERROR", "Insert failed", e)
    }
    }
    }
    }
    }*/
  init {
      //setupTagBuffer()
    //repeat(16) {
        /*handlerScope.launch {
            *//*for (uhftagInfo in tagBuffer) {
                showLog("method", "tagBuffer.consumeAsFlow_")
                if(isActionInventory && chkTrue(isInventoryOn.value)) saveToDB(uhftagInfo,topic,true)
                //processScannedData(uhftagInfo)
            }*//*

            tagBuffer.consumeAsFlow().chunked(100)
            .collect { tags -> showLog("method","tagBuffer.consumeAsFlow_"+tags.size)
            showLog("tagBuffer.consumeAsFlow_saveToDB",""+isActionInventory)
            if(isActionInventory && chkTrue(isInventoryOn.value)) saveToDB(tags, chkNull(topic, TopicConstants.INVENTORY), isPublishToMqtt && !isUnencoded && !isAlien) }
            *//*.collect { tags -> tags.map { tagInfo ->
          if (tagInfo.sessionId.isNullOrEmpty() && deviceSessionId.isNotEmpty()) tagInfo.sessionId = deviceSessionId
          if (tagInfo.sessionData.isNullOrEmpty() && sessionData.isNotEmpty()) tagInfo.sessionData = sessionData
          if (tagInfo.session_type.isNullOrEmpty() && sessionType.isNotEmpty()) tagInfo.session_type = sessionType
          if (tagInfo.transactionType.isNullOrEmpty() && transactionType.isNotEmpty()) tagInfo.transactionType = transactionType
          if (tagInfo.topic.isNullOrEmpty() && topic.isNotEmpty()) tagInfo.topic = topic
          if (isUpdateFound) tagInfo.isFound = true
          if (!(isPublishToMqtt && !isUnencoded && !isAlien)) tagInfo.isUploaded = true
        }*//*
            //saveToDB(tags, topic, isPublishToMqtt && !isUnencoded && !isAlien)
            //db.tagInfoDao().insertAll(tags)
            //  for(tagInfo in tags)
            // saveToDB(tagInfo, chkNull(topic,TopicConstants.INVENTORY),isPublishToMqtt && !isUnencoded && !isAlien)
//          db.tagInfoDao().insertAll(tags)
            //}
        }*/
    //}
  }

  private fun setupTagBuffer(){
      if(!isActionInventory) return
      tagBuffer = Channel<TagInfoEntity>(Channel.UNLIMITED)
      //startConsuming(handlerScope)
      startDatabaseConsumer(handlerScope,tagBuffer!!)
      /*handlerScope.launch {
          tagBuffer?.consumeAsFlow()?.chunked(100)?.collect { tags -> if (isActionInventory) saveToDB(tags, chkNull(topic, TopicConstants.INVENTORY), isPublishToMqtt && !isUnencoded && !isAlien) }
      }*/
  }

  fun startDatabaseConsumer(scope: CoroutineScope,tagBuffer: Channel<TagInfoEntity>) {
      scope.launch(Dispatchers.IO) {
            val currentBatch = mutableListOf<TagInfoEntity>()
            // Cap the batch size to protect RAM and database transaction limits
            val maxBatchSize = 500

            try {
                // This loop runs continuously until tagBuffer.close() is called
                while (true) {
                    // 1. Suspend until at least one item is available
                    val firstItem = tagBuffer.receive()
                    currentBatch.add(firstItem)

                    // 2. Start your 50ms window timer
                    val windowEndTime = System.currentTimeMillis() + 50

                    while (System.currentTimeMillis() < windowEndTime /*&& currentBatch.size < maxBatchSize*/) {
                    // 3. Instantly grab anything else already waiting in the queue
                        val nextItem = tagBuffer?.tryReceive()?.getOrNull()
                        if (nextItem != null) {
                            currentBatch.add(nextItem)
                        } else {
                            // Yield briefly to let producer threads fill the channel
                            delay(5)
                        }
                    }

                    // 4. Time's up! Flush the accumulated batch to the DB
                    if (currentBatch.isNotEmpty()) {
                        saveToDB(currentBatch.toMutableList(),chkNull(topic, TopicConstants.INVENTORY))
                        currentBatch.clear()
                    }
                }
            } catch (e: kotlinx.coroutines.channels.ClosedReceiveChannelException) {
                // 5. Channel closed gracefully. Flush any remaining items.
                if (currentBatch.isNotEmpty()) {
                    saveToDB(currentBatch.toMutableList(),chkNull(topic, TopicConstants.INVENTORY))
                    currentBatch.clear()
                }
                println("Consumer stopped: Channel closed.")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Consumer error: ${e.message}")
            }
        }
  }


  private fun startConsuming(scope: CoroutineScope) {
        tagBuffer?.consumeAsFlow()?.chunked(100)?.onEach { tagList  ->
          saveToDB(tagList,chkNull(topic, TopicConstants.INVENTORY))
        }?.catch { exception ->
            println("Stream encountered an error: ${exception.message}")
        }?.onCompletion { cause ->
                // 5. Triggered when the channel is closed via close()
                if (cause == null) {
                    println("Stream completed normally because channel closed.")
                } else {
                    println("Stream completed abruptly due to an error.")
                }
        }?.launchIn(scope)
            //.launchIn(scope)
            //?.onEach { tagInfo ->
                // 2. Process each incoming element
                //processTag(tagInfo)
            //}
            //?.flowOn(Dispatchers.IO) // 3. Offload processing to background threads
//            ?.catch { exception ->
//                // 4. Intercept any unexpected upstream exceptions
//                println("Stream encountered an error: ${exception.message}")
//            }
//            ?.onCompletion { cause ->
//                // 5. Triggered when the channel is closed via close()
//                if (cause == null) {
//                    println("Stream completed normally because channel closed.")
//                } else {
//                    println("Stream completed abruptly due to an error.")
//                }
//            }
//            .launchIn(scope) // 6. Runs the entire pipeline in the provided scope
    }

  fun <T> Flow<T>.chunked(duration: Long): Flow<List<T>> = channelFlow {
    //require(size > 0) { "Chunk size must be greater than 0" }
    require(duration > 0) { "Duration must be greater than 0" }

    val buffer = ArrayList<T>(5000)
    var timeoutJob = launch { } // Initialize with a dummy job

    val flushBuffer: suspend () -> Unit = {
      if (buffer.isNotEmpty()) {
        send(buffer.toList()) // Send a copy of the buffer
        buffer.clear()
        timeoutJob.cancel()
      }
    }
    collect { value ->
      if (buffer.isEmpty()) {
        // Start the timeout job when the first item of a new chunk arrives
        timeoutJob = launch {
          delay(duration)
          flushBuffer() // Flush if the duration passes
        }
      }

      buffer.add(value)

      /*if (buffer.size >= size) {
        flushBuffer() // Flush if the size limit is reached
      }*/
    }

    // Ensure any remaining items are flushed when the upstream flow completes
    flushBuffer()
  }


  constructor(context: CommonActivity,errMsg: MutableLiveData<String>) {
    this.context = context
    db = AppDatabase.getDbInstance(context)
    //inventoryEngine = InventoryEngine(db)
    /*{ count ->
     invScanCount = count
     isInventoryOn.postValue(true)   // keeps UI observer alive
    }*/
    this.errMsg = errMsg
    initSDK()
  }

  fun setSessionAndTransactionType(sessionType: String, transactionType: String = "",userRemark:String="",topic:String="") {
    showLog("method","setSessionAndTransactionType")
    this.sessionType = chkNull(sessionType, "")
    this.transactionType = chkNull(transactionType, "")
    this.topic = chkNull(topic, "")
    this.userRemark = chkNull(userRemark, "")
    //this.decodeType = ""
    /*this.zonePath = chkNull(zonePath,"")
    this.zoneName = chkNull(zoneName,"")*/
    showLog("rf_sessionType", sessionType)
    showLog("rf_tran", transactionType)
    /*showLog("rf_zonePath", zonePath)
    showLog("rf_zoneName", zoneName)*/
    //setDBObservers()
  }

  fun setSessionId(deviceSessionId:String="",sessionData:String=""){
    showLog("method","setSessionId")
    this.deviceSessionId = deviceSessionId;
    this.sessionData = sessionData;
    //this.decodeType = ""
  }

  fun setDBObservers() {
    //CoroutineScope(Dispatchers.IO).launch {
    /**db.tagInfoDao().isAllVerified(sessionType, transactionType).removeObservers(context)
    db.tagInfoDao().isAllVerified(sessionType, transactionType)
      .observe(context, Observer<Boolean?> { isVerified ->
        isAllVerified = chkTrue(isVerified)
      })*/

    /**db.tagInfoDao().getTagWriteVerifiedCount(sessionType, transactionType).removeObservers(context)
    db.tagInfoDao().getTagWriteVerifiedCount(sessionType, transactionType)
      .observe(context, Observer<Int?> { invCount ->
        tagVerifyCount = chkNull(invCount, 0)
      })*/

    /**db.tagInfoDao().getFoundCount(sessionType, transactionType).removeObservers(context)
    db.tagInfoDao().getFoundCount(sessionType, transactionType)
      .observe(context, Observer<Int?> { invCount ->
        itemFoundCount = chkNull(invCount, 0)
    })*/

    db.tagInfoDao().getTotalCount(sessionType, transactionType).removeObservers(context)
    db.tagInfoDao().getTotalCount(sessionType, transactionType)
      .observe(context, Observer<Int?> { invCount ->
        invScanCount = chkNull(invCount, 0)
      })
    //}
  }

  fun onCreate() {
    initSDK()
  }

  abstract fun dispose()
  fun onPause() {
      try {
        stopOperations()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

  fun onDestroy() {
    //inventoryEngine.destroy()
    dispose()
  }

  abstract fun onResume()
  abstract fun onConnect()
  abstract fun onDisconnect()
  abstract fun initSDK()
  abstract fun connect()
  abstract fun disconnect()
  abstract fun isConnected(): Boolean

  //Configurations
  abstract fun configure()
  abstract fun setPower(power: Int)
  abstract fun getPower(): Int
  abstract fun clearFilter(): Int

  abstract fun checkAndSetReader()
  abstract fun checkAndConnectReader()

  /**
   * Reset command flags.
   */
  private fun resetCommandFlags() {
    //Tag Read
    readTid = false
    readRssi = true
    readPC = false
    readUser = false
    readType = true
    readEAN = true

    //Search
    isActionTIDSearch = false
    isActionEPCSearch = false
    isActionSearch = false
    isLockSearchEPC = false

    this.searchEpc = ""
    this.searchBarcode = ""
    this.searchLockedEpc = ""
    this.scannedTids.clear()

    //Tag Write
    this.multiWriteListSize = 0
    this.multiWriteCount = 0
    this.isMultiWriteDone = false
    this.multiWriteSuccessCount = 0
  }

  fun resetStateData(){
    isTagWriteDone.value=false
    errMsg.value=""
    decodeType = ""
  }

  //Actions
  fun isProcessOn(): Boolean {
    return isScanningOn() || chkTrue(isTagWriteOn.value);
  }

  fun isScanningOn(): Boolean {
    return (isActionInventory && chkTrue(isInventoryOn.value)) || (isActionTagVerify && chkTrue(isTagVerifyOn.value)) || (isActionPick && chkTrue(isPickOn.value)) || (isActionSearch && chkTrue(isSearchOn.value))
  }

  public fun startTagVerify(invPower: Int? = 29) {
    if (isProcessOn()) return
    resetStateData()
    if (!isConnected()) {
      checkAndConnectReader(); return
    }
    handlerScope.launch {
      val isAllVerified = chkNull(db.tagInfoDao().isAllVerified(sessionType,transactionType).value,false)
      if (isAllVerified==true) return@launch
      readTid = true
      setupInventory(invPower = chkNull(invPower, getPower()), readTid)
      isActionTagVerify = true
      if (startInventory()) {
        startBeepTimer()
        isTagVerifyOn.postValue(true)
      }
    }
  }

  public fun startInventory(
    invPower: Int? = null,
    eans: List<String> = emptyList(),
    epcs: List<String> = emptyList(),
    excludedEpcs: List<String> = emptyList(),
    excludedEans: List<String> = emptyList(),
    publishToMqtt: Boolean = true,
    updateFound: Boolean = false,
    onlyUnencoded: Boolean=false,
    onlyAlien: Boolean=false,
    maxScanningLimit:Int = 0
  ) {
    showLog("method","startInventory")
    if (isProcessOn()) return
    resetStateData()
    if (!isConnected()) {
      checkAndConnectReader(); return
    }
    //handlerScope.launch {
      listEans = eans;
      listEpcs = epcs;
      listExcludedEans = excludedEans;
      listExcludedEpcs = excludedEpcs;
      isPublishToMqtt = publishToMqtt
      showLog("isPublishToMqtt", "" + isPublishToMqtt)
      isUpdateFound = updateFound
      isUnencoded = onlyUnencoded
      isAlien = onlyAlien
      maxScanLimit = maxScanningLimit
      //inwScanCount = invScanCount
      isMaxScanLimitReached = maxScanLimit>0 && invScanCount>=maxScanLimit
      //showLog("isMaxScanLimitReached_invScanCount0",""+invScanCount)
      //showLog("isMaxScanLimitReached0",""+isMaxScanLimitReached)
      if(isMaxScanLimitReached) return
      readTid = isUnencoded || isAlien || !isPublishToMqtt
      val result= setupInventory(invPower = chkNull(invPower, getPower()), readTid)
      showLog("setupInventory",""+result)
      isActionInventory = true
      setupTagBuffer()
      if (startInventory()) {
        showLog("startInventory",""+true)
        startBeepTimer()
         //Start the correct engine instead of the old beepTimer
        /*inventoryEngine.start(
           sessionType = sessionType,
           transactionType = transactionType,
            sessionId = deviceSessionId,
            sessionData = sessionData,
            topic = chkNull(topic, TopicConstants.INVENTORY),
            isPostToMqtt = isPublishToMqtt && !isUnencoded && !isAlien,
            isUpdateFound = isUpdateFound,
            maxScanLimit = maxScanLimit
        )*/
        isInventoryOn.postValue(true)
      }
      else showLog("startInventory",""+false)
    //}
  }

  public fun startSearch(
    searchType: String= SearchTypeConstant.BARCODE,
    searchValue: String ="",
    isTagVerify: Boolean = false,
    updateFound: Boolean = false,
  ) {
    if (isProcessOn()) return
    resetStateData()
    if (!isConnected()) {
      checkAndConnectReader(); return
    }
    handlerScope.launch {
      if (searchValue.equals("")) {
        setError("Search Value Empty")
        return@launch;
      }
      searchBarcode = if (searchType.equals(SearchTypeConstant.BARCODE)) searchValue else ""
      searchEpc = if (searchType.equals(SearchTypeConstant.EPC)) searchValue else if (searchType.equals(SearchTypeConstant.BARCODE)) EpcEncoderDecoderWrapper.getEpcFromBarcode(searchValue, true) else ""
      searchTid = if (searchType.equals(SearchTypeConstant.TID)) searchValue else ""
      isUpdateFound = updateFound

      showLog("searchBarcode", searchBarcode)
      showLog("searchEpc", searchEpc)
      showLog("searchTid", searchTid)

      isActionSearch = true
      isActionTIDSearch = searchType.equals(SearchTypeConstant.TID) && searchTid.isNotEmpty()
      isActionEPCSearch =
        !isActionTIDSearch && searchType.equals(SearchTypeConstant.EPC) && searchEpc.isNotEmpty()

      readTid = isActionTIDSearch

      setupSearch(isActionTIDSearch)

      if (setFilter(isActionSearch, isActionEPCSearch, isActionTIDSearch) && startSearch()) {
        showLog(
          "Set Filter:",
          setFilter(isActionSearch, isActionEPCSearch, isActionTIDSearch).toString()
        )
        startBeepTimer()
        isSearchOn.postValue(true)
      }
    }
  }

  public fun startEncoding(tagInfo: TagInfoEntity,currentPassword:String,passwords: List<String>) {
    if(multiWriteListSize <= 0 || multiWriteCount<=0){
     if (isProcessOn()) return
     resetStateData()
     if (!isConnected()) { checkAndConnectReader(); return }
      multiWriteSuccessCount = 0
      isMultiWriteDone = false
    }
    handlerScope.launch {
      if(tagInfo==null || /*tagInfo.epc.isNullOrEmpty() ||*/ tagInfo.tid.isNullOrEmpty() || tagInfo.newEpc.isNullOrEmpty()) return@launch
      if (tagInfo.tid.length < 8 || tagInfo.newEpc.length < 8) {
        setError(R.string.err_pick_no_tag)
        return@launch
      }
      if (NON_128_BIT_TIDS.contains(tagInfo.tid.uppercase().substring(0, 8)) && tagInfo.newEpc.length > 24) {
        setError(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids);
        return@launch
      }
      if (tagInfo.startTime.isEmpty()) tagInfo.startTime = getCurrentTime()
      if(multiWriteListSize <= 0 || multiWriteCount<=0) setupTagWrite(MAX_POWER_TO_SET - 5)
      showLog("startEncoding_tagInfo", tagInfo.toString())
      var offSet = 0
      var dataLen = 0
      var writeEpc = if (tagInfo.newEpc.length % 2 != 0 || tagInfo.newEpc.length == tagInfo.epc.length) tagInfo.newEpc else getWriteEpc(tagInfo.pcData, tagInfo.newEpc)
      showLog("writeEpc", writeEpc)
      when (writeEpc.length) {
        8, 12, 16, 20, 24, 28, 32, 36 -> {
          dataLen = writeEpc.length / 4
          offSet = (if (dataLen % 2 == 0) 2 else 1)
        }
      }
      if (offSet <= 0 || dataLen <= 0) saveDBTagWriteError(
        tagInfo,
        0,
        TopicConstants.ENCODE,
        R.string.err_encoding_write_fail
      )
      else startEncoding(tagInfo, currentPassword, passwords, 0, writeEpc, offSet, dataLen)
    }
  }

  private fun startDecoding(tagInfo: TagInfoEntity,decodeType: String=""){
    LogUtils.showLog("Decode_",decodeType)
    val currentPassword:String = DataStoreManager.getCurrentPassword()
    val passwords: List<String> = DataStoreManager.getOldPasswordList()
    startDecoding(tagInfo,currentPassword,passwords,decodeType)
  }

 public fun startReturn(tagTime: TagTime,currentPassword:String,passwords: List<String>) {
   if(tagTime.tagInfoId<=0){ return }
   //handlerScope.launch {
     //val tagInfo = db.tagInfoDao().getById(tagTime.tagInfoId)
     //val tagInfo = TagInfoEntity(sessionType=sessionType,transactionType=transactionType,epc=tagTime.epc,tid=chkNull(tagTime.tid,""),barcode=tagTime.barcode)
     //if(tagInfo!=null) startReturn(tagInfo,currentPassword,passwords)
   //}
 }

  public fun startReturn(tagInfo: TagInfoEntity,currentPassword:String,passwords: List<String>) {
    if(multiWriteListSize <= 0 || multiWriteCount<=0){
     if (isProcessOn()) return
     resetStateData()
     if (!isConnected()) { checkAndConnectReader(); return }
     multiWriteSuccessCount = 0
     isMultiWriteDone = false
    }
    handlerScope.launch {
    if(tagInfo==null || tagInfo.epc.isNullOrEmpty() || tagInfo.tid.isNullOrEmpty()) return@launch
    //handle just decoded tag (commented for now)
    /*if(tagInfo.isTagWriteDone && tagInfo.newEpc.isNotEmpty() && tagInfo.newEpc.startsWith(DataStoreManager.readFromPreferences("decodeBits","0")) && !tagInfo.epc.startsWith(DataStoreManager.readFromPreferences("decodeBits","0"))){
       val epc = tagInfo.epc
       val newEpc = tagInfo.newEpc
       //reset tagInfo object for return
    }*/
    if(!tagInfo.epc.startsWith(DataStoreManager.readFromPreferences("decodeBits","0"))){
        setError(R.string.err_not_decoded)
        return@launch
    }
    if(tagInfo.newEpc.isNullOrEmpty()) tagInfo.newEpc = EpcEncoderDecoderWrapper.getReturnEpc(tagInfo.epc)
    if(tagInfo.epc.equals(tagInfo.newEpc)) {
      setError(R.string.err_decoding_already_decoded)
      return@launch
    }
    if(tagInfo.newEpc.isNotEmpty()){
     tagInfo.barcode = EpcEncoderDecoderWrapper.getBarcodeFromEpc(tagInfo.newEpc)
    }
    if(tagInfo.startTime.isEmpty()) tagInfo.startTime=getCurrentTime()
    if(multiWriteListSize <= 0 || multiWriteCount<=0) setupTagWrite(MAX_POWER_TO_SET-5)
    decodeType = "RETURN"
    var offSet =0
    var dataLen =0
    when (tagInfo.newEpc.length) {
      8, 12, 16, 20, 24, 28, 32, 36 -> {
        dataLen = tagInfo.newEpc.length / 4
        offSet = (if (dataLen % 2 == 0) 2 else 1)
      }
    }
    if(offSet<=0 || dataLen<=0) saveDBTagWriteError(tagInfo,0,TopicConstants.DECODE,R.string.err_encoding_write_fail)
    else startDecoding(tagInfo,currentPassword,passwords,0,tagInfo.newEpc,offSet,dataLen)
    }
  }

  public fun startDecoding(tagInfo: TagInfoEntity,currentPassword:String,passwords: List<String>,decodeType: String = "") {
    showLog("startDecoding",""+decodeType)
    showLog("startDecoding",""+multiWriteListSize+"_"+multiWriteCount)
    if(multiWriteListSize <= 0 || multiWriteCount<=0){
     if (isProcessOn()) return
     resetStateData()
     if (!isConnected()) { checkAndConnectReader(); return }
     multiWriteSuccessCount = 0
     isMultiWriteDone = false
     LogUtils.showLog("Decode_",decodeType)
     if(this.decodeType.isNullOrEmpty() && decodeType.isNotEmpty()) this.decodeType = decodeType
    }
    handlerScope.launch {
    if(tagInfo==null || tagInfo.epc.isNullOrEmpty() || tagInfo.tid.isNullOrEmpty()) return@launch
    if(tagInfo.newEpc.isNullOrEmpty()) tagInfo.newEpc= DataStoreManager.readFromPreferences("decodeBits","0")+tagInfo.epc.substring(1)
    if(tagInfo.epc.equals(tagInfo.newEpc)) {
      setError(R.string.err_decoding_already_decoded)
      return@launch
    }
    if(tagInfo.startTime.isEmpty()) tagInfo.startTime=getCurrentTime()
    if(multiWriteListSize <= 0 || multiWriteCount<=0) setupTagWrite(MAX_POWER_TO_SET-5)
    var offSet =0
    var dataLen =0
    when (tagInfo.newEpc.length) {
      8, 12, 16, 20, 24, 28, 32, 36 -> {
        dataLen = tagInfo.newEpc.length / 4
        offSet = (if (dataLen % 2 == 0) 2 else 1)
      }
    }
    if(offSet<=0 || dataLen<=0) saveDBTagWriteError(tagInfo,0,TopicConstants.DECODE,R.string.err_encoding_write_fail)
    else startDecoding(tagInfo,currentPassword,passwords,0,tagInfo.newEpc,offSet,dataLen)
    }
  }

  fun validateData(ean: String, epc: String?, tid: String?, rssi: String?,serial: String?,isUnencodedTag:Boolean=false,isAlienTag:Boolean=false): Boolean {
    //showLog("isMaxScanLimitReached_inwScanCount1",""+inwScanCount)
    if (isMaxScanLimitReached) return false;
    isMaxScanLimitReached = maxScanLimit>0 && invScanCount>=maxScanLimit
    showLog("isMaxScanLimitReached1",""+isMaxScanLimitReached)
    if (isMaxScanLimitReached) return false;
    if (readTid && tid.isNullOrEmpty()) return false;
    if (listEans.isNotEmpty() && !listEans.contains(ean)) return false
    if (listEpcs.isNotEmpty() && !listEpcs.contains(epc)) return false
    if (listExcludedEans.isNotEmpty() && listExcludedEans.contains(ean)) return false
    if (listExcludedEpcs.isNotEmpty() && listExcludedEpcs.contains(epc)) return false
    if(isUnencoded && !isUnencodedTag) return false
    if(isAlien && !isAlienTag) return false
    //if (isUnencoded && !chkNull(epc,"").startsWith(DataStoreManager.readFromPreferences("decodeBits","0"))) return false
    /*showLog("invScanCount0",""+invScanCount)
    if (maxScanLimit>0 && invScanCount>=maxScanLimit) return false*/
    //if (isUnencoded && !ean.matches(Regex("(?i)("+BarcodeConstants.NON_ENCODED+"|"+BarcodeConstants.UNKNOWN+")"))) return false
    //if (isAlien && (ean.matches(Regex("(?i)("+BarcodeConstants.NON_ENCODED+"|"+BarcodeConstants.UNKNOWN+")"))) return false
    return true
  }

  private fun enrichTag(tag: TagInfoEntity): TagInfoEntity {
    if (tag.sessionId.isNullOrEmpty() && deviceSessionId.isNotEmpty()) tag.sessionId = deviceSessionId
    if (tag.sessionData.isNullOrEmpty() && sessionData.isNotEmpty()) tag.sessionData = sessionData
    if (tag.session_type.isNullOrEmpty() && sessionType.isNotEmpty()) tag.session_type = sessionType
    if (tag.transactionType.isNullOrEmpty() && transactionType.isNotEmpty()) tag.transactionType = transactionType
    if (tag.topic.isNullOrEmpty() && topic.isNotEmpty()) tag.topic = topic
    if (isUpdateFound) tag.isFound = true
    if (!isPublishToMqtt && !isUnencoded && !isAlien) tag.isUploaded = true
    return tag
  }
  public fun stopOperations() {
    showLog("method","stopOperations")
    showLog("isActionInventory_stopOperations", ""+isActionInventory)
    val wasInvAction=isActionInventory
    isActionInventory=false
    if(tagBuffer!=null){
        tagBuffer?.close()
        tagBuffer=null
    }
    //if(wasInvAction)  inventoryEngine.stop()
    showLog("isActionInventory_stopOperations1", ""+isActionInventory)
    /*try {
        tagBuffer.cancel()
    }catch (e: Exception) {e.printStackTrace()}*/
    //showLog("method", "stopOperations")
    //showLog("stopOperations", ""+invScanCount+"_Delay->"+((invScanCount/800)*10))
    //handlerScope.launch {
      /*if(wasInvAction && invScanCount>800) {
       delay(60)//((invScanCount/800)*10).toLong())
      }*/
      try {
        stopScanning()
      } catch (e: Exception) {
        e.printStackTrace()
      }
      resetCommandFlags()
      //delay(30)
      if (chkTrue(isInventoryOn.value)) isInventoryOn.postValue(false)
      if (chkTrue(isTagVerifyOn.value)) isTagVerifyOn.postValue(false)
      if (chkTrue(isSearchOn.value)) isSearchOn.postValue(false)
      if (chkTrue(isPickOn.value)) isPickOn.postValue(false)
      if (chkTrue(isTagWriteOn.value)) isTagWriteOn.postValue(false)

      //Actions
      isActionTagVerify = false
      isActionInventory = false
      isActionPick = false
      isActionTidPick = false
      isActionSearch = false
      isActionEPCSearch = false
      isActionTIDSearch = false

      showLog("isActionInventory", "" + isActionInventory)

      //power
      pickPower = defaultPickPower

      //Search
      searchPercent.postValue(0.0f)
      searchPhase.postValue("")
      searchRssi.postValue("")
      //percent = 0.0f
      listSearchPercent.clear()

      //Timer
      //if (pickCountDownTimer != null) pickCountDownTimer.cancel()
      if (beepTimer != null) {
        beepTimer?.cancel()
        beepTimer = null
      }
      if (pickTimer != null) {
        pickTimer?.cancel()
        pickTimer = null
      }

      //Inventory
      listEans = emptyList()
      listEpcs = emptyList()
      listExcludedEans = emptyList()
      listExcludedEpcs = emptyList()
      isUpdateFound = false
      isUnencoded = false
      isAlien = false
      isPublishToMqtt = false
      isMaxScanLimitReached = false

      //stopScanning()
    //}
  }

  public fun startPick(
    barcode: String = "",
    pickedEpcs: List<String> = emptyList(),
    pickedTids: List<String> = emptyList(),
    pickPower: Int = defaultPickPower,
    pickTime: Long = defaultPickTimeInMilliis,
    isDecodeOnPick: Boolean = false,
    isPostPicked: Boolean = true,
    isSavePickedToDB: Boolean = false,
    updateFound: Boolean = false,
    isAllowNonEncodedTags: Boolean = true,
    isAllowDuplicateTagRePick: Boolean = true,
  ) {
    if (isProcessOn()) return
    resetStateData()
    if (!isConnected()) {
      checkAndConnectReader(); return
    }
    showLog("method", "startPick")
    pickData.postValue(null)
    pickedListData.postValue(null)
    this.pickPower = pickPower
    this.pickedEpcs.clear()
    if (isNonEmpty(pickedEpcs)) this.pickedEpcs.addAll(pickedEpcs)
    this.pickedTids.clear()
    if (isNonEmpty(pickedTids)) this.pickedTids.addAll(pickedTids)
    pickBarcode = chkNull(barcode, "")
    this.isDecodeOnPick = isDecodeOnPick
    this.isPostPicked = isPostPicked
    this.isSavePickedToDB = isSavePickedToDB
    //this.isUpdateFound= updateFound
    this.isAllowNonEncodedTags = isAllowNonEncodedTags
    this.isAllowDuplicateTagRePick = isAllowDuplicateTagRePick
    this.pickCountDownTime = pickTime
    resetCommandFlags()
    this.pickTags.clear()
    this.pickTagData = Any()
    this.pickData.postValue(null)
    //Pick
    handlerScope.launch {
      readTid = true
      readPC = true
      setupPick(pickPower)
      isActionPick = true
      showLog("isActionPick01", "" + isActionPick)
      if (startInventory()) {
        //startBeepTimer()
        isPickOn.postValue(true)
        startPickTimer(pickTime)
      }
    }
  }


  private fun startBeepTimer() {
    confirmedDbCount=0
    /*dbUpdateTimer = fixedRateTimer("invDbTimer",initialDelay=0,period=100L){
        if(buffer.isNotEmpty()) saveToDB(buffer.toList(),chkNull(topic, TopicConstants.INVENTORY), isPublishToMqtt && !isUnencoded && !isAlien)
        buffer.clear()
    }*/
    //Called each time when 500 milliseconds (1/2 second) (the period parameter)
    //if(!isActionInventory)
    beepTimer = fixedRateTimer("invSerTimer", initialDelay = 0, period = 500L) {
      if (chkTrue(isSearchOn.value) || isActionSearch) {
        val oldPercent: Float = searchPercent.value ?: 0.0f
        val oldPhase: String = searchPhase.value ?: ""
        val oldRssi: String = searchRssi.value ?: ""
        val listSerPercent = ArrayList(listSearchPercent)
        showLog("percent:list",listSerPercent.toString())
        val newPercent = if(listSerPercent.isEmpty()) 0.0f else listSerPercent.max()
        showLog("percent:old_new", oldPercent.toString() + "_" + newPercent)
        showLog("phase:old_new", oldPhase + "_" + phase)
        showLog("rssi:old_new", oldRssi + "_" + rssi)
        listSearchPercent.clear()
        searchPercent.postValue(newPercent)
        searchPhase.postValue(phase)
        searchRssi.postValue(rssi)
        //if (newPercent > 0.0f) SoundUtils.searchBeep(context,newPercent.toInt());
      }
      else if (chkTrue(isInventoryOn.getValue()) || isActionInventory) {
        val oldcount: Int = scanCount
        showLog("oldcount", "" + oldcount)
        val invScanCount = confirmedDbCount//chkNull(db.tagInfoDao().getTotalCount(sessionType, transactionType).value,0)
        scanCount = invScanCount
        showLog("scanCount", "" + scanCount)
        LogUtils.showLog("saveToDB_topic_isPostToMqtt",topic+"_"+(isPublishToMqtt&& !isUnencoded && !isAlien))
        if(topic.equals(TopicConstants.INVENTORY) && isPublishToMqtt && !isUnencoded && !isAlien) {
          publishInventoryToMqtt()//db.tagInfoDao().getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, deviceSessionId));
        }
        if (scanCount > oldcount) {
          SoundUtils.inventoryBeep(context = context,scanCount-oldcount)
        }
      }
      else if (chkTrue(isTagVerifyOn.getValue()) || isActionTagVerify) {
        val oldcount: Int = verifyCount
        showLog("oldcount", "" + oldcount)
        val tagVerifyCount = chkNull(db.tagInfoDao().getTagWriteVerifiedCount(sessionType, transactionType).value,0)
        verifyCount = tagVerifyCount
        showLog("verifyCount", "" + verifyCount)
        if (verifyCount > oldcount) {
          SoundUtils.beep()
        }
      }
    }
    //beepTimer=Timer()
    /*beepTimer!!.scheduleAtFixedRate(object : TimerTask(){
      override fun run() {
        //Called each time when 500 milliseconds (1/2 second) (the period parameter)
        if(chkTrue(isSearchOn.getValue()) || isActionSearch){
          val oldPercent: Float = searchPercent.value
          val oldPhase: String = searchPhase.value
          val oldRssi: String = searchRssi.value
          showLog("percent:old_new",oldPercent.toString()+"_"+percent)
          showLog("phase:old_new",oldPhase+"_"+phase)
          showLog("rssi:old_new",oldRssi+"_"+rssi)
          searchPercent.postValue(percent)
          searchPhase.postValue(phase)
          searchRssi.postValue(rssi)
          if(percent>0.0f) SoundUtils.beep();
        }
        else if (chkTrue(isInventoryOn.getValue()) || isActionInventory) {
          val oldcount: Int = scanCount
          showLog("oldcount", "" + oldcount)
          scanCount = invScanCount
          showLog("scanCount", "" + scanCount)
          if (scanCount > oldcount) {
            SoundUtils.beep()
          }
        }
      }
    },0,500)*/
  }

  private fun startPickTimer(pickTime: Long = defaultPickTimeInMilliis) {
    pickTimer = Timer();
    pickTimer!!.schedule(object : TimerTask() {
      override fun run() {
        onPickTimerFinish();
      }
    }, pickTime)
  }

/**  private fun setPickDBObserver(epc:String,tid:String){
    db.tagInfoDao().isTagExist(sessionType, transactionType,tid,epc).removeObservers(context)
    db.tagInfoDao().isTagExist(sessionType, transactionType,tid,epc)
      .observe(context, Observer<Boolean?> { isTagExist ->
        if(isTagExist==true) setError(R.string.err_pick_already_picked_tag)
      })
  }*/

  private fun onPickTimerFinish() {
  handlerScope.launch {
    showLog("method", "onPickTimerFinish")
    try {
      stopOperations()
    } catch (e: Exception) {
      e.printStackTrace(); }
    showLog("pickTags11", "" + if (pickTags == null) 0 else pickTags.size)
    if (isNullOrEmpty(pickTags)) {
      showLog("isRePick", "" + isRePick)
      /*if (isRePick) {
        isRePick = false
        startPick(
          searchBarcode,
          pickedEpcs,
          pickedTids,
          pickPower,
          pickCountDownTime,
          isDecodeOnPick,
          isPostPicked
        )
      } else {*/
      isRePick = false
      setError(R.string.err_pick_no_tag)
      //}
    } else if (pickTags.size > 1) {
      isRePick = false
      setError(R.string.err_pick_multi_tag)
    } else if (pickTags.size == 1) {
      isRePick = false
      readTid = true;
      val tagInfo: TagInfoEntity = getTagInfo(pickTagData)
      if (tagInfo.epc.isNullOrEmpty() && tagInfo.tid.isNullOrEmpty()) {
        setError(R.string.err_pick_no_tag)
        return@launch
      }
      if (!isAllowDuplicateTagRePick && tagInfo.tid.isNotEmpty() && chkTrue(db.tagInfoDao().isTagExist(sessionType, transactionType,tagInfo.tid,tagInfo.epc))) {
        setError(R.string.err_pick_already_picked_tag)
        return@launch
      }
      if((!isAllowNonEncodedTags || isNonEmpty(pickBarcode)) && tagInfo.isUnencodedTag()){//isUnencodedTag(tagInfo.barcode)) {
       setError(R.string.err_pick_non_encoded_tag)
       return@launch
      }
      if (isNonEmpty(pickBarcode) && !isBarcodeMatching(tagInfo.barcode, pickBarcode)) {
        setError(String.format(context.getString(R.string.err_pick_wrong_tag),tagInfo.barcode))
        return@launch
      }
      if (isNonEmpty(errMsg.value)) return@launch
      /*else if (isNonEmpty(eans) && isNonEmpty(barcode) && !eans.contains(barcode))
        setError(String.format(java.lang.String.format(context.getString(R.string.err_pick_not_present_tag), barcode)))*/
      if (tagInfo.tid.isNotEmpty()) {
        if (isDecodeOnPick) startDecoding(tagInfo)
        if (isPostPicked) pickData.postValue(tagInfo)
        if (isSavePickedToDB) {
            saveToDB(tagInfo, chkNull(topic, TopicConstants.MOVEMENT), false)
        }
      }
      pickBarcode=""
    }
  }
  }

  private fun isUnencodedTag(barcode: String): Boolean {
    return barcode.equals(BarcodeConstants.UNKNOWN, ignoreCase = true) || barcode.equals(
      BarcodeConstants.NON_ENCODED,
      ignoreCase = true
    )
  }

  private fun isBarcodeMatching(barcode1: String, barcode2: String): Boolean {
    if (barcode1.equals(barcode2, true)) return true
    return false
  }

  abstract fun getTagInfo(any: Any): TagInfoEntity

  fun setError(errResId: Int) {
    if (errResId != 0) setError(context.getString(errResId))
  }

  protected fun setError(error: String) {
    showLog("rfid_error", error)
    errMsg.postValue(error)
  }

  abstract fun startInventory(): Boolean
  abstract fun stopScanning()
  abstract fun startSearch(): Boolean
  abstract protected fun setupInventory(invPower: Int?,readTid: Boolean): Boolean
  abstract protected fun setupTagWrite(writePower: Int?): Boolean
  abstract protected fun setupPick(pickPower: Int?): Boolean
  abstract protected fun setupSearch(isTidSearch: Boolean): Boolean


  abstract protected fun setSearchFilterBarcode(epc: String, isBC: Boolean=false, isSearchDecoded: Boolean=false): Boolean
  abstract protected fun setSearchFilterEpc(epc: String, isSearchDecoded: Boolean=false): Boolean
  abstract protected fun setSearchFilterTid(tid: String): Boolean

  protected fun setFilter(isActionSearch: Boolean, isActionEPCSearch: Boolean, isActionTIDSearch: Boolean): Boolean {
    if (!isActionSearch) return false
    return if (isActionTIDSearch) setSearchFilterTid(searchTid)
    else if (isActionEPCSearch) setSearchFilterEpc(searchEpc)
    else if(searchEpc.isNotEmpty() && searchEpc.length>2) setSearchFilterBarcode(searchEpc, searchEpc.substring(0, 2).matches(Regex("(?i)(BC|0C)")))
    else false
  }

  protected fun getWriteEpc(pcData: String, newEpc: String): String {
    val updatedPc = (newEpc.length / 8).toString() + if (pcData.length > 1) pcData.substring(1) else "000"
    return updatedPc + newEpc
  }

  public fun startEncoding(
    tagInfoList: List<TagInfoEntity>,
    currentPassword: String = "",
    passwords: List<String> = emptyList()
  ) {
    if (isProcessOn()) return
    resetStateData()
    if (!isConnected()) { checkAndConnectReader(); return }
    if(tagInfoList.isNullOrEmpty()) return
    multiWriteListSize = tagInfoList.size
    multiWriteCount = tagInfoList.size
    multiWriteSuccessCount = 0
    isMultiWriteDone = false
    setupTagWrite(MAX_POWER_TO_SET - 5)
    for (tagInfo in tagInfoList) {
      startEncoding(tagInfo, currentPassword, passwords)
    }
  }

  public fun startDecoding(
    tagInfoList: List<TagInfoEntity>,
    currentPassword: String = "",
    passwords: List<String> = emptyList(),
    decodeType: String = ""
  ) {
    showLog("startDecoding_list",""+decodeType)
    if (isProcessOn()) return
    resetStateData()
    if (!isConnected()) { checkAndConnectReader(); return }
    if(tagInfoList.isNullOrEmpty()) return
    multiWriteListSize = tagInfoList.size
    multiWriteCount = tagInfoList.size
    multiWriteSuccessCount = 0
    isMultiWriteDone = false
    setupTagWrite(MAX_POWER_TO_SET - 5)
    if(this.decodeType.isNullOrEmpty() && decodeType.isNotEmpty()) this.decodeType = decodeType
    for (tagInfo in tagInfoList) {
      startDecoding(tagInfo, currentPassword, passwords,decodeType)
    }
  }

  abstract fun startEncoding(
    tagInfo: TagInfoEntity,
    currentPassword: String = "",
    passwords: List<String> = emptyList(),
    retryCount: Int = 0,
    writeEpc:String,
    offSet: Int,
    dataLen:Int
  )

  abstract fun startDecoding(
    tagInfo: TagInfoEntity,
    currentPassword: String = "",
    passwords: List<String> = emptyList(),
    retryCount: Int = 0,
    writeEpc:String,
    offSet: Int,
    dataLen:Int
  )

  protected fun checkBluetoothConnection(): Boolean {
    if (mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (mBluetoothAdapter == null) {
      /*showShortToast(
          context,
          java.lang.String.format(
              context.getString(R.string.err_no_bluetooth),
              getTypeCharCode()
          )
      )*/
      return false
    } else if (!mBluetoothAdapter!!.isEnabled() /*&& bluetoothResultLauncher != null*/) {
      /*showShortToast(
          context,
          java.lang.String.format(
              context.getString(R.string.err_bluetooth_disabled),
              getTypeCharCode()
          )
      )*/
      /*context.showCustomAlertDialog("",
          java.lang.String.format(
              context.getString(R.string.err_bluetooth_disabled),
              getTypeCharCode()
          ),
          null,
          false,
          false,
          context.getString(if (ChainwayRFIDHandler.bluetoothResultLauncher != null) R.string.btn_enable else R.string.btn_ok),
          DialogInterface.OnClickListener { dialogInterface, i ->
              if (ChainwayRFIDHandler.bluetoothResultLauncher != null) ChainwayRFIDHandler.bluetoothResultLauncher.launch(
                  Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
              )
          },
          if (ChainwayRFIDHandler.bluetoothResultLauncher != null) context.getString(R.string.btn_cancel) else "",
          null
      )*/
      return false
    }
    return true
  }

  protected suspend fun processScannedData(scanned: List<Any>) {
    if(isMaxScanLimitReached) return
    val tagInfoList: List<TagInfoEntity> = scanned.map{ getTagInfo(scanned)}.filter { tagInfo -> validateData(tagInfo.barcode, tagInfo.epc, tagInfo.tid, tagInfo.rssi, tagInfo.serial, tagInfo.isUnencodedTag(),tagInfo.isAlien()) }.distinct()
    showLog("isActionPick", "" + (isActionPick && chkTrue(isPickOn.value)))
    showLog("isActionSearch", "" + (isActionSearch && chkTrue(isSearchOn.value)))
    showLog("isActionInventory", "" + (isActionInventory && chkTrue(isInventoryOn.value)))
    showLog("isActionTagVerify", "" + (isActionTagVerify && chkTrue(isTagVerifyOn.value)))
    if (isActionPick && chkTrue(isPickOn.value)) {
      if(tagInfoList.size>1 || pickTags.size>1) { stopOperations(); setError(R.string.err_pick_multi_tag); }
      if(tagInfoList.size==1) {
        val tagInfo = tagInfoList.get(0)
        pickTags.add(if (isActionTidPick) tagInfo.tid else tagInfo.epc)
        if (pickTags.size == 1) pickTagData = tagInfo
        showLog("Picked Tag Size:-", "" + pickTags.size)
      }
    }
    if(isActionInventory && chkTrue(isInventoryOn.value)) {
      saveToDB(tagInfoList, TopicConstants.INVENTORY,isPostToMqtt = isPublishToMqtt && !isUnencoded && !isAlien)
      //tagBuffer.send(tagInfoList)
    }
  }

  protected fun processScannedData(scanned: Any) {
    showLog("method", "processScannedData")
    showLog("isActionInventory_processScannedData", ""+isActionInventory)
    //handlerScope.launch {
    showLog("topic",topic+"_"+isActionInventory)
    if(isMaxScanLimitReached) return
    if(!isScanningOn()) return
    if(topic.equals(TopicConstants.INVENTORY) && !isActionInventory) return
    if(topic.equals(TopicConstants.INVENTORY) && !chkTrue(isInventoryOn.value)) return
    val tagInfo: TagInfoEntity = getTagInfo(scanned)
    showLog("epc1", tagInfo.epc)
    showLog("tid1", tagInfo.tid)
    val result = validateData(tagInfo.barcode, tagInfo.epc, tagInfo.tid, tagInfo.rssi,tagInfo.serial,tagInfo.isUnencodedTag(),tagInfo.isAlien())
    showLog("resultValidateData",""+result)
    if (result) {
      showLog("epc2", tagInfo.epc)
      showLog("tid2", tagInfo.tid)
      showLog("isActionPick", "" + (isActionPick && chkTrue(isPickOn.value)))
      showLog("isActionSearch", "" + (isActionSearch && chkTrue(isSearchOn.value)))
      showLog("isActionInventory", "" + (isActionInventory && chkTrue(isInventoryOn.value)))
      showLog("isActionTagVerify", "" + (isActionTagVerify && chkTrue(isTagVerifyOn.value)))
      if (isActionPick && chkTrue(isPickOn.value)) {
        pickTags.add(if (isActionTidPick) tagInfo.tid else tagInfo.epc)
        if (pickTags.size == 1) pickTagData = tagInfo
        showLog("Picked Tag Size:-", "" + pickTags.size)
      }
      else if (isActionSearch && chkTrue(isSearchOn.value)) {
        showLog("rssi1",tagInfo.rssi)
        showLog("epc3", tagInfo.epc)
        showLog("tid3", tagInfo.tid)
        if (tagInfo.rssi.isNotEmpty() && ((isActionTIDSearch && tagInfo.tid.equals(searchTid, true)) ||
          (isActionEPCSearch && tagInfo.epc.equals(searchEpc, true)) ||
          tagInfo.epc.startsWith(searchEpc, true))
        ) {
          showLog("rssi2",tagInfo.rssi)
          showLog("epc4", tagInfo.epc)
          showLog("tid4", tagInfo.tid)
          val actualPercent = getPercentageOld(tagInfo.rssi)
          showLog("actualPercent",""+actualPercent)
          listSearchPercent.add(actualPercent)
          //percent = actualPercent
          phase = tagInfo.phase
          rssi = tagInfo.rssi
          //searchPercent.postValue(actualPercent)
          //searchPhase.postValue(tagInfo.phase)
          searchRssi.postValue(tagInfo.rssi)
          val soundThreshold =  if(actualPercent>=90) 8 else if(actualPercent>=66) 16 else if(actualPercent>=33) 32 else 64
          if (actualPercent > 0) {
            counter_for_threshold_percentage_to_sound_beep++
            if (counter_for_threshold_percentage_to_sound_beep >= soundThreshold){//SOUND_THRESHOLD) {
              counter_for_threshold_percentage_to_sound_beep = 0
              //SoundUtils.playSound(context, R.raw.successbeep)
              SoundUtils.searchBeep(context, actualPercent.toInt())
            }
          }
        }
      }
      else if (isActionInventory && chkTrue(isInventoryOn.value) && !isMaxScanLimitReached) {
          showLog("epc3", tagInfo.epc)
          //inventoryEngine.onTagReceived(tagInfo)
          val result= tagBuffer?.trySend(enrichTag(tagInfo))
          if(result?.isFailure == true) showLog("scanChannel_dropped", "Tag dropped — channel full")
        /*if(chkNull(topic,TopicConstants.INVENTORY).equals(TopicConstants.INVENTORY)) tagBuffer.send(tagInfo)
       else *///saveToDB(tagInfo, chkNull(topic,TopicConstants.INVENTORY),isPublishToMqtt && !isUnencoded && !isAlien)
      }
      else if (isActionTagVerify && chkTrue(isTagVerifyOn.value)) {
        updateVerifiedToDB(tagInfo)
      }
    }
  }

  private fun safeFloat(value: String?, default: Float): Float {
    return value
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
      ?.toFloatOrNull()
      ?: default
  }

  protected fun getPercentage(value: String): Int {
    var value = value.toInt()
    value = abs(value)
    var a = 0
    when (value) {
      15 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      16 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      17 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      18 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      19 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      20 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      21 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      22 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      23 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      24 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      25 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      26 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      27 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      28 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      29 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      30 -> {
        a = 100
        a = 100
        a = 100
        a = 100
        a = 100
      }

      31 -> {
        a = 100
        a = 100
        a = 100
        a = 100
      }

      32 -> {
        a = 100
        a = 100
        a = 100
      }

      33 -> {
        a = 100
        a = 100
      }

      34 -> a = 100
      35 -> {
        a = 99
        a = 99
        a = 99
        a = 99
        a = 99
      }

      36 -> {
        a = 99
        a = 99
        a = 99
        a = 99
      }

      37 -> {
        a = 99
        a = 99
        a = 99
      }

      38 -> {
        a = 99
        a = 99
      }

      39 -> a = 99
      40 -> a = 98
      41 -> a = 97
      42 -> a = 96
      43 -> a = 94
      44 -> a = 92
      45 -> a = 90
      46 -> a = 89
      47 -> a = 87
      48 -> a = 85
      49 -> a = 84
      50 -> a = 82
      51 -> a = 79
      52 -> a = 75
      53 -> a = 72
      54 -> a = 70
      55 -> a = 67
      56 -> a = 65
      57 -> a = 62
      58 -> a = 60
      59 -> a = 57
      60 -> a = 54
      61 -> a = 51
      62 -> a = 48
      63 -> a = 43
      64 -> a = 40
      65 -> a = 36
      66 -> a = 33
      67 -> a = 31
      68 -> a = 29
      69 -> a = 27
      70 -> a = 25
      71 -> a = 23
      72 -> a = 21
      73 -> a = 19
      74 -> a = 17
      75 -> a = 15
      76 -> a = 13
      77 -> a = 11
      78 -> a = 10
      79 -> a = 8
      80 -> a = 7
      81 -> a = 6
      82 -> a = 5
      83 -> a = 4
      84 -> a = 3
      85 -> a = 2
      86 -> a = 1
      else -> a = 0
    }
    return a
  }

  protected fun getPercentageForGID(value: String): Int {
    var value = value.toInt()
    value = abs(value)
    var a = 0
    when (value) {
      15, 16, 17, 18, 19, 20, 21, 22, 23, 24 ->                 //pantaloons
        a = 100

      25 -> a = 100
      26 -> a = 100
      27 -> a = 100
      28 -> a = 100
      29 -> a = 100
      30 -> a = 99
      31 -> a = 98
      32 -> a = 97
      33 -> a = 96
      34 -> a = 95
      35 -> a = 93
      36 -> a = 91
      37 -> a = 89
      38 -> a = 87
      39 -> a = 85
      40 -> a = 84
      41 -> a = 83
      42 -> a = 82
      43 -> a = 81
      44 -> a = 80
      45 -> a = 79
      46 -> a = 78
      47 -> a = 77
      48 -> a = 76
      49 -> a = 75
      50 -> a = 74
      51 -> a = 73
      52 -> a = 70
      53 -> a = 68
      54 -> a = 63
      55 -> a = 60
      56 -> a = 55
      57 -> a = 52
      58 -> a = 50
      59 -> a = 48
      60 -> a = 47
      61 -> a = 44
      62 -> a = 42
      63 -> a = 41
      64 -> a = 40
      65 -> a = 36
      66 -> a = 33
      67 -> a = 30
      68 -> a = 28
      69 -> a = 25
      70 -> a = 22
      71 -> a = 20
      72 -> a = 18
      73 -> a = 15
      74 -> a = 13
      75 -> a = 12
      76 -> a = 11
      77 -> a = 10
      78 -> a = 9
      79 -> a = 8
      80 -> a = 7
      81 -> a = 6
      82 -> a = 5
      83 -> a = 4
      84 -> a = 3
      85 -> a = 2
      86, 87, 88, 89, 90, 91, 92, 93, 94, 95 -> a = 1
      else -> a = 0
    }
    return a
  }

  protected fun getPercentageOld(rssi: String): Float {
    val rssiValue = safeFloat(rssi, -100f)
    showLog("rssiValue",rssi+"=>"+rssiValue.toString())
    var percentValue: Float = calculateRssiPercent(2, ((rssiValue + 100.0f - 20.0f).toDouble() * 2.25).toFloat())
    if (percentValue > 100.0f) percentValue = 100.0f
    else if (percentValue < 1.0f) percentValue = 1.0f
    return percentValue
  }

  private fun calculateRssiPercent(var0: Int, var1: Float): Float {
    return (BigDecimal(var1.toDouble())).setScale(var0, 4).toFloat()
  }

  private fun publishEncodeToMqtt(tagInfo: TagInfoEntity) {
    MqttManager.publishEncode(tagInfo, sessionType,transactionType,deviceSessionId, sessionData)
  }

  private fun publishInventoryToMqtt(tagInfoList: List<TagInfoEntity>) {
    publishInventory(tagInfoList, sessionType,transactionType,deviceSessionId, sessionData)
  }

  private fun publishInventoryToMqtt() {
    var offset = 0
    //val chunkSize = 1000
    //var hasMoreData = true
    while(true){//hasMoreData){
      val chunk = db.tagInfoDao().getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, deviceSessionId, offset)//,chunkSize)
      if (chunk.isNotEmpty()) {
        // 1. Process/Upload your chunk of 1000 records here
        publishInventory(chunk,sessionType,transactionType,deviceSessionId, sessionData)
        // 2. Move to the next chunk
        offset += chunk.size
      } else {
        break
        //hasMoreData = false
      }
    }
    /*handlerScope.launch {
      MqttManager.publishInventory(db.tagInfoDao().getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, deviceSessionId), sessionType, transactionType, deviceSessionId, sessionData)
    }*/
  }
  private fun publishInventoryToMqtt(tagInfo: TagInfoEntity) {
      publishInventory(tagInfo, sessionType,transactionType,deviceSessionId, sessionData)
  }

  private fun publishMovementToMqtt(tagInfo: TagInfoEntity) {
    MqttManager.publishMovement(tagInfo, sessionType,transactionType,deviceSessionId, sessionData)
  }

  private fun publishDecodeToMqtt(tagInfo: TagInfoEntity) {
    showLog("publishDecodeToMqtt",decodeType)
    MqttManager.publishDecode(tagInfo, sessionType,tagInfo.decodeType,transactionType,deviceSessionId)
  }

  protected fun saveDBTagWriteError(
    tagInfo: TagInfoEntity,
    retryCount: Int,
    topic: String,
    @StringRes errMsgId: Int
  ) {
    if (errMsgId != 0) saveDBTagWriteError(tagInfo, retryCount, topic, context.getString(errMsgId))
  }

  protected fun saveDBTagWriteError(
    tagInfo: TagInfoEntity,
    retryCount: Int,
    topic: String,
    errMsg: String
  ) {
    handlerScope.launch {
    tagInfo.isUploaded = false
    tagInfo.retryWriteCount = retryCount
    tagInfo.writeFailReason = errMsg
    tagInfo.isTagWriteDone = false
    tagInfo.tagWriteStatus = StatusConstants.ERROR
    tagInfo.endTime = getCurrentTime()
    if (topic.equals(TopicConstants.DECODE)) {
      tagInfo.transactionType = transactionType
      tagInfo.remark = userRemark
    }
    if(decodeType.isNotEmpty()) tagInfo.decodeType = decodeType
    saveToDB(tagInfo, topic)
    updateTagWriteCount(false, errMsg)
    }
  }

  protected fun getCurrentTime(): String {
    return DateFormatUtils.getCurrentTime();// SimpleDateFormat(SERVER_DATE_TIME_FORMAT).format(Date(System.currentTimeMillis()))
  }

  protected fun saveDBTagWriteSuccess(tagInfo: TagInfoEntity, retryCount: Int, topic: String) {
    handlerScope.launch {
    tagInfo.isUploaded = false
    tagInfo.retryWriteCount = retryCount
    tagInfo.writeFailReason = ""
    tagInfo.isTagWriteDone = true
    tagInfo.tagWriteStatus = StatusConstants.COMPLETED
    tagInfo.endTime = getCurrentTime()
    if (topic.equals(TopicConstants.DECODE)) {
      tagInfo.transactionType = transactionType
      tagInfo.remark = userRemark
    }
    if(decodeType.isNotEmpty()) tagInfo.decodeType = decodeType
    LogUtils.showLog("tagInfo",decodeType+"--"+tagInfo.decodeType)
    saveToDB(tagInfo, topic)
    updateTagWriteCount(true)
    if (topic.equals(TopicConstants.ENCODE, true)) publishEncodeToMqtt(tagInfo)
    if (topic.equals(TopicConstants.DECODE, true)) publishDecodeToMqtt(tagInfo)
    if (topic.equals(TopicConstants.MOVEMENT, true)) publishMovementToMqtt(tagInfo)
    //updateTagWriteCount(true)
      //decodeType=""
    }
  }

  private fun updateTagWriteCount(isTagWriteSuccess: Boolean, errMsg: String = "") {
    showLog("updateTagWriteCount","method_"+isTagWriteSuccess+"_"+errMsg)
    //handlerScope.launch {
    val isMultiWrite = multiWriteListSize > 0
    showLog("updateTagWriteCount_isMultiWrite",""+isMultiWrite)
    if (isMultiWrite && !isMultiWriteDone) {
      if (isTagWriteSuccess) {
        multiWriteSuccessCount++
        SoundUtils.successBeep()
        //showLog("Done:", "$multiWriteSuccessCount of $multiWriteListSize")
      }
      multiWriteCount--
    }
    showLog("updateTagWriteCount_multiWriteCount",""+multiWriteCount)
    showLog("updateTagWriteCount_isMultiWriteDone0",""+isMultiWriteDone)
    if (multiWriteCount == 0 && !isMultiWriteDone) {
      isMultiWriteDone = true
      showLog("updateTagWriteCount_isMultiWriteDone1",""+isMultiWriteDone)
      val isDone = isTagWriteSuccess || (isMultiWrite && multiWriteSuccessCount > 0)
      showLog("updateTagWriteCount_isDone",""+isDone)
      isTagWriteOn.postValue(false)
      if (isDone) {
        isTagWriteDone.postValue(isDone)
        SoundUtils.successBeep()
      }
      else {
        showLog("updateTagWriteCount_errMsg",""+errMsg)
        setError(chkNull(errMsg, ""))
      }
      //decodeType=""
    }
    //}
  }

  protected fun updateVerifiedToDB(tagInfo: TagInfoEntity) {
    showLog("method", "updateVerifiedToDB")
    handlerScope.launch {
      val tagInfoDao = db.tagInfoDao();
      val isAllVerified = chkNull(tagInfoDao.isAllVerified(sessionType,transactionType).value,false)
      if (isAllVerified == true) {
        showLog("isAllVerified", "" + true);
        stopOperations()
        //SoundUtils.successBeep()
      }
      else if (tagInfo.epc.isNotEmpty() && tagInfo.tid.isNotEmpty()) {
        showLog("verifyTagInfo", tagInfo.epc + "_" + tagInfo.tid)
        val result = tagInfoDao.updateVerified(sessionType, transactionType, tagInfo.epc, tagInfo.tid)
        if (result > 0 && chkTrue(tagInfoDao.isAllVerified(sessionType, transactionType).value)) {
          showLog("isAllVerified", "" + true);
          stopOperations()
          SoundUtils.successBeep()
        }
        else if (result <= 0 && tagInfoDao.hasTid(sessionType, transactionType, tagInfo.tid)) {
          val tagInfoEntities = ArrayList(tagInfoDao.getByTid(sessionType, transactionType, tagInfo.tid))
          val updatedTagInfoEntities = tagInfoEntities.stream().filter { tagInfoEntity -> tagInfoEntity.isTagWriteDone && !tagInfoEntity.isTagWriteVerified }
              .map { tagInfoEntity ->
                tagInfoEntity.isTagWriteVerified = tagInfoEntities.size > 1 || tagInfoEntity.newEpc.equals(tagInfo.epc)
                tagInfoEntity.tagVerifyStatus =
                  if (tagInfoEntity.newEpc.equals(tagInfo.epc)) StatusConstants.VERIFIED
                  else if (tagInfoEntity.epc.equals(tagInfo.epc)) StatusConstants.NOT_WRITTEN
                  else if (tagInfoEntities.size > 1) StatusConstants.RE_ENCODED else StatusConstants.WRONG_EPC_WRITTEN
                return@map tagInfoEntity
              }.collect(Collectors.toList())
          tagInfoDao.updateAll(updatedTagInfoEntities)
        }
      }
    }
  }

  protected suspend fun saveToDB(tagInfoEntities: List<TagInfoEntity>, topic: String=TopicConstants.INVENTORY, isPostToMqtt: Boolean=isPublishToMqtt && !isUnencoded && !isAlien) {
    showLog("method","saveToDB")
    showLog("isActionInventory_saveToDB",""+isActionInventory)
    if(!isActionInventory) return
    if(!chkTrue(isInventoryOn.value)) return
    if(isMaxScanLimitReached) return
    else{
      /*tagInfoEntities.map { tagInfo ->
        tagInfo.insertTime = getCurrentTime()
        //if(tagInfo.id.isNullOrEmpty() && deviceSessionAssociatedId.isNotEmpty()) tagInfo.id = deviceSessionAssociatedId
        if (tagInfo.sessionId.isNullOrEmpty() && deviceSessionId.isNotEmpty()) tagInfo.sessionId = deviceSessionId
        if (tagInfo.sessionData.isNullOrEmpty() && sessionData.isNotEmpty()) tagInfo.sessionData = sessionData
        if (tagInfo.session_type.isNullOrEmpty() && sessionType.isNotEmpty()) tagInfo.session_type = sessionType
        if (tagInfo.transactionType.isNullOrEmpty() && transactionType.isNotEmpty()) tagInfo.transactionType = transactionType
        if (tagInfo.topic.isNullOrEmpty() && topic.isNotEmpty()) tagInfo.topic = topic
        if (isUpdateFound) tagInfo.isFound = true
        if (!isPostToMqtt) tagInfo.isUploaded = true
      }*/
      if(!isActionInventory) return
      if(!chkTrue(isInventoryOn.value)) return
      if(isMaxScanLimitReached) return
      showLog("method","saveToDB_insert")
      showLog("isActionInventory_saveToDB_insert1",""+isActionInventory)
      val insertedRowIds = db.tagInfoDao().insertAll(tagInfoEntities).filter { l->l>0 }
      //val newlyInserted = insertedRowIds.count { rowId -> rowId > 0 }
      val insertedTags = tagInfoEntities.filterIndexed { i, _ -> insertedRowIds.getOrElse(i) { -1L } > 0 }
      val newlyInserted     = insertedTags.size
      showLog("saveToDB_result",""+newlyInserted)
      if(newlyInserted <= 0) return
      confirmedDbCount += newlyInserted
     /* showLog("saveToDB_topic_isPostToMqtt",topic+"_"+isPostToMqtt)
      if(topic.equals(TopicConstants.INVENTORY) && isPostToMqtt) {
        publishInventoryToMqtt(db.tagInfoDao().getNonUploadedForBackgroundUpload(topic, sessionType, transactionType, deviceSessionId))
      }*/
      if(isUpdateFound){
        val epcs = insertedTags.map { tagInfo -> tagInfo.epc}.distinct()
        val barcodeWiseQty = insertedTags.map { tagInfo -> tagInfo.barcode}.groupingBy {it}.eachCount()
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
    }
  }

    protected suspend fun updateFound(tagInfoEntities:List<TagInfoEntity>){
        if(isUpdateFound){
            val epcs = tagInfoEntities.map { tagInfo -> tagInfo.epc}.distinct()
            val barcodeWiseQty = tagInfoEntities.map { tagInfo -> tagInfo.barcode}.groupingBy {it}.eachCount()
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
    }

  protected suspend fun saveToDB(tagInfo: TagInfoEntity, topic: String, isPostToMqtt: Boolean=true) {
    showLog("method","saveToDB1")
    if(isMaxScanLimitReached) return
    if(topic.equals(TopicConstants.INVENTORY) && !isActionInventory) return
    if(topic.equals(TopicConstants.INVENTORY) && !chkTrue(isInventoryOn.value)) return
    else {
        showLog("TransactionType:-", transactionType)
      //handlerScope.launch {
      //async {
        showLog("saveToDB", "Start")
        /* val totalCount = db.tagInfoDao().getTotalCount(sessionType, transactionType)
        showLog("saveToDB_totalCount",""+totalCount)
        isMaxScanLimitReached = maxScanLimit>0 && totalCount>=maxScanLimit
        showLog("isMaxScanLimitReached2.5",""+isMaxScanLimitReached)*/
        //if(isMaxScanLimitReached) return@launch
        if (tagInfo.epc.isNotEmpty() || tagInfo.newEpc.isNotEmpty()) {
          tagInfo.insertTime = getCurrentTime()
          //if(tagInfo.id.isNullOrEmpty() && deviceSessionAssociatedId.isNotEmpty()) tagInfo.id = deviceSessionAssociatedId
          if (tagInfo.sessionId.isNullOrEmpty() && deviceSessionId.isNotEmpty()) tagInfo.sessionId = deviceSessionId
          if (tagInfo.sessionData.isNullOrEmpty() && sessionData.isNotEmpty()) tagInfo.sessionData = sessionData
          if (tagInfo.session_type.isNullOrEmpty() && sessionType.isNotEmpty()) tagInfo.session_type = sessionType
          if (tagInfo.transactionType.isNullOrEmpty() && transactionType.isNotEmpty()) tagInfo.transactionType = transactionType
          if (tagInfo.topic.isNullOrEmpty() && topic.isNotEmpty()) tagInfo.topic = topic
          if (isUpdateFound) tagInfo.isFound = true
          if (!isPostToMqtt) tagInfo.isUploaded = true
          var result = 0L
          if (topic.equals(TopicConstants.INVENTORY) || isUpdateFound) {
            //if (!db.tagInfoDao().hasEpc(sessionType, transactionType, tagInfo.epc)) {
            showLog("saveToDB", "" + result)
            result = db.tagInfoDao().insertInv(tagInfo)
            if (isPostToMqtt && result > 0) publishInventoryToMqtt(tagInfo)
            //}
          } else
            result = db.tagInfoDao().insert(tagInfo)
          showLog("saveToDB", "" + result)
          //if (result > 0) ++inwScanCount
          //showLog("isMaxScanLimitReached_inwScanCount2", "" + inwScanCount)
          //isMaxScanLimitReached = maxScanLimit>0 && invScanCount>=maxScanLimit
          // showLog("isMaxScanLimitReached2",""+isMaxScanLimitReached)
          //if(isMaxScanLimitReached) return@launch
          //Update other tables if found
          if (isUpdateFound && result > 0) {
            val productZoneDataDao = db.productZoneDataDao()
            val dataQtyDao = db.dataQtyDao()
            /*if(productZoneDataDao.hasBarcodeZone(topic, sessionType, transactionType, tagInfo.barcode, zoneName,zonePath))  productZoneDataDao.updateFoundBarcodeZone(topic, sessionType, transactionType, tagInfo.barcode, zoneName,zonePath)
            else*/ if (productZoneDataDao.hasEpc(topic, sessionType, transactionType, tagInfo.epc)) productZoneDataDao.updateFoundEpc(topic, sessionType, transactionType, tagInfo.epc)
            else if (productZoneDataDao.hasBarcode(topic, sessionType, transactionType, tagInfo.barcode)) productZoneDataDao.updateFoundBarcode(topic, sessionType, transactionType, tagInfo.barcode)
            val hasEpcData = dataQtyDao.hasEpcData(topic, sessionType, transactionType)
            if (hasEpcData && dataQtyDao.hasEpc(topic, sessionType, transactionType, tagInfo.epc)) dataQtyDao.updateFoundEpc(topic, sessionType, transactionType, tagInfo.epc)
            else if (!hasEpcData && dataQtyDao.hasBarcode(topic, sessionType, transactionType, tagInfo.barcode)) {
                val foundQty = db.tagInfoDao().getFoundCount1(topic, sessionType, transactionType, tagInfo.barcode)
                if(foundQty>0) //dataQtyDao.updateFoundBarcode(topic, sessionType, transactionType, tagInfo.barcode)
                    dataQtyDao.updateFoundBarcodeQty1(topic, sessionType, transactionType, tagInfo.barcode,foundQty)
            }
            }
          }
        }
       //}
      //}
    }

  protected fun getUpdatedPasswordList(
    tid: String,
    currentPassword: String,
    passwords: List<String>
  ): List<String> {
    val listPasswords = ArrayList<String>(passwords)
    if (!listPasswords.contains(currentPassword)) listPasswords.add(0, currentPassword)
    if (listPasswords.isEmpty() || (!listPasswords.get(0).equals(defaultTagZeroPassword, true) && NON_PASSWORD_TIDS.contains(tid))) {
      if (listPasswords.contains(defaultTagZeroPassword)) listPasswords.remove(defaultTagZeroPassword)
      listPasswords.add(0, defaultTagZeroPassword)
    }
    else if (!listPasswords.contains(defaultTagZeroPassword)) {
      if (listPasswords.size == 1) listPasswords.add(defaultTagZeroPassword)
      else if (listPasswords.size > 1) listPasswords.add(1, defaultTagZeroPassword)
    }
    return listPasswords;
  }

  protected fun showLog(tag: String, message: String) {
    LogUtils.showLog(tag, message)
  }

  public fun setTriggerPressed(){
    if (!restrictTriggerPress) {
      showLog("restrictTriggerPress", "" + restrictTriggerPress)
      isTriggerPressed.postValue(true)
      checkTimer()
    }
  }


  protected fun checkTimer() {
    restrictTriggerPress = true
    handlerScope.launch {
      delay(500)
      restrictTriggerPress = false
    }
  }
}
