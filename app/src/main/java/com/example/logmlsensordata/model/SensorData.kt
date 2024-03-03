package com.example.logmlsensordata.model

data class SensorData(
    val accelerometer: FloatArray?,
    val gyroscope: FloatArray?,
    val magnetometer: FloatArray?,
    val gravity: FloatArray?
)

data class LocationSensorData(
    val latitude: Double,
    val longitude: Double,
    val speed: Float
)