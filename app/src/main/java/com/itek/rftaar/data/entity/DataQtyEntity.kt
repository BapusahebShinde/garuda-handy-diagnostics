package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull

@Entity(
  tableName = "data_qty",
  indices = [Index(value = ["topic","session_type","transaction_type","article","barcode","epc"], unique = true)]
)
data class DataQtyEntity(
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
  @SerializedName(value = "article")
  @ColumnInfo(name = "article", defaultValue = "")
  var article:String="",
  @SerializedName(value = "barcode")
  @ColumnInfo(name = "barcode", defaultValue = "")
  var barcode:String="",
  @SerializedName(value = "epc")
  @ColumnInfo(name = "epc", defaultValue = "")
  var epc:String="",
  @SerializedName(value = "qty")
  @ColumnInfo(name = "qty", defaultValue = "0")
  val qty:Int=0,
  @SerializedName(value = "scanQty")
  @ColumnInfo(name = "scan_qty", defaultValue = "0")
  var scanQty:Int=0
)