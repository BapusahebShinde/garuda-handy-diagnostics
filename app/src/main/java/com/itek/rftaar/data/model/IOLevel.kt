package com.itek.rftaar.data.model

import com.itek.rftaar.reader.constants.StatusConstants
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlinx.serialization.Serializable

@Serializable
data class IOLevel(val flowId: String, val flowName: String="", val level: Int, val node: String, val nodeLevel: Int, val total: Int, var completed: Int=0, var status:String="", var label: String=""){
    init {
      status = chkNull(status,if(completed==0) StatusConstants.PENDING else if(completed<total || (nodeLevel>1 && nodeLevel==level)) StatusConstants.INPROCESS else StatusConstants.COMPLETED)
      //TODO label = getLabelFromFlowId(flowId)
    }
}
