package com.itek.rftaar

import androidx.compose.runtime.mutableStateListOf
import com.itek.rftaar.data.model.IOLevel
import com.itek.rftaar.data.model.ZoneStockQty
import kotlinx.coroutines.flow.MutableStateFlow

object DataHolder {
    val zoneWiseStockQtyList = mutableStateListOf<ZoneStockQty>()
    val IOLevels = mutableStateListOf<IOLevel>()
    var currentScreen:String=""
    var isAppInForeground: Boolean = false
    val targetRoute = MutableStateFlow<String?>(null)
    val isForceLogOut = MutableStateFlow<Boolean>(false)
}