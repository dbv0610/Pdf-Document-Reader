// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * LOCA Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFLocaTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFLocaTable : TTFTable() {
    var offset: LongArray? = null

    override fun getTag(): String {
        return "loca"
    }

    @Throws(IOException::class)
    override fun readTable() {
        val format = (getTable("head") as TTFHeadTable).indexToLocFormat
        val numGlyphs = (getTable("maxp") as TTFMaxPTable).numGlyphs + 1
        offset = LongArray(numGlyphs)
        for (i in 0..<numGlyphs) {
            offset!![i] = (if (format.toInt() == TTFHeadTable.Companion.ITLF_LONG)
                ttf.readULong()
            else
                (
                        ttf.readUShort() * 2).toLong())
        }
    }

    override fun toString(): String {
        var str = super.toString()
        for (i in offset!!.indices) {
            if (i % 16 == 0) str += "\n  "
            str += offset!![i].toString() + " "
        }
        return str
    }
}
