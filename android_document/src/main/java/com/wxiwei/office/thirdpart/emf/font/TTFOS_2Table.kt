// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * OS/2 Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFOS_2Table.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFOS_2Table : TTFVersionTable() {
    var version: Int = 0

    var xAvgCharWidth: Short = 0

    var usWeightClass: Int = 0
    var usWidthClass: Int = 0

    var fsType: Short = 0

    var ySubscriptXSize: Short = 0
    var ySubscriptYSize: Short = 0
    var ySubscriptXOffset: Short = 0
    var ySubscriptYOffset: Short = 0

    var ySuperscriptXSize: Short = 0
    var ySuperscriptYSize: Short = 0
    var ySuperscriptXOffset: Short = 0
    var ySuperscriptYOffset: Short = 0

    var yStrikeoutSize: Short = 0
    var yStrikeoutPosition: Short = 0

    var sFamilyClass: Short = 0

    var panose: ByteArray = ByteArray(10)

    var ulUnicode: LongArray = LongArray(4)

    var achVendID: ByteArray = ByteArray(4)

    var fsSelection: Int = 0

    var usFirstCharIndex: Int = 0
    var usLastCharIndes: Int = 0

    var sTypoAscender: Int = 0
    var sTzpoDescender: Int = 0
    var sTypoLineGap: Int = 0

    var usWinAscent: Int = 0
    var usWinDescent: Int = 0

    var ulCodePageRange: LongArray = LongArray(2)

    override fun getTag(): String {
        return "OS/2"
    }

    @Throws(IOException::class)
    override fun readTable() {
        version = ttf.readUShort()
        xAvgCharWidth = ttf.readShort()
        usWeightClass = ttf.readUShort()
        usWidthClass = ttf.readUShort()
        fsType = ttf.readShort()

        ySubscriptXSize = ttf.readShort()
        ySubscriptYSize = ttf.readShort()
        ySubscriptXOffset = ttf.readShort()
        ySubscriptYOffset = ttf.readShort()
        ySuperscriptXSize = ttf.readShort()
        ySuperscriptYSize = ttf.readShort()
        ySuperscriptXOffset = ttf.readShort()
        ySuperscriptYOffset = ttf.readShort()
        yStrikeoutSize = ttf.readShort()
        yStrikeoutPosition = ttf.readShort()

        sFamilyClass = ttf.readShort()

        ttf.readFully(panose)

        for (i in ulUnicode.indices) ulUnicode[i] = ttf.readULong()
        ttf.readFully(achVendID)
        fsSelection = ttf.readUShort()

        usFirstCharIndex = ttf.readUShort()
        usLastCharIndes = ttf.readUShort()

        sTypoAscender = ttf.readUShort()
        sTzpoDescender = ttf.readUShort()
        sTypoLineGap = ttf.readUShort()

        usWinAscent = ttf.readUShort()
        usWinDescent = ttf.readUShort()

        ulCodePageRange[0] = ttf.readULong()
        ulCodePageRange[1] = ttf.readULong()
    }

    fun getAchVendID(): String {
        return String(achVendID)
    }

    override fun toString(): String {
        return (super.toString() + "\n  version: " + version + "\n  vendor: "
                + getAchVendID())
    }
}
