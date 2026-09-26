// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * MAXP Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFMaxPTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFMaxPTable : TTFVersionTable() {
    var numGlyphs: Int = 0

    var maxPoints: Int = 0
    var maxContours: Int = 0

    var maxCompositePoints: Int = 0
    var maxCompositeContours: Int = 0

    var maxZones: Int = 0

    var maxTwilightPoints: Int = 0

    var maxStorage: Int = 0

    var maxFunctionDefs: Int = 0

    var maxInstructionDefs: Int = 0

    var maxStackElements: Int = 0

    var maxSizeOfInstructions: Int = 0

    var maxComponentElements: Int = 0

    var maxComponentDepth: Int = 0

    override fun getTag(): String {
        return "maxp"
    }

    @Throws(IOException::class)
    override fun readTable() {
        readVersion()

        numGlyphs = ttf.readUShort()

        maxPoints = ttf.readUShort()
        maxContours = ttf.readUShort()
        maxCompositePoints = ttf.readUShort()
        maxCompositeContours = ttf.readUShort()
        maxZones = ttf.readUShort()
        maxTwilightPoints = ttf.readUShort()
        maxStorage = ttf.readUShort()
        maxFunctionDefs = ttf.readUShort()
        maxInstructionDefs = ttf.readUShort()
        maxStackElements = ttf.readUShort()
        maxSizeOfInstructions = ttf.readUShort()
        maxComponentElements = ttf.readUShort()
        maxComponentDepth = ttf.readUShort()
    }

    override fun toString(): String {
        return super.toString() + "\n" + "  numGlyphs: " + numGlyphs
    }
}
