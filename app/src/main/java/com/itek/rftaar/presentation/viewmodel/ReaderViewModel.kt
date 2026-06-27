package com.itek.rftaar.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.core.common.constants.MenuConstants
import com.itek.rftaar.core.common.utils.LogUtils.showLog
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.SearchLogEntity
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.MqttManager
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.reader.DeviceType
import com.itek.rftaar.reader.ReaderRepository
import com.itek.rftaar.reader.chainway.ChainwayRepository
import com.itek.rftaar.reader.constants.ReaderConstants
import com.itek.rftaar.reader.zebra.ZebraRepository
import com.itek.rftaar.sensors.MainSensorRepository
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Arrays
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.abs

class ReaderViewModel : ViewModel() {
  private lateinit var context: CommonActivity
  private var readerRepository: ReaderRepository? = null
  private var sensorRepository: MainSensorRepository? = null
  private var topic = "";
  private var sessionType = "";
  private var transactionType = "";
  private var sessionId = "";
  private var sessionData = "";

  private var referenceArray = arrayOf(0, 0, 0, 0);
  private var currentArray = arrayOf(0, 0, 0, 0);

  private var _angle = MutableStateFlow(0f)
  val angle: StateFlow<Float> = _angle.asStateFlow();
  private var preAngle=0f

  var searchAngle = MutableLiveData<Float>(0.0f)

  var searchLog: SearchLogEntity? = null


  /*private val _power = MutableStateFlow(30)
  val power: StateFlow<Int> = _power.asStateFlow()*/

  private var lastPercent=0f;

  //private val _sensorData = MutableLiveData<String?>()
  //val sensorData: LiveData<String?> get() = _sensorData

  fun isTagDetected(): Boolean {
    return pickData().value != null
  }

  fun clearBarcodeData() {
    error().value = ""
    barcodeData().value = ""
    pickData().value = null
    isTagWriteDone().value=false
  }

  fun clearTagData(isClearBarcode: Boolean = true) {
    showLog("method","clearTagData->("+isClearBarcode+")")
    error().value = ""
    if (isClearBarcode) barcodeData().value = ""
    pickData().value = null
    isTagWriteDone().value=false
  }


