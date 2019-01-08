package com.hamza.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    lateinit var sensorManager: SensorManager
    lateinit var magnetometer: Sensor
    lateinit var accelerometer: Sensor
    lateinit var arrow: ImageView
    var gravityValues = FloatArray(3)
    var accelerationValues = FloatArray(3)
    var rotationMatrix = FloatArray(9)
    var lastDirectionInDegrees = 0f

    val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            calculateCompassDirection(event)
        }

        // Does nothing
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arrow = findViewById(R.id.arrow)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }

    private fun calculateCompassDirection(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerationValues = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> gravityValues = event.values.clone()
        }

        val success = SensorManager.getRotationMatrix(
                rotationMatrix, null, accelerationValues, gravityValues)

        if (success) {
            val orientationValues = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            val azimuth = Math.toDegrees(-orientationValues[0].toDouble()).toFloat()
            val rotateAnimation = RotateAnimation(
                    lastDirectionInDegrees, azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotateAnimation.duration = 50
            rotateAnimation.fillAfter = true
            arrow.startAnimation(rotateAnimation)
            lastDirectionInDegrees = azimuth
        }
    }
}
