// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetPixelV TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetPixelV.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetPixelV() : EMFTag(15, 1) {
    private var point: Point? = null

    private var color: Color? = null

    constructor(point: Point?, color: Color?) : this() {
        this.point = point
        this.color = color
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetPixelV(emf.readPOINTL(), emf.readCOLORREF())
    }

    override fun toString(): String {
        return super.toString() + "\n  point: " + point + "\n  color: " + color
    }
}
