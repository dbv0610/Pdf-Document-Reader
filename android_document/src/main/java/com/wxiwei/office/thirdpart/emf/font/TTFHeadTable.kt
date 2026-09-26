// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import com.wxiwei.office.java.awt.Rectangle
import java.io.IOException

/**
 * HEAD Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFHeadTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFHeadTable : TTFVersionTable() {
    var fontRevisionMinor: Int = 0
    var fontRevisionMajor: Int = 0

    var checkSumAdjustment: Long = 0

    var magicNumber: Long = 0

    var baseline0: Boolean = false
    var sidebearing0: Boolean = false
    var instrDependOnSize: Boolean = false
    var forcePPEM2Int: Boolean = false
    var instrAlterAdvance: Boolean = false

    var unitsPerEm: Int = 0

    var created: ByteArray = ByteArray(8)

    var modified: ByteArray = ByteArray(8)

    var xMin: Short = 0
    var yMin: Short = 0
    var xMax: Short = 0
    var yMax: Short = 0

    var macBold: Boolean = false
    var macItalic: Boolean = false

    var lowestRecPPEM: Int = 0

    var fontDirectionHint: Short = 0

    var indexToLocFormat: Short = 0
    var glyphDataFormat: Short = 0

    override fun getTag(): String {
        return "head"
    }

    @Throws(IOException::class)
    override fun readTable() {
        readVersion()

        fontRevisionMajor = ttf.readUShort()
        fontRevisionMinor = ttf.readUShort()

        checkSumAdjustment = ttf.readULong()
        magicNumber = ttf.readULong()

        ttf.readUShortFlags() // flags
        baseline0 = ttf.flagBit(0)
        sidebearing0 = ttf.flagBit(1)
        instrDependOnSize = ttf.flagBit(2)
        forcePPEM2Int = ttf.flagBit(3)
        instrAlterAdvance = ttf.flagBit(4)

        unitsPerEm = ttf.readUShort()

        ttf.readFully(created)
        ttf.readFully(modified)

        xMin = ttf.readShort()
        yMin = ttf.readShort()
        xMax = ttf.readShort()
        yMax = ttf.readShort()

        ttf.readUShortFlags() // macstyle
        macBold = ttf.flagBit(0)
        macItalic = ttf.flagBit(1)

        lowestRecPPEM = ttf.readUShort()
        fontDirectionHint = ttf.readShort()
        indexToLocFormat = ttf.readShort()
        if ((indexToLocFormat.toInt() != ITLF_LONG) && (indexToLocFormat.toInt() != ITLF_SHORT)) System.err.println(
            "Unknown value for indexToLocFormat: "
                    + indexToLocFormat
        )
        glyphDataFormat = ttf.readShort()
    }

    override fun toString(): String {
        var str = (super.toString() + "\n" + "  magicNumber: 0x"
                + Integer.toHexString(magicNumber.toInt()) + " ("
                + (if (magicNumber == 0x5f0f3cf5L) "ok" else "wrong") + ")\n")
        str += "  indexToLocFormat: " + indexToLocFormat + " "
        if (indexToLocFormat.toInt() == ITLF_LONG) str += " (long)\n"
        else if (indexToLocFormat.toInt() == ITLF_SHORT) str += "(short)\n"
        else str += "(illegal value)\n"
        str += ("  bbox: (" + xMin + "," + yMin + ") : (" + xMax + "," + yMax
                + ")")
        return str
    }

    val maxCharBounds: Rectangle
        get() = Rectangle(
            xMin.toInt(),
            yMin.toInt(),
            xMax - xMin,
            yMax - yMin
        )

    companion object {
        const val FDH_MIXED: Int = 0

        const val FDH_LEFT_TO_RIGHT: Int = 1

        const val FDH_LEFT_TO_RIGHT_NEUTRAL: Int = 2

        val FDH_RIGHT_TO_LEFT: Int = -1

        val FDH_RIGHT_TO_LEFT_NEUTRAL: Int = -2

        const val ITLF_SHORT: Int = 0

        const val ITLF_LONG: Int = 1
    }
}
