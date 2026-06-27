package com.itek.rftaar.reader

import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue

open class ReaderRepository {
  protected lateinit var context: CommonActivity
  protected var rfidHandler: RFIDHandler? = null
  protected var barcodeHandler: BarcodeHandler? = null
  protected var isSetupBarcodeReader = false;
  var errMsg: MutableLiveData<String> = MutableLiveData("")

  constructor(context: CommonActivity,isSetupBarcodeReader:Boolean=false){
    this.context=context
    this.isSetupBarcodeReader=isSetupBarcodeReader;
  }

  /**
   * Is reader set mutable live data.
   *
   * @return the mutable live data
   */
  fun isReaderSet(): MutableLiveData<Boolean?>{
    return if (rfidHandler != null) rfidHandler!!.isReaderSet else MutableLiveData(null)
  }

  /**
   * Is device configured mutable live data.
   *
   * @return the mutable live data
   */
  fun isDeviceConfigured(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isDeviceConfigured else MutableLiveData(null)
  }

  /**
   * Reader power mutable live data.
   *
   * @return the mutable live data
   */
  fun readerPower(): MutableLiveData<Int> {
    return if (rfidHandler != null) rfidHandler!!.readerPower else MutableLiveData(null)
  }

  /**
   * On create.
   *
   */
  fun onCreate() {
    if (rfidHandler != null) rfidHandler!!.onCreate()
    if (barcodeHandler != null) barcodeHandler!!.init()
  }

  /**
   * On resume.
   *
   */
  fun onResume() {
    if (rfidHandler != null) rfidHandler!!.onResume()
    if (barcodeHandler != null) barcodeHandler!!.onResume()
  }

  /**
   * On pause.
   */
  fun onPause() {
    if (rfidHandler != null) rfidHandler!!.onPause()
    if (barcodeHandler != null) barcodeHandler!!.onPause()
  }


  protected open fun unregisterReceiver(){}

  /**
   * On destroy.
   */
  fun onDestroy() {
    unregisterReceiver()
    if (rfidHandler != null) rfidHandler!!.onDestroy()
    if (barcodeHandler != null) barcodeHandler!!.onDestroy()
  }

  /**
   * Check and set reader.
   */
  fun checkAndSetReader() {
    if (rfidHandler != null) rfidHandler?.checkAndSetReader()
  }

  /**
   * Check and connect reader.
   */
  fun checkAndConnectReader() {
    if (rfidHandler != null) rfidHandler?.checkAndConnectReader()
  }

  fun setTriggerValue(value: Boolean) {
    if (rfidHandler != null) rfidHandler!!.isTriggerPressed.postValue(value)
  }

  /**
   * Is process on mutable live data.
   *
   * @return the mutable live data
   */
  fun isProcessOn(): MutableLiveData<Boolean?> {
    return MutableLiveData<Boolean?>(
      (rfidHandler != null && rfidHandler!!.isProcessOn()) || (barcodeHandler != null && chkTrue(barcodeHandler!!.isBarcodeOn.value))
    )
  }

  /**
   * Is reader connected boolean.
   *
   * @return the boolean
   */
  fun isReaderConnected(): Boolean {
    return rfidHandler != null && rfidHandler!!.isConnected()
  }

  /**
   * Is trigger pressed mutable live data.
   *
   * @return the mutable live data
   */
  fun isTriggerPressed(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isTriggerPressed else MutableLiveData(null)
  }

  /**
   * Is barcode on mutable live data.
   *
   * @return the mutable live data
   */
  fun isBarcodeOn(): MutableLiveData<Boolean> {
    return if (barcodeHandler != null) barcodeHandler!!.isBarcodeOn else MutableLiveData(null)
  }

  /**
   * Barcode data mutable live data.
   *
   * @return the mutable live data
   */
  fun barcodeData(): MutableLiveData<String> {
    return if (barcodeHandler != null) barcodeHandler!!.barcodeData else MutableLiveData(null)
  }

  /**
   * Read tag password mutable live data.
   *
   * @return the mutable live data
   */
  fun readTagPassword(): MutableLiveData<String> {
    return if (rfidHandler != null) rfidHandler!!.readTagPassword else MutableLiveData(null)
  }

