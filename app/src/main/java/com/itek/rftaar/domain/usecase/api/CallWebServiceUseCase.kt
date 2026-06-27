package com.itek.rftaar.domain.usecase.api

import android.os.Bundle
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.domain.repository.ApiRepository
import org.json.JSONObject
import javax.inject.Inject

class CallWebServiceUseCase @Inject constructor(
  private val repository: ApiRepository
) {
  suspend operator fun invoke(
      baseUrl: String,
      url: String,
      appendData: String? = null,
      queryMap: Map<String,String>?,
      jsonRequest: JSONObject? = null,
      isToken: Boolean = false,
      args: Bundle?=null
  ): ApiResult {
    return repository.callWebService(baseUrl,url, appendData, queryMap,jsonRequest, isToken,args)
  }
}