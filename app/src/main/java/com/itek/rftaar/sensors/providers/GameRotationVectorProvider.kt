package com.itek.rftaar.sensors.providers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.itek.rftaar.sensors.direction.OrientationProvider
import kotlin.math.acos

/**
 * The orientation provider that delivers the current orientation from the [Android][Sensor.TYPE_ROTATION_VECTOR].
 *
 * @author Bapusaheb Shinde
 */
class GameRotationVectorProvider(sensorManager: SensorManager) :
  OrientationProvider(sensorManager) {
  /**
   * Temporary quaternion to store the values obtained from the SensorManager
   */
  private val temporaryQuaternion = FloatArray(4)

  /**
   * Initialises a new RotationVectorProvider
   *
   * @param sensorManager The android sensor manager
   */
  init {
    //The rotation vector sensor that is being used for this provider to get device orientation
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR))
  }

  override fun onSensorChanged(event: SensorEvent) {
    // we received a sensor event. it is a good practice to check
    // that we received the proper event
    if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
      // convert the rotation-vector to a 4x4 matrix. the matrix
      // is interpreted by Open GL as the inverse of the
      // rotation-vector, which is what we want.
      SensorManager.getRotationMatrixFromVector(
        currentOrientationRotationMatrix.matrix,
        event.values
      )

      // Get Quaternion
      // Calculate angle. Starting with API_18, Android will provide this value as event.values[3], but if not, we have to calculate it manually.
      //SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);
      //currentOrientationQuaternion.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);
      currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix)

      var currentAngle =
        (2.0f * acos(currentOrientationQuaternion.getW().toDouble()) * 180.0f / Math.PI).toFloat()
          .toDouble()
      if (currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle
      sensorValues.postValue(currentAngle.toString() + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ())
    }
  }
}

