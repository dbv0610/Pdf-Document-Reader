package com.wxiwei.office.officereader.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.wxiwei.office.constant.MainConstant

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + MainConstant.TABLE_RECENT + " ('name' VARCHAR(30))")
        db.execSQL("CREATE TABLE IF NOT EXISTS " + MainConstant.TABLE_STAR + " ('name' VARCHAR(30))")
        db.execSQL("CREATE TABLE IF NOT EXISTS " + MainConstant.TABLE_SETTING + " ('count' VARCHAR(30))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + MainConstant.TABLE_RECENT)
        db.execSQL("DROP TABLE IF EXISTS " + MainConstant.TABLE_STAR)
        db.execSQL("DROP TABLE IF EXISTS " + MainConstant.TABLE_SETTING)
        onCreate(db)
    }

    fun dispose() {
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DB_NAME = "wxiweiReader.db"
    }
}
