package com.example.logmlsensordata

import android.content.Context
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.mutableFloatStateOf
import com.example.sensordatatocsv.DatabaseHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logmlsensordata.ml.DnnConsumption
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TimerViewModel(private val context: Context, private val sensorRepository: SensorRepo) : ViewModel() {
    private val databaseHelper = DatabaseHelper(context)
    private val _isTimerRunning = MutableStateFlow(false)
    private val _consumptionValue = MutableStateFlow(0.0f)
    private val sensorDataHistoryList = List(13) { SensorDataHistory() }
    private lateinit var model : DnnConsumption
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()
    val consumptionValue: StateFlow<Float> = _consumptionValue.asStateFlow()

    private var job: Job? = null

    private val gpsHandler = GPSHandler(context)
    fun startTimer() {
        gpsHandler.startLocationUpdates()
        model = DnnConsumption.newInstance(context)

        job = viewModelScope.launch {
            _isTimerRunning.value = true
            var count = 0

            while (isActive) {
                count++
                val sensorData = sensorRepository.getLatestSensorData()
                val locationSensorData = gpsHandler.locationData.value

                val latValue = locationSensorData?.latitude ?: 0.0
                val longValue = locationSensorData?.longitude ?: 0.0
                val speedValue = locationSensorData?.speed ?: 0f

                val accelerometerValues = sensorData.accelerometer
                val gyroscopeValues = sensorData.gyroscope
                val magnetometerValues = sensorData.magnetometer
                val gravityValues = sensorData.gravity

                val currentTimeStamp = ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSxxx"))

                val dnnInputSensorData = FloatArray(39) { 0.0f }

                // insert values
                dnnInputSensorData[0] = speedValue                                    // speed_phone
                dnnInputSensorData[1] = accelerometerValues?.getOrNull(0) ?: 0f // accelerometer_x
                dnnInputSensorData[2] = accelerometerValues?.getOrNull(1) ?: 0f // accelerometer_y
                dnnInputSensorData[3] = accelerometerValues?.getOrNull(2) ?: 0f // accelerometer_z
                dnnInputSensorData[4] = gyroscopeValues?.getOrNull(0) ?: 0f     // gyroscope_x
                dnnInputSensorData[5] = gyroscopeValues?.getOrNull(1) ?: 0f     // gyroscope_y
                dnnInputSensorData[6] = gyroscopeValues?.getOrNull(2) ?: 0f     // gyroscope_z
                dnnInputSensorData[7] = gravityValues?.getOrNull(0) ?: 0f       // gravity_x
                dnnInputSensorData[8] = gravityValues?.getOrNull(1) ?: 0f       // gravity_y
                dnnInputSensorData[9] = gravityValues?.getOrNull(2) ?: 0f       // gravity_z
                dnnInputSensorData[10] = magnetometerValues?.getOrNull(0) ?: 0f // magnetometer_x
                dnnInputSensorData[11] = magnetometerValues?.getOrNull(1) ?: 0f // magnetometer_y
                dnnInputSensorData[12] = magnetometerValues?.getOrNull(2) ?: 0f // magnetometer_z

                // insert values to sensorDataHistoryList
                sensorDataHistoryList[0].add(dnnInputSensorData[0])
                sensorDataHistoryList[1].add(dnnInputSensorData[1])
                sensorDataHistoryList[2].add(dnnInputSensorData[2])
                sensorDataHistoryList[3].add(dnnInputSensorData[3])
                sensorDataHistoryList[4].add(dnnInputSensorData[4])
                sensorDataHistoryList[5].add(dnnInputSensorData[5])
                sensorDataHistoryList[6].add(dnnInputSensorData[6])
                sensorDataHistoryList[7].add(dnnInputSensorData[7])
                sensorDataHistoryList[8].add(dnnInputSensorData[8])
                sensorDataHistoryList[9].add(dnnInputSensorData[9])
                sensorDataHistoryList[10].add(dnnInputSensorData[10])
                sensorDataHistoryList[11].add(dnnInputSensorData[11])
                sensorDataHistoryList[12].add(dnnInputSensorData[12])

                //calculate and insert average
                dnnInputSensorData[13] = sensorDataHistoryList[0].average   // speed_phone_avg
                dnnInputSensorData[14] = sensorDataHistoryList[1].average   // accelerometer_x_avg
                dnnInputSensorData[15] = sensorDataHistoryList[2].average   // accelerometer_y_avg
                dnnInputSensorData[16] = sensorDataHistoryList[3].average   // accelerometer_z_avg
                dnnInputSensorData[17] = sensorDataHistoryList[4].average   // gyroscope_x_avg
                dnnInputSensorData[18] = sensorDataHistoryList[5].average   // gyroscope_y_avg
                dnnInputSensorData[19] = sensorDataHistoryList[6].average   // gyroscope_z_avg
                dnnInputSensorData[20] = sensorDataHistoryList[7].average   // gravity_x_avg
                dnnInputSensorData[21] = sensorDataHistoryList[8].average   // gravity_y_avg
                dnnInputSensorData[22] = sensorDataHistoryList[9].average   // gravity_z_avg
                dnnInputSensorData[23] = sensorDataHistoryList[10].average  // magnetometer_x_avg
                dnnInputSensorData[24] = sensorDataHistoryList[11].average  // magnetometer_y_avg
                dnnInputSensorData[25] = sensorDataHistoryList[12].average  // magnetometer_z_avg

                //calculate and insert standard deviation
                dnnInputSensorData[26] = sensorDataHistoryList[0].standardDeviation     // speed_phone_std
                dnnInputSensorData[27] = sensorDataHistoryList[1].standardDeviation     // accelerometer_x_std
                dnnInputSensorData[28] = sensorDataHistoryList[2].standardDeviation     // accelerometer_y_std
                dnnInputSensorData[29] = sensorDataHistoryList[3].standardDeviation     // accelerometer_z_std
                dnnInputSensorData[30] = sensorDataHistoryList[4].standardDeviation     // gyroscope_x_std
                dnnInputSensorData[31] = sensorDataHistoryList[5].standardDeviation     // gyroscope_y_std
                dnnInputSensorData[32] = sensorDataHistoryList[6].standardDeviation     // gyroscope_z_std
                dnnInputSensorData[33] = sensorDataHistoryList[7].standardDeviation     // gravity_x_std
                dnnInputSensorData[34] = sensorDataHistoryList[8].standardDeviation     // gravity_y_std
                dnnInputSensorData[35] = sensorDataHistoryList[9].standardDeviation     // gravity_z_std
                dnnInputSensorData[36] = sensorDataHistoryList[10].standardDeviation    // magnetometer_x_std
                dnnInputSensorData[37] = sensorDataHistoryList[11].standardDeviation    // magnetometer_y_std
                dnnInputSensorData[38] = sensorDataHistoryList[12].standardDeviation    // magnetometer_z_std

                if (count % 3 == 0) {

                    databaseHelper.insertHistoryData(dnnInputSensorData)

                    if (dnnInputSensorData.all { !it.isNaN() }) {
                        _consumptionValue.value = Predict(dnnInputSensorData)
                    }

                    databaseHelper.insertSensorData(
                        timestamp = currentTimeStamp,
                        gnssLatitude = latValue,
                        gnssLongitude = longValue,
                        speed = speedValue,
                        accelerometer = accelerometerValues,
                        gyroscope = gyroscopeValues,
                        gravity = gravityValues,
                        magnetometer = magnetometerValues,
                        consumption_est = _consumptionValue.value
                    )
                    count = 0
                }

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
        gpsHandler.stopLocationUpdates()
        job?.cancel()
        model.close()
    }

    private fun getFloatTimestamp(): Double {
        val timestamp = Instant.now().toEpochMilli()
        val seconds = timestamp / 1000
        val milliseconds = timestamp % 1000
        return "$seconds.$milliseconds".toDouble()
    }

    private fun Predict(uebergabe: FloatArray): Float {
    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 39), DataType.FLOAT32)
    inputFeature0.loadArray(uebergabe.map { it }.toFloatArray())

    val outputs = model.process(inputFeature0)
    val outputFeature0 = outputs.outputFeature0AsTensorBuffer
    val pred = outputFeature0.floatArray
    return pred[0]
    }

    fun clearDatabaseTables() {
        databaseHelper.deleteFromAllTables()
    }

}