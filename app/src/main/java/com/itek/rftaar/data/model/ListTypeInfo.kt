package com.itek.rftaar.data.model

import com.google.gson.annotations.SerializedName
import com.itek.rftaar.core.common.constants.DateFormatUtils
import com.itek.rftaar.mqtt.constants.SearchListTypeConstant
import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.utils.CommonUtils.chkNull

data class ListTypeInfo(
    @SerializedName(value="sessionId")
    val sessionId: String = "",
    @SerializedName(value="searchTypeId")
    val searchTypeId: String = "",
    @SerializedName(value="searchMasterId")
    val searchMasterId: String = "",
    @SerializedName(value="childSearchMasterId")
    val childSearchMasterId: String = "",
    @SerializedName(value="referenceNumber")
    val referenceNumber: String = "",
    @SerializedName(value="hasReferenceNumber")
    val hasReferenceNumber: Boolean = false,
    @SerializedName(value="status")
    val status: String = StatusConstants.PENDING,
    @SerializedName(value="qty")
    val qty: Int = 0,
    @SerializedName(value="createdAt")
    val createdAt: String = "",
    @SerializedName(value="expiredAt")
    val expiredAt: String = "",
    @SerializedName(value="searchTypeName")
    val searchTypeName: String = SearchListTypeConstant.LIST_BASED_SEARCH,
    @SerializedName(value="childSearchTypeName")
    val childSearchTypeName: String = "",
    @SerializedName(value="bucket")
    val bucket: String = "",
){

    fun getDisplayName():String{
        return chkNull(childSearchTypeName,"")
    }

    fun getDisplayLabel():String{
        return if(hasReferenceNumber && referenceNumber.isNotEmpty()) getSafeReferenceNumber() else getDisplayName()
    }

    fun getDisplayTitle():String{
        return getDisplayName() + (if(hasReferenceNumber && referenceNumber.isNotEmpty()) " ("+referenceNumber+")" else "")
    }

    fun getSafeReferenceNumber(): String {
        return chkNull(referenceNumber, getDisplayName())
    }

    fun getSafeQty(): Int {
        return chkNull(qty, 0)
    }

    fun getDisplayDate():String{
        return chkNull(DateFormatUtils.formatToDisplayTime(createdAt,DateFormatUtils.UTC_DATE_TIME_FORMAT),"")
    }

    fun isValid(): Boolean{
        return sessionId.trim().isNotEmpty() && searchTypeId.trim().isNotEmpty() && qty>0 && getDisplayDate().isNotEmpty() && getDisplayName().isNotEmpty()
    }
}
