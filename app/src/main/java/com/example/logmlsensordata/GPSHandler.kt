package com.example.logmlsensordata

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.logmlsensordata.model.LocationSensorData
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GPSHandler(context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationData = MutableStateFlow<LocationSensorData?>(null)
    val locationData = _locationData.asStateFlow()

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            for (location in p0.locations) {
                val locationSensorData = LocationSensorData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    speed = location.speed
                )
                _locationData.value = locationSensorData
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        locationCallback.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 200
            )
                .setMinUpdateIntervalMillis(100)
                .build()

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    fun stopLocationUpdates() {
        locationCallback.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
    }
}