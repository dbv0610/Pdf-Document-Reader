// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ModifyWorldTransform TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ModifyWorldTransform.java 10377 2007-01-23 15:44:34Z duns $
 */
class ModifyWorldTransform() : EMFTag(36, 1), EMFConstants {
    private var transform: AffineTransform? = null

    private var mode = 0

    constructor(transform: AffineTransform, mode: Int) : this() {
        this.transform = transform
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ModifyWorldTransform(emf.readXFORM(), emf.readDWORD())
    }

    override fun toString(): String {
        return super.toString() + "\n  transform: " + transform + "\n  mode: " + mode
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // MWT_IDENTITY 	Resets the current world transformation by using
        // the identity matrix. If this mode is specified, the XFORM structure
        // pointed to by lpXform is ignored.
        if (mode == EMFConstants.MWT_IDENTITY) {
            if (renderer.getPath() != null) {
                renderer.setPathTransform(AffineTransform())
            } else {
                renderer.resetTransformation()
            }
        } else if (mode == EMFConstants.MWT_LEFTMULTIPLY) {
            if (renderer.getPath() != null) {
                renderer.getPathTransform().concatenate(transform!!)
                renderer.transform(transform)
            } else {
                renderer.transform(transform)
            }
        } else if (mode != EMFConstants.MWT_RIGHTMULTIPLY) {
            // TODO expected that this should work but it doesn't
            // doing nothing renders the right emf embedding

            /* if (renderer.getPath() != null) {
                AffineTransform transform = new AffineTransform(this.transform);
                transform.concatenate(renderer.getPathTransform());
                renderer.setPathTransform(transform);
            } else {
                AffineTransform transform = new AffineTransform(this.transform);
                transform.concatenate(renderer.getTransform());
                renderer.resetTransformation();
                renderer.transform(transform);
            }*/
        } else {
        }
    }
}
