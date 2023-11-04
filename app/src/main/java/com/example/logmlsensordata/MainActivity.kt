package com.example.logmlsensordata

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.logmlsensordata.ui.theme.LogmlsensordataTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng



class MainActivity : ComponentActivity() {

    private val permission = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequired: Boolean = false
    private lateinit var sensorHandler: SensorHandler
    private lateinit var  sensorRepo: SensorRepo
    private lateinit var  viewModelFactory: TimerViewModelFactory
    private lateinit var  viewModel: TimerViewModel

    override fun onResume() {
        super.onResume()
        if (locationRequired) {
            startLocationUpdates()
        }
        sensorHandler.registerSensors()
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        sensorHandler.unregisterSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregisterSensors()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationCallback?.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 200
            )
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(200)
                .build()

            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorHandler = SensorHandler(this)
        sensorRepo = SensorRepo(sensorHandler)
        viewModelFactory = TimerViewModelFactory(this, sensorRepo)
        viewModel = ViewModelProvider(this, viewModelFactory).get(TimerViewModel::class.java)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {

            var currentLocation by remember {
                mutableStateOf(LatLng(0.toDouble(), 0.toDouble()))
            }

            locationCallback = object: LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for (location in p0.locations) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            }

            LogmlsensordataTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen()
                    //LocationScreen(this@MainActivity, currentLocation)
                }
            }
        }
    }

    @Composable
    fun TimerScreen(viewModel: TimerViewModel = viewModel(factory = TimerViewModelFactory(LocalContext.current, sensorRepo))) {
        val isTimerRunning by viewModel.isTimerRunning.collectAsState()

        Button(
            onClick = {
                if (isTimerRunning) {
                    viewModel.stopTimer()
                } else {
                    viewModel.startTimer()
                }
            }
        ) {
            Text(if (isTimerRunning) "Stop Timer" else "Start Timer")
        }
    }

    @Composable
    private fun LocationScreen(context: Context, currentLocation: LatLng) {
        
        var launchMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            permissionMaps ->
            val areGranted = permissionMaps.values.reduce() {acc, next -> acc && next}
            if (areGranted) {
                locationRequired = true
                startLocationUpdates()
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()) {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Aktuelle Position: ${currentLocation.latitude}/${currentLocation.longitude}")
                Button(onClick = {

//                    databaseHelper.insertSensorData(getFloatTimestamp() ,currentLocation.latitude, currentLocation.longitude, accelerometerValues, gyroscopeValues, magnetometerValues, gravityValues)


                    //saveSensorsToCSV(context)
//                    if (permission.all {
//                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
//                        }) {
//                        startLocationUpdates()
//                    }
//                    else {
//                        launchMultiplePermissions.launch(permission)
//                    }

                }) {
                    Text(text = "Get your Location")
                }
            }
        }
    }

}

//fun saveSensorsToCSV(context: Context) {
//    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
//
//    val csvHeader = "Name,Type,Vendor,Version,Resolution,Power,MaxRange,MinDelay\n"
//    val fileName = "sensors.csv"
//
//    // Öffnen Sie einen File Descriptor für den Dokumenteordner
//    val documentsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//    val file = File(documentsFolder, fileName)
//
//    try {
//        val fileWriter = FileWriter(file)
//
//        fileWriter.append(csvHeader)
//
//        for (sensor in deviceSensors) {
//            val line = "${sensor.name},${sensor.type},${sensor.vendor},${sensor.version},${sensor.resolution},${sensor.power},${sensor.maximumRange},${sensor.minDelay}\n"
//            fileWriter.append(line)
//        }
//
//        fileWriter.flush()
//        fileWriter.close()
//
//        Toast.makeText(context, "Gespeichert", Toast.LENGTH_SHORT).show()
//    } catch (e: IOException) {
//        e.printStackTrace()
//        Toast.makeText(context, "Speichern nicht möglich", Toast.LENGTH_SHORT).show()
//    }
//}

