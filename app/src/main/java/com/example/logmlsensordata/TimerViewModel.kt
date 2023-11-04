package com.example.logmlsensordata

import android.content.Context
import com.example.sensordatatocsv.DatabaseHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logmlsensordata.model.SensorData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class TimerViewModel(context: Context, private val sensorRepository: SensorRepo) : ViewModel() {
    private val databaseHelper = DatabaseHelper(context)
    private val _isTimerRunning = MutableStateFlow(false)

    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var job: Job? = null

    fun startTimer() {
        job = viewModelScope.launch {
            _isTimerRunning.value = true
            while (isActive) {

                val sensorData = sensorRepository.getLatestSensorData()

                val accelerometerValues = sensorData.accelerometer
                val gyroscopeValues = sensorData.gyroscope
                val magnetometerValues = sensorData.magnetometer
                val gravityValues = sensorData.gravity

                databaseHelper.insertSensorData(getFloatTimestamp() ,0.0, 0.0, accelerometerValues, gyroscopeValues, magnetometerValues, gravityValues)

                delay(200)
            }
        }
    }

    fun stopTimer() {
        job?.cancel()
        _isTimerRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    private fun getFloatTimestamp(): Double {
        val timestamp = Instant.now().toEpochMilli()
        val seconds = timestamp / 1000
        val milliseconds = timestamp % 1000
        return "$seconds.$milliseconds".toDouble()
    }
}