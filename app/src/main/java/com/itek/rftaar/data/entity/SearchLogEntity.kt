package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.mqtt.constants.TopicConstants
import org.jetbrains.annotations.NotNull

@Entity(tableName = "search_log", indices = [Index(value = ["topic","session_type","transaction_type","session_id","search_type","search_value","start_time","end_time"], unique = true)])
data class SearchLogEntity(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk_id", defaultValue = "0") val pkId: Int = 0,
  @SerializedName(value = "topic")
  @ColumnInfo(name = "topic", defaultValue = TopicConstants.SEARCH)
  @NotNull
  var topic: String = TopicConstants.SEARCH,
  @SerializedName(value = "sessionType")
  @ColumnInfo(name = "session_type", defaultValue = "")
  @NotNull
  var sessionType: String = "",
  @SerializedName(value = "transactionType")
  @ColumnInfo(name = "transaction_type", defaultValue = "")
  var transactionType: String = "",
  @SerializedName(value = "searchType")
  @ColumnInfo(name = "search_type", defaultValue = "")
  var searchType: String = "",
  @SerializedName(value = "searchValue")
  @ColumnInfo(name = "search_value", defaultValue = "")
  var searchValue: String = "",
  @SerializedName(value = "referenceNumber", alternate = ["refNumber"])
  @ColumnInfo(name = "ref_number", defaultValue = "")
  var referenceNumber: String = "",
  @SerializedName(value = "sessionId")
  @ColumnInfo(name = "session_id", defaultValue = "")
  var sessionId: String = "",
  @SerializedName(value = "sessionData")
  @ColumnInfo(name = "session_data", defaultValue = "")
  var sessionData: String = "",
  @SerializedName(value = "startTime")
  @ColumnInfo(name = "start_time", defaultValue = "")
  var startTime: String = "",
  @SerializedName(value = "endTime")
  @ColumnInfo(name = "end_time", defaultValue = "")
  var endTime: String = "",
  @SerializedName(value = "isUploaded")
  @ColumnInfo(name = "is_uploaded", defaultValue = "false")
  var isUploaded: Boolean = false,
) {


  constructor(topic: String = TopicConstants.SEARCH, sessionType: String, transactionType: String, sessionId: String, searchType: String, searchValue: String, sessionData:String="", refNumber:String="", startTime: String= DateFormatUtils.getCurrentAPITime()) : this() {
    this.topic = topic
    this.sessionType = sessionType
    this.transactionType = transactionType
    this.sessionId = sessionId
    this.sessionData = sessionData
    this.searchType = searchType
    this.searchValue = searchValue
    this.referenceNumber = refNumber
    this.startTime = startTime
  }
}