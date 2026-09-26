// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * POST Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFPostTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFPostTable : TTFTable() {
    var format: Double = 0.0

    var italicAngle: Double = 0.0

    var underlinePosition: Short = 0
    var underlineThickness: Short = 0

    var isFixedPitch: Long = 0

    var minMemType42: Long = 0
    var maxMemType42: Long = 0
    var minMemType1: Long = 0
    var maxMemType1: Long = 0

    var glyphNameIndex: IntArray? = null

    override fun getTag(): String {
        return "post"
    }

    @Throws(IOException::class)
    override fun readTable() {
        format = ttf.readFixed()

        italicAngle = ttf.readFixed()

        underlinePosition = ttf.readFWord()
        underlineThickness = ttf.readFWord()

        isFixedPitch = ttf.readULong()

        minMemType42 = ttf.readULong()
        maxMemType42 = ttf.readULong()
        minMemType1 = ttf.readULong()
        maxMemType1 = ttf.readULong()

        if (format == 2.0) {
            glyphNameIndex = ttf.readUShortArray(ttf.readUShort())
        } else if (format == 2.5) {
            System.err.println("Format 2.5 for post notimplemented yet.")
        }
    }

    override fun toString(): String {
        var str = (super.toString() + " format: " + format + "\n  italic:"
                + italicAngle + " ulPos:" + underlinePosition + " ulThick:"
                + underlineThickness + " isFixed:" + isFixedPitch)
        if (glyphNameIndex != null) {
            str += "\n  glyphNamesIndex[" + glyphNameIndex!!.size + "] = {"
            for (i in glyphNameIndex!!.indices) {
                if (i % 16 == 0) str += "\n    "
                str += glyphNameIndex!![i].toString() + " "
            }
            str += "\n  }"
        }
        return str
    }
}
