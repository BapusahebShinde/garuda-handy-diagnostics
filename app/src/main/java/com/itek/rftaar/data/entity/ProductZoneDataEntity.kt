package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull

@Entity(
  "product_zones", indices = [Index(
    value = ["topic", "session_type", "transaction_type", "asset_location_path", "asset_location_name", "barcode", "quantity", "epc"],
    unique = true
  )]
)
data class ProductZoneDataEntity(
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
  @SerializedName(value = "sessionId")
  @ColumnInfo(name = "session_id", defaultValue = "")
  var sessionId: String = "",
  @SerializedName(value = "assetLocationPath")
  @ColumnInfo(name = "asset_location_path", defaultValue = "")
  var assetLocationPath: String = "",
  @SerializedName(value = "assetLocationName")
  @ColumnInfo(name = "asset_location_name", defaultValue = "")
  var assetLocationName: String = "",
  @SerializedName(value = "barcode")
  @ColumnInfo(name = "barcode", defaultValue = "")
  var barcode: String = "",
  @SerializedName(value = "customField")
  @ColumnInfo(name = "custom_field", defaultValue = "")
  var customField: String = "",
  @SerializedName(value = "stock")
  @ColumnInfo(name = "stock", defaultValue = "0")
  var stock: Int = 0,
  @SerializedName(value = "totalAvailableStock", alternate = ["totalStock"])
  @ColumnInfo(name = "total_stock", defaultValue = "0")
  var totalAvailableStock: Int = 0,
  @SerializedName(value = "quantity")
  @ColumnInfo(name = "quantity", defaultValue = "0")
  var quantity: Int = 0,
  @SerializedName(value = "epc")
  @ColumnInfo(name = "epc", defaultValue = "")
  var epc: String = "",
  @SerializedName(value = "displayData")
  @ColumnInfo(name = "display_data", defaultValue = "")
  var displayData: String = "",
  @SerializedName(value = "foundQty")
  @ColumnInfo(name = "found_qty", defaultValue = "0")
  var foundQty: Int = 0,
  @SerializedName(value = "decodeQty")
  @ColumnInfo(name = "decode_qty", defaultValue = "0")
  var decodeQty: Int = 0,
  @SerializedName(value = "errMsg")
  @ColumnInfo(name = "err_msg", defaultValue = "")
  var errMsg: String = "",
) {

  constructor(topic: String, sessionType: String, transactionType: String,sessionId: String) : this() {
    this.topic = topic
    this.sessionType = sessionType
    this.transactionType = transactionType
    this.sessionId = sessionId
  }

  constructor(
    topic: String,
    sessionType: String,
    transactionType: String,
    sessionId: String,
    assetLocationPath: String = "",
    assetLocationName: String = "",
    barcode: String = ""
  ) : this(topic, sessionType, transactionType,sessionId) {
    this.assetLocationPath = assetLocationPath
    this.assetLocationName = if (assetLocationName.contains("/")) assetLocationName.substring(
      assetLocationName.lastIndexOf("/") + 1
    ).trim() else assetLocationName
    this.barcode = barcode
  }


}