package com.itek.rftaar.sensors.providers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.itek.rftaar.sensors.direction.OrientationProvider
import kotlin.math.acos

/**
 * The orientation provider that delivers the current orientation from the [ Gravity][Sensor.TYPE_GRAVITY] and [Compass][Sensor.TYPE_MAGNETIC_FIELD].
 *
 * @author Bapusaheb Shinde
 */
class GravityAndCompassProvider(sensorManager: SensorManager) : OrientationProvider(sensorManager) {
  /**
   * Compass values
   */
  private val magnitudeValues: FloatArray? = FloatArray(3)

  /**
   * Gravity values
   */
  private val gravityValues: FloatArray? = FloatArray(3)

  /**
   * Inclination values
   */
  var inclinationValues: FloatArray = FloatArray(16)

  /**
   * Initialises a new GravityAndCompassProvider
   *
   * @param sensorManager The android sensor manager
   */
  init {
    //Add the compass and the gravity sensor
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY))
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
  }

  override fun onSensorChanged(event: SensorEvent) {
    // we received a sensor event. it is a good practice to check
    // that we received the proper event
    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      System.arraycopy(event.values, 0, magnitudeValues, 0, magnitudeValues!!.size)
    } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
      System.arraycopy(event.values, 0, gravityValues, 0, gravityValues!!.size)
    }

    if (magnitudeValues != null && gravityValues != null) {
      // Fuse gravity-sensor (virtual sensor) with compass
      SensorManager.getRotationMatrix(
        currentOrientationRotationMatrix.matrix,
        null,
        gravityValues,
        magnitudeValues
      )
      // Transform rotation matrix to quaternion
      currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix)

      var currentAngle =
        (2.0f * acos(currentOrientationQuaternion.getW().toDouble()) * 180.0f / Math.PI).toFloat()
          .toDouble()
      if (currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle

      sensorValues.postValue(currentAngle.toString() + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ())
    }
  }
}
