package com.example.sensordatatocsv

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sensordatabase.db"
        private const val DATABASE_VERSION = 4
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE sensordata (
                timestamp REAL PRIMARY KEY, 
                gnsslatitude REAL,
                gnsslongitude REAL,
                accelerometer_x REAL,
                accelerometer_y REAL,
                accelerometer_z REAL,
                gyroscope_x REAL,
                gyroscope_y REAL,
                gyroscope_z REAL,
                gravity_x REAL,
                gravity_y REAL,
                gravity_z REAL,
                magnetometer_x REAL,
                magnetometer_y REAL,
                magnetometer_z REAL
            )
        """
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS sensordata")
        onCreate(db)
    }

    fun insertSensorData(timestamp: Double, gnssLatitude: Double, gnssLongitude: Double, accelerometer: FloatArray?, gyroscope: FloatArray?, gravity: FloatArray?, magnetometer: FloatArray?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("timestamp", timestamp)
            put("gnsslatitude", gnssLatitude)
            put("gnsslongitude", gnssLongitude)
            put("accelerometer_x", accelerometer?.getOrNull(0) ?: 0f)
            put("accelerometer_y", accelerometer?.getOrNull(1) ?: 0f)
            put("accelerometer_z", accelerometer?.getOrNull(2) ?: 0f)
            put("gyroscope_x", gyroscope?.getOrNull(0) ?: 0f)
            put("gyroscope_y", gyroscope?.getOrNull(1) ?: 0f)
            put("gyroscope_z", gyroscope?.getOrNull(2) ?: 0f)
            put("gravity_x", gravity?.getOrNull(0) ?: 0f)
            put("gravity_y", gravity?.getOrNull(1) ?: 0f)
            put("gravity_z", gravity?.getOrNull(2) ?: 0f)
            put("magnetometer_x", magnetometer?.getOrNull(0) ?: 0f)
            put("magnetometer_y", magnetometer?.getOrNull(1) ?: 0f)
            put("magnetometer_z", magnetometer?.getOrNull(2) ?: 0f)
        }
        val result = db.insert("sensordata", null, contentValues)
        return !result.equals(-1L)
    }

    fun getAllData(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM sensordata", null)
    }

}
