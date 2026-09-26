// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer

/**
 * EMF LogPen
 * 
 * @author Mark Donszelmann
 * @version $Id: LogPen.java 10515 2007-02-06 18:42:34Z duns $
 */
class LogPen : AbstractPen {
    private val penStyle: Int

    private val width: Int

    private val color: Color

    constructor(penStyle: Int, width: Int, color: Color) {
        this.penStyle = penStyle
        this.width = width
        this.color = color
    }

    constructor(emf: EMFInputStream) {
        penStyle = emf.readDWORD()
        width = emf.readDWORD()
        /* int y = */
        emf.readDWORD()
        color = emf.readCOLORREF()
    }

    override fun toString(): String {
        return ("  LogPen\n" + "    penstyle: " + penStyle + "\n    width: " + width
                + "\n    color: " + color)
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.setUseCreatePen(true)
        renderer.setPenPaint(color)
        renderer.setPenStroke(createStroke(renderer, penStyle, null, width.toFloat()))
    }
}