  /**
   * Is inventory on mutable live data.
   *
   * @return the mutable live data
   */
  fun isInventoryOn(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isInventoryOn else MutableLiveData(false)
  }

  /**
   * Is tag verify on mutable live data.
   *
   * @return the mutable live data
   */
  fun isTagVerifyOn(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isTagVerifyOn else MutableLiveData(null)
  }

  /**
   * Is search on mutable live data.
   *
   * @return the mutable live data
   */
  fun isSearchOn(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isSearchOn else MutableLiveData(null)
  }

  /**
   * Search percentage mutable live data.
   *
   * @return the mutable live data
   */
  fun searchPercentage(): MutableLiveData<Float> {
    return if (rfidHandler != null) rfidHandler!!.searchPercent else MutableLiveData(null)
  }

  /**
   * Search rssi mutable live data.
   *
   * @return the mutable live data
   */
  fun searchRssi(): MutableLiveData<String> {
    return if (rfidHandler != null) rfidHandler!!.searchRssi else MutableLiveData(null)
  }

  /**
   * Search phase mutable live data.
   *
   * @return the mutable live data
   */
  fun searchPhase(): MutableLiveData<String> {
    return if (rfidHandler != null) rfidHandler!!.searchPhase else MutableLiveData(null)
  }

  /**
   * Is pick on mutable live data.
   *
   * @return the mutable live data
   */
  fun error(): MutableLiveData<String> {
    showLog("error_readerRepo",chkNull(errMsg.value,""))
    //showLog("error_readerRepo1",rfidHandler!!.errMsg.value+"_"+barcodeHandler!!.errMsg.value)
    return errMsg
    /*return if (rfidHandler != null && barcodeHandler!=null && rfidHandler!!.errMsg.value.isNullOrEmpty() && barcodeHandler!!.errMsg.value.isNotEmpty()) barcodeHandler!!.errMsg
    else if (rfidHandler != null) rfidHandler!!.errMsg
    else if(barcodeHandler!=null) barcodeHandler!!.errMsg
    else MutableLiveData(null)*/
  }

  /**
   * Is pick on mutable live data.
   *
   * @return the mutable live data
   */
  fun isPickOn(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isPickOn else MutableLiveData(null)
  }

  /**
   * pick data mutable live data.
   *
   * @return the mutable live data
   */
  fun pickData(): MutableLiveData<TagInfoEntity?> {
    return if (rfidHandler != null) rfidHandler!!.pickData else MutableLiveData(null)
  }

  /**
   * pick list data mutable live data.
   *
   * @return the mutable live data
   */
  fun pickedListData(): MutableLiveData<List<TagInfoEntity>?> {
    return if (rfidHandler != null) rfidHandler!!.pickedListData else MutableLiveData(null)
  }

  /**
   * Is decode on mutable live data.
   *
   * @return the mutable live data
   */
  fun isTagWriteOn(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isTagWriteOn else MutableLiveData(null)
  }

  /**
   * Is decode done mutable live data.
   *
   * @return the mutable live data
   */
  fun isTagWriteDone(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isTagWriteDone else MutableLiveData(null)
  }

  /**
   * Is session on mutable live data.
   *
   * @return the mutable live data
   */
  fun isSessionOn(): MutableLiveData<Boolean> {
    return if (rfidHandler != null) rfidHandler!!.isSessionOn else MutableLiveData(null)
  }

  /**
   * Soft scan.
   */
  fun scanBarcode(scanType: String="") {
    showLog("scanBarcode_scanType",chkNull(scanType,"--"))
    if (barcodeHandler != null) barcodeHandler!!.startScan(scanType)
    if (rfidHandler!=null) rfidHandler!!.resetStateData();
  }

  /**
   * Perform encoding.
   *
   * @param tagInfoEntity the tag info entity
   */
  fun performEncoding(tagInfoEntity: TagInfoEntity,currentPassword:String="",passwords:List<String> = emptyList()) {
    if (rfidHandler != null) rfidHandler?.startEncoding(tagInfoEntity,currentPassword,passwords)
  }

