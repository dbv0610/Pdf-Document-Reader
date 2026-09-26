// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PolyPolyline16 TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyPolyline16.java 10510 2007-01-30 23:58:16Z duns $
 */
class PolyPolyline16 : AbstractPolyPolyline {
    private var numberOfPolys = 0

    constructor() : super(90, 1, null, null, null)

    constructor(
        bounds: Rectangle?, numberOfPolys: Int, numberOfPoints: IntArray?,
        points: Array<Array<Point?>?>?
    ) : super(90, 1, bounds, numberOfPoints, points) {
        this.numberOfPolys = numberOfPolys
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val bounds = emf.readRECTL()
        val np = emf.readDWORD()
        /* int totalNumberOfPoints = */
        emf.readDWORD()
        val pc = IntArray(np)
        val points = arrayOfNulls<Array<Point?>>(np)
        for (i in 0..<np) {
            pc[i] = emf.readDWORD()
            points[i] = arrayOfNulls<Point>(pc[i])
        }
        for (i in 0..<np) {
            points[i] = emf.readPOINTS(pc[i])
        }
        return PolyPolyline16(bounds, np, pc, points)
    }
}
