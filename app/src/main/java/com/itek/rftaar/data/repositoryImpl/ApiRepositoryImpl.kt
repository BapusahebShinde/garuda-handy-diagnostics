package com.itek.rftaar.data.repositoryImpl

import android.content.Context
import android.os.Bundle
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractBoolean
import com.itek.rftaar.core.common.utils.ParseUtils.extractInt
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.remote.HttpClientProvider
import com.itek.rftaar.data.remote.RetrofitProvider
import com.itek.rftaar.di.ApiService
import com.itek.rftaar.domain.repository.ApiRepository
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.getRequestJson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response


class ApiRepositoryImpl(private val context: Context) : ApiRepository {
  override suspend fun callWebService(
    baseUrl: String,
    url: String,
    appendData: String?,
    queryMap: Map<String, String>?,
    jsonRequest: JSONObject?,
    isToken: Boolean,
    args: Bundle?,
    retryCount: Int
  ): ApiResult {
    //val baseUrl = DataStoreManager.readFromPreferences(ParameterConstants.BASE_URL, UrlConstants.BASE_URL)
    val api = RetrofitProvider.createApiService(baseUrl, HttpClientProvider.provideOkHttpClient(isToken))
    if (!isToken && url != UrlConstants.USER_LOGIN && url != UrlConstants.DEVICE_LOGIN && url != UrlConstants.VALIDATE_URL && DataStoreManager.getAccessTokenTime() <= HttpClientProvider.TIME_OUT) {
      val apiResult: ApiResult = refreshAccessToken(api)
      if (!apiResult.isSuccess) {
        return ApiResult(
          url,
          appendData,
            if(jsonRequest==null && queryMap!=null && queryMap.isNotEmpty()) JSONObject(queryMap) else jsonRequest,
          false,
          null,
          apiResult.responseCode,
          chkNull(apiResult.errMsg, "")
        )
      }
    }
    //else {
    val response: Response<ResponseBody>? =
      invokeMethod(api, url, appendData, queryMap, jsonRequest, args)
    if (response != null) {
      val code = response.code();
      try {
        //Re-call same method (which will re-call refreshAccessToken) in case of token expired response
        if(code==401 && retryCount<3 && url != UrlConstants.USER_LOGIN && url != UrlConstants.DEVICE_LOGIN && url != UrlConstants.VALIDATE_URL){
          DataStoreManager.setAccessTokenTime(0L)
          return callWebService(baseUrl,url,appendData,queryMap,jsonRequest,false,args,retryCount+1)
        }
        //LogUtils.showLog("response.body",chkNull(response.body()?.string(),"-"))
        val jsonResponse = response.body()?.string()?.let { JSONObject(it) }
        val isSuccess = response.isSuccessful && jsonResponse != null;
        //Pending in case of network error set specific error message (e.g. 404)
        var errMsg: String = if (isSuccess) ""
        else if (jsonResponse != null) ParseUtils.extractErrMsg(jsonResponse)
        else if (response.errorBody() != null) ParseUtils.extractErrMsg(response.errorBody()?.string()?.let { JSONObject(it) })
        else if (response.message() != null) response.message()
        else ""
        return ApiResult(
          url,
          appendData,
          if(jsonRequest==null && queryMap!=null && queryMap.isNotEmpty()) JSONObject(queryMap) else jsonRequest,
          isSuccess,
          jsonResponse,
          code,
          errMsg,
          args
        )
      } catch (e: Exception) {
        e.printStackTrace()
        return ApiResult(url, appendData, if(jsonRequest==null && queryMap!=null && queryMap.isNotEmpty()) JSONObject(queryMap) else jsonRequest,false,null,code,e.message, args = args)
      }
      //return chkNull(response.body(),chkNull(response.errorBody(),response.message())) // Map DTO to domain model
    } else {
      return ApiResult(url, appendData, if(jsonRequest==null && queryMap!=null && queryMap.isNotEmpty()) JSONObject(queryMap) else jsonRequest, args = args)
      //throw Exception("API Error: ${response?.code()}")
    }
    //}
  }

