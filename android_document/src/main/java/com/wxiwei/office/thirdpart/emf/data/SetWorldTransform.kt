// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetWorldTransform TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetWorldTransform.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetWorldTransform() : EMFTag(35, 1) {
    private var transform: AffineTransform? = null

    constructor(transform: AffineTransform?) : this() {
        this.transform = transform
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetWorldTransform(emf.readXFORM())
    }

    override fun toString(): String {
        return super.toString() + "\n  transform: " + transform
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        if (renderer.getPath() != null) {
            renderer.setPathTransform(transform)
        } else {
            renderer.resetTransformation()
            renderer.transform(transform)
        }
    }
}
