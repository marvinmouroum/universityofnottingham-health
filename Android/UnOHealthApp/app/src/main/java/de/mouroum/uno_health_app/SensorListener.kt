package de.mouroum.uno_health_app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import android.app.Activity
import android.graphics.Color

class SensorListener:SensorEventListener {

    var reference:Float = 0.0f
    var context:Activity? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {

        var value:FloatArray = (event?.values ?: floatArrayOf(0.0f))
        reference = value[0]
        Log.d("debug",reference.toString())

        if(event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            Log.d("STEP","step counter received value")
        }

    }
}