  private suspend fun invokeMethod(
    apiCall: ApiService,
    url: String,
    appendData: String?,
    queryMap: Map<String, String>?,
    jsonRequest: JSONObject?,
    args: Bundle?
  ): Response<ResponseBody>? {
    val page = extractInt(args, ParameterConstants.PAGE, 1)

    when (url) {
      UrlConstants.VALIDATE_URL -> return apiCall.validateURL()
      UrlConstants.USER_LOGIN -> return apiCall.userLogin(getRequestJson(jsonRequest))
      UrlConstants.FORGOT_PASSWORD -> return apiCall.userForgotPassword(getRequestJson(jsonRequest))
      UrlConstants.USER_DETAILS -> return apiCall.userDetails()
      UrlConstants.DEVICE_CHECK -> return apiCall.deviceCheck(appendData)
      UrlConstants.DEVICE_CREATE -> return apiCall.deviceCreate(getRequestJson(jsonRequest))
      UrlConstants.DEVICE_LOGIN -> return apiCall.deviceLogin(getRequestJson(jsonRequest))
      UrlConstants.DEVICE_MENUS -> return apiCall.deviceMenus(queryMap)
      UrlConstants.LOCATION_LIST -> return apiCall.locationList(appendData)
      UrlConstants.LOCATION_CONFIG -> return apiCall.locationConfig(appendData)
      UrlConstants.LOCATION_ZONES -> return apiCall.locationZones(appendData)
      UrlConstants.LOCATION_SUB_ZONES -> return apiCall.locationSubZones(appendData)
      UrlConstants.GET_ENCODE_COUNT -> return apiCall.encodeCount(queryMap)
      UrlConstants.ENCODING -> return apiCall.encode(getRequestJson(jsonRequest))
      UrlConstants.GET_PASSWORD -> return apiCall.getPasswords(appendData)
      UrlConstants.GET_DECODE_TYPES -> return apiCall.getDecodeTypes(queryMap)
      UrlConstants.GET_SEARCH_COUNT -> return apiCall.searchCount(queryMap)
      UrlConstants.PRODUCTS -> return apiCall.products(queryMap)
      UrlConstants.PRODUCT_MAPPING -> return apiCall.productMap(queryMap)
      UrlConstants.PRODUCT_CHART -> return apiCall.productChart(queryMap)
      UrlConstants.PRODUCT_UPDATE -> return apiCall.productUpdate(getRequestJson(jsonRequest))
      UrlConstants.PRODUCT_SEARCH_LIST -> return apiCall.productSearchList(queryMap)
      UrlConstants.PRODUCT_SEARCH_LIST_CONFIG -> return apiCall.productSearchListConfig(queryMap)
      UrlConstants.PRODUCT_SEARCH_LIST_DETAILS -> return apiCall.productSearchListDetails(queryMap)
      UrlConstants.GET_OMNI_DASHBOARD -> return apiCall.getOmniDashboard(queryMap)
      UrlConstants.GET_STOCK_COUNT -> return apiCall.stockCount(queryMap)
      UrlConstants.CUSTOM_INVENTORY_PARAMS -> return apiCall.customInventoryParamter(queryMap)
      UrlConstants.CUSTOM_INVENTORY_ADVANCED_FILTERS -> return apiCall.customInventoryAdvancedFilters(queryMap)
      UrlConstants.CUSTOM_INVENTORY_EAN_LIST -> return apiCall.customInventoryEanList(queryMap)
      UrlConstants.STOCK_DISCREPANCY -> return apiCall.stockDiscrepancy(queryMap)
      UrlConstants.STOCK_DISCREPANCY_LOCATION -> return apiCall.stockDiscrepancyLocation(queryMap)
      UrlConstants.START_DEVICE_SESSION_INVENTORY -> return apiCall.startDeviceSessionInventory(getRequestJson(jsonRequest))
      UrlConstants.STOP_DEVICE_SESSION_INVENTORY -> return apiCall.stopDeviceSessionInventory(getRequestJson(jsonRequest))
      UrlConstants.CANCEL_DEVICE_SESSION_INVENTORY -> return apiCall.cancelDeviceSessionInventory(getRequestJson(jsonRequest))
      UrlConstants.GET_REPLENISHMENT_LIST -> return apiCall.getReplenishmentList(queryMap)
      UrlConstants.GET_REPLENISHMENT_DASHBOARD -> return apiCall.getReplenishmentDashboard(queryMap)
      UrlConstants.GET_INWARD_LIST -> return apiCall.getInwardList(queryMap)
      UrlConstants.GET_INWARD_CHILDREN_LIST -> return apiCall.getInwardChildrenList(queryMap)
      UrlConstants.GET_INWARD_BOX_DETAILS -> return apiCall.getInwardBoxDetails(queryMap)
      UrlConstants.UPLOAD_INWARD_SCANNED -> return apiCall.uploadInwardScanned(getRequestJson(jsonRequest))
      UrlConstants.COMPLETE_INWARD_NODE -> return apiCall.completeInwardNode(getRequestJson(jsonRequest))
      UrlConstants.GET_OUTWARD_LIST -> return apiCall.getOutwardList(queryMap)
      UrlConstants.GET_OUTWARD_CHILDREN_LIST -> return apiCall.getOutwardChildrenList(queryMap)
      UrlConstants.GET_OUTWARD_BOX_DETAILS -> return apiCall.getOutwardBoxDetails(queryMap)
      UrlConstants.UPLOAD_OUTWARD_SCANNED -> return apiCall.uploadOutwardScanned(getRequestJson(jsonRequest))
      UrlConstants.COMPLETE_OUTWARD_NODE -> return apiCall.completeOutwardNode(getRequestJson(jsonRequest))

      else -> return null
    }
  }

