package com.itek.rftaar.presentation.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itek.rftaar.R
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.utils.FileUtils.writeApiLog
import com.itek.rftaar.core.common.utils.ToastUtils
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.domain.usecase.api.CallWebServiceUseCase
import com.itek.rftaar.network.NetworkMonitor
import com.itek.rftaar.utils.CommonUtils.chkNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject constructor(private val callWebServiceUseCase: CallWebServiceUseCase) : ViewModel() {

    /*  private val _apiResult = MutableLiveData<ApiResult>()
      val apiResult: LiveData<ApiResult> get() = _apiResult*/

    private val _apiResult = MutableSharedFlow<ApiResult>(replay = 0)
    val apiResult = _apiResult.asSharedFlow()

    val apiErrorMsg = MutableLiveData<String>("")

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun callApi(url: String, appendData:String?=null, queryMap: HashMap<String, String>?=null, jsonRequest: JSONObject? = null, isToken: Boolean = false, args: Bundle?=null,baseUrl: String= DataStoreManager.readFromPreferences(ParameterConstants.BASE_URL, UrlConstants.BASE_URL)) {
        apiErrorMsg.postValue("")
        if(!isNetworkConnected(true)) return
        viewModelScope.launch {
            try {
                writeApiLog(
                    apiUrl = url,
                    request = if (jsonRequest!=null)jsonRequest.toString() else if (queryMap != null) queryMap.toString() else if (appendData != null) appendData else ""
                )
                _isLoading.postValue(true)
                val result = callWebServiceUseCase(baseUrl,url,appendData, queryMap,jsonRequest, isToken,args)
                if(!result.isSuccess) apiErrorMsg.postValue(chkNull(result.errMsg,""))
                writeApiLog(
                    apiUrl = url,
                    response = if (result != null && result.response != null) result.response.toString() else if (result != null && result.errMsg != null) result.errMsg else "",
                    responseCode = result.responseCode
                )
                _apiResult.emit(result)
            }
            catch (e: Exception) {
                e.printStackTrace()
                writeApiLog(
                    apiUrl = url,
                    request = if (jsonRequest!=null)jsonRequest.toString() else if (queryMap != null) queryMap.toString() else if (appendData != null) appendData else "",
                    response = e.message,
                    responseCode = -1
                )
                _apiResult.emit(ApiResult(url,appendData,jsonRequest))
            }
            finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun isNetworkConnected(isShowErrToast: Boolean = false): Boolean {
        val isConnected:Boolean= NetworkMonitor.isNetworkConnected
        if(!isConnected) {
            if(isShowErrToast) ToastUtils.showLongToast(R.string.err_internet_no_connect)
        }
        return isConnected
    }
}