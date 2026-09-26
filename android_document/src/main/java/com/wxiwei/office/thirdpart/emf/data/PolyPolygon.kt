// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PolyPolygon TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyPolygon.java 10377 2007-01-23 15:44:34Z duns $
 */
class PolyPolygon : AbstractPolyPolygon {
    private var start = 0
    private var end = 0

    constructor() : super(8, 1, null, null, null)

    constructor(
        bounds: Rectangle?,
        start: Int,
        end: Int,
        numberOfPoints: IntArray?,
        points: Array<Array<Point?>?>?
    ) : super(8, 1, bounds, numberOfPoints, points) {
        this.start = start
        this.end = end
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
        return PolyPolygon(bounds, 0, np - 1, pc, points)
    }
}