  @RequiresApi(Build.VERSION_CODES.DONUT)
  fun init(context: CommonActivity) {
    this.context = context
    readerRepository = findReaderRepository()
    sensorRepository = if (DataStoreManager.getIsSensorAvailable() == true) MainSensorRepository(this.context) else null

    barcodeData()?.observeForever { barcodeData ->
      showLog("rvm_barcodeData", barcodeData)
    }

    pickData()?.observeForever { pickData ->
      showLog("rvm_pickData", "" + pickData?.epc)
      /*timer.schedule(50) {
        if (pickData != null) readerRepository?.pickData()?.postValue(null)
      }*/
    }

    pickedListData()?.observeForever { pickedListData ->
      showLog("rvm_pickedListData", "" + pickedListData?.size)
      /*timer.schedule(50) {
        if (pickedListData != null && pickedListData.isNotEmpty()) readerRepository?.pickedListData()?.postValue(null)
      }*/
    }

    isBarcodeOn()?.observeForever { isBarcodeOn->
      /*if(chkTrue(isBarcodeOn)){
        barcodeData().value=""
      }*/
      showLog("rvm_isBarcodeOn", "" + isBarcodeOn)
    }

    isTagVerifyOn()?.observeForever { isTagVerifyOn ->
      showLog("rvm_isTagVerifyOn", "" + isTagVerifyOn)
    }

    isInventoryOn()?.observeForever { isInventoryOn ->
      showLog("rvm_isInventoryOn", "" + isInventoryOn)
    }

    isPickOn()?.observeForever { isPickOn ->
      showLog("rvm_isPickOn", "" + isPickOn)
    }

    isTagWriteOn()?.observeForever { isTagWriteOn ->
      showLog("rvm_isTagWriteOn", "" + isTagWriteOn)
    }

    isTagWriteDone()?.observeForever {isTagWriteDone->
      showLog("rvm_isTagWriteDone", "" + isTagWriteDone)
      error().value = ""
      barcodeData().value = ""
      if(chkTrue(isTagWriteDone)) {
        pickData().value = null
        pickedListData().value = null
      }
    }

    isTriggerPressed()?.observeForever { isTriggerPressed->
      showLog("rvm_isTriggerPressed", "" + isTriggerPressed)
    }

    isProcessOn().observeForever { isProcessRunning ->
      showLog("rvm_isProcessOn", "" + isProcessRunning)
      if (chkTrue(isProcessRunning)) {
        if(isTagWriteDone().value==true) clearTagData()
        else clearError()
      }
    }

    readerPower().observeForever { power ->
      showLog("rvm_power", "" + power)
      //_power.value = power
    }

    error().observeForever { errorMsg ->
      showLog("rvm_error", errorMsg)
    }

    sensorData().observeForever { sensorData ->
      showLog("rvm_sensorData", "" + sensorData)
      if(chkTrue(isSearchOn().value) && sensorData!=null && !sensorData!!.equals("0$0$0$0",true)) {
          showLog("SENSORDataObserve", sensorData);
          currentArray[0] = sensorData.split("$")[0].toFloat().toInt()
          currentArray[1] = sensorData.split("$")[1].toFloat().toInt()
          currentArray[2] = sensorData.split("$")[2].toFloat().toInt()
          currentArray[3] = sensorData.split("$")[3].toFloat().toInt()
      }
      //_sensorData.value = sensorData
    }

    isSearchOn()?.observeForever { isSearchOn ->
      showLog("rvm_isSearchOn", "" + isSearchOn)
    }

    searchPercentage()?.observeForever { percentage ->
      showLog("rvm_ser_percentage", "" + percentage)
      if(percentage==null || percentage<=0) resetDirectionArrays()
      else if(isSensorAvailable()){
          calculateDirection(percentage,currentArray.clone(),referenceArray.clone(),percentage,lastPercent)
          if(percentage>lastPercent) {
            lastPercent = percentage
            referenceArray[0] = currentArray[0]
            referenceArray[1] = currentArray[1]
            referenceArray[2] = currentArray[2]
            referenceArray[3] = currentArray[3]
          }
      }
    }

    searchRssi()?.observeForever { rssi ->
      showLog("rvm_ser_rssi", "" + rssi)
      //_searchRssi.value=rssi
    }

    searchPhase()?.observeForever { phase ->
      showLog("rvm_ser_phase", "" + phase)
      //_searchPhase.value=phase
    }

    readerRepository?.onResume()
  }

  fun isSensorAvailable(): Boolean{
    return sensorRepository!=null;
  }

  private fun calculateDirection(
    percentage: Float,
    currentArray: Array<Int>,
    referenceArray: Array<Int>,
    curPercentage: Float,
    refPercentage: Float
  ){

    //calculate direction
    showLog("DIR,REF", Arrays.asList(referenceArray).toString());
    showLog("DIR,CUR", Arrays.asList(currentArray).toString() + "\n..");
    val refAngle = referenceArray[0]

    // refAngle = refAngle>360.00?360.00-refAngle:refAngle;
    val refAngleX = referenceArray[1]
    val refAngleY = referenceArray[2]
    val refDirection = referenceArray[3]
    //val refPercentage = referenceArray[4]

    val curAngle = currentArray[0]

    //curAngle = curAngle>360.00?360-curAngle:curAngle;
    val curAngleX = currentArray[1]
    val curAngleY = currentArray[2]
    val curDirection = currentArray[3]
    //val curPercentage = currentArray[4]


    showLog("DIRECTIONDATA", "Move " + curAngle + "  " + refAngle);
    if (curPercentage > 0 && refPercentage > 0) {
      val diff =
        if (abs(refAngle) > abs(curAngle)) abs(refAngle) - abs(curAngle) else abs(curAngle) - abs(
          refAngle
        )
      val diffX =
        if (abs(refAngleX) > abs(curAngleX)) abs(refAngleX) - abs(curAngleX) else abs(curAngleX) - abs(
          refAngleX
        )
      val diffY =
        if (abs(refAngleY) > abs(curAngleY)) abs(refAngleY) - abs(curAngleY) else abs(curAngleY) - abs(
          refAngleY
        )

      showLog("diff",""+diff)
      showLog("diffX",""+diff)
      showLog("diffY",""+diff)

      if (refAngle > curAngle) {
        /*var rotAngle = diff
        if (rotAngle > 180) {
          rotAngle = (360 - diff) * -1
        }
        preAngle = rotAngle.toFloat()
        _angle.value=rotAngle.toFloat();*/

        updateSearchDir(if (curDirection < 0) 1 else 2, diff, diffX, diffY, "Move refAngle > curAngle  " + refAngle, percentage)
      } else {
        updateSearchDir(if (curDirection < 0) 2 else 1, 360 - diff, diffX, diffY, "Move curAngle > refAngle" + refAngle, percentage) //diff);
      }

    }
    else {
      //
      if (curPercentage == 0.0f) {
        //updateSearchDir(2,0.0,"Go Straight");//add);
      }
    }
  }

