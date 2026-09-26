// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Arc2D
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * Pie TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: Pie.java 10377 2007-01-23 15:44:34Z duns $
 */
class Pie : AbstractArc {
    constructor() : super(47, 1, null, null, null)

    constructor(bounds: Rectangle?, start: Point?, end: Point?) : super(47, 1, bounds, start, end)

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return Pie(
            emf.readRECTL(),
            emf.readPOINTL(),
            emf.readPOINTL()
        )
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.fillAndDrawOrAppend(
            getShape(renderer, Arc2D.PIE)
        )
    }
}
