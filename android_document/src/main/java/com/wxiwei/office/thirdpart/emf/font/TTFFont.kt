// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * TrueType Font with all its tables.
 * 
 * @author Simon Fischer
 * @version $Id: TTFFont.java 8584 2006-08-10 23:06:37Z duns $
 */
abstract class TTFFont {
    private val entry: MutableMap<String?, Any?> = HashMap()

    abstract fun getFontVersion(): Int

    @Throws(IOException::class)
    fun newTable(tag: String, input: TTFInput?) {
        entry.put(tag, initTable(tag, input))
    }

    @Throws(IOException::class)
    private fun initTable(name: String, input: TTFInput?): Any? {
        var table: TTFTable? = null
        for (i in TTFTable.Companion.TT_TAGS.indices) {
            if (name == TTFTable.Companion.TT_TAGS[i]) {
                try {
                    table = TTFTable.Companion.TABLE_CLASSES[i]!!.newInstance() as TTFTable
                    table.init(this, input!!)
                    return table
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }
        System.err.println("Table '" + name + "' ignored.")
        return null
    }

    open fun show() {
        println("Tables:")
        val i = entry.values.iterator()
        while (i.hasNext()) {
            println(i.next())
        }
    }

    /** Returns the table with the given tag and reads it if necessary.  */
    @Throws(IOException::class)
    fun getTable(tag: String?): TTFTable {
        val table = entry.get(tag) as TTFTable?
        if (!table!!.isRead) table.read()
        return table
    }

    /**
     * Reads all tables. This method does not need to be called since the tables
     * are read on demand (<tt>getTable()</tt>. It might be useful to call
     * it in order to print out all available information.
     */
    @Throws(IOException::class)
    fun readAll() {
        val i = entry.values.iterator()
        while (i.hasNext()) {
            val table = i.next() as TTFTable?
            if ((table != null) && (!table.isRead)) table.read()
        }
    }

    @Throws(IOException::class)
    open fun close() {
    }
}
