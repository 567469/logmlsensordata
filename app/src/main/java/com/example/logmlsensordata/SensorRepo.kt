package com.example.logmlsensordata

import com.example.logmlsensordata.model.SensorData

class SensorRepo(private val sensorHandler: SensorHandler) {
    fun getLatestSensorData(): SensorData {
        return sensorHandler.getSensorData()
    }
}