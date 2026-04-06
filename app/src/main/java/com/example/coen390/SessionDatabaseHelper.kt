package com.example.coen390

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SessionDatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COLUMN_DATE TEXT,$COLUMN_MIN_TIME TEXT, $COLUMN_MAX_TIME TEXT, $COLUMN_MEAN_TIME TEXT, $COLUMN_MIN_FORCE TEXT, $COLUMN_MAX_FORCE TEXT, $COLUMN_MEAN_FORCE TEXT, $COLUMN_MODE TEXT, $COLUMN_TOTAL_HITS INTEGER)"
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

    fun insertSession(session: Session) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MIN_TIME, session.min_hit.time)
            put(COLUMN_MAX_TIME, session.max_hit.time)
            put(COLUMN_MEAN_TIME, session.mean_hit.time)
            put(COLUMN_MIN_FORCE, session.min_hit.force)
            put(COLUMN_MAX_FORCE, session.max_hit.force)
            put(COLUMN_MEAN_FORCE, session.mean_hit.force)
            put(COLUMN_MODE, session.mode)
            put(COLUMN_DATE, session.date)
            put(COLUMN_TOTAL_HITS, session.total_hits)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    companion object{
        const val DATABASE_NAME = "sessions.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "Sessions"
        const val COLUMN_MIN_TIME = "Min_Hit_Time"

        const val COLUMN_MAX_TIME = "Max_Hit_Time"

        const val COLUMN_MEAN_TIME = "Mean_Hit_Time"

        const val COLUMN_MIN_FORCE = "Min_Hit_Force"

        const val COLUMN_MAX_FORCE = "Max_Hit_Force"

        const val COLUMN_MEAN_FORCE = "Mean_Hit_Force"

        const val COLUMN_MODE = "Mode"

        const val COLUMN_DATE = "Date"

        const val COLUMN_TOTAL_HITS = "Total_Hits"
    }
}