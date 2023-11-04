package com.example.logmlsensordata

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TimerViewModelFactory (private val context: Context, private val sensorRepo: SensorRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(context, sensorRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}