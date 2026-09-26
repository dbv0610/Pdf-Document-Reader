package com.wxiwei.office.officereader.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.wxiwei.office.constant.MainConstant
import java.io.File

class DBService(context: Context) {
    private var dbHelper: DBHelper? = null

    init {
        createDataBase(context)
    }

    fun createDataBase(context: Context) {
        if (dbHelper == null) {
            dbHelper = DBHelper(context)
            val db = dbHelper!!.writableDatabase
            db.execSQL("CREATE TABLE IF NOT EXISTS " + MainConstant.TABLE_RECENT + " ('name' VARCHAR(30))")
            db.execSQL("CREATE TABLE IF NOT EXISTS " + MainConstant.TABLE_STAR + " ('name' VARCHAR(30))")
            db.execSQL("CREATE TABLE IF NOT EXISTS " + MainConstant.TABLE_SETTING + " ('count' VARCHAR(30))")
        }
    }

    fun insertRecentFiles(tableName: String, name: String) {
        if (queryItem(tableName, name)) deleteItem(tableName, name)
        val db = dbHelper!!.writableDatabase
        val cursor = db.rawQuery("select * from $tableName", null)
        deleteItems(tableName, cursor.count - getRecentMax() + 1)
        cursor.close()
        db.execSQL("INSERT INTO $tableName (name) values(?)", arrayOf<Any>(name))
    }

    fun insertStarFiles(tableName: String, name: String) {
        if (queryItem(tableName, name)) return
        dbHelper!!.writableDatabase.execSQL("INSERT INTO $tableName (name) values(?)", arrayOf<Any>(name))
    }

    fun queryItem(tableName: String, name: String): Boolean {
        val cursor = dbHelper!!.writableDatabase.rawQuery("select * from $tableName where name like ?", arrayOf(name))
        val result = cursor.moveToFirst()
        cursor.close()
        return result
    }

    fun deleteItem(tableName: String, name: String) {
        dbHelper!!.writableDatabase.execSQL("delete from $tableName where name=?", arrayOf<Any>(name))
    }

    fun deleteItems(tableName: String, count: Int) {
        val cursor = dbHelper!!.writableDatabase.rawQuery("select * from $tableName", null)
        cursor.moveToFirst()
        var remaining = count
        while (remaining > 0) {
            deleteItem(tableName, cursor.getString(0))
            cursor.moveToNext()
            remaining--
        }
        cursor.close()
    }

    fun get(tableName: String, fileList: MutableList<File>) {
        val cursor = dbHelper!!.writableDatabase.rawQuery("select * from $tableName", null)
        while (cursor.moveToNext()) {
            val file = File(cursor.getString(0))
            if (file.exists()) fileList.add(file) else deleteItem(tableName, file.absolutePath)
        }
        cursor.close()
    }

    fun getCount(tableName: String): Int {
        val cursor = dbHelper!!.writableDatabase.rawQuery("select * from $tableName", null)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun getRecentMax(): Int {
        var max = 10
        val db = dbHelper!!.writableDatabase
        val cursor = db.rawQuery("select * from " + MainConstant.TABLE_SETTING, null)
        if (cursor.moveToFirst()) max = cursor.getInt(0)
        else db.execSQL("INSERT INTO " + MainConstant.TABLE_SETTING + " (count) values(?)", arrayOf<Any>(10))
        cursor.close()
        return max
    }

    fun changeRecentCount(value: Int) {
        val db = dbHelper!!.writableDatabase
        val cursor = db.rawQuery("select * from " + MainConstant.TABLE_SETTING, null)
        if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            if (count != value) {
                db.execSQL("update " + MainConstant.TABLE_SETTING + " set count = " + value + " where count = " + count)
                val has = getCount(MainConstant.TABLE_RECENT)
                if (has > value) deleteItems(MainConstant.TABLE_RECENT, has - value)
            }
        } else db.execSQL("INSERT INTO " + MainConstant.TABLE_SETTING + " (count) values(?)", arrayOf<Any>(10))
        cursor.close()
    }

    fun dropTable(tableName: String) { dbHelper!!.writableDatabase.execSQL("DROP TABLE IF EXISTS $tableName") }

    fun closeDatabase() { dbHelper!!.writableDatabase.close() }

    fun dispose() {
        closeDatabase()
        dbHelper!!.dispose()
        dbHelper = null
    }
}
