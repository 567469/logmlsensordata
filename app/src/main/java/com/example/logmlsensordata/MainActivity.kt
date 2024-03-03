package com.example.logmlsensordata

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.logmlsensordata.ui.theme.LogmlsensordataTheme
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    private lateinit var sensorRepo: SensorRepo
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var sensorHandler: SensorHandler
    private lateinit var viewModelFactory: TimerViewModelFactory

    override fun onResume() {
        super.onResume()
        sensorHandler.registerSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.unregisterSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.unregisterSensors()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        saveSensorsToCSV(this)

        sensorHandler = SensorHandler(this)
        sensorRepo = SensorRepo(sensorHandler)
        viewModelFactory = TimerViewModelFactory(this, sensorRepo)
        timerViewModel = ViewModelProvider(this, viewModelFactory).get(TimerViewModel::class.java)

        setContent {

            LogmlsensordataTheme {
                Surface(
//                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen()
                }
            }
        }
    }

    @Composable
    fun TimerScreen(
        viewModel: TimerViewModel = viewModel(
            factory = TimerViewModelFactory(
                LocalContext.current,
                sensorRepo
            )
        )
    ) {
        val consumption by viewModel.consumptionValue.collectAsState()
        val isTimerRunning by viewModel.isTimerRunning.collectAsState()
        val context = LocalContext.current
        if (context is ComponentActivity) {
            context.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isTimerRunning) {
                            viewModel.stopTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    }, modifier = Modifier
                        .fillMaxHeight()
                        .width(150.dp)
                ) {
                    Text(if (isTimerRunning) "Stop Timer" else "Start Timer")
                }
            }

            Text(text = String.format("%.2f", consumption), fontSize = 100.sp)
            Text("kwh/100km", fontSize = 50.sp)

            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val databaseName = "sensordatabase.db"
                    copyDatabase(context, databaseName)
                    val localFilePath = File(context.getExternalFilesDir(null), databaseName).absolutePath
                    val remoteFilePath = "/${getCurrentTimestampForFilename()}/${databaseName}"

                    val uploadSuccess = uploadFileSftp("u154290-sub12.your-storagebox.de", 22, "u154290-sub12", "fqABh7tFDXP3zMEK", localFilePath, remoteFilePath)

                    withContext(Dispatchers.Main) {
                        if (uploadSuccess) {
                            Toast.makeText(context, "Upload erfolgreich!", Toast.LENGTH_LONG).show()
                            viewModel.clearDatabaseTables()
                        } else {
                            Toast.makeText(context, "Upload fehlgeschlagen.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                Text("Datenbank hochladen")
            }
        }
    }
}

fun saveSensorsToCSV(context: Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

    val csvHeader = "Name,Type,Vendor,Version,Resolution,Power,MaxRange,MinDelay\n"
    val fileName = "sensors.csv"

    val documentsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val file = File(documentsFolder, fileName)

    try {
        val fileWriter = FileWriter(file)

        fileWriter.append(csvHeader)

        for (sensor in deviceSensors) {
            val line = "${sensor.name},${sensor.type},${sensor.vendor},${sensor.version},${sensor.resolution},${sensor.power},${sensor.maximumRange},${sensor.minDelay}\n"
            fileWriter.append(line)
        }

        fileWriter.flush()
        fileWriter.close()

        Toast.makeText(context, "Gespeichert", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Speichern nicht möglich", Toast.LENGTH_SHORT).show()
    }
}

fun uploadFileSftp(host: String, port: Int, username: String, password: String, localFilePath: String, remoteFilePath: String): Boolean {
    return try {
        val jsch = JSch()
        val session: Session = jsch.getSession(username, host, port).apply {
            setPassword(password)
            setConfig("StrictHostKeyChecking", "no")
        }

        session.connect()

        val channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()

        // Extrahiere den Pfad aus dem remoteFilePath
        val remoteDirPath = remoteFilePath.substringBeforeLast('/')

        // Erstelle den Zielordner rekursiv, falls notwendig
        createRemotePath(channel, remoteDirPath)

        // Hochladen der Datei
        channel.put(localFilePath, remoteFilePath)

        channel.disconnect()
        session.disconnect()
        true // Upload erfolgreich
    } catch (e: Exception) {
        e.printStackTrace()
        false // Fehler beim Upload
    }
}

fun createRemotePath(channel: ChannelSftp, path: String) {
    var currentPath = ""
    path.split('/').forEach { folder ->
        if (folder.isNotEmpty()) {
            currentPath += "/$folder"
            try {
                channel.cd(currentPath)
            } catch (e: Exception) {
                // Ordner existiert nicht, also erstelle ihn
                try {
                    channel.mkdir(currentPath)
                    channel.cd(currentPath)
                } catch (e: Exception) {
                    // Fehler beim Erstellen des Ordners, wirf eine Ausnahme, um den Upload fehlschlagen zu lassen
                    throw RuntimeException("Fehler beim Erstellen des Ordners auf dem SFTP-Server: $currentPath")
                }
            }
        }
    }
}



fun copyDatabase(context: Context, databaseName: String) {
    val dbPath = context.getDatabasePath(databaseName).absolutePath
    val dbFile = File(dbPath)
    val outputFile = File(context.getExternalFilesDir(null), databaseName)

    dbFile.inputStream().use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

fun getCurrentTimestampForFilename(): String {
    val timestamp = System.currentTimeMillis()
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

