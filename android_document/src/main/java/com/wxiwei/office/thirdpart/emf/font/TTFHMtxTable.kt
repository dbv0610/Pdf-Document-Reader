// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * HMTX Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFHMtxTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFHMtxTable : TTFTable() {
    lateinit var advanceWidth: IntArray

    lateinit var leftSideBearing: ShortArray

    lateinit var leftSideBearing2: ShortArray

    override fun getTag(): String {
        return "hmtx"
    }

    @Throws(IOException::class)
    override fun readTable() {
        val numberOfHMetrics = (getTable("hhea") as TTFHHeaTable).numberOfHMetrics
        val numGlyphs = (getTable("maxp") as TTFMaxPTable).numGlyphs

        advanceWidth = IntArray(numberOfHMetrics)
        leftSideBearing = ShortArray(numberOfHMetrics)
        for (i in 0..<numberOfHMetrics) {
            advanceWidth[i] = ttf.readUFWord()
            leftSideBearing[i] = ttf.readFWord()
        }

        leftSideBearing2 = ttf.readShortArray(numGlyphs - numberOfHMetrics)
    }

    override fun toString(): String {
        var str = super.toString()
        str += "\n  hMetrics[" + advanceWidth.size + "] = {"
        for (i in advanceWidth.indices) {
            if (i % 8 == 0) str += "\n    "
            str += "(" + advanceWidth[i] + "," + leftSideBearing[i] + ") "
        }
        str += "\n  }"
        str += "\n  lsb[" + leftSideBearing2.size + "] = {"
        for (i in leftSideBearing2.indices) {
            if (i % 16 == 0) str += "\n    "
            str += leftSideBearing2[i].toString() + " "
        }
        str += "\n  }"
        return str
    }
}
