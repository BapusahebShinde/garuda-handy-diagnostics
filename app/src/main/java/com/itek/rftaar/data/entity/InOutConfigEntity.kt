package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
  tableName = "config",
  indices = [Index(value = ["topic","id"], unique = true)]
)
data class InOutConfigEntity(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk_id", defaultValue = "0") val pkId: Int = 0,
  @SerializedName(value = "topic")
  @ColumnInfo(name = "topic", defaultValue = "")
  var topic:String="",
  @SerializedName(value = "id")
  @ColumnInfo(name = "id", defaultValue = "")
  val id:String="",
  @SerializedName(value = "hierarchyName")
  @ColumnInfo(name = "hierarchy_name", defaultValue = "")
  val hierarchyName:String="",
  @SerializedName(value = "depth")
  @ColumnInfo(name = "depth", defaultValue = "0")
  val depth:Int=0,
  @SerializedName(value = "levelsJArray")
  @ColumnInfo(name = "levels_jarray", defaultValue = "[]")
  var levelsJArray:String="[]",
  @SerializedName(value = "rulesJObj")
  @ColumnInfo(name = "rules_jobj", defaultValue = "{}")
  var rulesJObj:String="{}"
)