  protected fun updateSearchDir(direction: Int, angle: Int, message: String?, percent: Float) {
    updateSearchDir(direction, angle, null, null, message, percent)
  }

  protected fun updateSearchDir(
    direction: Int,
    angle: Int,
    angleX: Int?,
    angleY: Int?,
    message: String?,
    percent: Float
  ) {
    //Rotate ImageView based on direction and angle
    if (isSensorAvailable() && chkTrue(isSearchOn().value)) {
      showLog("DIRECTION.,angle", direction.toString() + "," + angle + "\n" + message);
      val rotation: Float = preAngle
      /*if (percent >= 33 && !isShowArrow && currentArray.isNotEmpty()) isShowArrow = true
      if (percent <= 0 && isShowArrow) isShowArrow = false
      imgSearchDir.setVisibility(if (isShowArrow) View.VISIBLE else View.GONE)*/

      var rotAngle = angle.toFloat()
      if (rotAngle > 180) {
        rotAngle = (360 - angle.toFloat()) * -1
      }
      searchAngle.value=rotAngle
      //_angle.value=rotAngle
      preAngle = rotAngle

//      showLog("DIRECTION..", "PreAngle:" + rotation + "_CurAngle:" + rotAngle);
//      showLog("DIRECTION..", message+" "+rotation+"  "+(direction == 1 ? -1.0f : 1.5f) * angle.floatValue());
      //imgSearchDir.animate().rotation(rotAngle).start()

      //imgSearchDir.setImageResource(if (percent >= 30) R.drawable.top_green1 else R.drawable.top_red1)

      // Create our Preview view and set it as the content of our Activity

      //      imgSearchDir.animate().rotation((direction == 1 ? -1.0f : 1.5f) * angle.floatValue()).start();

      //   textSearchDir.setText(message+" "+rotation+"  "+(direction == 1 ? -1.0f : 1.5f) * angle.floatValue());

      //imgSearchDir.setColorFilter(colorId);
      // imgSearchDir.setColorFilter(imgSearchDir.getContext().getResources().getColor(colorId), PorterDuff.Mode.SRC_ATOP);
    }
  }

  private fun resetDirectionArrays() {
    preAngle=0f
    lastPercent = 0f
    referenceArray = arrayOf(0, 0, 0, 0, 0);
    currentArray = arrayOf(0, 0, 0, 0, 0);
  }

  @RequiresApi(Build.VERSION_CODES.DONUT)
  fun findReaderRepository(): ReaderRepository {
    val readerType = findReaderType()
    showLog("readerType", "" + readerType);
    return when (readerType) {
      DeviceType.CHAINWAY -> ChainwayRepository(context)
      DeviceType.ZEBRA -> ZebraRepository(context)
      else -> ReaderRepository(context)
    }
  }

