package com.itek.rftaar.utils

import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractBoolean
import com.itek.rftaar.domain.model.LocationModel
import org.json.JSONArray

object ZoneUtils {
  fun processChildren(childrenArray: JSONArray,dataList:MutableList<LocationModel>,isOnlyReplenishmentSources:Boolean=false) {
      LogUtils.showLog("method", "processChildren")
      LogUtils.showLog("childrenArray", childrenArray.toString())
      LogUtils.showLog("dataList", "" + dataList.size)
      if (childrenArray != null && childrenArray.length() > 0) {
          for (i in 0 until childrenArray.length()) {
              val obj = childrenArray.getJSONObject(i)
              //Check & Only Add Replenishment Sources
              val isReplenishmentSource = extractBoolean(obj, ParameterConstants.REPLENISHMENT_SOURCE,false)
              if(isOnlyReplenishmentSources && !isReplenishmentSource) continue
              val location = LocationModel(
                  id = obj.optString(ParameterConstants.ID),
                  name = obj.optString(ParameterConstants.NAME),
                  code = obj.optString(ParameterConstants.CODE),
                  path = obj.optString(ParameterConstants.PATH)
              )
              dataList.add(location)
              val responseArray = ParseUtils.extractJSONArray(obj, ParameterConstants.CHILDREN, JSONArray())
              if (responseArray != null && responseArray.length() > 0) processChildren(responseArray, dataList,isOnlyReplenishmentSources);
          }
      }
  }

  fun processChildren(childrenArray: JSONArray,dataListSrc:MutableList<LocationModel>,dataListDest:MutableList<LocationModel>){
    LogUtils.showLog("method","processChildren")
    LogUtils.showLog("childrenArray",childrenArray.toString())
    if(childrenArray!=null && childrenArray.length()>0){
      for (i in 0 until childrenArray.length()) {
        val obj = childrenArray.getJSONObject(i)
        val location = LocationModel(
          id = obj.optString(ParameterConstants.ID),
          name = obj.optString(ParameterConstants.NAME),
          code = obj.optString(ParameterConstants.CODE),
          path = obj.optString(ParameterConstants.PATH)
        )
        val isReplenishmentSource =extractBoolean(obj, ParameterConstants.REPLENISHMENT_SOURCE,false)
        if(isReplenishmentSource) dataListSrc.add(location)
        else dataListDest.add(location)
        val responseArray = ParseUtils.extractJSONArray(obj, ParameterConstants.CHILDREN, JSONArray())
        if(responseArray!=null && responseArray.length()>0) processChildren(responseArray,dataListSrc,dataListDest);
      }
    }
  }
}