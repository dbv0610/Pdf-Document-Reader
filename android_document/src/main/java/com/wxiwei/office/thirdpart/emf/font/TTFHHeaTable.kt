package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * HHEA Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFHHeaTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFHHeaTable : TTFVersionTable() {
    var ascender: Short = 0
    var descender: Short = 0
    var lineGap: Short = 0

    var advanceWidthMax: Int = 0

    var minLeftSideBearing: Short = 0
    var minRightSideBearing: Short = 0

    var xMaxExtent: Short = 0

    var caretSlopeRise: Short = 0
    var caretSlopeRun: Short = 0

    var metricDataFormat: Short = 0

    var numberOfHMetrics: Int = 0

    override fun getTag(): String {
        return "hhea"
    }

    @Throws(IOException::class)
    override fun readTable() {
        readVersion()

        ascender = ttf.readFWord()
        descender = ttf.readFWord()
        lineGap = ttf.readFWord()

        advanceWidthMax = ttf.readUFWord()
        minLeftSideBearing = ttf.readFWord()
        minRightSideBearing = ttf.readFWord()

        xMaxExtent = ttf.readFWord()

        caretSlopeRise = ttf.readShort()
        caretSlopeRun = ttf.readShort()

        for (i in 0..4) ttf.checkShortZero()

        metricDataFormat = ttf.readShort()
        numberOfHMetrics = ttf.readUShort()
    }

    override fun toString(): String {
        var str = super.toString()
        str += ("\n  asc:" + ascender + " desc:" + descender + " lineGap:"
                + lineGap + " maxAdvance:" + advanceWidthMax)
        str += ("\n  metricDataFormat:" + metricDataFormat + " #HMetrics:"
                + numberOfHMetrics)
        return str
    }
}
