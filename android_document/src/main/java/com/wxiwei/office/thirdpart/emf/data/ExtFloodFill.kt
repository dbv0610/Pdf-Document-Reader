// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExtFloodFill TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtFloodFill.java 10367 2007-01-22 19:26:48Z duns $
 */
class ExtFloodFill() : EMFTag(53, 1), EMFConstants {
    private var start: Point? = null

    private var color: Color? = null

    private var mode = 0

    constructor(start: Point?, color: Color?, mode: Int) : this() {
        this.start = start
        this.color = color
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ExtFloodFill(emf.readPOINTL(), emf.readCOLORREF(), emf.readDWORD())
    }

    override fun toString(): String {
        return (super.toString() + "\n  start: " + start + "\n  color: " + color + "\n  mode: "
                + mode)
    }
}
