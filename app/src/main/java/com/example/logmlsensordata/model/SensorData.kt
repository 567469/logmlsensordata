package com.example.logmlsensordata.model

data class SensorData(
    val accelerometer: FloatArray?,
    val gyroscope: FloatArray?,
    val magnetometer: FloatArray?,
    val gravity: FloatArray?
)