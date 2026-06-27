package com.itek.rftaar.reader.chainway

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.R
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.reader.RFIDHandler
import com.itek.rftaar.reader.constants.ReaderConstants
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import com.itek.rftaar.utils.CommonUtils.isNonEmpty
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.Gen2Entity
import com.rscja.deviceapi.entity.InventoryParameter
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback
import com.rscja.deviceapi.interfaces.KeyEventCallback
import com.rscja.deviceapi.interfaces.ScanBTCallback

class ChainwayRFIDHandler(context: CommonActivity, errMsg: MutableLiveData<String>) : RFIDHandler(context,errMsg) {
  val mReader: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
  private var reader: RFIDWithUHFBLE? = null
  private var isConnected = false
  private var isInit = false
  private var readerName: String = "(?i)(Chainway).*"

  private fun assignBluetoothReader() {
    if (reader == null) reader = RFIDWithUHFBLE.getInstance()
    if (reader != null && checkBluetoothConnection()) {
      val savedBluetoothAddress = DataStoreManager.readFromPreferences(ReaderConstants.SAVED_BLUETOOTH_READER_ADDRESS, "")
      if (isNonEmpty(savedBluetoothAddress)) {
        if (reader?.connectStatus == ConnectionStatus.DISCONNECTED) reader?.connect(savedBluetoothAddress)
      }
      else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        val listPairedDevices = mBluetoothAdapter!!.bondedDevices
        if (isNonEmpty(listPairedDevices)) {
          var mDevice: BluetoothDevice? = null
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val device = listPairedDevices.stream().filter { it: BluetoothDevice ->
              it.name.matches(readerName.toRegex())
            }.findFirst()
            mDevice = if ((device != null)) device.get() else null
          } else {
            for (bt in listPairedDevices) if (bt.name.matches(readerName.toRegex())) {
              mDevice = bt
              break
            }
          }
          if (mDevice != null) {
            DataStoreManager.saveToPreferences(
              ReaderConstants.SAVED_BLUETOOTH_READER_ADDRESS,
              mDevice.address
            )
            if (reader?.connectStatus == ConnectionStatus.DISCONNECTED) reader?.connect(mDevice.address)
          } else {
            //context.showCustomErrDialog(R.string.err_printer_not_paired)
          }
        }
        else reader?.startScanBTDevices(ScanBTCallback { bluetoothDevice, i, bytes ->
          if (bluetoothDevice != null && bluetoothDevice.name.matches(readerName.toRegex())) {
            DataStoreManager.saveToPreferences(
              ReaderConstants.SAVED_BLUETOOTH_READER_ADDRESS,
              bluetoothDevice.address
            )
            if (reader?.connectStatus == ConnectionStatus.DISCONNECTED) reader?.connect(
              bluetoothDevice.address
            )
          }
        })
      }
    }
  }

  override fun dispose() {
    try {
      isInit=false
      mReader.free()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onResume() {
    if(!isInit) initSDK()
    //else if(!isConnected()) connect()
  }

  override fun onConnect() {
    TODO("Not yet implemented")
  }

  override fun onDisconnect() {
    TODO("Not yet implemented")
  }

  override fun initSDK() {
    if(!isInit) init()
  }


  private fun init() {
    showLog("chainwayRFID_method","init")
    if (isBluetoothReader && !checkBluetoothConnection()) return
    if (initReader()) {
      isInit=true;
      /*if (reader != null && reader.getConnectStatus() == ConnectionStatus.DISCONNECTED) {
        if (isNonEmpty(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS)))
          reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS))
        else assignBluetoothReader()
      }*/

      val sdkVersion = if (isBluetoothReader) reader?.getVersion() else mReader.version
      if (isNonEmpty(sdkVersion) && !DataStoreManager.readFromPreferences(ReaderConstants.READER_SDK_VERSION, "").equals(sdkVersion, true))
        DataStoreManager.saveToPreferences(ReaderConstants.READER_SDK_VERSION, sdkVersion)
      configure()
    }
    setConnectionStatusCallback()
    setInventoryCallback()
    setKeyEventCallback()
  }

  private val defaultConnectionStatusCallback =
    ConnectionStatusCallback<Any> { connectionStatus: ConnectionStatus?, o: Any? ->
      //showLog("connectionStatus", connectionStatus?.name ?: "Null")
      isConnected = connectionStatus != null && connectionStatus == ConnectionStatus.CONNECTED
      //showLog("isConnected", "" + isConnected)
      //isReaderSet.postValue(if (connectionStatus == null) null else isConnected)
    }

  private fun setConnectionStatusCallback(connectionStatusCallback: ConnectionStatusCallback<Any> = defaultConnectionStatusCallback) {
    reader?.setConnectionStatusCallback(connectionStatusCallback)
    if (mReader != null) mReader.setConnectionStatusCallback(connectionStatusCallback)
  }

  private val defaultIUHFInventoryCallback = IUHFInventoryCallback { uhftagInfo: UHFTAGInfo ->
    showLog("method", "IUHFInventoryCallback")
    showLog("isActionInventory_IUHFInventoryCallback", ""+isActionInventory)
    if(!isScanningOn()) return@IUHFInventoryCallback
    else {
      processScannedData(uhftagInfo)
      //val result = tagBuffer.trySend(getTagInfo(uhftagInfo))//handlerScope.launch { processScannedData(uhftagInfo) }
      //if(result.isFailure) showLog("scanChannel_dropped", "Tag dropped — channel full")
    }


  }

  private val defaultKeyEventCallback: KeyEventCallback = object : KeyEventCallback {
    override fun onKeyDown(keyCode: Int) {
      //showLog("onKeyDown", "" + keyCode)
      if (keyCode == 1) {
        //setTriggerPressed()
        //isTriggerPressed.postValue(true)
      }
    }

    override fun onKeyUp(keyCode: Int) {
      //showLog("onKeyUp", "" + keyCode)
    }

  }

  private fun setKeyEventCallback(keyEventCallback: KeyEventCallback = defaultKeyEventCallback) {
    reader?.setKeyEventCallback(keyEventCallback)
    //if(mReader != null) mReader.setKeyEventCallback(keyEventCallback);
  }

  override fun configure() {
    val maxPower = MAX_POWER_TO_SET
    val power = maxPower;

    //showLog("config power", type.name() + ":" + power)
    setTagFocus(false)
    setFastID(false)
    setPower(power)
    setEPCMode()
    setSession("S0", "A")
    clearFilters()
    //showLog(ChainwayRFIDHandler.TAG_LOG, "CONFIG SUCCESS")
    isDeviceConfigured.postValue(true)
  }

  fun setSession() {
    if (!isConnected) checkAndConnectReader()
  }

  /*@Override
  public void configureSessionAction(final AppCommonMethods.SessionAction sessionAction){
    switch(sessionAction){
      case INVENTORY:
        break;
      case SEARCH:
        readRssi = true;
        break;
      case PICK:
        readEAN = true;
        readTid = true;
        break;
      case ENCODE:
        readEAN = true;
        readTid = true;
        readPC = true;
        break;
      case DECODE:
        readEAN = true;
        readTid = true;
        readPC = true;
        break;
      default:
        break;
    }
  }*/

  private fun clearFilters() {
    //Old Code (Commented)
    /*if(setFilter(1, 32, 0, "00"))
      showLog("clearFilters","cleared");
    else
      showLog("clearFilters","failed");*/

    //New Code
    try {
      if (chkNull(reader, mReader) != null &&
        setFilter(RFIDWithUHFUART.Bank_EPC, 0, 0, "") &&
        setFilter(RFIDWithUHFUART.Bank_TID, 0, 0, "")
      ) {/*showLog("clearFilters", "cleared")*/
      }
      //else showLog("clearFilters", "failed")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun setSession(session: String, invType: String) {
    val sessions: Array<out String> = context.resources.getStringArray(R.array.sessions)
    val inventoryTypes: Array<out String> = context.resources.getStringArray(R.array.inventoryTypes)
    val seesionid = sessions.indexOf(session)
    val inventoried = inventoryTypes.indexOf(invType)
    if (seesionid < 0 || inventoried < 0) {
      return
    }


    //old sdk code
    /*char[] p = mReader.getGen2();
    if(p != null && p.length >= 14){
      int g = p[12];
      int linkFrequency = p[13];
      if(inventoried == 2){
        linkFrequency = 1;
        inventoried = g;
      }
      else{
        linkFrequency = 0;
      }
      if(mReader.setGen2(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], seesionid, inventoried, linkFrequency)){
        showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "SET");
      }
      else{
        showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "FAIL");
      }
    }*/

    //new SDK code
    val gen2Entity = getGen2()
    if (gen2Entity != null) {
      gen2Entity.queryTarget = inventoried
      gen2Entity.querySession = seesionid
      if (setGen2(gen2Entity)) {
        //showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "SET")
      } else {
        //showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "FAIL")
      }
    } else {
      //showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "FAIL")
    }
  }


  //setup inventory callback
  private fun setInventoryCallback(inventoryCallback: IUHFInventoryCallback = defaultIUHFInventoryCallback) {
    reader?.setInventoryCallback(inventoryCallback)
    if (mReader != null) mReader.setInventoryCallback(inventoryCallback)
  }

  override fun checkAndConnectReader() {
    if (isBluetoothReader) {
      if (!checkBluetoothConnection()) return
      else if (reader == null) assignBluetoothReader()
      else if (reader?.connectStatus != ConnectionStatus.CONNECTED) initSDK()
      else if (!chkTrue(isDeviceConfigured.value)) configure()
      return
    }
    if (chkNull(reader, mReader) == null) {
      isReaderSet!!.postValue(null)
      initSDK()
    } else if (getConnectStatus() == ConnectionStatus.DISCONNECTED) {
      initSDK()
    } else if (!chkTrue(isDeviceConfigured.value)) configure()
  }

  override fun checkAndSetReader() {
    if (isBluetoothReader && !checkBluetoothConnection()) return
    if (chkNull(reader, mReader) == null) {
      isReaderSet!!.postValue(null)
      initSDK()
    } else if (getConnectStatus() == ConnectionStatus.DISCONNECTED) {
      initSDK()
    }
  }

  private fun initReader(): Boolean {
    return if (reader != null) reader!!.init(context) else if (mReader != null) mReader.init(context) else false
  }

  private fun getGen2(): Gen2Entity? {
    return if(reader!=null) reader!!.gen2 else mReader.gen2
  }

  private fun setGen2(gen2Entity: Gen2Entity): Boolean {
    return if (reader != null) reader!!.setGen2(gen2Entity)
    else if (mReader != null) mReader.setGen2(gen2Entity)
    else false
  }

  override fun getPower(): Int {
    return if (reader != null) reader!!.getPower() else if (mReader != null) mReader.power else -1
  }

  override fun setPower(power: Int) {
      showLog("SetPower:",""+power)

      if (reader != null && reader?.setPower(power) == true) readerPower.postValue(power)
      else if (mReader != null && mReader.setPower(power)) readerPower.postValue(power)
      showLog("SetPower 1:",""+power)
  }

  fun setTagFocus(isSet: Boolean) {
    showLog("SetTagFocus:",""+isSet)
    if (reader != null) reader?.setTagFocus(isSet)
    else if (mReader != null) mReader.setTagFocus(isSet)
  }

  fun setFastID(isSet: Boolean) {
    showLog("setFastID:",""+isSet)
    if (reader != null) reader?.setFastID(isSet)
    else if (mReader != null) mReader.setFastID(isSet)
  }

  private fun setEPCAndTIDMode(): Boolean {
    return if (reader != null) reader!!.setEPCAndTIDMode() else if (mReader != null) mReader.setEPCAndTIDMode() else false
  }

  private fun setEPCMode(): Boolean {
    return if (reader != null) reader!!.setEPCMode() else if (mReader != null) mReader.setEPCMode() else false
  }

  private fun setFilter(filterBank: Int, offSet: Int, len: Int, filterData: String): Boolean {
    return if (reader != null) reader!!.setFilter(
      filterBank,
      offSet,
      len,
      filterData
    ) else if (mReader != null) mReader.setFilter(filterBank, offSet, len, filterData) else false
  }

  private fun getConnectStatus(): ConnectionStatus {
    return if (reader != null) reader!!.getConnectStatus() else if (mReader != null) mReader.connectStatus else ConnectionStatus.DISCONNECTED
  }

  override fun connect() {
    TODO("Not yet implemented")
  }

  override fun disconnect() {
    TODO("Not yet implemented")
  }

  override fun isConnected(): Boolean {
    return chkNull(reader, mReader) != null && getConnectStatus() != ConnectionStatus.DISCONNECTED
  }

  override fun clearFilter(): Int {
    TODO("Not yet implemented")
  }

  override fun startInventory():Boolean {
    /** Temp code for reading Phase Angle (Now commented)
    var inventoryParameter: InventoryParameter? = null
    if (isDebugApp && isActionSearch && !isActionTIDSearch) {
    inventoryParameter = InventoryParameter()
    inventoryParameter.setResultData(InventoryParameter.ResultData().setNeedPhase(true))
    }**/
    return  if (reader != null) reader!!.startInventoryTag()
    else if (mReader != null) mReader.startInventoryTag()
    else false
  }

  private fun startSearch(inventoryParameter: InventoryParameter):Boolean {
    /** Temp code for reading Phase Angle (Now commented)
    var inventoryParameter: InventoryParameter? = null
    if (isDebugApp && isActionSearch && !isActionTIDSearch) {
    inventoryParameter = InventoryParameter()
    inventoryParameter.setResultData(InventoryParameter.ResultData().setNeedPhase(true))
    }**/
    return  if (reader != null) reader!!.startInventoryTag(inventoryParameter)
    else if (mReader != null) mReader.startInventoryTag()//mReader.startInventoryTag(inventoryParameter) //Commented Since Issue for TID search due to phase angle
    else false
  }

  override fun stopScanning() {
    if (reader != null) reader!!.stopInventory()
    if (mReader != null) mReader.stopInventory()
  }

  override fun startSearch(): Boolean {
    val inventoryParameter = InventoryParameter()
    inventoryParameter.setResultData(InventoryParameter.ResultData().setNeedPhase(true))
    return startSearch(inventoryParameter)
  }

  /**
   * add Filters.
   *
   * @param tag  the tag
   * @param isBc the is bc
   */
  protected override fun setSearchFilterBarcode(tag: String, isBc: Boolean,isSearchDecoded: Boolean): Boolean {
    // Add state aware pre-filter
    showLog("tag_isBC", tag + "_" + isBc)
    val filterBank = RFIDWithUHFUART.Bank_EPC
    //68 =>substring(9) 80=>substring(12)
    val tag1 = if(!isBc && isSearchDecoded) tag.substring(1) else tag
    val offSet = if (isBc) 68 /*80*/ else if(isSearchDecoded) 32+4 else 32
    val len = tag1.length * 4
    //select tags that match the criteria
    try {
      //if(setFilter(filterBank, offSet, len, tag)){ //Previous Code
      return setFilter(filterBank, offSet, len, if (tag1.length % 2 == 0) tag1 else tag1 + "0")
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      return false;
    }
  }

  /**
   * Add epc basedfilters.
   *
   * @param tag  the tag
   * @param isBc the is bc
   */
  protected override fun setSearchFilterEpc (tag: String, isSearchDecoded: Boolean): Boolean {
    // Add state aware pre-filter
    val filterBank = RFIDWithUHFUART.Bank_EPC
    val tag1 = if(isSearchDecoded) tag.substring(1) else tag
    val offSet = if(isSearchDecoded) 32+4 else 32
    val len = tag1.length * 4
    //select tags that match the criteria
    try {
      return setFilter(filterBank, offSet, len, if (tag1.length % 2 == 0) tag1 else tag1 + "0")
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      return false
    }
  }

  protected override fun setSearchFilterTid(tid: String):Boolean {
    // Add state aware pre-filter
    showLog("setTidBasedFilters", tid)
    val filterBank = RFIDWithUHFUART.Bank_TID
    val offSet = 0
    val len = tid.length * 4
    //select tags that match the criteria
    try {
      return setFilter(filterBank, offSet, len, tid)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
      return false;
    }
  }

  override fun setupInventory(invPower: Int?,readTid: Boolean): Boolean {
    val maxPower: Int = MAX_POWER_TO_SET
    var power:Int =  chkNull(invPower,maxPower)
    setSession("S0", "A")
    clearFilters()
    setPower(power)
    setTagFocus(true)//readTid)//!readTid)
    setFastID(false)
    return if(readTid) setEPCAndTIDMode() else setEPCMode()
  }

  override fun setupPick(pickPower:Int?):Boolean{
    val maxPower: Int = if (Build.MODEL.equals("C71", ignoreCase = true)) 20 else 15
    var power:Int =  chkNull(pickPower,maxPower)
    if (Build.MODEL.equals("C71", ignoreCase = true)) power += 5
    setSession("S0", "A")
    clearFilters()
    setPower(power)
    setTagFocus(false)
    setFastID(false)
    return setEPCAndTIDMode();
    //if(isActionTidPick) addTIDBasedfilters(SCANNED_TID);
  }

  override fun setupTagWrite(tagWritePower:Int?):Boolean{
    val maxPower: Int = MAX_POWER_TO_SET
    var power:Int =  chkNull(tagWritePower, maxPower)
    setSession("S0", "A")
    clearFilters()
    setPower(power)
    setTagFocus(false)
    setFastID(false)
    return setEPCAndTIDMode();
  }

  override fun setupSearch(isTidSearch: Boolean):Boolean{
    /*val maxPower: Int = chkNull(searchPower,MAX_POWER_TO_SET)
    var power:Int =  chkNull(searchPower, maxPower)*/
    setSession("S0",if(Build.MODEL.equals("C5P", ignoreCase = true) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "A" else "B")
    clearFilters()
    setPower(MAX_POWER_TO_SET-1)
    setTagFocus(false)
    setFastID(false)
    return if(isTidSearch || readTid) setEPCAndTIDMode() else setEPCMode()
  }

  override fun startEncoding(tagInfo: TagInfoEntity,currentPassword:String,passwords: List<String>,retryCount:Int,writeEpc:String,offSet: Int,dataLen:Int) {
      showLog("startEncoding_tagInfo",tagInfo.toString())
      showLog("dataLen",""+dataLen)
      showLog("offSet",""+offSet)
      val listPasswords = getUpdatedPasswordList(tagInfo.tid,currentPassword,passwords)
      var isWriteSuccess = false
      for (pass in listPasswords) {
        showLog("pass",""+pass)
        //if(isNonEmpty(pass) && writeData(pass, RFIDWithUHFUART.Bank_TID, 0, 96, tagInfo.tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, writeEpc)) {
        if(isNonEmpty(pass) && writeEpc(pass, tagInfo.tid,  offSet, dataLen, writeEpc)) {
          //insert Data in table.
          isWriteSuccess = true
          showLog("isWriteSuccess",""+isWriteSuccess)
          try {
            if(!NON_PASSWORD_TIDS.contains(tagInfo.tid) && !currentPassword.equals(defaultTagZeroPassword) && !pass.equals(currentPassword)) {
              if(writeReserved(pass,tagInfo.tid, newPassword = currentPassword)) {
                val result = lockReservedMemory(currentPassword, tagInfo.tid)
                showLog("lockReservedMemory",""+result);
              }
              else showLog("writeReserved",""+false);
            }
          }
          catch (e: java.lang.Exception) {
            e.printStackTrace()
          }
          break
        }
      }
      if (!isWriteSuccess) {
        showLog("LOCKMEMORY1", "FAIL")
        if(retryCount<tagWriteRetryLimit) startEncoding(tagInfo,currentPassword,passwords,retryCount+1,writeEpc,offSet,dataLen)
        else saveDBTagWriteError(tagInfo,retryCount,TopicConstants.ENCODE,R.string.err_encoding_auth_fail)
      }
      else saveDBTagWriteSuccess(tagInfo,retryCount,TopicConstants.ENCODE)
  }

  override fun startDecoding(tagInfo: TagInfoEntity,currentPassword: String,passwords: List<String>,retryCount:Int,writeEpc:String,offSet: Int,dataLen:Int) {
    isTagWriteDone.postValue(false)
    showLog("method","startDecoding")
    val listPasswords = getUpdatedPasswordList(tagInfo.tid,currentPassword,passwords)
    var isWriteSuccess = false
    for (pass in listPasswords) {
        //if(isNonEmpty(pass) && writeData(pass, RFIDWithUHFUART.Bank_TID, 0, 96, tagInfo.tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, tagInfo.newEpc)) {
        if(isNonEmpty(pass) && writeEpc(pass, tagInfo.tid,  offSet, dataLen, tagInfo.newEpc)) {
          //insert Data in table.
          isWriteSuccess = true
          break;
        }
    }
    if (!isWriteSuccess) {
     if(retryCount<tagWriteRetryLimit) startDecoding(tagInfo,currentPassword,passwords,retryCount+1,writeEpc,offSet,dataLen)
     else saveDBTagWriteError(tagInfo,retryCount,TopicConstants.DECODE,R.string.err_encoding_auth_fail)
    }
    else saveDBTagWriteSuccess(tagInfo,retryCount,TopicConstants.DECODE)
  }

  override fun getTagInfo(scanned: Any): TagInfoEntity {
    if(scanned is TagInfoEntity) return scanned
    if(scanned is UHFTAGInfo){
      val tagInfoEntity = TagInfoEntity(sessionType=sessionType,transactionType=transactionType,epc=scanned.epc,tid=chkNull(scanned.getTid(),""), rssi = chkNull(scanned.rssi,""),pc=chkNull(scanned.getPc(),""))
      if(scanned.phase!=null) tagInfoEntity.phase= scanned.phase.toString()
      return tagInfoEntity
    }
    return TagInfoEntity()
  }

  private fun writeEpc(password: String, tid: String, offSet:Int, dataLen:Int, writeEpc:String) : Boolean{
    return writeData(password, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, writeEpc);
  }

  private fun writeReserved(password: String, tid: String, newPassword: String): Boolean {
    return writeData(password, filterData = tid,writeFilterBank = RFIDWithUHFUART.Bank_RESERVED, writeOffSet = 2, writeDataLen = 2, writeData = newPassword)
  }

  private fun readReserved(password: String, tid: String): String {
    return readData(password, filterData = tid, readFilterBank = RFIDWithUHFUART.Bank_RESERVED, readOffSet = 2, readDataLen = 2)
  }


  private fun readData(
    password: String,
    filterBank: Int=RFIDWithUHFUART.Bank_TID,
    filterOffset: Int=0,
    filterDataLen: Int=96,
    filterData: String,
    readFilterBank: Int,
    readOffSet: Int,
    readDataLen: Int
  ): String {
    return if (reader != null) reader!!.readData(
      password,
      filterBank,
      filterOffset,
      filterDataLen,
      filterData,
      readFilterBank,
      readOffSet,
      readDataLen
    ) else (if (mReader != null) mReader.readData(
      password,
      filterBank,
      filterOffset,
      filterDataLen,
      filterData,
      readFilterBank,
      readOffSet,
      readDataLen
    ) else null)!!
  }

  private fun writeData(password: String, filterBank: Int=RFIDWithUHFUART.Bank_TID, filterOffset: Int=0, filterDataLen: Int=96, filterData: String, writeFilterBank: Int, writeOffSet: Int, writeDataLen: Int, writeData: String): Boolean {
    return if (reader != null) reader!!.writeData(password, filterBank, filterOffset, filterDataLen, filterData, writeFilterBank, writeOffSet, writeDataLen, writeData)
    else if (mReader != null) mReader.writeData(password, filterBank, filterOffset, filterDataLen, filterData, writeFilterBank, writeOffSet, writeDataLen, writeData)
    else false
  }

  private fun lockReservedMemory(password: String, tid: String): Boolean {
    return if (reader != null)  reader!!.lockMem(password, RFIDWithUHFUART.Bank_TID, 0, 96, tid, "0280A0")
    else if (mReader != null)  mReader!!.lockMem(password, RFIDWithUHFUART.Bank_TID, 0, 96, tid, "0280A0")
    else false
  }
}