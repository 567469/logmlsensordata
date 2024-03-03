package com.example.logmlsensordata

import java.util.LinkedList
import kotlin.math.sqrt

const val SIZE_HISTORY_QUEUE = 15
class SensorDataHistory {
    private val queue = LinkedList<Float>()
    private var sum = 0.0
    private var sumOfSquares = 0.0

    fun add(value: Float) {
        if (queue.size == SIZE_HISTORY_QUEUE) {
            val removed = queue.remove()
            sum -= removed
            sumOfSquares -= removed * removed
        }
        queue.add(value)
        sum += value
        sumOfSquares += value * value
    }
    val average: Float
        get() = if (queue.isNullOrEmpty()) 0.0f else (sum / queue.size).toFloat()

    val standardDeviation: Float
        get() {
            if (queue.isNullOrEmpty()) return 0.0f
            val mean = average.toDouble()
            return sqrt((sumOfSquares) / queue.size - mean * mean).toFloat()
        }

}