  private suspend fun refreshAccessToken(api: ApiService): ApiResult {
    val jsonRequest = JSONObject()
    jsonRequest.put(
      ParameterConstants.USERNAME,
      DataStoreManager.readFromPreferences(ParameterConstants.USERNAME, "")
    )
    jsonRequest.put(
      ParameterConstants.PASSWORD,
      DataStoreManager.readFromPreferences(ParameterConstants.PASSWORD, "")
    )
    val response = callWebService(
      DataStoreManager.readFromPreferences(
        ParameterConstants.BASE_URL,
        UrlConstants.BASE_URL
      ), UrlConstants.USER_LOGIN, null, null, jsonRequest = jsonRequest, isToken = true
    )
    if (response.isSuccess && response.response != null) {
      try {
        val json = response.response
        val token =
          "${json.optString(ParameterConstants.TOKEN_TYPE)} ${json.optString(ParameterConstants.ACCESS_TOKEN)}"
        DataStoreManager.saveToPreferences(ParameterConstants.ACCESS_TOKEN, token)
        val refreshToken =
          "${json.optString(ParameterConstants.TOKEN_TYPE)} ${json.optString(ParameterConstants.REFRESH_TOKEN)}"
        DataStoreManager.saveToPreferences(ParameterConstants.REFRESH_TOKEN, refreshToken)
        val expiry = json.optLong(ParameterConstants.EXPIRES_IN)
        DataStoreManager.setAccessTokenTime(expiry)

          //Temp code ?
          val isMqttSecure = extractBoolean(json,ParameterConstants.IS_MQTT_SECURE,false)
          val mqttPort = extractInt(json,ParameterConstants.MQTT_PORT,if(isMqttSecure) 8883 else 1883)
          val mqttDom = extractString(json,ParameterConstants.MQTT_DOMAIN,extractString(json,ParameterConstants.MQTT_IP,""))

          val fullMqttURL = (if(isMqttSecure) "ssl://" else "tcp://")+mqttDom+":"+mqttPort
          LogUtils.showLog("fullMqttURL",fullMqttURL)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return response;
  }
}


