Dieses Repository ist Teil der Bachelorarbeit von Simon J. Heilmeier (Matr.Nr.: 567469) mit dem Titel:

# Prototypische Entwicklung einer Smartphone-Anwendung zur Verbrauchserkennung elektrifizierter Fahrzeuge

Bei der Implementierung der fogenden Android App auf Basis von Kotlin wurde ChatGPT4 [^1] verwendet um im Entwicklungsprozess zu unterstützen.

Inhaltsverzeichnis:

- [DatabaseHelper.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/DatabaseHelper.kt)  --> Verwaltung und Bereitstellung der SQLite Datenbank
- [GPSHandler.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/GPSHandler.kt)  --> Bereitstellung der GPS-Sensordaten
- [MainActivity.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/MainActivity.kt)  --> Hauptformular, Initialisierung, GUI , SFTP-Upload
- [SensorDataHistory.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/SensorDataHistory.kt)  --> Queue für die Berechnung von AVG und STD
- [SensorHandler.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/SensorHandler.kt)  --> Bereitstellung der Sensordaten von Beschleunigungssensor, Gyroskop, Magnetomerter und Schwerkraftsensor
- [SensorRepo.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/SensorRepo.kt)  --> Stellt getLatestSensorData() des sensorHandlers bereit
- [TimerViewModel.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/TimerViewModel.kt)  --> Implementierung des Timers mit Abfrage der Sensordaten, Aufruf des KNN etc. (*Funktionskern der APP*)
- [TimerViewModelFactory.kt](https://github.com/567469/logmlsensordata/blob/master/app/src/main/java/com/example/logmlsensordata/TimerViewModelFactory.kt)  --> Factory für den Timer und das SensorRepo

Zur Ausführung benötigte Files: (*Können für die Begutachtung freigegeben werden* [Simon J. Heilmeier](mailto:567469@fom-net.de?subject=[GitHub]%20Daten-Freigabe))

  - dnn_consumption.tflite
    (enthält das KNN, welches in Zukunft weiter entwickelt wird und somit nicht für die Veröffentlichung bestimmt ist)




[^1]: https://chat.openai.com/

