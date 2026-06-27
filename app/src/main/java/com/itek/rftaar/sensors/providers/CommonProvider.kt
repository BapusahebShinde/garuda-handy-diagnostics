package com.itek.rftaar.sensors.providers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.core.database.DataStoreManager.getSensorTypeValue
import com.itek.rftaar.core.database.DataStoreManager.setSensorTypeValue
import com.itek.rftaar.sensors.direction.OrientationProvider
import com.itek.rftaar.utils.CommonUtils.chkNull
import kotlin.math.acos

/**
 * The orientation provider that delivers the current orientation from the [ Accelerometer][Sensor.TYPE_ACCELEROMETER] and [Compass][Sensor.TYPE_MAGNETIC_FIELD].
 *
 * @author Bapusaheb Shinde 17-11-2022
 */
class CommonProvider(sensorManager: SensorManager) : OrientationProvider(sensorManager) {
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
     * Initialises a new CommonProvider
     *
     * @param sensorManager The android sensor manager
     */
    init {
      registerSensor()
    }

    /**
     * registers the sensor stored in the Shared Preferences
     *
     * @param isStart
     */
    private fun registerSensor(isStart: Boolean = false) {
      sensorList.clear()
      try {
        val sensor = sensorManager.getDefaultSensor(chkNull(DataStoreManager.getSensorTypeValue(),0))
        if (sensor != null) {
          sensorList.add(sensor)
          if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD * 100 + Sensor.TYPE_GRAVITY || sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD * 100 + Sensor.TYPE_ACCELEROMETER) {
            val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            if (gravitySensor != null) sensorList.add(gravitySensor)
            val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometerSensor != null) sensorList.add(accelerometerSensor)
          }
        }
        else setNextSensor()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    override fun onSensorChanged(event: SensorEvent) {
      //LogUtils.showLog("onSensorChanged"+event.sensor.getType(),""+event.values.toString())
      if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
        if (isEmptyArray(event.values)) setNextSensor(true)
        else callRotationVector(event.values)
      }
      if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || event.sensor.getType() == Sensor.TYPE_GRAVITY || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
          var currentAngle = (2.0f * acos(
            currentOrientationQuaternion.getW().toDouble()
          ) * 180.0f / Math.PI).toFloat().toDouble()
          if (currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle
          sensorValues.postValue(currentAngle.toString() + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ())
        } else setNextSensor(true)
      }
      if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
        if (isEmptyArray(event.values)) setNextSensor(true)
        else callRotationVector(event.values)
      }
      if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
        if (isEmptyArray(event.values)) setNextSensor(true)
        else callRotationVector(event.values)
      }
      if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
        if (isEmptyArray(event.values)) setNextSensor(true)
      }
    }

    private fun setNextSensor() {
      setNextSensor(false)
    }

    /**
     * Sets the Next Sensor if the Current Sensor from Shared Preferences is unavailable or returns null/empty values
     *
     * @param isProcessing
     */
    private fun setNextSensor(isProcessing: Boolean) {
      if (isProcessing) stop()
      when (getSensorTypeValue()) {

        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
          setSensorTypeValue(Sensor.TYPE_MAGNETIC_FIELD)
          registerSensor(isProcessing)
        }

        Sensor.TYPE_MAGNETIC_FIELD -> {
          setSensorTypeValue(Sensor.TYPE_ROTATION_VECTOR)
          registerSensor(isProcessing)
        }

        Sensor.TYPE_ROTATION_VECTOR -> {
          setSensorTypeValue(Sensor.TYPE_GAME_ROTATION_VECTOR)
          registerSensor(isProcessing)
        }

        Sensor.TYPE_GAME_ROTATION_VECTOR -> {
          setSensorTypeValue(Sensor.TYPE_GYROSCOPE)
          registerSensor(isProcessing)
        }

        else -> setSensorTypeValue(0)
      }
    }

    /**
     * handles the event values for all Sensors with Rotation Vector
     *
     * @param eventValues
     */
    private fun callRotationVector(eventValues: FloatArray?) {
      // convert the rotation-vector to a 4x4 matrix. the matrix
      // is interpreted by Open GL as the inverse of the
      // rotation-vector, which is what we want.
      SensorManager.getRotationMatrixFromVector(
        currentOrientationRotationMatrix.matrix,
        eventValues
      )


      // Get Quaternion
      // Calculate angle. Starting with API_18, Android will provide this value as event.values[3], but if not, we have to calculate it manually.
      //SensorManager.getQuaternionFromVector(temporaryQuaternion, event.values);
      //currentOrientationQuaternion.setXYZW(temporaryQuaternion[1], temporaryQuaternion[2], temporaryQuaternion[3], -temporaryQuaternion[0]);
      currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix)

      var currentAngle = (2.0f * acos(currentOrientationQuaternion.getW().toDouble()) * 180.0f / Math.PI).toFloat().toDouble()
      if (currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle
      try{
      sensorValues.postValue(currentAngle.toString() + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ())
      }catch (e: Exception) {e.printStackTrace()}
    }
}