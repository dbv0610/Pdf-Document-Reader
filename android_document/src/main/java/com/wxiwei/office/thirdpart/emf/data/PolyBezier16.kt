// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PolyBezier16 TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyBezier16.java 10367 2007-01-22 19:26:48Z duns $
 */
class PolyBezier16 : PolyBezier {
    constructor() : super(85, 1, null, 0, null)

    constructor(bounds: Rectangle?, numberOfPoints: Int, points: Array<Point?>?) : super(
        85,
        1,
        bounds,
        numberOfPoints,
        points
    )

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val r = emf.readRECTL()
        val n = emf.readDWORD()
        return PolyBezier16(r, n, emf.readPOINTS(n))
    }
}
