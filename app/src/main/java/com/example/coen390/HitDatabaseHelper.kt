package com.example.coen390

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HitDatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TIME REAL, $COLUMN_FORCE REAL)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(
        p0: SQLiteDatabase?,
        p1: Int,
        p2: Int
    ) {
        val dropTable = "DROP TABLE IF EXISTS $TABLE_NAME"
        p0?.execSQL(dropTable)
        onCreate(p0)
    }

    fun insertTime(time: Hit) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIME, time.time)
            put(COLUMN_FORCE, time.force)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    companion object{
        const val DATABASE_NAME = "times.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "times"
        const val COLUMN_ID = "id"
        const val COLUMN_TIME = "time"

        const val COLUMN_FORCE = "force"

    }
}