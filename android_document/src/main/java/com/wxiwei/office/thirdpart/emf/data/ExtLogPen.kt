// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer

/**
 * EMF ExtLogPen
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtLogPen.java 10515 2007-02-06 18:42:34Z duns $
 */
class ExtLogPen : AbstractPen {
    private val penStyle: Int

    private val width: Int

    private val brushStyle: Int

    private val color: Color

    private val hatch: Int

    private val style: IntArray

    constructor(
        penStyle: Int,
        width: Int,
        brushStyle: Int,
        color: Color,
        hatch: Int,
        style: IntArray
    ) {
        this.penStyle = penStyle
        this.width = width
        this.brushStyle = brushStyle
        this.color = color
        this.hatch = hatch
        this.style = style
    }

    constructor(emf: EMFInputStream, len: Int) {
        penStyle = emf.readDWORD()
        width = emf.readDWORD()
        brushStyle = emf.readUINT()
        color = emf.readCOLORREF()
        hatch = emf.readULONG()
        val nStyle = emf.readDWORD()
        // it seems we always have to read one!
        if (nStyle == 0 && len > 44) emf.readDWORD()
        style = emf.readDWORD(nStyle)
    }

    override fun toString(): String {
        val s = StringBuffer()
        s.append("  ExtLogPen\n")
        s.append("    penStyle: ")
        s.append(Integer.toHexString(penStyle))
        s.append("\n")
        s.append("    width: ")
        s.append(width)
        s.append("\n")
        s.append("    brushStyle: ")
        s.append(brushStyle)
        s.append("\n")
        s.append("    color: ")
        s.append(color)
        s.append("\n")
        s.append("    hatch: ")
        s.append(hatch)
        s.append("\n")
        for (i in style.indices) {
            s.append("      style[")
            s.append(i)
            s.append("]: ")
            s.append(style[i])
            s.append("\n")
        }
        return s.toString()
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.setUseCreatePen(false)
        renderer.setPenPaint(color)
        renderer.setPenStroke(createStroke(renderer, penStyle, style, width.toFloat()))
    }
}
