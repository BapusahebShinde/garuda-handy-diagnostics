package com.itek.rftaar.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.itek.rftaar.core.common.utils.JsonUtils
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    application: Application
) : ViewModel() {

    private val db = AppDatabase.getDbInstance(application)
    private val menuDao = db.menuDao()
    val menus = menuDao.getAllHomeMenus().asLiveData()
    val context = Dispatchers.IO

    fun insertMenu(response: JSONObject) = viewModelScope.launch(context) {
        LogUtils.showLog("insertMenu", "Response → $response")
        JsonUtils.parseLocationConfig(response)
    }

    fun setupMenus(response: JSONObject) = viewModelScope.launch(context) {
        LogUtils.showLog("setupMenu", "Response → $response")
        JsonUtils.parseMenus(response)
    }
}

