package com.example.logmlsensordata

// Struktur bereitgestellt von ChatGpt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.logmlsensordata.model.SensorData

class SensorHandler(context: Context) : SensorEventListener {

    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Sensors
    private var sensorAccelerometer : Sensor? = null
    private var sensorMagneticField : Sensor? = null
    private var sensorGyroscope : Sensor? = null
    private var sensorGravity : Sensor? = null

    private var accelerometerValues:  FloatArray? = null
    private var gyroscopeValues: FloatArray? = null
    private var magnetometerValues: FloatArray? = null
    private var gravityValues: FloatArray? = null

    init {
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL)

        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)

        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // ToDo
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = event.values.clone()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetometerValues = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeValues = event.values.clone()
            }
            Sensor.TYPE_GRAVITY -> {
               gravityValues  = event.values.clone()
            }
        }
    }

    fun registerSensors() {
        // Registriere die Sensoren mit einem Listener
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    fun getSensorData(): SensorData {
        return SensorData(
            accelerometerValues,
            gyroscopeValues,
            magnetometerValues,
            gravityValues
        )
    }
}
