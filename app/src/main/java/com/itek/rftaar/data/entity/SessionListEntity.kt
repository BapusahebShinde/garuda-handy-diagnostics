package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull

@Entity("session_list", indices = [Index(value = ["topic","session_type","transaction_type","value"], unique = true)])
data class SessionListEntity(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk_id", defaultValue = "0") val pkId: Int = 0,
  @SerializedName(value = "topic")
  @ColumnInfo(name = "topic", defaultValue = "")
  @NotNull
  var topic: String = "",
  @SerializedName(value = "sessionType")
  @ColumnInfo(name = "session_type", defaultValue = "")
  @NotNull
  var sessionType: String = "",
  @SerializedName(value = "transactionType")
  @ColumnInfo(name = "transaction_type", defaultValue = "")
  var transactionType: String = "",
  @SerializedName(value = "value")
  @ColumnInfo(name = "value", defaultValue = "")
  @NotNull
  var value: String = ""
){

  constructor(topic:String,sessionType:String,transactionType: String="",value: String) : this() {
    this.topic=topic
    this.sessionType=sessionType
    this.transactionType=transactionType
    this.value=value
  }
}