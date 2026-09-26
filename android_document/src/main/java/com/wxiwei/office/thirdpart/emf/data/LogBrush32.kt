// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import java.util.logging.Logger

/**
 * EMF LogBrush32
 * 
 * @author Mark Donszelmann
 * @version $Id: LogBrush32.java 10377 2007-01-23 15:44:34Z duns $ see
 * http://msdn.microsoft.com/library/default.asp?url=/library/en-us/gdi/brushes_8yk2.asp
 */
class LogBrush32 : EMFConstants, GDIObject {
    private val style: Int

    private val color: Color

    private val hatch: Int

    constructor(style: Int, color: Color, hatch: Int) {
        this.style = style
        this.color = color
        this.hatch = hatch
    }

    constructor(emf: EMFInputStream) {
        style = emf.readUINT()
        color = emf.readCOLORREF()
        hatch = emf.readULONG()
    }

    override fun toString(): String {
        return ("  LogBrush32\n" + "    style: " + style + "\n    color: " + color + "\n    hatch: "
                + hatch)
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        if (style == EMFConstants.BS_SOLID) {
            renderer.setBrushPaint(color)
        } else if (style == EMFConstants.BS_NULL) {
            // note: same value as BS_HOLLOW
            // Should probably do this by making a paint implementation that does nothing,
            // but a 100% transparent color works just as well for now.
            renderer.setBrushPaint(Color(0, 0, 0, 0))

            // TODO: Support pattern types
            // TODO: Support hatching
            // TODO: Support DIB types
        } else {
            val logger = Logger.getLogger("org.freehep.graphicsio.emf")
            logger.warning("LogBrush32 style not supported: " + toString())
            renderer.setBrushPaint(color)
        }
    }
}
