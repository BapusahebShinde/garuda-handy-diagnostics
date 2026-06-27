package com.itek.rftaar.domain.repository

import android.os.Bundle
import com.itek.rftaar.api.ApiResult
import org.json.JSONObject

interface ApiRepository {
    suspend fun callWebService(
        baseUrl: String,
        url: String,
        appendData: String?,
        queryMap: Map<String,String>?,
        jsonRequest: JSONObject? = null,
        isToken: Boolean=false,
        args:Bundle?=null,
        retryCount:Int =0
    ): ApiResult
}
