package com.itek.rftaar.reader.zebra

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.R
import com.itek.rftaar.core.common.utils.BaseUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.reader.RFIDHandler
import com.itek.rftaar.utils.CommonUtils
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.isNonEmpty
import com.itek.rftaar.utils.CommonUtils.isNullOrEmpty
import com.zebra.rfid.api3.AccessFilter
import com.zebra.rfid.api3.Antennas
import com.zebra.rfid.api3.Antennas.AntennaRfConfig
import com.zebra.rfid.api3.Antennas.SingulationControl
import com.zebra.rfid.api3.BATCH_MODE
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION
import com.zebra.rfid.api3.ENUM_NEW_KEYLAYOUT_TYPE
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.FILTER_ACTION
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE
import com.zebra.rfid.api3.INVENTORY_STATE
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.LOCK_DATA_FIELD
import com.zebra.rfid.api3.LOCK_PRIVILEGE
import com.zebra.rfid.api3.MEMORY_BANK
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.PreFilters
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.RFIDResults
import com.zebra.rfid.api3.RFIDResults.RFID_API_SUCCESS
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RegionInfo
import com.zebra.rfid.api3.RegulatoryConfig
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.SESSION
import com.zebra.rfid.api3.SL_FLAG
import com.zebra.rfid.api3.START_TRIGGER_TYPE
import com.zebra.rfid.api3.STATE_AWARE_ACTION
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE
import com.zebra.rfid.api3.TAG_FIELD
import com.zebra.rfid.api3.TARGET
import com.zebra.rfid.api3.TagAccess
import com.zebra.rfid.api3.TagData
import com.zebra.rfid.api3.TagDataArray
import com.zebra.rfid.api3.TagStorageSettings
import com.zebra.rfid.api3.TriggerInfo
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Long
import java.util.Locale
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Exception
import kotlin.Int
import kotlin.NullPointerException
import kotlin.NumberFormatException
import kotlin.RuntimeException
import kotlin.String
import kotlin.TODO
import kotlin.Throws

class ZebraRFIDHandler(context: CommonActivity, errMsg: MutableLiveData<String>) : RFIDHandler(context,errMsg),IDcsSdkApiDelegate,Readers.RFIDReaderEventHandler,RfidEventsListener {
  companion object {
    private const val DEVICE_STATUS_CONNECTED: String = "connected"
    private const val DEVICE_STATUS_DISCONNECTED: String = "disconnected"
    private const val DEVICE_BATTERY_LOW: String = "low"
    private const val WRITE_OPERATION_TIMEOUT = 2000//500 //2000;
    private const val READ_OPERATION_TIMEOUT = 1000
  }

  var readers: Readers? = null
  var readerDevice: ReaderDevice? = null
  var availableRFIDReaderList: ArrayList<ReaderDevice>? = null
  var mReader: RFIDReader? = null
  val readername = "(?i)(MC|RFD)";
  var isBlueToothDependent = false
  val eventHandler: RfidEventsListener = this

  override fun onResume() {
    connectTask()
  }

  override fun onConnect() {
    TODO("Not yet implemented")
  }

  override fun onDisconnect() {
    TODO("Not yet implemented")
  }

  override fun initSDK() {
    if (readers == null || mReader == null) createInstance()
    else if (mReader != null && !isConnected()) connectTask()
    else if (mReader != null && isConnected()) configure()
  }

