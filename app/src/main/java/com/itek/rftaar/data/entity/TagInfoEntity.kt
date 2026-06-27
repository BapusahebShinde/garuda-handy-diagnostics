package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.reader.epcwrapper.EpcEncoderDecoderWrapper
import com.itek.rftaar.reader.epcwrapper.constants.BarcodeConstants
import com.itek.rftaar.utils.CommonUtils.chkNull
import org.jetbrains.annotations.NotNull

@Entity("tag_info", indices = [Index(value = ["session_id","epc","tid","new_epc","id","write_retry_count","write_fail_reason"], unique = true),Index(value = ["is_uploaded"])])
data class TagInfoEntity(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk_id", defaultValue = "0") val pkId: Int = 0,
  @SerializedName(value = "id")
  @ColumnInfo(name="id", defaultValue = "")
  @NotNull
  var id:String="",
  @SerializedName(value = "encodeLogId")
  @ColumnInfo(name="encode_log_id", defaultValue = "")
  @NotNull
  var encodeLogId:String="",
  @SerializedName(value = "customerId")
  @ColumnInfo(name="customer_id", defaultValue = "")
  @NotNull
  var customerId:String="",
  @SerializedName(value = "sessionType")
  @ColumnInfo(name = "session_type", defaultValue = "")
  @NotNull
  var session_type: String = "",
  @SerializedName(value = "tid")
  @ColumnInfo(name = "tid", defaultValue = "")
  @NotNull
  var tid: String = "",
  @SerializedName(value = "epc")
  @ColumnInfo(name = "epc", defaultValue = "")
  @NotNull
  var epc: String = "",
  @SerializedName(value = "pcData", alternate = ["pc"])
  @ColumnInfo(name = "pc_data")
  @NotNull
  var pcData: String = "",
  @SerializedName(value = "newEpc")
  @ColumnInfo(name = "new_epc", defaultValue = "")
  @NotNull
  var newEpc: String = "",
  @SerializedName(value = "barcode")
  @ColumnInfo(name = "barcode", defaultValue = "")
  @NotNull
  var barcode: String = "",
  @SerializedName(value = "serial")
  @ColumnInfo(name = "serial")
  @NotNull
  var serial: String = "",
  @SerializedName(value = "rssi")
  @ColumnInfo(name = "rssi")
  var rssi: String = "",
  @SerializedName(value = "phase")
  @ColumnInfo(name = "phase")
  var phase: String = "",
  @SerializedName(value = "insertTime")
  @ColumnInfo(name = "insert_time", defaultValue = "")
  @NotNull
  var insertTime: String = "",
  @SerializedName(value = "retryWriteCount")
  @ColumnInfo(name = "write_retry_count", defaultValue = "0")
  var retryWriteCount: Int = 0,
  @SerializedName(value = "writeFailReason")
  @ColumnInfo(name = "write_fail_reason")
  var writeFailReason: String = "",
  @SerializedName(value = "verifyFailReason")
  @ColumnInfo(name = "verify_fail_reason")
  var verifyFailReason: String = "",
  @SerializedName(value = "isUploaded")
  @ColumnInfo(name = "is_uploaded", defaultValue = "false")
  var isUploaded: Boolean = false,
  @SerializedName(value = "isFound")
  @ColumnInfo(name = "is_found", defaultValue = "false")
  var isFound: Boolean = false,
  @SerializedName(value = "isTagWriteDone")
  @ColumnInfo(name = "is_tag_write_done", defaultValue = "false")
  var isTagWriteDone: Boolean = false,
  @SerializedName(value = "isTagWriteVerified", alternate = ["isVerified"])
  @ColumnInfo(name = "is_verified", defaultValue = "false")
  var isTagWriteVerified: Boolean = false,
  @SerializedName(value = "tagWriteStatus")
  @ColumnInfo(name = "tag_write_status", defaultValue = StatusConstants.PENDING)
  var tagWriteStatus: String = StatusConstants.PENDING,
  @SerializedName(value = "tagVerifyStatus")
  @ColumnInfo(name = "tag_verify_status", defaultValue = StatusConstants.PENDING)
  var tagVerifyStatus: String = StatusConstants.PENDING,
  @SerializedName(value = "remark")
  @ColumnInfo(name = "remark", defaultValue = "")
  var remark: String = "",
  @SerializedName(value = "decodeType")
  @ColumnInfo(name = "decode_type", defaultValue = "")
  var decodeType: String = "",
  @SerializedName(value = "topic")
  @ColumnInfo(name = "topic", defaultValue = "")
  var topic: String = "",
  @SerializedName(value = "sessionId")
  @ColumnInfo(name = "session_id", defaultValue = "")
  var sessionId: String = "",
  @SerializedName(value = "sessionData")
  @ColumnInfo(name = "session_data", defaultValue = "")
  var sessionData: String = "",
  @SerializedName(value = "startTime")
  @ColumnInfo(name = "start_time", defaultValue = "")
  @NotNull
  var startTime: String = "",
  @SerializedName(value = "endTime")
  @ColumnInfo(name = "end_time", defaultValue = "")
  @NotNull
  var endTime: String = "",
  @SerializedName(value = "transactionType")
  @ColumnInfo(name = "transaction_type", defaultValue = "")
  @NotNull
  var transactionType: String = "",
  @SerializedName(value = "isDeleted")
  @ColumnInfo(name = "is_deleted", defaultValue = "false")
  var isDeleted: Boolean = false,
  ) {
  constructor(sessionType:String,transactionType: String) : this() {
    this.session_type=sessionType
    this.transactionType=transactionType
  }

  constructor(sessionType:String,transactionType: String,barcode:String="",epc:String="",tid:String="") : this(sessionType,transactionType) {
      this.barcode=barcode
      this.epc=epc
      if(chkNull(epc,"").isNotEmpty() && chkNull(barcode,"").isEmpty()){
          try {
              this.barcode = EpcEncoderDecoderWrapper.getBarcodeFromEpc(epc)
          }catch (e:Exception){e.printStackTrace()}
      }
      if(chkNull(epc,"").isNotEmpty() && chkNull(serial,"").isEmpty()) {
          try {
              this.serial = EpcEncoderDecoderWrapper.getSerialFromEpc(epc)
          } catch (e: Exception) {
              e.printStackTrace()
          }
      }
      this.tid=tid
  }

  constructor(sessionType:String,transactionType: String,barcode:String="",epc:String="",tid:String="",rssi:String="",pc:String="") : this(sessionType,transactionType,barcode,epc,tid) {
    this.rssi=rssi
    this.pcData=pc
  }

  private fun isDecodedTag() : Boolean{
    val epc = if(isTagWriteDone) newEpc else epc
    return epc.isNotEmpty() && epc.startsWith(DataStoreManager.readFromPreferences("decodeBits","0"))
  }

  fun isUnencodedTag(): Boolean{
    return isDecodedTag() || (barcode.isNotEmpty() && isUnencodedTag(barcode))
  }

  private fun isUnencodedTag(barcode: String): Boolean {
    return barcode.equals(BarcodeConstants.UNKNOWN, ignoreCase = true) || barcode.equals(BarcodeConstants.NON_ENCODED, ignoreCase = true)
  }

  private fun isSold(epc:String): Boolean{
    return chkNull(epc,"").startsWith(DataStoreManager.readFromPreferences("decodeBits","0"))
  }

  fun isAlien(): Boolean{
    return serial.isNotEmpty() && isAlien(serial)
  }

  private fun isAlien(serial:String): Boolean{
    if(!isUnencodedTag(barcode) && DataStoreManager.readFromPreferences("enableAlienTags",false) && DataStoreManager.readFromPreferences("enableVendorSerialCode",false) && DataStoreManager.readFromPreferences("vendorSerialCode","").isNotEmpty()) {
      return !chkNull(serial,"").startsWith(DataStoreManager.readFromPreferences("vendorSerialCode",""))
    }
    return false
  }

  override fun equals(other: Any?): Boolean {
    if(other is TagInfoEntity)
    return this.topic.equals(other.topic,true) && this.session_type.equals(other.session_type,true)&&
    this.transactionType.equals(other.transactionType,true) && this.sessionId.equals(other.sessionId,true)&&
    this.epc.equals(other.epc,true) && this.tid.equals(other.tid,true) && this.newEpc.equals(other.newEpc,true) &&
    this.retryWriteCount==other.retryWriteCount && this.writeFailReason.equals(other.writeFailReason,true) && this.isFound.equals(other.isFound)
    else return false //return super.equals(other)
  }
}