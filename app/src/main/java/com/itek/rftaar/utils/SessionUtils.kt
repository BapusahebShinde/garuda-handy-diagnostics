package com.itek.rftaar.utils

import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.DataStoreManager
import java.util.Calendar
import java.util.Locale

object SessionUtils {

    fun generateOfflineSessionId(sessionType: String, transactionType: String = ""): String {
        if (sessionType.isEmpty()) return ""
        val cal = Calendar.getInstance()
        var sessionId = sessionType.uppercase(Locale.getDefault()).replace("_","")
        sessionId += transactionType.uppercase(Locale.getDefault()).replace("_","")
        sessionId += DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID,"")
        sessionId += cal.get(Calendar.YEAR)
        sessionId += (cal.get(Calendar.MONTH) + 1).toString(32).uppercase()
        sessionId += cal.get(Calendar.DAY_OF_MONTH).toString(32).uppercase()
        sessionId += cal.get(Calendar.HOUR_OF_DAY).toString(32).uppercase()
        sessionId += String.format("%02d", cal.get(Calendar.MINUTE))
        sessionId += String.format("%02d", cal.get(Calendar.SECOND))

        LogUtils.showLog("DeviceSession", "Generated sessionId = $sessionId")
        return UUIDV5.generateUUID(sessionId).toString()
    }

}