  private fun createInstance() {
    showLog("zebra_method","createInsance")
    // Based on support available on host device choose the reader type
    handlerScope.launch {
      try {
        readers = Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL)
        availableRFIDReaderList = readers?.GetAvailableRFIDReaderList()
        //Latest SDK (2.0.2.86)
        if (isNullOrEmpty(availableRFIDReaderList)) {
          readers?.Dispose()
          readers = null
          readers = Readers(context, ENUM_TRANSPORT.SERVICE_USB)
          availableRFIDReaderList = readers?.GetAvailableRFIDReaderList()
        }
        if (isNullOrEmpty(availableRFIDReaderList)) {
          readers?.Dispose()
          readers = null
          readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
          availableRFIDReaderList = readers?.GetAvailableRFIDReaderList()
          if (availableRFIDReaderList!!.isNotEmpty()) isBlueToothDependent = true
        }
      } catch (e: InvalidUsageException) {
        e.printStackTrace()
        readers?.Dispose()
        readers = null
        try {
          readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
          availableRFIDReaderList = readers?.GetAvailableRFIDReaderList()
          if (availableRFIDReaderList!!.isNotEmpty()) isBlueToothDependent = true
        } catch (ex: InvalidUsageException) {
          ex.printStackTrace()
        }
      }
      if (readers != null && isNonEmpty(availableRFIDReaderList)) {
        showLog("SIZE", availableRFIDReaderList!!.size.toString() + "")
        // if single reader is available then connect it
        if (availableRFIDReaderList!!.size == 1) {
          mReader = availableRFIDReaderList!!.get(0).getRFIDReader()
        } else {
          if (availableRFIDReaderList != null) {
            for (device in availableRFIDReaderList) {
              showLog("zebra_createInstance", "NAME" + device.getName())
              if (device.getName().matches(readername.toRegex())) {
                mReader = device.getRFIDReader()
                break
              }
            }
          }
        }
        if (mReader != null) connectTask()
      }
    }
  }

  private fun connectTask() {
    showLog("zebra_method","connectTask")
    handlerScope.launch {
      val result: String = connect1()
      if (mReader != null && result.equals("CONNECTED", ignoreCase = true)) configure()
      else if (result.contains("Failed to find or connect reader"))
        setError(result)
      else if (result.contains("Connection failed RFID_COMM_OPEN_ERROR"))
        setError(result)
      else if (result.contains("Connection failed null RFID_COMM_OPEN_ERROR"))
        setError(result)
      else if (result.contains("RFID_READER_REGION_NOT_CONFIGURED"))
        connectAndSetRegion("India")
      else {
        if (isConnected()) configure()
        else setError(result)
      }
    }
  }

  override fun connect() {
    showLog("zebra_method","connect")
    handlerScope.launch {
      if (mReader == null) return@launch
      showLog("connect ", mReader!!.getHostName())
      try {
        if (!isConnected()) {
          // Establish connection to the RFID Reader
          mReader!!.connect()
          showLog("CONFIG", "1")
          //return DEVICE_STATUS_CONNECTED
        } else {
          //return DEVICE_STATUS_CONNECTED
        }
      } catch (e: InvalidUsageException) {
        e.printStackTrace()
      } catch (e: OperationFailureException) {
        e.printStackTrace()
        val des = chkNull(e.getResults().toString(),"")
        if (e.getResults() === RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
          setError("RFID_READER_REGION_NOT_CONFIGURED")
          //return "RFID_READER_REGION_NOT_CONFIGURED"
        } else {
          if(!isConnected()) setError("Connection failed " + chkNull(e.getVendorMessage(),"") + " " + des)
          //return if (mReader!!.isConnected()) DEVICE_STATUS_CONNECTED
        }
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun disconnect() {
    showLog("zebra_method","disconnect")
    handlerScope.launch {
      showLog("method","disconnect")
      try {
        if(!isConnected()) return@launch
        stopOperations()
        if(mReader?.Events != null && eventHandler != null)
         mReader?.Events!!.removeEventsListener(eventHandler)
        mReader!!.disconnect()
      } catch (e: InvalidUsageException) {
        e.printStackTrace()
        showLog("MSG", chkNull(e.getVendorMessage(),""))
      } catch (e: OperationFailureException) {
        e.printStackTrace()
        showLog("MSG1", chkNull(e.getVendorMessage(),""))
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
        showLog("MSG2", chkNull(e.message,""))
      }
    }
  }

  override fun isConnected(): Boolean {
    showLog("zebra_method","isConnected")
    var isBluetoothOn = true
    if (isBlueToothDependent) {//||DataStoreManager.getIsDeviceBluetoothDependent()) {
      val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
      isBluetoothOn = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
    }
    return isBluetoothOn && mReader != null && mReader?.Events!=null && mReader?.Events?.rfidConnectionState!=null && mReader!!.isConnected()
  }

  override fun configure() {
    showLog("zebra_method","configure")
    handlerScope.launch { 
      if(!isConnected()) return@launch
      try {
        showLog("zebra_method","configure1")
        showLog("mReader",""+(mReader!=null))
        showLog("mReader_Events",""+(mReader?.Events!=null))
        showLog("mReader_Config",""+(mReader?.Config!=null))

        /*if (type === AppCommonMethods.SessionType.INVENTORY || type === AppCommonMethods.SessionType.ADD_INVENTORY || type === AppCommonMethods.SessionType.BRAND_INVENTORY || type === AppCommonMethods.SessionType.FILTER_INVENTORY || type === AppCommonMethods.SessionType.STOCK_CORRECTION) {
          mReader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP)
        } else {
          mReader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP)
          //reader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
        }*/

        val region: String = mReader?.Config!!.getRegulatoryConfig().getRegion()
        showLog("Region", region)
        showLog("TimeOUT", "" + mReader!!.getTimeout())
        if (!region.uppercase(Locale.getDefault()).matches("^IND[A-Z]*$".toRegex()))
          connectAndSetRegion("INDIA")

        setupEvents()

        setupTrigger()

        clearFilters()
        /*val maxPower = MAX_POWER_TO_SET
        val power =
          if (type === AppCommonMethods.SessionType.INWARD || type === AppCommonMethods.SessionType.OUTWARD) (MIN_POWER_TO_SET * inwDefPowerMultiplier) as Int else if (type === AppCommonMethods.SessionType.INWARD_TOTE) MIN_POWER_TO_SET else if (type === AppCommonMethods.SessionType.DECODING) (MIN_POWER_TO_SET * decodePickMinPowerMultiplier) as Int else chkZero(
            SharedPrefManager.getInt(
              type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(),
              maxPower / 10
            ),
            maxPower / 10
          ) * 10
        showLog("maxPower", "" + maxPower)
        showLog("power", "" + power)
        configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A)
        readerPower.postValue(power / 10)*/

        mReader?.Config!!.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE)
        if (mReader?.getTransport() != "SERVICE_USB") mReader?.Config!!.setBatchMode(BATCH_MODE.DISABLE)
        showLog("Batch_Mode", mReader?.Config!!.getBatchModeConfig().toString())
        setBatteryConfiguration()

        isDeviceConfigured.postValue(true)
        showLog("isDeviceConfigured", "" + true)
        showLog("zebra_method","configure2")
        //checkReaderCapabilities();
      }
      catch (e: InvalidUsageException) {
        e.printStackTrace()
        val vendorMessage = chkNull(e.getVendorMessage(),"")
        showLog("EXC1Config", vendorMessage)
        isDeviceConfigured.postValue(false)
      } catch (e: OperationFailureException) {
        e.printStackTrace()
        //Response timeout
        val vendorMessage = chkNull(e.getVendorMessage(),"")
        showLog("EXCConfig", vendorMessage)
        if (vendorMessage.equals("Charging in Progress-Command Not allowed", ignoreCase = true)) {
          setError(vendorMessage)
        }
        if (vendorMessage.equals("Response timeout", ignoreCase = true)) {
          disconnect()
          setError("Reader Connection failed!\nPlease check the reader and retry.")
        }
        if (vendorMessage.contains("Region Not Set")) {
          connectAndSetRegion("India")
        }

        //if(!vendorMessage.equalsIgnoreCase("Operation In Progress-Command Not Allowed"))
        isDeviceConfigured.postValue(false)
      } catch (e: java.lang.Exception) {
        showLog("EXCConfig", chkNull(e.message,""))
        e.printStackTrace()
        isDeviceConfigured.postValue(false)
      }
    }
  }

  fun setBatteryConfiguration(){
    showLog("zebra_method","setBatteryConfiguration")
    if(!isConnected()) return
    try{
        mReader?.Config!!.getDeviceStatus(true, false, false);
      }
      catch(e:Exception){ e.printStackTrace(); }
  }

  private fun setupEvents(){
    showLog("zebra_method","setupEvents")
    handlerScope.launch {
      try {
        if (mReader == null || mReader?.Events == null) return@launch
        showLog("zebra_method", "setupEvents1")
        // receive events from reader
        mReader?.Events!!.addEventsListener(eventHandler)
        showLog("zebra_method","addEventsListener"+(eventHandler!=null))
        // HH event
        mReader?.Events!!.setHandheldEvent(true)
        // tag event with tag data
        mReader?.Events!!.setBatteryEvent(true)
        mReader?.Events!!.setTagReadEvent(true)
        mReader?.Events!!.setAttachTagDataWithReadEvent(false)
        mReader?.Events!!.setReaderDisconnectEvent(true)
        //mReader.Events.setAntennaEvent(true);
        //mReader.Events.setBatchModeEvent(true);
        mReader?.Events!!.setBufferFullEvent(true)
        mReader?.Events!!.setBufferFullWarningEvent(true)
        //mReader.Events.setGPIEvent(true);
        //mReader.Events.setInventoryStartEvent(true);
        //mReader.Events.setInventoryStopEvent(true);
        //mReader.Events.setOperationEndSummaryEvent(true);
        mReader?.Events!!.setPowerEvent(true)
        showLog("zebra_method","setupEvents2")
      }catch (e: Exception) {e.printStackTrace()}
    }
  }


  private fun setupTrigger(){
    showLog("zebra_method","setupTrigger")
    handlerScope.launch {
      try {
        if (mReader == null || mReader?.Config == null) return@launch
        showLog("zebra_method","setupTrigger1")
        val triggerInfo: TriggerInfo = TriggerInfo();
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);

        // set trigger mode as rfid so scanner beam will not come
        mReader?.Config!!.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true);
        mReader?.Config!!.setKeylayoutType(
          ENUM_NEW_KEYLAYOUT_TYPE.RFID,
          ENUM_NEW_KEYLAYOUT_TYPE.RFID
        );
        // set start and stop triggers
        mReader?.Config!!.setStartTrigger(triggerInfo.StartTrigger);
        mReader?.Config!!.setStopTrigger(triggerInfo.StopTrigger);
        showLog("zebra_method","setupTrigger2")
      }catch (e: Exception) {e.printStackTrace()}
    }
  }

  override fun setPower(power: Int) {
    showLog("zebra_method","setPower_"+power)
    if(!isConnected()) return
    handlerScope.launch {
          var oldPower = 0
          try {
            if (mReader == null || mReader?.Config == null || mReader?.Config?.Antennas == null) return@launch
            showLog("zebra_method","setPower1_"+power)
            //setupTrigger()
            val config: AntennaRfConfig = mReader?.Config?.Antennas!!.getAntennaRfConfig(1)
            oldPower = config.getTransmitPowerIndex() / 10
            config.setTransmitPowerIndex(power * 10)
            config.setrfModeTableIndex(0)
            config.setTari(0)
            mReader?.Config?.Antennas!!.setAntennaRfConfig(1, config)
            readerPower.postValue(power)
            showLog("power", "" + power)
            showLog("zebra_method","setPower2_"+power)
          } catch (e: InvalidUsageException) {
            e.printStackTrace()
            showLog("CONFIGEXC0", chkNull(e.message,""))
            readerPower.postValue(oldPower)
          } catch (e: OperationFailureException) {
            e.printStackTrace()
            showLog("CONFIGEXC0", chkNull(e.message,""))
            readerPower.postValue(oldPower)
          }
        }
  }

  @Throws(OperationFailureException::class, InvalidUsageException::class)
  private fun configAction(power: Int, session: SESSION?, inventory_state: INVENTORY_STATE?): Boolean {
    showLog("zebra_method","configAction")
    if (mReader == null || mReader?.Config == null || mReader?.Config?.Antennas == null) return false
    showLog("zebra_method","configAction1")
    var isPowerSet = false
    var isSessionSet = false
    var isTagFieldsSet = false

    try {
      //showLog(TAG + "_" + sessionType.name() + "_configAction_power", "" + power)
      val config: AntennaRfConfig = mReader?.Config?.Antennas!!.getAntennaRfConfig(1)
      config.setTransmitPowerIndex(power*10)
      config.setrfModeTableIndex(0)
      config.setTari(0)
      mReader?.Config?.Antennas!!.setAntennaRfConfig(1, config)
      isPowerSet = true;
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    //AppCommonMethods.logInFile(context, sessionType.name(), "_CONFIG_SET_POWER (" + power + ")")
    try {
      val tagst = TagStorageSettings()
      if (BaseUtils.isDebuggable()) {
        tagst.setTagFields(TAG_FIELD.PHASE_INFO)
        tagst.setTagFields(TAG_FIELD.ANTENNA_ID)
        tagst.setTagFields(TAG_FIELD.TAG_SEEN_COUNT)
        tagst.setTagFields(TAG_FIELD.CHANNEL_INDEX)
        tagst.setTagFields(TAG_FIELD.XPC)
        tagst.setTagFields(TAG_FIELD.FIRST_SEEN_TIME_STAMP)
        tagst.setTagFields(TAG_FIELD.LAST_SEEN_TIME_STAMP)
        tagst.setTagFields(TAG_FIELD.CRC)
      }
      if(isActionPick) tagst.setTagFields(TAG_FIELD.PC)
      tagst.setTagFields(TAG_FIELD.PEAK_RSSI)
      mReader?.Config!!.setTagStorageSettings(tagst)
      isTagFieldsSet = true;
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }

    try {
      val s1_singulationControl: SingulationControl = mReader?.Config?.Antennas!!.getSingulationControl(1)
      s1_singulationControl.setSession(session)
      s1_singulationControl.Action.setInventoryState(inventory_state)
      s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL)
      s1_singulationControl.setTagPopulation(200.toShort())
      mReader?.Config?.Antennas!!.setSingulationControl(1, s1_singulationControl)
      mReader?.Config!!.setUniqueTagReport(false)
      if(isActionInventory || isActionTagVerify) mReader?.Config!!.setUniqueTagReport(true)
      isSessionSet = true
      /*if(sessionType==AppCommonMethods.SessionType.OFF_RANGE && sessionAction== AppCommonMethods.SessionAction.INVENTORY)
        reader.Config.setUniqueTagReport(false);*/
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return isPowerSet && isTagFieldsSet && isSessionSet
  }

  override fun getPower(): Int {
    if (isConnected() && mReader != null && mReader?.Config != null && mReader?.Config?.Antennas != null){
      try {
        return mReader?.Config?.Antennas!!.getAntennaRfConfig(1).transmitPowerIndex / 10;
      } catch (e: Exception) { e.printStackTrace(); }
    }
    return -1
  }

  override fun clearFilter(): Int {
    TODO("Not yet implemented")
  }

  /**
   * Connect string.
   *
   * @return the string
   */
  fun connect1(): String {
    if (mReader != null) {
      //showLog(TAG, "connect " + mReader.getHostName())
      try {
        if (!isConnected()) {
          // Establish connection to the RFID Reader
          connect()
          return "CONNECTED"
        } else {
          return "CONNECTED"
        }
      } catch (e: InvalidUsageException) {
        e.printStackTrace()
      } catch (e: OperationFailureException) {
        e.printStackTrace()
        val des = chkNull(e.results.toString(),"")
        return if (e.results === RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
          "RFID_READER_REGION_NOT_CONFIGURED"
        } else {
          if (isConnected()) "CONNECTED"
          else "Connection failed " + chkNull(e.vendorMessage,"") + " " + des
        }
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
    return ""
  }

  /**
   * Connect and set region.
   *
   * @param region the region
   */
  fun connectAndSetRegion(region: String?) {
    showLog("zebra_method","connectAndSetRegion")
    //showLog(TAG, "ConnectAndSetRegion")
    this.isActionTagVerify = false
    this.isActionInventory = false
    this.isActionSearch = false
    this.isActionTIDSearch = false
    this.isActionEPCSearch = false
    this.isLockSearchEPC = false
    this.searchEpc = ""
    this.searchBarcode = ""
    this.searchLockedEpc = ""
    //setProgressMessage(true)
    handlerScope.launch {
      var selectedRegionInfo: RegionInfo? = null
      try {
        val a: Int = chkNull(mReader?.ReaderCapabilities?.SupportedRegions?.length(), 0)
        for (reagions in 0 until a) {
          selectedRegionInfo = mReader?.Config?.getRegionInfo(
            mReader?.ReaderCapabilities?.SupportedRegions?.getRegionInfo(reagions)
          )
          val channelname = selectedRegionInfo?.name
          if (channelname.equals(region, ignoreCase = true)) {
            break
          }
        }
        if (selectedRegionInfo != null) {
          val regulatoryConfig: RegulatoryConfig? = mReader?.Config?.getRegulatoryConfig()
          regulatoryConfig?.region = selectedRegionInfo.regionCode
          regulatoryConfig?.setIsHoppingOn(selectedRegionInfo.isHoppingConfigurable)
          regulatoryConfig?.setEnabledChannels(selectedRegionInfo.supportedChannels)
          mReader?.Config?.setRegulatoryConfig(regulatoryConfig)
        }
      } catch (invalidUsageException: InvalidUsageException) {
        invalidUsageException.printStackTrace()
      } catch (operationFailureException: OperationFailureException) {
        operationFailureException.printStackTrace()
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun checkAndSetReader() {
    checkReaderConnection()
  }

  override fun checkAndConnectReader() {
    checkReaderConnection()
  }

  fun checkReaderConnection() {
    if (readers == null) createInstance()
    else if (readers != null && mReader == null) initSDK()
    else if (mReader != null && !isConnected()) connectTask()
    else if (isDeviceConfigured.value==false) configure()
  }

  override fun startInventory(): Boolean {
    showLog("zebra_method","startInventory")
    try{
      if (readTid) { // tid based Inventory (Epc + Tid mode)
       val tagAccess = TagAccess()
       val readAccessParams = tagAccess.ReadAccessParams()
       //Set the param values
       readAccessParams.setCount(0)
       readAccessParams.setOffset(0)
       readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID)
       mReader?.Actions?.TagAccess!!.readEvent(readAccessParams, null, null)
      }
      else mReader?.Actions?.Inventory!!.perform()
      return true
    }
    catch(e:InvalidUsageException){
      e.printStackTrace();
      val vendorMessage = chkNull(e.getVendorMessage(),"")
      showLog("EXC1INVENTORY",vendorMessage);
      stopOperations()
      setError(vendorMessage)
    }
    catch(e:OperationFailureException){
      e.printStackTrace();
      //Response timeout
      val vendorMessage = chkNull(e.getVendorMessage(),"")
      showLog("EXCINVENTORY", vendorMessage);
      if(vendorMessage.equals("Charging in Progress-Command Not allowed",true)){
        setError(vendorMessage)
      }
      if(vendorMessage.equals("Response timeout",true)){
        disconnect();
        setError(context.getString(R.string.err_reader_connection))
      }
      if(vendorMessage.contains("Region Not Set",true)){
        connectAndSetRegion("India");
      }
      if(!vendorMessage.equals("Operation In Progress-Command Not Allowed",true)){
        stopOperations()
        setError(vendorMessage)
      }
    }
    catch(e:Exception){
      e.printStackTrace();
    }
    return false
  }

  override fun getTagInfo(scanned: Any): TagInfoEntity {
    showLog("zebra_method","getTagInfo")
    if(scanned is TagInfoEntity) return scanned
    if(scanned is TagData){
      val tagInfoEntity = TagInfoEntity(sessionType=sessionType,transactionType=transactionType,epc=scanned.tagID,tid=if(scanned.memoryBank==MEMORY_BANK.MEMORY_BANK_TID) chkNull(scanned.memoryBankData,"") else "", rssi = chkNull(scanned.peakRSSI.toString(),""),pc=chkNull(scanned.pc.toString(),""))
      if(scanned.phase!=null) tagInfoEntity.phase= scanned.phase.toString()
      return tagInfoEntity
    }
    return TagInfoEntity()
  }

  private fun clearFilters() {
    showLog("zebra_method","clearFilters")
    if (mReader == null || mReader?.Actions == null || mReader?.Actions?.PreFilters == null || mReader?.Actions?.PreFilters!!.length() <= 0) return
    try {
      showLog("clearFilters0", "" + mReader?.Actions?.PreFilters!!.length())
      mReader?.Actions?.PreFilters?.deleteAll()
      mReader?.Actions?.purgeTags()
      showLog("clearFilters1", "" + mReader?.Actions?.PreFilters!!.length())
      setSingulationControlPrefilterReset();
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  override fun stopScanning() {
    showLog("zebra_method","stopScanning")
    handlerScope.launch {
      // check reader connection
      if (!isConnected()) return@launch
      try {
        clearFilters();
        if (mReader != null && mReader?.Actions != null && mReader?.Actions?.Inventory != null)
          mReader?.Actions?.Inventory!!.stop();
      } catch (e: InvalidUsageException) {
        showLog("STOPEXC1", chkNull(e.vendorMessage,""));
      } catch (e: OperationFailureException) {
        e.printStackTrace();
        showLog("STOPEXC2", chkNull(e.vendorMessage,""));
        showLog("STOPEXC2", chkNull(e.message, ""));
        showLog("STOPEXC2", chkNull(e.localizedMessage,""));
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun startSearch(): Boolean {
    showLog("zebra_method","startSearch")
    return startInventory()
  }

  override fun setupInventory(invPower: Int?, readTid: Boolean): Boolean {
    showLog("zebra_method","setupInventory")
    try {
      val maxPower: Int = MAX_POWER_TO_SET
      var power: Int = chkNull(invPower, maxPower)
      //mReader?.Actions?.PreFilters!!.deleteAll()
      clearFilters()
      mReader?.Config?.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE)
      return configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A)
    }catch (e: Exception) {e.printStackTrace();}
    return false
  }

  override fun setupTagWrite(tagWritePower: Int?): Boolean {
    showLog("zebra_method","setupTagWrite")
    try {
      val maxPower: Int = MAX_POWER_TO_SET
      var power: Int = chkNull(tagWritePower, maxPower)
      //mReader?.Actions?.PreFilters!!.deleteAll()
      mReader?.Config?.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE)
      clearFilters()
      setSingulationControl()
      setStopTriggerRead()
      return configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A)
    }catch (e: Exception) {e.printStackTrace();}
    return false
  }

  override fun setupPick(pickPower: Int?): Boolean {
    showLog("zebra_method","setupPick_"+pickPower)
    try {
      val maxPower: Int = 15
      var power: Int = chkNull(pickPower, maxPower)
      //mReader?.Actions?.PreFilters!!.deleteAll()
      mReader?.Config?.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE)
      clearFilters()
      return configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A)
    }catch (e: Exception) {e.printStackTrace();}
    return false
  }

  override fun setupSearch(isTidSearch: Boolean): Boolean {
    try {
     /* val maxPower: Int = MAX_POWER_TO_SET
      var power: Int = chkNull(searchPower, maxPower)*/
      mReader?.Config?.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE)
      clearFilters()
      return configAction(MAX_POWER_TO_SET, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_B)
    }catch (e: Exception) {e.printStackTrace();}
    return false
  }

  override fun setSearchFilterBarcode(epc: String, isBC: Boolean, isSearchDecoded: Boolean): Boolean {
    if(!isConnected()) return false
    // Add state aware pre-filter
    val filters = PreFilters()
    val filter = filters.PreFilter()
    filter.setAntennaID(1.toShort()) // Set this filter for Antenna ID 1
    filter.setTagPattern(epc) // Tags which starts with passed pattern
    filter.setTagPatternBitCount(epc.length * 4) // set tag pattern length
    if (isBC) {
        //68 =>substring(9) 80=>substring(12)
        filter.setBitOffset(68) // skip PC bits (always it should be in bit length)
      } else {
        filter.setBitOffset(32) // skip PC bits (always it should be in bit length)
        if(isSearchDecoded){
          val epc1=epc.substring(1)
          filter.setTagPatternBitCount(epc1.length * 4)
          filter.setTagPattern(epc1)
          filter.setBitOffset(32+4)
        }
      }

    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC)
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE) // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0) // inventoried flag of session S1 of matching tags to B
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A)

      // not to select tags that match the criteria
    try {
        if (mReader != null && mReader!!.isConnected() && mReader?.Actions != null && mReader?.Actions?.PreFilters != null) {
          mReader?.Actions?.PreFilters!!.add(filter)
          showLog("EPC_FILTER_SET", epc)
          return true;
        }
      } catch (e: InvalidUsageException) {
        e.printStackTrace()
      } catch (e: OperationFailureException) {
        e.printStackTrace()
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
    return false
  }

  override fun setSearchFilterEpc(epc: String, isSearchDecoded: Boolean): Boolean {
    if (!isConnected()) return false

    // Add state aware pre-filter
    val filters = PreFilters()
    val filter = filters.PreFilter()
    filter.setAntennaID(1.toShort()) // Set this filter for Antenna ID 1
    //search Decoded + Non-Decoded EPCs in same filter (Now Commented)
    if(isSearchDecoded){//searchDecoded){
      val epc1=epc.substring(1)
      filter.setTagPatternBitCount(epc1.length * 4)
      filter.setTagPattern(epc1)
      filter.setBitOffset(32+4)
    }
    else {
      filter.setTagPattern(epc) // Tags which starts with passed pattern
      filter.setTagPatternBitCount(epc.length * 4)
      filter.setBitOffset(32) // skip PC bits (always it should be in bit length)
    }

    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC)
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE) // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0) // inventoried flag of session S1 of matching tags to B
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A)

    // not to select tags that match the criteria
    try {
      if (mReader != null && mReader!!.isConnected() && mReader?.Actions != null && mReader?.Actions?.PreFilters != null) {
        mReader?.Actions?.PreFilters!!.add(filter)
        return true;
      }
    } catch (e: InvalidUsageException) {
      e.printStackTrace()
    } catch (e: OperationFailureException) {
      e.printStackTrace()
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return false
  }

  override fun setSearchFilterTid(tid: String): Boolean {
    if(!isConnected()) return false
    // Add state aware pre-filter
    val filters = PreFilters()
    val filter = filters.PreFilter()
    filter.setAntennaID(1.toShort()) // Set this filter for Antenna ID 1
    filter.setTagPattern(tid) // Tags which starts with passed pattern
    filter.setTagPatternBitCount(tid.length * 4)
    filter.setBitOffset(0) // skip PC bits (always it should be in bit length)

    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID)
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE) // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0) // inventoried flag of session S1 of matching tags to B
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A)

    //filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    // not to select tags that match the criteria
    try {
      if (mReader != null && mReader!!.isConnected() && mReader?.Actions != null && mReader?.Actions?.PreFilters != null){
        mReader?.Actions?.PreFilters!!.add(filter)
        showLog("TID_FILTER_SET", tid)
        return true
      }
    } catch (e: InvalidUsageException) {
      e.printStackTrace()
    } catch (e: OperationFailureException) {
      e.printStackTrace()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }


  private fun setPrefilter(tagId:String, storeRestore :Boolean):RFIDResults{
    showLog("setPrefilter_START", tagId);
    /*val filterList: = PreFilters.PreFilter[]
    PreFilters.PreFilter[] filterList = PreFilters.PreFilter[1];*/
    //val filterList: Array<PreFilters.PreFilter> = Array(1) { PreFilters.PreFilter() }
    //val filterList = Array(1) { PreFilters.PreFilter() }

    //val filters = PreFilters()
    val filter = PreFilters().PreFilter()
    filter.setAntennaID(1);// Set this filter for Antenna ID 1

    if(tagId.length < 16){
      return RFID_API_SUCCESS;
    }
    val subTag:String = tagId.substring(16); // 8 words, cut 4 word = 8 bytes = 16 char
    filter.setTagPattern(subTag);
    filter.setTagPatternBitCount(subTag.length * 4); // 4 words len =  8 byte = 16 char, subTag leb = 16 char = 16*4 = 64;
    filter.setBitOffset((2 + 4) * 2 * 8); // 2 word + 4 word data = *2 byte = *8 bit
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);

    //INV B and Action=B
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    //filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B);
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);

    val filterList: Array<PreFilters.PreFilter> = Array(1) { filter }
    filterList[0] = filter;
    // not to select tags that match the criteria
    try{
      mReader?.Actions?.PreFilters!!.add(filterList, null);
      //reader.Actions.PreFilters.add(filter);
    }
    catch(e:InvalidUsageException){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    catch(e:OperationFailureException){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    return RFID_API_SUCCESS;
  }

  private fun setPrefilterTid(tagId:String, storeRestore:Boolean):RFIDResults{
    showLog("setPrefilterTID_START", tagId);
    //val filterList: PreFilters.PreFilter[] = PreFilters.PreFilter[1];
    //val filters = PreFilters()
    val filter = PreFilters().PreFilter()
    filter.setAntennaID(1);// Set this filter for Antenna ID 1

    //    if(tagId.length() < 16){
    //      return RFID_API_SUCCESS;
    //    }
    //String subTag = tagId.substring(16); // 8 words, cut 4 word = 8 bytes = 16 char
    filter.setTagPattern(tagId);
    filter.setTagPatternBitCount(tagId.length * 4); // 4 words len =  8 byte = 16 char, subTag leb = 16 char = 16*4 = 64;
    filter.setBitOffset(0); // 2 word + 4 word data = *2 byte = *8 bit
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);

    //INV B and Action=B
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    //filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B);
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);

    val filterList: Array<PreFilters.PreFilter> = Array(1) { filter }
    filterList[0] = filter;
    // not to select tags that match the criteria
    try{
      mReader?.Actions?.PreFilters!!.add(filterList, null);
      //mReader?.Actions?.PreFilters!!.add(filter);// null);
    }
    catch(e:InvalidUsageException){
      e.printStackTrace();
      showLog("setPrefilterTid_Error", chkNull(e.vendorMessage,"--"))
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    catch(e:OperationFailureException){
      showLog("setPrefilterTid_Error", chkNull(e.vendorMessage,"--"))
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    catch (e: Exception){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    showLog("setPrefilterTid_Done", "Success")
    return RFID_API_SUCCESS;
  }

  private fun setStopTrigger(){
    val triggerInfo = TriggerInfo();
    triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
    triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_ACCESS_N_ATTEMPTS_WITH_TIMEOUT);
    val stopTrigger = triggerInfo.StopTrigger;
    stopTrigger.AccessCount.setN(1);
    stopTrigger.AccessCount.setTimeout(500);
    try{
      mReader?.Config!!.setStopTrigger(stopTrigger);
    }
    catch(e:InvalidUsageException){
      throw RuntimeException(e);
    }
    catch(e:OperationFailureException){
      throw RuntimeException(e);
    }
  }

  private fun setStopTriggerRead(){
    val triggerInfo = TriggerInfo();
    triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
    triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT);
    val stopTrigger = triggerInfo.StopTrigger;
    stopTrigger.TagObservation.setTimeout(100);
    stopTrigger.TagObservation.setN(1);

    try{
      mReader?.Config!!.setStopTrigger(stopTrigger);
    }
    catch(e:InvalidUsageException){
      throw RuntimeException(e);
    }
    catch(e:OperationFailureException){
      throw RuntimeException(e);
    }

  }

  private fun setSingulationControlPrefilterReset(){
    try{
      LogUtils.showLog("setSingulationControl", "SingulationControl...");

      val singulationControl = Antennas.SingulationControl();

      singulationControl.setSession(SESSION.SESSION_S1); //to do
      singulationControl.setTagPopulation(200);
      singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
      //mRfidReader.Config.Antennas.setSingulationControl(1, singulationControl);
      mReader?.Config?.Antennas!!.setSingulationControl(1, singulationControl);
      mReader?.Actions?.PreFilters!!.deleteAll();
      clearFilters();
      val triggerInfo = TriggerInfo();
      triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
      triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
      try{
        mReader?.Config!!.setStopTrigger(triggerInfo.StopTrigger);
      }
      catch(e: InvalidUsageException){
        throw RuntimeException(e);
      }
      catch(e: OperationFailureException){
        throw RuntimeException(e);
      }
    }
    catch(e:InvalidUsageException){
      e.printStackTrace();
    }
    catch(e:OperationFailureException){
      e.printStackTrace();
    }
  }

  private fun setSingulationControl(){
    try{
      LogUtils.showLog("setSingulationControl", "SingulationControl S0 INV_B");

      val singulationControl = Antennas.SingulationControl();

      //Prefilter Action B and S0
      singulationControl.setSession(SESSION.SESSION_S0);
      singulationControl.setTagPopulation(64);
      singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_B);

      //mRfidReader.Config.Antennas.setSingulationControl(1, singulationControl);
      mReader?.Config?.Antennas!!.setSingulationControl(1, singulationControl);
    }
    catch(e:InvalidUsageException){
      e.printStackTrace();
    }
    catch(e:OperationFailureException){
      e.printStackTrace();
    }
  }

  override fun startEncoding(
    tagInfo: TagInfoEntity,
    currentPassword: String,
    passwords: List<String>,
    retryCount: Int,
    writeEpc: String,
    offset: Int,
    dataLen: Int
  ) {

    showLog("startEncoding_tagInfo",tagInfo.toString())
    showLog("dataLen",""+dataLen)
    showLog("offSet",""+offset)
    val listPasswords = getUpdatedPasswordList(tagInfo.tid,currentPassword,passwords)
    //val filterSet= setSearchFilterTid(tagInfo.tid);
    val rfidResults = setPrefilterTid(tagInfo.tid, true);
    showLog("setPrefilterTid_rfidResults",""+rfidResults)
    val filterSet= rfidResults.equals(RFID_API_SUCCESS)
    showLog("TidfilterSet",""+filterSet)
    if(!filterSet) return
    var isWriteSuccess = false
    val results = ArrayList<String>(0)
    for (pass in listPasswords) {
      if(isNullOrEmpty(pass)) continue
      showLog("pass",""+pass)
      val result = writeEpc(pass,  tagInfo.tid, offset, dataLen, writeEpc)
      showLog("result",""+result)
      if(result.equals("Success",true)) {
        //insert Data in table.
        isWriteSuccess = true
        showLog("isWriteSuccess",""+isWriteSuccess)
        try {
          if(!NON_PASSWORD_TIDS.contains(tagInfo.tid) && !currentPassword.equals(defaultTagZeroPassword) && !pass.equals(currentPassword)) {
            if(writeReserved(pass,tagInfo.epc, newPassword = currentPassword)) {
              val result = lockReservedMemory(currentPassword, tagInfo.epc)
              showLog("lockReservedMemory",""+result);
            }
            else showLog("writeReserved",""+false);
          }
        }
        catch (e:Exception) {
          e.printStackTrace()
        }
        break
      }
      else results.add(result)
    }
    if (!isWriteSuccess) {
      showLog("LOCKMEMORY1", "FAIL")
      if(retryCount<tagWriteRetryLimit) startEncoding(tagInfo,currentPassword,passwords,retryCount+1,writeEpc,offset,dataLen)
      else {
        val reason = if(results.isNullOrEmpty() || results.contains(context.getString(R.string.err_encoding_auth_fail))) context.getString(R.string.err_encoding_auth_fail)
        else if (results.size==1) results.get(0) else chkNull(CommonUtils.getMostRepeatedValue(results),results.get(0))
        saveDBTagWriteError(tagInfo,retryCount,TopicConstants.ENCODE,reason)
      }
    }
    else {
      saveDBTagWriteSuccess(tagInfo,retryCount,TopicConstants.ENCODE)
    }
  }

  private fun writeEpc(password: String, tid: String, offSet:Int, dataLen:Int, writeEpc:String) : String{
    showLog("writeEpc",""+offSet+"_"+writeEpc);
    val writeData = if (offSet==1) writeEpc.substring(4) else writeEpc
    val writeDataPC = if (offSet==1) writeEpc.substring(0, 4) else ""
    val iTotalWriteLengthWORDPC = (writeDataPC.length / 4)
    val iTotalWriteLengthWORDEPC = (writeData.length / 4)
    val tagData = TagData()
    //val tagAccess = TagAccess()


    /**////////////////////////////////////////// */
    // Step 5: Write 8 tags
    /**////////////////////////////////////////// */
    var result: String = ""
    try {
      val tagData1 = TagData()
      if (isNonEmpty(writeDataPC)) { //Write PC Data
        showLog("writingData PC", writeDataPC + "_" + iTotalWriteLengthWORDPC)
        val writeAccessParams = TagAccess().WriteAccessParams()
        writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC)
        writeAccessParams.setOffset(1)
        writeAccessParams.setWriteData(writeDataPC)
        writeAccessParams.setWriteDataLength(iTotalWriteLengthWORDPC)
        writeAccessParams.setAccessPassword(Long.decode("0X" + password))
        showLog("writeDataPC",  writeDataPC + "_" + iTotalWriteLengthWORDPC)
        mReader?.Actions?.TagAccess!!.write(
          null,
          writeAccessParams,
          null,
          tagData1,
          true,
          WRITE_OPERATION_TIMEOUT
        )
      }
      val writeAccessParams = TagAccess().WriteAccessParams()
      showLog("writingData Epc", writeData + "_" + iTotalWriteLengthWORDEPC + " with pass:" + password)
      writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC)
      writeAccessParams.setOffset(2)
      writeAccessParams.setWriteData(writeData)
      writeAccessParams.setWriteDataLength(iTotalWriteLengthWORDEPC)
      writeAccessParams.setAccessPassword(Long.decode("0X" + password))

      val accessFilter = AccessFilter()
      accessFilter.TagPatternA.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
      accessFilter.TagPatternA.setBitOffset(0);
      accessFilter.TagPatternA.setTagPatternBitCount(tid.length*8);
      accessFilter.TagPatternA.setTagMask(tid);
      accessFilter.TagPatternA.setTagMaskBitCount(tid.length*8);
      //accessFilter.setAccessFilterMatchPattern()
      //accessFilter.accessFilterMatchPattern=AccessFilter.

      //val tagData = TagData()
      mReader?.Actions?.TagAccess!!.writeblock(
        null,
        writeAccessParams,
        null,
        tagData,
        true,
        WRITE_OPERATION_TIMEOUT
      )
      result = "Success"
      //updateTagWriteCount(true);
      //LogUtils.showLog("ECRT", "### DONE Read= " + tag.getTagID());
      LogUtils.showLog("ECRT", "\r\n")
    } catch (e: InvalidUsageException) {
      e.printStackTrace()
      result = chkNull(e.getLocalizedMessage(),"")
      showLog("Write Failed:", result)
      //AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result)
      //result = "Failed";
      // tiempo = (System.currentTimeMillis() - tiempo);
      // iErrorExcetion++;
      //e.printStackTrace();
    } catch (e: OperationFailureException) {
      e.printStackTrace()
      result = chkNull(e.getVendorMessage(),"")
      showLog("Write Failed:", result)
      //AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result)
      //tiempo = (System.currentTimeMillis() - tiempo);
      //result = "Failed";
      //iErrorExcetion++;
      //final String sQFE = e1.getVendorMessage();
      //showLog("ECRT", "QFE=" + sQFE + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
      //listener.updateUI(tag.getTagID() + ",OFE=" + e1.getVendorMessage() + ",ms=" + tiempo);
    }
    catch (e: NullPointerException) {
      e.printStackTrace()
      result = chkNull(e.message,"")
      showLog("Write Failed:", result)
      //AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result)
    }
    catch (e: Exception) {
      e.printStackTrace()
      result = chkNull(e.message,"")
      showLog("Write Failed:", result)
      //AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result)
    }
    showLog("result1", result)
    showLog("TAG WRITE RESULT", result + " Returned TagId " + tagData.getTagID())
    showLog("TAG WRITE RESULT", result + " OpStatus: " + tagData.getOpStatus())
    showLog("TAG WRITE RESULT", result + " numofwordswritten " + tagData.getNumberOfWords())

    /**////////////////////////////////////////// */
    // Step 6: Write Results
    /**////////////////////////////////////////// */
    if (tagData != null && isNonEmpty(tagData.getTagID()) && tagData.getTagID().equals(writeEpc,true)) {
      return result//.equals("Success", ignoreCase = true)
    }
    /*else if(tagData!=null && isNonEmpty(tagData.getTagID()) && !tagData.getTagID().equals(writeEpc,true)){
      result = "write failed"
      return result
    }*/

    if (tagData != null && tagData.getOpStatus() != null) {
      //val iWrittenWord = tagData.getNumberOfWords();
      //showLog("Originaln EPC", result + " TagId " + epcdt)
      showLog("TAG WRITE RESULT", result + " Returned TagId " + tagData.getTagID())
      showLog("TAG WRITE RESULT", result + " OpStatus: " + tagData.getOpStatus())
      showLog("TAG WRITE RESULT", result + " numofwordswritten " + tagData.getNumberOfWords())

      showLog("TAG WRITE RESULT", "\r\n")

      val iWritten = tagData.getNumberOfWords()

      if (iWritten == iTotalWriteLengthWORDEPC) {
        return result
        // iWriteOK++;
        // LogUtils.showLog("ECRT", "Passed=" + iWriteOK);
        // listener.updateUI(tag.getTagID() + " " + tagData.getOpStatus().toString() + " " + tiempo + ", Written=" + iWritten);
      }
      else {
        // if(iWritten > 0) iWritePartial++;
        val sError = tagData.getOpStatus().toString()
        //AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING_failed->" + sError)

        showLog(
          "sError",
          "NEW_EPC=" + writeEpc + ", WRITTEN=" + tagData.getTagID() + ", Error=" + sError + " ,Written=" + iWritten
        )

        if (sError.contains("ACCESS_TAG_PASSWORD_ERROR")) return context.getString(R.string.err_encoding_auth_fail)
        if (sError.contains("ACCESS_NO_RESPONSE_FROM_TAG")) return context.getString(R.string.err_encoding_tag_response_fail)
        if (sError.contains("ACCESS_INSUFFICIENT_POWER")) return context.getString(R.string.err_encoding_access_power_insufficient)
        if (sError.contains("ACCESS_TAG_MEMORY_OVERRUN_ERROR")) return context.getString(R.string.err_encoding_overrun_fail)
        if (sError.contains("ACCESS_TAG_CRC_ERROR")) return context.getString(R.string.err_encoding_crc_fail)
        if (sError.contains("ACCESS_TAG_MEMORY_LOCKED_ERROR")) context.getString(R.string.err_encoding_fail_locked_memory)
        return sError
        // showLog("isRe" + logTag + "e", "" + isRetryEncode)
        /*if (iWritten > 1 && iWritten < iTotalWriteLengthWORDEPC){// && isRetryEncode) {
          //return false //"Re" + logTag + "e" + tagData.getTagID()
          *//*pickTag.epc=tagData.getTagID();
            performEncoding(pickTag,currentPassword);
            isReEncode = false;*//*
        } else  //          if(sError.contains("ACCESS_NO_RESPONSE_FROM_TAG")){
        //            // iErrorNoResponse++;
        //            //LogUtils.showLog("ECRT", "No_Res:" + "Written=" + tagData.getNumberOfWords() + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
        //          }
        //          else if(sError.contains("ACCESS_INSUFFICIENT_POWER")){
        //            // iErrorPower++;
        //          }
        //          else if(sError.contains("ACCESS_TAG_CRC_ERROR")){
        //            //iErrorCRC++;
        //          }
        //else iErrorOther++;
          return sError
        //listener.updateUI(tag.getTagID() + ",err=" + sError + ",Written=" + iWritten);
      }*/
      }
    }
    else if (tagData != null && tagData.getNumberOfWords() == iTotalWriteLengthWORDEPC) {
      return result//.equals("Success", ignoreCase = true)
    }
    /*AppCommonMethods.logInFile(
      context,
      sessionType.name(),
      "_STOP_" + logTag.uppercase(Locale.getDefault()) + "ING->failed"
    )*/


    /*if(result.equalsIgnoreCase("Success")){
        return context.getString(R.string.err_encoding_default);
      }*/
    //err_encoding_default
    return "failed"//.equals("Success", ignoreCase = true)
    /* && isRetryEncode
    ) "Re" + logTag + "e" else if (result.equals(
        "Success",
        ignoreCase = true
      )
    ) context.getString(if (sessionAction === AppCommonMethods.SessionAction.DECODE) R.string.err_decoding_default else R.string.err_encoding_default) else result*/
  }

  private fun writeReserved(password: String, epc: String, newPassword: String): Boolean {
    showLog("Write Password Operation", "START")
    showLog("ENCODING_ENCODE_PASSWORD_WRITE", "START" + " (" + password + "->" + newPassword+ ")")
    //val rfidResults: RFIDResults = setPrefilter(epc, true)
    //setSearchFilterEpc(epc)
    //showLog("setPrefilter_Result1", "" + (rfidResults.ordinal == RFIDResults.RFID_API_SUCCESS.ordinal))


    //addEpcBasedFilters(epc,epc.length()>24);
    val actualCurrentPassword: String = newPassword
    showLog("CMDTAGCURRENT", password)
    showLog("CMDACTUALCURRENT", actualCurrentPassword)
    if (mReader != null && mReader!!.isConnected() && mReader?.Actions != null && mReader?.Actions?.TagAccess != null) {
      val tagAccess = TagAccess()
      val writeAccessParams = tagAccess.WriteAccessParams()
      //final TagAccess.WriteSpecificFieldAccessParams writeSpecificFieldAccessParams = tagAccess.new WriteSpecificFieldAccessParams();
      try {
        writeAccessParams.setAccessPassword(Long.decode("0X" + password.trim { it <= ' ' }))
        //writeSpecificFieldAccessParams.setAccessPassword(Long.decode("0X" + tagCurrentPassword.trim()));
      } catch (nfe: NumberFormatException) {
        nfe.printStackTrace()
      }

      writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED)
      writeAccessParams.setOffset(2) //AppCommonMethods.parseInt("2"));
      writeAccessParams.setWriteData(actualCurrentPassword.trim { it <= ' ' })

      //writeSpecificFieldAccessParams.setWriteData(actualCurrentPassword.trim());
      writeAccessParams.setWriteDataLength(2)
      writeAccessParams.setWriteRetries(3)
      //writeSpecificFieldAccessParams.setWriteDataLength(2);
      val tagData = TagData()
      try {
        //AntennaInfo antennaInfo = new AntennaInfo();
        //antennaInfo.setAntennaID(reader.Config.Antennas.getAvailableAntennas());
        //reader.Actions.TagAccess.writeAccessPasswordWait(epc, writeSpecificFieldAccessParams, antennaInfo);
        if (mReader != null && mReader!!.isConnected() && mReader?.Actions != null && mReader!!.Actions.TagAccess != null) {
          //Current Logic
          //reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);//encPickedData, false, true);//, epc.length()<=24, false);
          mReader?.Actions?.TagAccess!!.writeblock(
            null,
            writeAccessParams,
            null,
            tagData,
            true,
            WRITE_OPERATION_TIMEOUT * 2
          )
          //reader.Actions.TagAccess.writeAccessPasswordWait(epc,writeSpecificFieldAccessParams,null);
          //AppCommonMethods.logInFile(context,sessionType.name(),"WRPASSWORDCMD:SUCCESS:");
          //showLog("SUCCESS", "WRITE PASS IN TAG");
        }
      } catch (e: InvalidUsageException) {
        e.printStackTrace()
        val vendorMessage = chkNull(e.getVendorMessage(),"")
        //logInFile(context,"WRPASSWORDCMD:ERR:" + );
        showLog("WRITEPASSWORDERROR1", vendorMessage)
      } catch (e: OperationFailureException) {
        val msg = chkNull(e.getVendorMessage(),"")
        //logInFile(context,"WRPASSWORDCMD:ERR:" + msg);
        showLog("WRITEPASSWORDERROR2", msg)
        //LOCK_ACQUIRE_FAILURE in C1G2AccessOperation
        //access tag crc error
        //access no response from tag
        e.printStackTrace()
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
      if (tagData != null && tagData.getOpStatus() != null) {
        showLog("TAG PASS WRITE RESULT", " OpStatus: " + tagData.getOpStatus())
        showLog("TAG PASS WRITE RESULT", " numofwordswritten " + tagData.getNumberOfWords())
        val iWritten = tagData.getNumberOfWords()
        if (iWritten == 2) {
          showLog("SUCCESS", "WRITE PASS IN TAG")
          showLog("ENCODING_ENCODE_PASSWORD_WRITE", "SUCCESS")
          return true;
          /*if (!actualCurrentPassword.equals(defaultTagZeroPassword, ignoreCase = true) && password.equals(defaultTagZeroPassword, ignoreCase = true))
            lockReservedMemory(actualCurrentPassword,epc)*/
        }
        else {
          // if(iWritten > 0) iWritePartial++;
          val sError = tagData.getOpStatus().toString()
          showLog(
            "ECRT",
            "ID=" + tagData.getTagID() + ", Error=" + sError + " ,Written=" + iWritten
          )
          showLog("ENCODING_ENCODE_PASSWORD_WRITE", "Failed->" + sError)
          //          if(sError.contains("ACCESS_NO_RESPONSE_FROM_TAG")){
          //            // iErrorNoResponse++;
          //            //LogUtils.showLog("ECRT", "No_Res:" + "Written=" + tagData.getNumberOfWords() + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
          //          }
          //          else if(sError.contains("ACCESS_INSUFFICIENT_POWER")){
          //            // iErrorPower++;
          //          }
          //          else if(sError.contains("ACCESS_TAG_CRC_ERROR")){
          //            //iErrorCRC++;
          //          }
          //else iErrorOther++;
          //return sError;
          //listener.updateUI(tag.getTagID() + ",err=" + sError + ",Written=" + iWritten);
        }
      }
      else {
        return false
        showLog("ENCODING_ENCODE_PASSWORD_WRITE", "Failed")
        showLog("TAG PASS WRITE RESULT", "failed!")
      }
    }
    else {
      showLog("ENCODING_ENCODE_PASSWORD_WRITE", "Failed->Reader Disconnected")
      return false
      /*isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
      setProgressMessage(false);*/
      //updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
    }
    return false
  }

  private fun readReserved(password: String, tid: String): String {
    return "";
  }

  private fun lockReservedMemory(password: String, epc: String): Boolean {
    //Set the param values
    showLog("Lock Password Operation", "START")
    showLog("ENCODING_ENCODE_PASSWORD_LOCK", "START")
    //val epc: String? = pickedTag.newEpc
    val lockDataField = LOCK_DATA_FIELD.LOCK_EPC_MEMORY
    val lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE
    val lockDataFieldReserved = LOCK_DATA_FIELD.LOCK_ACCESS_PASSWORD
    val tagAccess = TagAccess()
    showLog("lockId", lockPrivilege.toString())
    val lockAccessParams = tagAccess.LockAccessParams()
    if (lockDataField != null) lockAccessParams.setLockPrivilege(lockDataField, lockPrivilege)
    if (lockDataFieldReserved != null) lockAccessParams.setLockPrivilege(lockDataFieldReserved, lockPrivilege)
    try {
      lockAccessParams.setAccessPassword(Long.decode("0X" + password.trim { it <= ' ' }))
    }
    catch (nfe: java.lang.NumberFormatException) {
      nfe.printStackTrace()
    }
    if (mReader != null && mReader!!.isConnected() && mReader?.Actions != null && mReader?.Actions?.TagAccess != null) {
      try {
        mReader?.Actions?.TagAccess!!.lockWait(epc, lockAccessParams, null, true)
        showLog("Lock Password Operation", "SUCCESS")
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Success")
        return true
      } catch (e: InvalidUsageException) {
        val vendorMessage = chkNull(e.getVendorMessage(),"")
        showLog("LCPASSWORDCMD:ERR:", vendorMessage)
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->" + vendorMessage)
        //logInFile(context,"LCPASSWORDCMD:ERR:" + vendorMessage);
        e.printStackTrace()
        return false
      } catch (e: OperationFailureException) {
        val vendorMessage = chkNull(e.getVendorMessage(),"")
        showLog("LCPASSWORDCMD:ERR:", vendorMessage)
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->" + vendorMessage)
        //logInFile(context,"LCPASSWORDCMD:ERR:" + vendorMessage);
        e.printStackTrace()
        return false
      } catch (e: java.lang.Exception) {
        showLog("LCPASSWORDCMD:ERR:", chkNull(e.message,""))
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->" + chkNull(e.message,""))
        e.printStackTrace()
        return false
      }
    }
    else {
      showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->Reader Disconnected")
      return false
      /*isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
      setProgressMessage(false);*/
      //updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
    }
    showLog("Lock Password Operation", "END")
    return false;
  }

  override fun startDecoding(
    tagInfo: TagInfoEntity,
    currentPassword: String,
    passwords: List<String>,
    retryCount: Int,
    writeEpc: String,
    offset: Int,
    dataLen: Int
  ) {
    isTagWriteDone.postValue(false)
    showLog("method","startDecoding")
    val listPasswords = getUpdatedPasswordList(tagInfo.tid,currentPassword,passwords)
    var isWriteSuccess = false
    val results = ArrayList<String>(0)
    for (pass in listPasswords) {
      if(isNullOrEmpty(pass)) continue
      //if(isNonEmpty(pass) && writeData(pass, RFIDWithUHFUART.Bank_TID, 0, 96, tagInfo.tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, tagInfo.newEpc)) {
      //if(isNonEmpty(pass) && writeEpc(pass, tagInfo.tid, offset, dataLen, tagInfo.newEpc).equals("Success",false)) {
      val result = writeEpc(pass, tagInfo.tid, offset, dataLen, tagInfo.newEpc)
      if (result.equals("Success", true)) {
       //insert Data in table.
       isWriteSuccess = true
       break;
      }
      else results.add(result)
    }
    if (!isWriteSuccess) {
      if(retryCount<tagWriteRetryLimit) startDecoding(tagInfo,currentPassword,passwords,retryCount+1,writeEpc,offset,dataLen)
      else {
        val reason = if(results.isNullOrEmpty() || results.contains(context.getString(R.string.err_encoding_auth_fail))) context.getString(R.string.err_encoding_auth_fail)
        else if (results.size==1) results.get(0) else chkNull(CommonUtils.getMostRepeatedValue(results),results.get(0))
        saveDBTagWriteError(tagInfo,retryCount,TopicConstants.DECODE,reason)//R.string.err_encoding_auth_fail)
      }
    }
    else saveDBTagWriteSuccess(tagInfo,retryCount,TopicConstants.DECODE)
  }

  override fun dispose() {
    try {
      if (readers != null) {
        mReader = null
        readers?.Dispose()
        readers = null
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun dcssdkEventScannerAppeared(dcsScannerInfo: DCSScannerInfo?) {
  }

  override fun dcssdkEventScannerDisappeared(i: Int) {
  }

  override fun dcssdkEventCommunicationSessionEstablished(dcsScannerInfo: DCSScannerInfo?) {
  }

  override fun dcssdkEventCommunicationSessionTerminated(i: Int) {
  }

  override fun dcssdkEventBarcode(barcodeData: ByteArray?, barcodeType: Int, fromScannerID: Int) {
    val s = kotlin.text.String(barcodeData!!)
    //context.barcodeData(s);
    LogUtils.showLog("zebra_dcssdkEventBarcode", "barcaode =" + s)
  }

  override fun dcssdkEventImage(bytes: ByteArray?, i: Int) {
  }

  override fun dcssdkEventVideo(bytes: ByteArray?, i: Int) {
  }

  override fun dcssdkEventBinaryData(bytes: ByteArray?, i: Int) {
  }

  override fun dcssdkEventFirmwareUpdate(firmwareUpdateEvent: FirmwareUpdateEvent?) {
  }

  override fun dcssdkEventAuxScannerAppeared(dcsScannerInfo: DCSScannerInfo?, dcsScannerInfo1: DCSScannerInfo?) {
  }

  override fun RFIDReaderAppeared(readerDevice: ReaderDevice?) {
    showLog("zebra_RFIDReaderAppeared",(if (readerDevice != null) readerDevice!!.getName() else ""));
  }

  override fun RFIDReaderDisappeared(readerDevice: ReaderDevice?) {
    showLog("zebra_RFIDReaderDisappeared ",(if (readerDevice != null) readerDevice!!.getName() else ""));
    if (readerDevice != null && mReader != null && readerDevice.getName().equals(mReader!!.getHostName())) disconnect();
  }

  override fun eventReadNotify(e: RfidReadEvents?) {
    showLog("zebra_method","eventReadNotify")
    /*try {
      if (!isCommandForSearch) {
        Thread.sleep(5)
      }
    } catch (interruptedException: InterruptedException) {
      interruptedException.printStackTrace()
    }*/
    try {
      // Recommended to use new method getReadTagsEx for better performance in case of large tag population
      if(mReader==null || mReader?.Actions==null) return
      showLog("zebra_method","eventReadNotify1")
      val myTags: TagDataArray = mReader?.Actions!!.getReadTagsEx(200)
      showLog("myTags", "" + myTags!!.length)
      //if (myTags != null && myTags!!.length>0)
      //processScannedData(myTags.tags) //TODO process all records at once;
      //val myTags: Array<TagData> = mReader?.Actions!!.getReadTags(100)
      if (myTags != null && myTags!!.length > 0)
        for (tagData in myTags!!.tags)
          if (tagData != null && isNonEmpty(tagData.getTagID()))
             processScannedData(tagData)
    }catch (e: Exception) {e.printStackTrace()}
  }

  // Status Event Notification
  override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents) {
    showLog("zebra_method", "eventStatusNotify")
    showLog("isReader", "" + (mReader != null))
    showLog("isReaderConnected", "" + isConnected())
    showLog("rfidStatusEvents", "" + rfidStatusEvents.StatusEventData.getStatusEventType())
    handlerScope.launch {
      if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
        showLog(
          "HANDHELD_TRIGGER_EVENT",
          rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent().toString()
        )
        showLog(
          "HANDHELD_TRIGGER_EVENT_TRIGGER_TYPE",
          rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldTriggerType()
            .toString()
        )
        if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
          setTriggerPressed()
          /*if (!restrictTriggerPress) {
            showLog("restrictTriggerPress", "" + restrictTriggerPress)
            isTriggerPressed.postValue(true)
            checkTimer()
          }*/
        }
        if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
          /*if (!restrictTriggerPress) {
            showLog("restrictTriggerPress", "" + restrictTriggerPress)
            isTriggerPressed.postValue(false)
            checkTimer()
          }*/
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
          // Access operation started
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
          // Access operation stopped - Can be used to signal waiting thread
        }
      }
      if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.BATTERY_EVENT) {
        showLog("BATTERY_EVENT_LEVEL", "" + rfidStatusEvents.StatusEventData.BatteryData.getLevel())
        showLog(
          "BATTERY_EVENT_CHARGING",
          "" + rfidStatusEvents.StatusEventData.BatteryData.getCharging()
        )
        showLog("BATTERY_EVENT_CAUSE", rfidStatusEvents.StatusEventData.BatteryData.getCause())
        val batteryData = rfidStatusEvents.StatusEventData.BatteryData
        if (batteryData != null) {
          showLog("BATTERY cause", chkNull(batteryData.getCause(), ""))
          val isChargerConnected = batteryData != null && batteryData.getCharging() && chkNull(
            batteryData.getCause(),
            ""
          ).contains(DEVICE_STATUS_CONNECTED)
          val level = batteryData.getLevel()
          showLog("rfidStatusBattery", "" + level)
          if (isChargerConnected) {
            setError(batteryData.getCause())
            stopOperations()
          } else if (!batteryData!!.getCharging() && chkNull(batteryData.getCause(), "").contains(
              DEVICE_BATTERY_LOW
            )
          ) {
            //if (level <= 20) {
            setError(
              String.format(
                context.getString(R.string.err_reader_battery_low),
                level.toString() + "%"
              )
            )
            stopOperations()
          }
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
          disconnect()
          showLog(
            "DISCONNECTION_EVENT_INFO",
            rfidStatusEvents.StatusEventData.DisconnectionEventData.m_DisconnectionEvent.eventInfo.toString()
          )
          showLog(
            "DISCONNECTION_EVENT_READERNAME",
            rfidStatusEvents.StatusEventData.DisconnectionEventData.m_DisconnectionEvent.getreadername()
          )
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT) {
          //TODO show Error Dialog (on Handler/Thread)
          val temperatureAlarmData = rfidStatusEvents.StatusEventData.TemperatureAlarmData
          if (temperatureAlarmData != null) {
            setError(temperatureAlarmData.getCause())
            stopOperations()
            //context.showCustomErrDialog(temperatureAlarmData.getCause())
            showLog("TemperatureAlarmEvent_Cause", temperatureAlarmData.getCause())
            showLog(
              "TemperatureAlarmEvent_AlarmLevel",
              temperatureAlarmData.getAlarmLevel().toString()
            )
            showLog(
              "TemperatureAlarmEvent_CurrentTemperature",
              "" + temperatureAlarmData.getCurrentTemperature()
            )
            showLog(
              "TemperatureAlarmEvent_TemperatureSource",
              temperatureAlarmData.getTemperatureSource().toString()
            )
            showLog("TemperatureAlarmEvent_PATTemperature", "" + temperatureAlarmData.getPATemp())
            showLog(
              "TemperatureAlarmEvent_AmbientTemperature",
              "" + temperatureAlarmData.getAmbientTemp()
            )
          }
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.READER_EXCEPTION_EVENT) {
          showLog(
            "ReaderExceptionEvent",
            rfidStatusEvents.StatusEventData.ReaderExceptionEventData.getReaderExceptionEventInfo()
          )
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.POWER_EVENT) {
          showLog("PowerEvent_Cause", rfidStatusEvents.StatusEventData.PowerData.getCause())
          showLog("PowerEvent_Power", "" + rfidStatusEvents.StatusEventData.PowerData.getPower())
          showLog(
            "PowerEvent_Current",
            "" + rfidStatusEvents.StatusEventData.PowerData.getCurrent()
          )
          showLog(
            "PowerEvent_Voltage",
            "" + rfidStatusEvents.StatusEventData.PowerData.getVoltage()
          )
          //stopInventory();
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.WPA_EVENT) {
          //Don't Handle
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.BUFFER_FULL_WARNING_EVENT) {
          //TODO show Warning Dialog
          val bufferFullWarningEventData =
            rfidStatusEvents.StatusEventData.BufferFullWarningEventData
          if (bufferFullWarningEventData != null) {
            showLog("BufferFullWarningEvent_Cause", bufferFullWarningEventData.toString())
            //setError(bufferFullWarningEventData.toString())
          }
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.BUFFER_FULL_EVENT) {
          val bufferFullEventData = rfidStatusEvents.StatusEventData.BufferFullWarningEventData
          if (bufferFullEventData != null) {
            showLog("BufferFullEvent_Cause", bufferFullEventData.toString())
            //setError(bufferFullEventData.toString())
            //stopOperations()
          }
        }
        if (rfidStatusEvents.StatusEventData.getStatusEventType() === STATUS_EVENT_TYPE.OPERATION_END_SUMMARY_EVENT) {
          //Don't Handle
        }
      }
    }
  }
}