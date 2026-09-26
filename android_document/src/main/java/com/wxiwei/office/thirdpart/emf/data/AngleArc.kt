// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * AngleArc TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: AngleArc.java 10367 2007-01-22 19:26:48Z duns $
 */
class AngleArc() : EMFTag(41, 1) {
    private var center: Point? = null

    private var radius = 0

    private var startAngle = 0f
    private var sweepAngle = 0f

    constructor(center: Point?, radius: Int, startAngle: Float, sweepAngle: Float) : this() {
        this.center = center
        this.radius = radius
        this.startAngle = startAngle
        this.sweepAngle = sweepAngle
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return AngleArc(emf.readPOINTL(), emf.readDWORD(), emf.readFLOAT(), emf.readFLOAT())
    }

    override fun toString(): String {
        return (super.toString() + "\n  center: " + center + "\n  radius: " + radius
                + "\n  startAngle: " + startAngle + "\n  sweepAngle: " + sweepAngle)
    }
}
