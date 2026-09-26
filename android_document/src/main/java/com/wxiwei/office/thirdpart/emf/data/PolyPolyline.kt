// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException
import kotlin.math.min

/**
 * PolyPolyline TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyPolyline.java 10510 2007-01-30 23:58:16Z duns $
 */
class PolyPolyline : AbstractPolyPolyline {
    private var start = 0
    private var end = 0

    constructor() : super(7, 1, null, null, null)

    constructor(
        bounds: Rectangle?,
        start: Int,
        end: Int,
        numberOfPoints: IntArray,
        points: Array<Array<Point?>?>?
    ) : super(7, 1, bounds, numberOfPoints, points) {
        this.start = start
        this.end = min(end, numberOfPoints.size - 1)
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
            points[i] = emf.readPOINTL(pc[i])
        }
        return PolyPolyline(bounds, 0, np - 1, pc, points)
    }
}
