package com.itek.rftaar.di

import com.google.gson.JsonObject
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @Streaming
    @GET
    suspend fun downloadFileByUrl(@Url fileUrl: String?): Response<ResponseBody>

    @GET(UrlConstants.VALIDATE_URL)
    suspend fun validateURL(): Response<ResponseBody>

    //Forgot Password
    @POST(UrlConstants.FORGOT_PASSWORD)
    suspend fun userForgotPassword(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    //Login
    @POST(UrlConstants.USER_LOGIN)
    suspend fun userLogin(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @GET(UrlConstants.USER_DETAILS)
    suspend fun userDetails(): Response<ResponseBody>

    //Locations
    @GET(UrlConstants.LOCATION_LIST + "{" + ParameterConstants.CUSTOMER_ID + "}?isActive=true&isPhysical=true&page=1&limit=" + UrlConstants.LOCATION_LIST_PAGE_LIMIT)
    suspend fun locationList(@Path(ParameterConstants.CUSTOMER_ID) type: String?): Response<ResponseBody>

    @GET(UrlConstants.LOCATION_CONFIG + "{" + ParameterConstants.LOCATION_ID + "}")
    suspend fun locationConfig(@Path(ParameterConstants.LOCATION_ID) locationId: String?): Response<ResponseBody>

    @GET(UrlConstants.LOCATION_CONFIG + "{" + ParameterConstants.LOCATION_ID + "}/children")
    suspend fun locationZones(@Path(ParameterConstants.LOCATION_ID) locationId: String?): Response<ResponseBody>

    @GET(UrlConstants.LOCATION_CONFIG + "{" + ParameterConstants.LOCATION_ID + "}/descendants")
    suspend fun locationSubZones(@Path(ParameterConstants.LOCATION_ID) locationId: String?): Response<ResponseBody>

    //Devices
    @POST(UrlConstants.DEVICE_CREATE)
    suspend fun deviceCreate(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @GET(UrlConstants.DEVICE_CHECK + "{" + ParameterConstants.DEVICE_ID + "}")
    suspend fun deviceCheck(@Path(ParameterConstants.DEVICE_ID) locationId: String?): Response<ResponseBody>

    @POST(UrlConstants.DEVICE_LOGIN)
    suspend fun deviceLogin(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @GET(UrlConstants.DEVICE_MENUS)
    suspend fun deviceMenus(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_ENCODE_COUNT)
    suspend fun encodeCount(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @POST(UrlConstants.ENCODING)
    suspend fun encode(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @GET(UrlConstants.PRODUCTS)
    suspend fun products(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.PRODUCT_MAPPING)
    suspend fun productMap(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>
    @GET(UrlConstants.PRODUCT_CHART)
    suspend fun productChart(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>


    @GET(UrlConstants.GET_SEARCH_COUNT)
    suspend fun searchCount(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>

    @POST(UrlConstants.PRODUCT_UPDATE)
    suspend fun productUpdate(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @GET(UrlConstants.PRODUCT_SEARCH_LIST)
    suspend fun productSearchList(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.PRODUCT_SEARCH_LIST_CONFIG)
    suspend fun productSearchListConfig(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.PRODUCT_SEARCH_LIST_DETAILS)
    suspend fun productSearchListDetails(@QueryMap(encoded = true) map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_OMNI_DASHBOARD)
    suspend fun getOmniDashboard(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    //Decoding (Get Password List)
    @GET(UrlConstants.GET_PASSWORD + "{" + ParameterConstants.LOCATION_ID + "}")
    suspend fun getPasswords(@Path(ParameterConstants.LOCATION_ID) locationId: String?): Response<ResponseBody>

    // Decoding Types
    @GET(UrlConstants.GET_DECODE_TYPES)
    suspend fun getDecodeTypes(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    //Inventory
    @POST(UrlConstants.START_DEVICE_SESSION_INVENTORY)
    suspend fun startDeviceSessionInventory(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @POST(UrlConstants.STOP_DEVICE_SESSION_INVENTORY)
    suspend fun stopDeviceSessionInventory(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @POST(UrlConstants.CANCEL_DEVICE_SESSION_INVENTORY)
    suspend fun cancelDeviceSessionInventory(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @GET(UrlConstants.GET_STOCK_COUNT)
    suspend fun stockCount(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.CUSTOM_INVENTORY_PARAMS)
    suspend fun customInventoryParamter(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.CUSTOM_INVENTORY_ADVANCED_FILTERS)
    suspend fun customInventoryAdvancedFilters(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.CUSTOM_INVENTORY_EAN_LIST)
    suspend fun customInventoryEanList(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.STOCK_DISCREPANCY)
    suspend fun stockDiscrepancy(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.STOCK_DISCREPANCY_LOCATION)
    suspend fun stockDiscrepancyLocation(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    //Replenishment
    @GET(UrlConstants.GET_REPLENISHMENT_LIST)
    suspend fun getReplenishmentList(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_REPLENISHMENT_DASHBOARD)
    suspend fun getReplenishmentDashboard(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    //Inward
    @GET(UrlConstants.GET_INWARD_LIST)
    suspend fun getInwardList(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_INWARD_CHILDREN_LIST)
    suspend fun getInwardChildrenList(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_INWARD_BOX_DETAILS)
    suspend fun getInwardBoxDetails(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @POST(UrlConstants.UPLOAD_INWARD_SCANNED)
    suspend fun uploadInwardScanned(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @POST(UrlConstants.COMPLETE_INWARD_NODE)
    suspend fun completeInwardNode(@Body jsonRequest: JsonObject?): Response<ResponseBody>


    //Outward
    @GET(UrlConstants.GET_OUTWARD_LIST)
    suspend fun getOutwardList(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_OUTWARD_CHILDREN_LIST)
    suspend fun getOutwardChildrenList(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @GET(UrlConstants.GET_OUTWARD_BOX_DETAILS)
    suspend fun getOutwardBoxDetails(@QueryMap map: Map<String, String>?): Response<ResponseBody>

    @POST(UrlConstants.UPLOAD_OUTWARD_SCANNED)
    suspend fun uploadOutwardScanned(@Body jsonRequest: JsonObject?): Response<ResponseBody>

    @POST(UrlConstants.COMPLETE_OUTWARD_NODE)
    suspend fun completeOutwardNode(@Body jsonRequest: JsonObject?): Response<ResponseBody>





}