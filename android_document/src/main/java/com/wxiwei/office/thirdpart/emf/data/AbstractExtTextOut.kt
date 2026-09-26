// Copyright 2007, FreeHEP
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag

/**
 * Abstraction of commonality between the [ExtTextOutA] and [ExtTextOutW] tags.
 * 
 * @author Daniel Noll (daniel@nuix.com)
 * @version $Id: ExtTextOutW.java 10140 2006-12-07 07:50:41Z duns $
 */
abstract class AbstractExtTextOut
/**
 * Constructs the tag.
 * 
 * @param id id of the element
 * @param version emf version in which this element was first supported
 * @param bounds text boundary
 * @param mode text mode
 * @param xScale horizontal scale factor
 * @param yScale vertical scale factor
 */ protected constructor(
    id: Int,
    version: Int,
    private val bounds: Rectangle?,
    private val mode: Int,
    private val xScale: Float,
    private val yScale: Float
) : EMFTag(id, version), EMFConstants {
    abstract fun getText(): Text?

    override fun toString(): String {
        return (super.toString() + "\n  bounds: " + bounds + "\n  mode: " + mode + "\n  xScale: "
                + xScale + "\n  yScale: " + yScale + "\n" + getText().toString())
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        val text = getText()!!
        renderer.drawOrAppendText(
            text.string,
            text.pos!!.x.toFloat(),
            text.pos!!.y.toFloat()
        )
    }
}
