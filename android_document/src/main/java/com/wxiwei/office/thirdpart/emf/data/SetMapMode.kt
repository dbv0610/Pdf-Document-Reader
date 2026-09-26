// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.geom.AffineTransform.Companion.getScaleInstance
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetMapMode TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetMapMode.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetMapMode() : EMFTag(17, 1), EMFConstants {
    private var mode = 0

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetMapMode(emf.readDWORD())
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
        // MM_ANISOTROPIC 	Logical units are mapped to arbitrary
        // units with arbitrarily scaled axes. Use the SetWindowExtEx
        // and SetViewportExtEx functions to specify the units,
        // orientation, and scaling.
        if (mode == EMFConstants.MM_ANISOTROPIC) {
            renderer.setMapModeIsotropic(false)
        } else if (mode == EMFConstants.MM_HIENGLISH) {
            // TODO not sure
            val scale = 0.001 * 25.4
            renderer.setMapModeTransform(getScaleInstance(scale, scale))
        } else if (mode == EMFConstants.MM_HIMETRIC) {
            // TODO not sure
            val scale = 0.01
            renderer.setMapModeTransform(getScaleInstance(scale, scale))
        } else if (mode == EMFConstants.MM_ISOTROPIC) {
            renderer.setMapModeIsotropic(true)
            renderer.fixViewportSize()
        } else if (mode == EMFConstants.MM_LOENGLISH) {
            // TODO not sure
            val scale = 0.01 * 25.4
            renderer.setMapModeTransform(getScaleInstance(scale, scale))
        } else if (mode == EMFConstants.MM_LOMETRIC) {
            // TODO not sure
            val scale = 0.1
            renderer.setMapModeTransform(getScaleInstance(scale, scale))
        } else if (mode == EMFConstants.MM_TEXT) {
            renderer.setMapModeTransform(getScaleInstance(1.0, -1.0))
        } else if (mode == EMFConstants.MM_TWIPS) {
            renderer.setMapModeTransform(
                getScaleInstance(
                    EMFRenderer.TWIP_SCALE,
                    EMFRenderer.TWIP_SCALE
                )
            )
        }
    }
}
