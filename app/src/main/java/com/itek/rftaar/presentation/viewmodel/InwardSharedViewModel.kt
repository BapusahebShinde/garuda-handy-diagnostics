package com.itek.rftaar.presentation.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.itek.rftaar.DataHolder
import com.itek.rftaar.data.model.IOLevel


/*data class InwardHierarchy(
    val poNo: String? = null,
    val asnNo: String? = null,
    val huNo: String? = null
)*/

class InwardSharedViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_INWARD_LEVELS = "key_inward_levels"
        private const val KEY_SESSION_ID = "key_session_id"
    }

    val inwardLevels = DataHolder.IOLevels//mutableStateListOf<InwardLevel>()
    /*private val _hierarchy = MutableStateFlow(InwardHierarchy())
    val hierarchy = _hierarchy.asStateFlow()
*/

    init {
        // Restore after process death
        /*savedStateHandle.get<List<InwardLevel>>(KEY_INWARD_LEVELS)?.let {
            inwardLevels.clear()
            inwardLevels.addAll(it)
        }*/
    }

    fun addInwardLevel(IOLevel: IOLevel, menuCode: String? =  ""){
        inwardLevels.add(IOLevel)
        //savedStateHandle[KEY_INWARD_LEVELS] = inwardLevels
    }

    fun removeInwardLevel(IOLevel: IOLevel, menuCode: String? =  ""){
        inwardLevels.remove(IOLevel)
        //savedStateHandle[KEY_INWARD_LEVELS] = inwardLevels
    }

    fun getLastInwardLevel(): IOLevel? {
        return if(inwardLevels.isNotEmpty()) inwardLevels.get(inwardLevels.size-1) else null
    }

    fun removeLastInwardLevel(){
        if(inwardLevels.isNotEmpty()) inwardLevels.removeAt(inwardLevels.size-1)
        //savedStateHandle[KEY_INWARD_LEVELS] = inwardLevels
    }

    fun getInwardLevelList(): SnapshotStateList<IOLevel> {
        return inwardLevels;
    }

    /*private fun updateSessionId(menuCode: String) {
        savedStateHandle[KEY_SESSION_ID] = generateOfflineSessionId(menuCode)
    }*/

    fun generateOfflineSessionId(menuCode: String): String {
        val inwardCode = menuCode.substringAfter("MENU_").trim().uppercase()
        if (inwardLevels.isEmpty()) {
            return inwardCode
        }

        val levelPath = inwardLevels.joinToString("_") { level ->
            level.node
                .substringAfter(":")
                .trim()
                .uppercase()
        }

        return "${inwardCode}_$levelPath"
    }



    /*fun setInwardLevels(list: List<InwardLevel>) {
        inwardLevels.clear()
        inwardLevels.addAll(list)
        savedStateHandle[KEY_INWARD_LEVELS] = list
        //currentLevelIndex = 0
    }*/

    /*fun clear() {
        inwardLevels.clear()
        savedStateHandle.remove<List<InwardLevel>>(KEY_INWARD_LEVELS)
    }*/

    //var currentLevelIndex = 0

    /*fun moveToNextLevel() {
        if (currentLevelIndex < inwardLevels.lastIndex) {
            currentLevelIndex++
        }
    }*/

    /*fun getCurrentLevel(): InwardLevel? {
        return inwardLevels.getOrNull(currentLevelIndex)
    }

    fun hasNextLevel(): Boolean {
        return currentLevelIndex < inwardLevels.lastIndex
    }*/

    /*fun setPo(po: String) {
        _hierarchy.value = InwardHierarchy(poNo = po)
    }

    fun setAsn(asn: String) {
        _hierarchy.value = _hierarchy.value.copy(asnNo = asn)
    }

    fun setHu(hu: String) {
        _hierarchy.value = _hierarchy.value.copy(huNo = hu)
    }

    fun clearFromTrip() {
        _hierarchy.value = _hierarchy.value.copy(asnNo = null, huNo = null)
    }

    fun clearFromDetail() {
        _hierarchy.value = _hierarchy.value.copy(huNo = null)
    }

    fun clearAll() {
        _hierarchy.value = InwardHierarchy()
    }*/
}
