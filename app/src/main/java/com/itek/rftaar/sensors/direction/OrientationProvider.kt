package com.itek.rftaar.sensors.direction

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.MutableLiveData
import com.itek.rftaar.sensors.direction.representation.MatrixF4x4
import com.itek.rftaar.sensors.direction.representation.Quaternion

/**
 * Classes implementing this interface provide an orientation of the device
 * either by directly accessing hardware, using Android sensor fusion or fusing
 * sensors itself.
 *
 *
 * The orientation can be provided as rotation matrix or quaternion.
 *
 * @author Bapusaheb Shinde
 */
abstract class OrientationProvider(protected var sensorManager: SensorManager) : SensorEventListener {
  /**
   * Sync-token for syncing read/write to sensor-data from sensor manager and
   * fusion algorithm
   */
  @JvmField
  protected val synchronizationToken: Any = Any()

  /**
   * The matrix that holds the current rotation
   */
  @JvmField
  protected val currentOrientationRotationMatrix: MatrixF4x4

  /**
   * The quaternion that holds the current rotation
   */
  @JvmField
  protected val currentOrientationQuaternion: Quaternion
  var sensorValues: MutableLiveData<String> = MutableLiveData<String>("0$0$0$0")

  /**
   * The list of sensors used by this provider
   */
  @JvmField
  protected var sensorList: MutableList<Sensor?> = ArrayList<Sensor?>()

  /**
   * Initialises a new OrientationProvider
   *
   * @param sensorManager The android sensor manager
   */
  init {
    // Initialise with identity
    currentOrientationRotationMatrix = MatrixF4x4()


    // Initialise with identity
    currentOrientationQuaternion = Quaternion()
  }

  /**
   * Starts the sensor fusion (e.g. when resuming the activity)
   */
  fun start() {
    // enable our sensor when the activity is resumed, ask for
    // 10 ms updates.
    for (sensor in sensorList) {
      // enable our sensors when the activity is resumed, ask for
      // 20 ms updates (Sensor_delay_game)
      sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }
  }

  /**
   * Stops the sensor fusion (e.g. when pausing/suspending the activity)
   */
  fun stop() {
    // make sure to turn our sensors off when the activity is paused
    for (sensor in sensorList) {
      sensorManager.unregisterListener(this, sensor)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // Not doing anything
  }

  /**
   * Get the current rotation of the device in the rotation matrix format (4x4 matrix)
   */
  fun getRotationMatrix(matrix: MatrixF4x4) {
    synchronized(synchronizationToken) {
      matrix.set(currentOrientationRotationMatrix)
    }
  }

  /**
   * Get the current rotation of the device in the quaternion format (vector4f)
   */
  fun getQuaternion(quaternion: Quaternion) {
    synchronized(synchronizationToken) {
      quaternion.set(currentOrientationQuaternion)
    }
  }

  /**
   * Get the current rotation of the device in the Euler angles
   */
  fun getEulerAngles(angles: FloatArray?) {
    synchronized(synchronizationToken) {
      SensorManager.getOrientation(currentOrientationRotationMatrix.matrix, angles)
    }
  }

  fun getSensorData(): MutableLiveData<String> {
    return sensorValues
  }

  protected fun isEmptyArray(arr: FloatArray?): Boolean {
    if (arr == null) return true
    var isEmpty = true
    for (f in arr) {
      if (f != 0.0f) {
        isEmpty = false
        break
      }
    }
    return isEmpty
  }
}
