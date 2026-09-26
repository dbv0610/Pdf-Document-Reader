// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PolyDraw16 TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyDraw16.java 10367 2007-01-22 19:26:48Z duns $
 */
class PolyDraw16() : EMFTag(92, 1), EMFConstants {
    private var bounds: Rectangle? = null

    private lateinit var points: Array<Point?>

    private var types: ByteArray? = null

    constructor(bounds: Rectangle?, points: Array<Point?>, types: ByteArray?) : this() {
        this.bounds = bounds
        this.points = points
        this.types = types
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val n: Int
        return PolyDraw16(
            emf.readRECTL(),
            emf.readPOINTS(emf.readDWORD().also { n = it }),
            emf.readBYTE(n)
        )
    }

    override fun toString(): String {
        return super.toString() + "\n  bounds: " + bounds + "\n  #points: " + points.size
    }
}
