package com.example.aeye.listener

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.pow
import kotlin.math.sqrt

class ShakeDetector : SensorEventListener{
    private var shakeTimeStamp : Long = 0
    private var mShakeCount : Int = 0
    private val shakeSkipTimeMs : Int = 500
    private val shakeCountingResetTermMs : Int = 3000
    private val shakeThresholdGravity: Float = 2.7F

    private var mListener : OnShakeListener? = null;

    fun setOnShakeListener(listener: OnShakeListener) {
        this.mListener = listener
    }

    interface OnShakeListener {
        fun onShake(count: Int)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //ignore
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val axisX: Float = event.values[0]
            val axisY: Float = event.values[1]
            val axisZ: Float = event.values[2]

            val gravityX = axisX / SensorManager.GRAVITY_EARTH
            val gravityY = axisY / SensorManager.GRAVITY_EARTH
            val gravityZ = axisZ / SensorManager.GRAVITY_EARTH

            val f: Float = gravityX.pow(2) + gravityY.pow(2) + gravityZ.pow(2)
            val squaredD: Double = sqrt(f.toDouble())
            val gForce = squaredD.toFloat()

            if (gForce > shakeThresholdGravity){
                val now : Long = System.currentTimeMillis()
                // ignore shake events too close to each other (500ms)
                if (shakeTimeStamp + shakeSkipTimeMs > now)
                    return
                // reset the shake count after 3 seconds of no shakes
                if (shakeTimeStamp + shakeSkipTimeMs < now )
                    mShakeCount = 0
                //Update
                shakeTimeStamp = now
                mShakeCount++
                mListener?.onShake(mShakeCount)
            }

        }
    }
}