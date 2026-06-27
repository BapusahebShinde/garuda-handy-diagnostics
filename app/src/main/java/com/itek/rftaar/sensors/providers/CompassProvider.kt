package com.itek.rftaar.sensors.providers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.itek.rftaar.sensors.direction.OrientationProvider
import kotlin.math.acos

/**
 * The orientation provider that delivers the current orientation from the [ Accelerometer][Sensor.TYPE_ACCELEROMETER] and [Compass][Sensor.TYPE_MAGNETIC_FIELD].
 *
 * @author Bapusaheb Shinde 17-11-2022
 */
class CompassProvider(sensorManager: SensorManager) : OrientationProvider(sensorManager) {
  /**
   * Inclination values
   */
  val inclinationValues: FloatArray = FloatArray(16)

  /**
   * Compass values
   */
  private val magnitudeValues = FloatArray(3)

  /**
   * Accelerometer values
   */
  private val accelerometerValues = FloatArray(3)
  private val gravityValues = FloatArray(3)


  private val temporaryQuaternion = FloatArray(4)

  /**
   * Initialises a new AccelerometerAndCompassProvider
   *
   * @param sensorManager The android sensor manager
   */
  init {
    //Add the compass and the accelerometer
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY))
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
  }

  override fun onSensorChanged(event: SensorEvent) {
    // we received a sensor event. it is a good practice to check
    // that we received the proper event

    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      System.arraycopy(event.values, 0, magnitudeValues, 0, magnitudeValues.size)
    } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.size)
    } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
      System.arraycopy(event.values, 0, gravityValues, 0, accelerometerValues.size)
    }

    if (!isEmptyArray(magnitudeValues) && (!isEmptyArray(accelerometerValues) || !isEmptyArray(
        gravityValues
      ))
    ) {
      // Fuse accelerometer with compass
      SensorManager.getRotationMatrix(
        currentOrientationRotationMatrix.matrix,
        null,
        if (!isEmptyArray(gravityValues)) gravityValues else accelerometerValues,
        magnitudeValues
      )
      // Transform rotation matrix to quaternion
      currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix)


      //SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);
      //currentOrientationQuaternion.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);
      var currentAngle =
        (2.0f * acos(currentOrientationQuaternion.getW().toDouble()) * 180.0f / Math.PI).toFloat()
          .toDouble()
      if (currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle

      sensorValues.postValue(currentAngle.toString() + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ())
    } else {
      //handle different sensor call
    }
  }
}
