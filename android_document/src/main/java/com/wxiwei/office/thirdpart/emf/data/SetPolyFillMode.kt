// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.geom.Path2D
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetPolyFillMode TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetPolyFillMode.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetPolyFillMode() : EMFTag(19, 1), EMFConstants {
    private var mode = 0

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetPolyFillMode(emf.readDWORD())
    }


    override fun toString(): String {
        return super.toString() + "\n  mode: " + mode
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.setWindingRule(getWindingRule(mode))
    }

    /**
     * gets a winding rule for GeneralPath creation based on
     * EMF SetPolyFillMode.
     * 
     * @param polyFillMode PolyFillMode to convert
     * @return winding rule
     */
    private fun getWindingRule(polyFillMode: Int): Int {
        if (polyFillMode == EMFConstants.WINDING) {
            return Path2D.WIND_EVEN_ODD
        } else if (polyFillMode == EMFConstants.ALTERNATE) {
            return Path2D.WIND_NON_ZERO
        } else {
            return Path2D.WIND_EVEN_ODD
        }
    }
}
