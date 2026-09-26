// Copyright 2002-2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Arc2D
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * Chord TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: Chord.java 10377 2007-01-23 15:44:34Z duns $
 */
class Chord : AbstractArc {
    private val bounds: Rectangle? = null

    private val start: Point? = null
    private val end: Point? = null

    constructor() : super(46, 1, null, null, null)

    constructor(bounds: Rectangle?, start: Point?, end: Point?) : super(46, 1, bounds, start, end)

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return Chord(
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
            getShape(renderer, Arc2D.CHORD)
        )
    }
}