  @RequiresApi(Build.VERSION_CODES.DONUT)
  private fun findReaderType(): DeviceType {
    val deviceType: Int = DataStoreManager.readFromPreferences(ReaderConstants.DEVICE_TYPE, 0);
    var readerType = DeviceType.get(deviceType)
    if (deviceType <= 0) {
      for (dt in DeviceType.values())
        if (Build.BRAND.uppercase(Locale.getDefault())
            .contains(dt.name.uppercase(Locale.getDefault())) ||
          Build.MANUFACTURER.uppercase(Locale.getDefault())
            .contains(dt.name.uppercase(Locale.getDefault()))
        ) {
          readerType = dt
          DataStoreManager.saveToPreferences(ReaderConstants.DEVICE_TYPE, dt.value)
          break;
        }
    }
    return readerType;
  }

  fun onCreate() {
    readerRepository?.onCreate()
  }

  fun onResume() {
    readerRepository?.onResume()
  }

  fun onPause() {
    readerRepository?.onPause()
    //check if session type is search
    stopSensor()
  }

  fun onDestroy() {
    readerRepository?.onDestroy()
  }

  private fun sensorData():MutableLiveData<String>{
    return if (sensorRepository != null) sensorRepository!!.getSensorData() else MutableLiveData(null)
  }

  /**
   * Is reader set mutable live data.
   *
   * @return the mutable live data
   */
  protected fun isReaderSet(): MutableLiveData<Boolean?> {
    return if (readerRepository != null) readerRepository!!.isReaderSet() else MutableLiveData(null)
  }

  /**
   * Is device configured mutable live data.
   *
   * @return the mutable live data
   */
  protected fun isDeviceConfigured(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isDeviceConfigured() else MutableLiveData(
      null
    )
  }

  /**
   * Reader power mutable live data.
   *
   * @return the mutable live data
   */
  protected fun readerPower(): MutableLiveData<Int> {
    return if (readerRepository != null) readerRepository!!.readerPower() else MutableLiveData(null)
  }

  /**
   * Check and set reader.
   */
  fun checkAndSetReader() {
    if (readerRepository != null) readerRepository?.checkAndSetReader()
  }

  /**
   * Perform tag verify toggle
   */
  fun toggleTagVerify() {
    if (chkTrue(isTagVerifyOn().value)) stopOperations()
    else performTagVerify()
  }

  /**
   * Check and connect reader.
   */
  fun checkAndConnectReader() {
    if (readerRepository != null) readerRepository?.checkAndConnectReader()
  }

  fun setTriggerValue(value: Boolean) {
    if (readerRepository != null) readerRepository!!.isTriggerPressed().postValue(value)
  }

  /**
   * Is process on mutable live data.
   *
   * @return the mutable live data
   */
  fun isProcessOn(): MutableLiveData<Boolean?> {
    return if (readerRepository != null) readerRepository!!.isProcessOn() else MutableLiveData(null)
  }

  private fun clearError() {
    showLog("method", "clearError")
    error().value=""
  }

  /**
   * Is reader connected boolean.
   *
   * @return the boolean
   */
  fun isReaderConnected(): Boolean {
    return readerRepository != null && readerRepository!!.isReaderConnected()
  }

  /**
   * Is trigger pressed mutable live data.
   *
   * @return the mutable live data
   */
  fun isTriggerPressed(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isTriggerPressed() else MutableLiveData(null)
  }

