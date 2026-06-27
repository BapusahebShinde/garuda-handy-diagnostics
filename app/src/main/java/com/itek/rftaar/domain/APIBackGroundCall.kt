package com.itek.rftaar.domain

import android.content.Context
import android.os.Bundle
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.utils.FileUtils.writeApiLog
import com.itek.rftaar.core.common.utils.JsonUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.repositoryImpl.ApiRepositoryImpl
import com.itek.rftaar.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object APIBackGroundCall {

    fun callApi(context: Context, url: String, appendData:String?=null, queryMap: HashMap<String, String>?=null, jsonRequest: JSONObject? = null, isToken: Boolean = false, args: Bundle?=null, baseUrl: String= DataStoreManager.readFromPreferences(ParameterConstants.BASE_URL, UrlConstants.BASE_URL)) {
        if(!NetworkMonitor.isNetworkConnected) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                writeApiLog(
                    apiUrl = url,
                    request = if (jsonRequest!=null) jsonRequest.toString() else if (queryMap != null) queryMap.toString() else if (appendData != null) appendData else ""
                )
                val result =  ApiRepositoryImpl(context).callWebService(baseUrl,url,appendData, queryMap,jsonRequest, isToken,args)
                writeApiLog(
                    apiUrl = url,
                    response = if (result != null && result.response != null) result.response.toString() else if (result != null && result.errMsg != null) result.errMsg else "",
                    responseCode = result.responseCode
                )
                handleFullyOfflineResponse(result)
            }
            catch (e: Exception) {
                e.printStackTrace()
                writeApiLog(
                    apiUrl = url,
                    request = if (jsonRequest!=null) jsonRequest.toString() else if (queryMap != null) queryMap.toString() else if (appendData != null) appendData else "",
                    response = e.message,
                    responseCode = -1
                )
            }
            finally {
            }
        }
    }

    private suspend fun handleFullyOfflineResponse(result: ApiResult){
            if(result.response==null || !result.isSuccess) return
            val jsonResponse = result.response
            when (result.url) {
                UrlConstants.DEVICE_MENUS -> JsonUtils.parseMenus(jsonResponse)
            }
    }
}