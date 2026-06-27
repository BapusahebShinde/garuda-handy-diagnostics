package com.itek.rftaar.sensors

import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.CommonActivity
import com.itek.rftaar.sensors.direction.OrientationProvider
import com.itek.rftaar.sensors.providers.CommonProvider

class MainSensorRepository {
  protected lateinit var context: CommonActivity
  protected var currentOrientationProvider: OrientationProvider? = null

  constructor(context: CommonActivity){
    this.context=context
    currentOrientationProvider = CommonProvider(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
  }

  /**
   * Get sensor instance object.
   *
   * @return the object
   */
  fun getSensor() {
    currentOrientationProvider = CommonProvider(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
  }

  fun getSensorAndStart() {
    if (currentOrientationProvider == null) getSensor()
    if (currentOrientationProvider != null) currentOrientationProvider!!.start()
  }

  fun stopSensor() {
    if (currentOrientationProvider != null) currentOrientationProvider!!.stop()
  }

  fun onResume() {
    // Ideally a game should implement onResume() and onPause()
    // to take appropriate action when the activity looses focus
    getSensorAndStart()
  }

  fun onPause() {
    stopSensor()
  }

  fun getSensorData(): MutableLiveData<String> {
    return if (currentOrientationProvider != null) currentOrientationProvider!!.getSensorData() else MutableLiveData<String>("0$0$0$0")
  }
}