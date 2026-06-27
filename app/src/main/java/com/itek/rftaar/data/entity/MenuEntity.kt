package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull

@Entity(tableName = "menus", indices = [Index(value = ["code"], unique = true)])
data class MenuEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk_id", defaultValue = "0") val pkId: Int = 0,
    @SerializedName(value = "code", alternate = ["menuCode"])
    @ColumnInfo(name = "code", defaultValue = "")
    @NotNull
    var code: String = "",
    @SerializedName(value = "isEnabled", alternate = ["enabled"])
    @ColumnInfo(name = "is_enabled", defaultValue = "false")
    var isEnabled: Boolean = false,
    @ColumnInfo(name = "icon", defaultValue = "")
    val icon: String = "",
    @SerializedName(value = "isActive", alternate = ["active"])
    @ColumnInfo(name = "is_active", defaultValue = "false")
    var isActive: Boolean = false,
    @SerializedName(value = "label", alternate = ["menuName"])
    @ColumnInfo(name = "label", defaultValue = "")
    val label: String = "",
    @SerializedName(value = "parentCode", alternate = ["menuParentCode"])
    @ColumnInfo(name = "parent_code", defaultValue = "")
    var parentCode: String = "",
    @SerializedName(value = "path", alternate = ["menuPath"])
    @ColumnInfo(name = "path", defaultValue = "")
    var path: String = "",
    @SerializedName(value = "sequence", alternate = ["seq"])
    @ColumnInfo(name = "sequence", defaultValue = "0")
    val sequence: Int =0,
    @ColumnInfo(name = "has_dashboard", defaultValue = "false")
    var hasDashboard: Boolean = false,
)