  /**
   * Is barcode on mutable live data.
   *
   * @return the mutable live data
   */
  fun isBarcodeOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isBarcodeOn() else MutableLiveData(null)
  }

  /**
   * Barcode data mutable live data.
   *
   * @return the mutable live data
   */
  fun barcodeData(): MutableLiveData<String> {
    return if (readerRepository != null) readerRepository!!.barcodeData() else MutableLiveData(null)
  }

  /**
   * Read tag password mutable live data.
   *
   * @return the mutable live data
   */
  fun readTagPassword(): MutableLiveData<String> {
    return if (readerRepository != null) readerRepository!!.readTagPassword() else MutableLiveData(
      null
    )
  }

  /**
   * Is inventory on mutable live data.
   *
   * @return the mutable live data
   */
  fun isInventoryOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isInventoryOn()
    else MutableLiveData(false)
  }

  /**
   * Is tag verify on mutable live data.
   *
   * @return the mutable live data
   */
  fun isTagVerifyOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isTagVerifyOn()
    else MutableLiveData(null)
  }

  /**
   * Is search on mutable live data.
   *
   * @return the mutable live data
   */
  fun isSearchOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isSearchOn() else MutableLiveData(null)
  }

  /**
   * Search percentage mutable live data.
   *
   * @return the mutable live data
   */
  fun searchPercentage(): MutableLiveData<Float> {
    return if (readerRepository != null) readerRepository!!.searchPercentage() else MutableLiveData(null)
  }

  /**
   * Search rssi mutable live data.
   *
   * @return the mutable live data
   */
  fun searchRssi(): MutableLiveData<String> {
    return if (readerRepository != null) readerRepository!!.searchRssi() else MutableLiveData(null)
  }

  /**
   * Search phase mutable live data.
   *
   * @return the mutable live data
   */
  fun searchPhase(): MutableLiveData<String> {
    return if (readerRepository != null) readerRepository!!.searchPhase() else MutableLiveData(null)
  }

  /**
   * Is pick on mutable live data.
   *
   * @return the mutable live data
   */
  fun error(): MutableLiveData<String> {
    return if (readerRepository != null) readerRepository!!.error() else MutableLiveData(null)
  }


  /**
   * Is pick on mutable live data.
   *
   * @return the mutable live data
   */
  fun isPickOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isPickOn() else MutableLiveData(null)
  }

  fun pickData(): MutableLiveData<TagInfoEntity?> {
    return if (readerRepository != null) readerRepository!!.pickData() else MutableLiveData(null)
  }

  fun pickedListData(): MutableLiveData<List<TagInfoEntity>?> {
    return if (readerRepository != null) readerRepository!!.pickedListData() else MutableLiveData(
      null
    )
  }

  /**
   * Is decode on mutable live data.
   *
   * @return the mutable live data
   */
  fun isTagWriteOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isTagWriteOn() else MutableLiveData(null)
  }

  /**
   * Is decode done mutable live data.
   *
   * @return the mutable live data
   */
  fun isTagWriteDone(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isTagWriteDone() else MutableLiveData(null)
  }

  /**
   * Is session on mutable live data.
   *
   * @return the mutable live data
   */
  fun isSessionOn(): MutableLiveData<Boolean> {
    return if (readerRepository != null) readerRepository!!.isSessionOn() else MutableLiveData(null)
  }

  /**
   * Soft scan.
   */
  fun scanBarcode(scanType: String = "") {
    if (readerRepository != null) readerRepository!!.scanBarcode(scanType = scanType)
  }

  fun scanBarcodeAndTag(scanType: String = "") {
    if (readerRepository != null) {
      readerRepository!!.scanBarcode(scanType!!)
      Timer().schedule(100) {
        readerRepository!!.performPick("")
      }
    }
  }

  fun getSensorAndStart() {
    if (sensorRepository != null) {
      sensorRepository!!.getSensorAndStart()
    }
  }

  fun stopSensor() {
    if (sensorRepository != null) {
      sensorRepository!!.stopSensor()
    }
  }

  /**
   * Perform encoding.
   *
   * @param tagInfoEntity the tag info entity
   */
  fun performEncoding(
    tagInfoEntity: TagInfoEntity,
    currentPassword: String = DataStoreManager.getCurrentPassword(),
    passwords: List<String> = DataStoreManager.getOldPasswordList()
  ) {
    if (readerRepository != null) readerRepository?.performEncoding(tagInfoEntity, currentPassword, passwords)
  }

  /**
   * Perform encoding.
   *
   * @param tagInfoEntityList the tag info entity list
   */
  fun performEncoding(
    tagInfoEntityList: List<TagInfoEntity>,
    currentPassword: String = DataStoreManager.getCurrentPassword(),
    passwords: List<String> = DataStoreManager.getOldPasswordList()
  ) {
    if (readerRepository != null) readerRepository?.performEncoding(tagInfoEntityList, currentPassword, passwords)
  }

  /**
   * Perform decoding.
   *
   * @param tagInfoEntity the tag info entity
   */
  fun performReturn(
    tagInfoEntity: TagInfoEntity,
    currentPassword: String = DataStoreManager.getCurrentPassword(),
    passwords: List<String> = DataStoreManager.getOldPasswordList()
  ) {
    if (readerRepository != null) readerRepository?.performReturn(tagInfoEntity, currentPassword, passwords)
  }

  fun performReturn(
    tagTime: TagTime,
    currentPassword: String = DataStoreManager.getCurrentPassword(),
    passwords: List<String> = DataStoreManager.getOldPasswordList(),
  ) {
    if (readerRepository != null) readerRepository?.performReturn(tagTime, currentPassword, passwords)
  }

  fun performDecoding(
    tagInfoEntity: TagInfoEntity,
    currentPassword: String = DataStoreManager.getCurrentPassword(),
    passwords: List<String> = DataStoreManager.getOldPasswordList(),
    decodeType: String = ""
  ) {
    if (readerRepository != null) readerRepository?.performDecoding(tagInfoEntity, currentPassword, passwords,decodeType)
  }

  /**
   * Perform encoding.
   *
   * @param tagInfoEntityList the tag info entity list
   */
  fun performDecoding(
    tagInfoEntityList: List<TagInfoEntity>,
    currentPassword: String = DataStoreManager.getCurrentPassword(),
    passwords: List<String> = DataStoreManager.getOldPasswordList(),
    decodeType: String = ""
  ) {
    if (readerRepository != null) readerRepository?.performDecoding(tagInfoEntityList, currentPassword, passwords,decodeType)
  }

  /**
   * Perform pick.
   *
   * @param barcode the string
   * @param pickedEpcs the list of string
   * @param pickPower the int
   * @param pickTime the long
   * @param isDecodeOnPick the boolean
   * @param isPostPicked the boolean
   */
  fun performPick(
    barcode: String = "",
    pickedEpcs: List<String> = emptyList(),
    pickedTids: List<String> = emptyList(),
    pickPower: Int = 7,
    pickTime: Long = 1000,
    isDecodeOnPick: Boolean = false,
    isPostPicked: Boolean = true,
    isSavePickedToDB: Boolean = false,
    isUpdateFound: Boolean = false,
    isAllowNonEncodedTags: Boolean = true,
    isAllowDuplicateTagRePick: Boolean = true
  ) {
    if (readerRepository != null) readerRepository?.performPick(
      barcode,
      pickedEpcs,
      pickedTids,
      pickPower,
      pickTime,
      isDecodeOnPick,
      isPostPicked,
      isSavePickedToDB,
      isUpdateFound,
      isAllowNonEncodedTags,
      isAllowDuplicateTagRePick
    )
  }

  fun performInventory(
    invPower: Int? = null,
    eans: List<String> = emptyList(),
    epcs: List<String> = emptyList(),
    excludedEpcs: List<String> = emptyList(),
    excludedEans: List<String> = emptyList(),
    isPublishToMqtt: Boolean = true,
    updateFound: Boolean = false,
    onlyUnencoded: Boolean = false,
    onlyAlien: Boolean = false,
    maxScanLimit : Int = 0
  ) {
    showLog("performInventory_eans", "" + eans.size)
    if (readerRepository != null) readerRepository?.performInventory(
      invPower,
      eans,
      epcs,
      excludedEpcs,
      excludedEans,
      isPublishToMqtt,
      updateFound,
      onlyUnencoded,
      onlyAlien,
      maxScanLimit
    )
  }

  fun performSearch(searchType: String,value: String) {
    getSensorAndStart()
    if (readerRepository != null) readerRepository?.performSearch(searchType, value)
  }

  fun performTagVerify() {
    if (readerRepository != null) readerRepository?.performTagVerify()
  }

  fun stopOperations() {
    if (readerRepository != null) readerRepository?.stopOperations()
    stopSensor()
  }

  fun setSessionAndTransactionType(
    sessionType: String,
    transactionType: String = "",
    userRemark:String = "",
    topic: String = "",
  ) {
    if (readerRepository != null) {
      readerRepository?.setSessionAndTransactionType(sessionType, transactionType,userRemark,topic)
      this.topic=topic
      this.sessionType=sessionType
      this.transactionType=transactionType
    }
    clearTagData(!sessionType.equals(MenuConstants.BULK_ENCODE,true) && !sessionType.equals(MenuConstants.MULTI_ENCODE,true))
  }

  fun setSessionId(sessionId:String,sessionData:String=""){
    if (readerRepository != null) {
      readerRepository?.setSessionId(sessionId,sessionData)
      this.sessionId=sessionId
      this.sessionData=sessionData
    }
  }

  fun clearSessionAndTransactionType() {
    showLog("method", "clearSessionAndTransactionType")
    stopSensor()
    topic="";
    sessionType="";
    transactionType="";
    sessionId="";
    sessionData="";
    searchLog=null
    context.runOnUiThread {
      barcodeData().value = ""
      error().value = ""
      isTagWriteDone().value = false
      pickData().value = null
      pickedListData().value = null
      //sensorData().value = null
      searchRssi().value = null
      searchPhase().value = null
      //searchPercentage().value = 0f
      if (readerRepository != null) {
        readerRepository?.setSessionAndTransactionType("", "","","")
        readerRepository?.setSessionId("","")
      }
    }
  }

  fun setPower(power: Int) {
    if (readerRepository != null) readerRepository?.setPower(power)
  }

  fun toggleInventory(
    invPower: Int? = null,
    eans: List<String> = emptyList(),
    epcs: List<String> = emptyList(),
    excludedEpcs: List<String> = emptyList(),
    excludedEans: List<String> = emptyList(),
    isPublishToMqtt: Boolean = true,
    updateFound: Boolean = false,
    onlyUnencoded: Boolean = false,
    onlyAlien: Boolean = false,
    maxScanLimit:Int = 0
  ) {
    if (chkTrue(isInventoryOn().value)) stopOperations()
    else performInventory(
      invPower,
      eans,
      epcs,
      excludedEpcs,
      excludedEans,
      isPublishToMqtt,
      updateFound,
      onlyUnencoded,
      onlyAlien,
      maxScanLimit
    )
  }

  fun toggleSearch(
    searchType: String = SearchTypeConstant.BARCODE,
    value: String = "",
  ) {
    val preHeader = TopicConstants.SEARCH+"_"+sessionType+"_"
    if (chkTrue(isSearchOn().value)) {
      stopOperations()
      val endTime = DateFormatUtils.getCurrentTime()
      DataStoreManager.saveToPreferences(preHeader+ParameterConstants.END_DATE_TIME, endTime)
      if(searchLog!=null) {
        val serLog = searchLog!!
        CoroutineScope(Dispatchers.IO).launch {
          serLog.endTime = endTime
          AppDatabase.getDbInstance(context).searchLogDao().insert(serLog)
          MqttManager.publishSearch(serLog)
        }
      }
      else MqttManager.publishSearch(sessionType=sessionType,transactionType=transactionType)
    }
    else {
      val startTime = DateFormatUtils.getCurrentTime()
      searchLog = SearchLogEntity(TopicConstants.SEARCH,sessionType,transactionType,sessionId,searchType,value,sessionData,startTime=startTime)
      DataStoreManager.saveToPreferences(preHeader+ParameterConstants.SEARCH_VALUE_TYPE,searchType)
      DataStoreManager.saveToPreferences(preHeader+ParameterConstants.SEARCH_VALUE,value)
      DataStoreManager.saveToPreferences(preHeader+ParameterConstants.START_DATE_TIME, startTime)
      performSearch(searchType, value)
    }
  }
}