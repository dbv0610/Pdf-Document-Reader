// Copyright 2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.awt.geom.Area
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag


/**
 * base class for all tags that change the
 * clipping area of the [EMFRenderer]
 * 
 * @author Steffen Greiffenberg
 * @version $Id$
 */
abstract class AbstractClipPath protected constructor(id: Int, version: Int, val mode: Int) :
    EMFTag(id, version) {
    override fun toString(): String {
        return super.toString() + "\n  mode: " + mode
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     * @param shape shape to use as clipping area
     */
    fun render(renderer: EMFRenderer, shape: Shape?) {
        if (shape != null) {
            // The new clipping region includes the intersection
            // (overlapping areas) of the current clipping region and the current shape.
            if (mode == EMFConstants.RGN_AND) {
                renderer.clip(shape)
            } else if (mode == EMFConstants.RGN_COPY) {
                // rest the clip ...
//                AffineTransform at = renderer.getTransform();
                val matrix = renderer.getMatrix()
                // temporarly switch to the base transformation to
                // aplly the base clipping area
                renderer.resetTransformation()
                // set the clip
                renderer.setClip(renderer.getInitialClip())
                //                renderer.setTransform(at);
                renderer.setMatrix(matrix)
                renderer.clip(shape)
            } else if (mode == EMFConstants.RGN_DIFF) {
                val clip = renderer.getClip()
                if (clip != null) {
                    val a = Area(shape)
                    a.subtract(Area(clip))
                    renderer.setClip(a)
                } else {
                    renderer.setClip(shape)
                }
            } else if (mode == EMFConstants.RGN_OR) {
                val path = GeneralPath(shape)
                val clip = renderer.getClip()
                if (clip != null) {
                    path.append(clip, false)
                }
                renderer.setClip(path)
            } else if (mode == EMFConstants.RGN_XOR) {
                val clip = renderer.getClip()
                if (clip != null) {
                    val a = Area(shape)
                    a.exclusiveOr(Area(clip))
                    renderer.setClip(a)
                } else {
                    renderer.setClip(shape)
                }
            }
        }

        // delete the current shape
        renderer.setPath(null)
    }
}
