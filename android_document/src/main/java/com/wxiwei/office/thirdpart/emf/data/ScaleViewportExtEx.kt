// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ScaleViewportExtEx TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ScaleViewportExtEx.java 10367 2007-01-22 19:26:48Z duns $
 */
class ScaleViewportExtEx() : EMFTag(31, 1) {
    private var xNum = 0
    private var xDenom = 0
    private var yNum = 0
    private var yDenom = 0

    constructor(xNum: Int, xDenom: Int, yNum: Int, yDenom: Int) : this() {
        this.xNum = xNum
        this.xDenom = xDenom
        this.yNum = yNum
        this.yDenom = yDenom
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        /* int[] bytes = */

        emf.readUnsignedByte(len)
        return ScaleViewportExtEx(
            emf.readLONG(), emf.readLONG(), emf.readLONG(),
            emf.readLONG()
        )
    }

    override fun toString(): String {
        return (super.toString() + "\n  xNum: " + xNum + "\n  xDenom: " + xDenom + "\n  yNum: "
                + yNum + "\n  yDenom: " + yDenom)
    }
}
