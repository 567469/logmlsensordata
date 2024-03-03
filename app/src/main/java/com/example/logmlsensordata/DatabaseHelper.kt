package com.example.sensordatatocsv

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sensordatabase.db"
        private const val DATABASE_VERSION = 5
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE sensordata (
                timestamp TIMESTAMP PRIMARY KEY, 
                gnsslatitude REAL,
                gnsslongitude REAL,
                speed REAL, 
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
                magnetometer_z REAL,
                consumption_est REAL
            )
        """
        db?.execSQL(createTable)

        val createTable1 = """
            CREATE TABLE sensordatahistory (
                speed REAL, 
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
                magnetometer_z REAL,
                speed_avg REAL, 
                accelerometer_x_avg REAL,
                accelerometer_y_avg REAL,
                accelerometer_z_avg REAL,
                gyroscope_x_avg REAL,
                gyroscope_y_avg REAL,
                gyroscope_z_avg REAL,
                gravity_x_avg REAL,
                gravity_y_avg REAL,
                gravity_z_avg REAL,                
                magnetometer_x_avg REAL,
                magnetometer_y_avg REAL,
                magnetometer_z_avg REAL,
                speed_std REAL, 
                accelerometer_x_std REAL,
                accelerometer_y_std REAL,
                accelerometer_z_std REAL,
                gyroscope_x_std REAL,
                gyroscope_y_std REAL,
                gyroscope_z_std REAL,
                gravity_x_std REAL,
                gravity_y_std REAL,
                gravity_z_std REAL,
                magnetometer_x_std REAL,
                magnetometer_y_std REAL,
                magnetometer_z_std REAL
            )
        """
        db?.execSQL(createTable1)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS sensordata")
        db?.execSQL("DROP TABLE IF EXISTS sensordatahistory")
        onCreate(db)
    }

    fun insertSensorData(timestamp: String, gnssLatitude: Double, gnssLongitude: Double, speed: Float,accelerometer: FloatArray?, gyroscope: FloatArray?, gravity: FloatArray?, magnetometer: FloatArray?, consumption_est: Float): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("timestamp", timestamp)
            put("gnsslatitude", gnssLatitude)
            put("gnsslongitude", gnssLongitude)
            put("speed", speed)
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
            put("consumption_est", consumption_est)
        }
        val result = db.insert("sensordata", null, contentValues)
        return !result.equals(-1L)
    }

    fun insertHistoryData(historyArray: FloatArray): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            // org
            put("speed", historyArray[0])
            put("accelerometer_x", historyArray[1])
            put("accelerometer_y", historyArray[2])
            put("accelerometer_z", historyArray[3])
            put("gyroscope_x", historyArray[4])
            put("gyroscope_y", historyArray[5])
            put("gyroscope_z", historyArray[6])
            put("gravity_x", historyArray[7])
            put("gravity_y", historyArray[8])
            put("gravity_z", historyArray[9])
            put("magnetometer_x", historyArray[10])
            put("magnetometer_y", historyArray[11])
            put("magnetometer_z", historyArray[12])
            // avg
            put("speed_avg", historyArray[13])
            put("accelerometer_x_avg", historyArray[14])
            put("accelerometer_y_avg", historyArray[15])
            put("accelerometer_z_avg", historyArray[16])
            put("gyroscope_x_avg", historyArray[17])
            put("gyroscope_y_avg", historyArray[18])
            put("gyroscope_z_avg", historyArray[19])
            put("gravity_x_avg", historyArray[20])
            put("gravity_y_avg", historyArray[21])
            put("gravity_z_avg", historyArray[22])
            put("magnetometer_x_avg", historyArray[23])
            put("magnetometer_y_avg", historyArray[24])
            put("magnetometer_z_avg", historyArray[25])
            // std
            put("speed_std", historyArray[26])
            put("accelerometer_x_std", historyArray[27])
            put("accelerometer_y_std", historyArray[28])
            put("accelerometer_z_std", historyArray[29])
            put("gyroscope_x_std", historyArray[30])
            put("gyroscope_y_std", historyArray[31])
            put("gyroscope_z_std", historyArray[32])
            put("gravity_x_std", historyArray[33])
            put("gravity_y_std", historyArray[34])
            put("gravity_z_std", historyArray[35])
            put("magnetometer_x_std", historyArray[36])
            put("magnetometer_y_std", historyArray[37])
            put("magnetometer_z_std", historyArray[38])
        }
        val result = db.insert("sensordatahistory", null, contentValues)
        return !result.equals(-1L)
    }

    fun getAllData(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM sensordata", null)
    }

    fun deleteFromAllTables() {
        val db = this.readableDatabase
        db.execSQL("DELETE FROM sensordata")
        db.execSQL("DELETE FROM sensordatahistory")
        db.close()
    }


}
