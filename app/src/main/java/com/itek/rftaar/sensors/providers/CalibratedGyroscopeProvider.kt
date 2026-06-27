package com.itek.rftaar.sensors.providers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.itek.rftaar.sensors.direction.OrientationProvider
import com.itek.rftaar.sensors.direction.representation.Quaternion
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The orientation provider that delivers the relative orientation from the [ Gyroscope][Sensor.TYPE_GYROSCOPE]. This sensor does not deliver an absolute orientation (with respect to magnetic north and gravity) but
 * only a relative measurement starting from the point where it started.
 *
 * @author Alexander Pacha
 */
class CalibratedGyroscopeProvider(sensorManager: SensorManager) :
  OrientationProvider(sensorManager) {
  /**
   * The quaternion that stores the difference that is obtained by the gyroscope.
   * Basically it contains a rotational difference encoded into a quaternion.
   *
   *
   * To obtain the absolute orientation one must add this into an initial position by
   * multiplying it with another quaternion
   */
  private val deltaQuaternion = Quaternion()

  /**
   * The time-stamp being used to record the time when the last gyroscope event occurred.
   */
  private var timestamp: Long = 0

  /**
   * Value giving the total velocity of the gyroscope (will be high, when the device is moving fast and low when
   * the device is standing still). This is usually a value between 0 and 10 for normal motion. Heavy shaking can
   * increase it to about 25. Keep in mind, that these values are time-depended, so changing the sampling rate of
   * the sensor will affect this value!
   */
  private var gyroscopeRotationVelocity = 0.0

  /**
   * Temporary variable to save allocations.
   */
  private val correctedQuaternion = Quaternion()

  //private boolean isAlternate= true;
  /**
   * Initialises a new CalibratedGyroscopeProvider
   *
   * @param sensorManager The android sensor manager
   */
  init {
    //Add the gyroscope
    sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE))
  }

  override fun onSensorChanged(event: SensorEvent) {
    // we received a sensor event. it is a good practice to check
    // that we received the proper event

    if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      // This timestamps delta rotation to be multiplied by the current rotation
      // after computing it from the gyro sample data.

      if (timestamp != 0L) {
        val dT: Float = (event.timestamp - timestamp) * NS2S
        // Axis of the rotation sample, not normalized yet.
        var axisX = event.values[0]
        var axisY = event.values[1]
        var axisZ = event.values[2]


        // Calculate the angular speed of the sample
        gyroscopeRotationVelocity = sqrt((axisX * axisX + axisY * axisY + axisZ * axisZ).toDouble())


        // Normalize the rotation vector if it's big enough to get the axis
        if (gyroscopeRotationVelocity > EPSILON) {
          axisX /= gyroscopeRotationVelocity.toFloat()
          axisY /= gyroscopeRotationVelocity.toFloat()
          axisZ /= gyroscopeRotationVelocity.toFloat()
        }


        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        val thetaOverTwo = gyroscopeRotationVelocity * dT / 2.0f
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)
        deltaQuaternion.setX((sinThetaOverTwo * axisX).toFloat())
        deltaQuaternion.setY((sinThetaOverTwo * axisY).toFloat())
        deltaQuaternion.setZ((sinThetaOverTwo * axisZ).toFloat())
        deltaQuaternion.setW(-cosThetaOverTwo.toFloat())


        // Matrix rendering in CubeRenderer does not seem to have this problem.
        synchronized(synchronizationToken) {
          // Move current gyro orientation if gyroscope should be used
          deltaQuaternion.multiplyByQuat(currentOrientationQuaternion, currentOrientationQuaternion)
        }

        correctedQuaternion.set(currentOrientationQuaternion)
        // We inverted w in the deltaQuaternion, because currentOrientationQuaternion required it.
        // Before converting it back to matrix representation, we need to revert this process
        correctedQuaternion.w(-correctedQuaternion.w())
        var currentAngle =
          (2.0f * acos(currentOrientationQuaternion.getW().toDouble()) * 180.0f / Math.PI).toFloat()
            .toDouble()
        //   if(isAlternate){
        if (currentOrientationQuaternion.getZ() < 0) currentAngle = 360 - currentAngle
        sensorValues.postValue(currentAngle.toString() + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ())


        //        LogUtils.showLog("OrignalvalC", currentAngle + "$" + currentOrientationQuaternion.getX() + "$" + currentOrientationQuaternion.getY() + "$" + currentOrientationQuaternion.getZ());
        synchronized(synchronizationToken) {
          // Set the rotation matrix as well to have both representations
          SensorManager.getRotationMatrixFromVector(
            currentOrientationRotationMatrix.matrix,
            correctedQuaternion.array()
          )
        }
      }
      timestamp = event.timestamp
    }
  }

  companion object {
    /**
     * Constant specifying the factor between a Nano-second and a second
     */
    private val NS2S = 1.0f / 1000000000.0f

    /**
     * This is a filter-threshold for discarding Gyroscope measurements that are below a certain level and
     * potentially are only noise and not real motion. Values from the gyroscope are usually between 0 (stop) and
     * 10 (rapid rotation), so 0.1 seems to be a reasonable threshold to filter noise (usually smaller than 0.1) and
     * real motion (usually > 0.1). Note that there is a chance of missing real motion, if the use is turning the
     * device really slowly, so this value has to find a balance between accepting noise (threshold = 0) and missing
     * slow user-action (threshold > 0.5). 0.1 seems to work fine for most applications.
     */
    private const val EPSILON = 0.1
  }
}