  /**
   * Perform encoding.
   *
   * @param tagInfoEntityList the tag info entity list
   */
  fun performEncoding(tagInfoEntityList: List<TagInfoEntity>,currentPassword:String="",passwords:List<String> = emptyList()) {
    if (rfidHandler != null) rfidHandler?.startEncoding(tagInfoEntityList,currentPassword,passwords)
  }

  /**
   * Perform return.
   *
   * @param tagTime the tag Time
   */
  fun performReturn(tagTime: TagTime,currentPassword:String="",passwords:List<String> = emptyList()) {
    if (rfidHandler != null) rfidHandler?.startReturn(tagTime,currentPassword,passwords)
  }

   /**
   * Perform return.
   *
   * @param tagInfoEntity the tag info entity
   */
  fun performReturn(tagInfoEntity: TagInfoEntity,currentPassword:String="",passwords:List<String> = emptyList()) {
    if (rfidHandler != null) rfidHandler?.startReturn(tagInfoEntity,currentPassword,passwords)
  }

  /**
   * Perform decoding.
   *
   * @param tagInfoEntity the tag info entity
   */
  fun performDecoding(tagInfoEntity: TagInfoEntity,currentPassword:String="",passwords:List<String> = emptyList(),decodeType: String = "") {
    if (rfidHandler != null) rfidHandler?.startDecoding(tagInfoEntity,currentPassword,passwords,decodeType)
  }

  /**
   * Perform decoding.
   *
   * @param tagInfoEntityList the tag info entity list
   */
  fun performDecoding(tagInfoEntityList: List<TagInfoEntity>,currentPassword:String="",passwords:List<String> = emptyList(),decodeType: String = "") {
    if (rfidHandler != null) rfidHandler?.startDecoding(tagInfoEntityList,currentPassword,passwords,decodeType)
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
  fun performPick(barcode: String = "", pickedEpcs: List<String> = emptyList(),pickedTids: List<String> = emptyList(), pickPower: Int = 7, pickTime: Long = 1000, isDecodeOnPick: Boolean = false, isPostPicked: Boolean = true,isSavePickedToDB: Boolean = false,isUpdateFound: Boolean = false, isAllowNonEncodedTags: Boolean = true, isAllowDuplicateTagRePick: Boolean = true) {
    if (rfidHandler != null) rfidHandler?.startPick(barcode,pickedEpcs,pickedTids,pickPower,pickTime,isDecodeOnPick,isPostPicked,isSavePickedToDB, isUpdateFound,isAllowNonEncodedTags,isAllowDuplicateTagRePick)
  }

  fun performInventory(invPower:Int?=null,eans: List<String> = emptyList(), epcs: List<String> = emptyList(), excludedEpcs: List<String> = emptyList(), excludedEans: List<String> = emptyList(),isPublishToMqtt: Boolean = true,updateFound: Boolean=false,onlyUnencoded: Boolean=false, onlyAlien: Boolean=false, maxScanLimit:Int=0){
    if (rfidHandler != null) rfidHandler?.startInventory(invPower,eans,epcs,excludedEpcs,excludedEans,isPublishToMqtt,updateFound,onlyUnencoded,onlyAlien,maxScanLimit)
  }

  fun performSearch(searchType:String ="",value: String="",isTagVerify: Boolean=false){
    if (rfidHandler != null) rfidHandler?.startSearch(searchType,value,isTagVerify)
  }

  fun performTagVerify(){
    if (rfidHandler != null) rfidHandler?.startTagVerify()
  }

  fun stopOperations(){
    if (rfidHandler != null) rfidHandler?.stopOperations()
  }

  fun setSessionAndTransactionType(sessionType:String,transactionType: String="",userRemark:String="",topic: String="",){
    if (rfidHandler != null) rfidHandler?.setSessionAndTransactionType(sessionType,transactionType,userRemark,topic)
  }

  fun setSessionId(sessionId:String,sessionData:String=""){
    if(rfidHandler!=null) rfidHandler?.setSessionId(sessionId,sessionData)
  }

  fun setPower(power: Int){
    if (rfidHandler != null) rfidHandler?.setPower(power)
  }

  protected fun showLog(tag:String,message:String){
    LogUtils.showLog(tag,message)
  }

}