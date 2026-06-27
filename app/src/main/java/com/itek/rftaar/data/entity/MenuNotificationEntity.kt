package com.itek.rftaar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull


@Entity(
    tableName = "menu_notifications",
    indices = [Index(value = ["title","message","type","type_id","date"], unique = true)]
)
data class MenuNotificationEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "pk_id", defaultValue = "0") val pkId: Int = 0,
    @SerializedName(value = "title")
    @ColumnInfo(name = "title", defaultValue = "")
    @NotNull
    var title: String = "",
    @SerializedName(value = "message")
    @ColumnInfo(name = "message", defaultValue = "")
    @NotNull
    var message: String = "",
    @SerializedName(value = "type")
    @ColumnInfo(name = "type", defaultValue = "")
    @NotNull
    var type: String = "",
    @SerializedName(value = "typeId")
    @ColumnInfo(name = "type_id", defaultValue = "")
    @NotNull
    var typeId: String = "",
    @SerializedName(value = "userId")
    @ColumnInfo(name = "user_id", defaultValue = "")
    @NotNull
    var userId: String = "",
    @SerializedName(value = "date")
    @ColumnInfo(name = "date", defaultValue = "")
    @NotNull
    var date: String = "",
    @SerializedName(value = "validTill")
    @ColumnInfo(name = "valid_till", defaultValue = "24")
    @NotNull
    var validTill: Int = 24,
    @SerializedName(value = "validTillDate")
    @ColumnInfo(name = "valid_till_date", defaultValue = "")
    @NotNull
    var validTillDate: String = "",
    @SerializedName(value = "isRead")
    @ColumnInfo(name = "is_read", defaultValue = "false")
    @NotNull
    var isRead: Boolean = false,
)