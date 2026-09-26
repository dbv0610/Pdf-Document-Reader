// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ScaleWindowExtEx TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ScaleWindowExtEx.java 10367 2007-01-22 19:26:48Z duns $
 */
class ScaleWindowExtEx() : EMFTag(32, 1) {
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
        return ScaleWindowExtEx(emf.readLONG(), emf.readLONG(), emf.readLONG(), emf.readLONG())
    }

    override fun toString(): String {
        return (super.toString() + "\n  xNum: " + xNum + "\n  xDenom: " + xDenom + "\n  yNum: "
                + yNum + "\n  yDenom: " + yDenom)
    }
}
