/*
package com.itek.rftaar.sensors.providers

import android.content.Context
import android.hardware.SensorManager
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.core.common.utils.LogUtils.showLog
import com.itek.rftaar.sensors.MainSensorRepository

class CommonSensorRepository : MainSensorRepository {
  */
/**
   * Instantiates a new Main repository.
   *
   * @param context the context
   *//*

  constructor(context: CommonActivity) :super(context)

  override fun getSensor() {
    currentOrientationProvider = CommonProvider(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
  }

  fun checkSensorData(){
    if (currentOrientationProvider != null){
      currentOrientationProvider!!.getSensorData().observeForever{sensorData ->
        showLog("sr_sensorData22", "" + sensorData)
      }
    }
  }
}*/
