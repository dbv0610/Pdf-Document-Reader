// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetMiterLimit TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetMiterLimit.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetMiterLimit() : EMFTag(58, 1) {
    private var limit = 0

    constructor(limit: Int) : this() {
        this.limit = limit
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetMiterLimit(emf.readDWORD())
    }

    override fun toString(): String {
        return super.toString() + "\n  limit: " + limit
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The SetMiterLimit function sets the limit for the length of miter
        // joins for the specified device context.
        // The miter length is defined as the distance from the intersection
        // of the line walls on the inside of the join to the intersection of
        // the line walls on the outside of the join. The miter limit is the
        // maximum allowed ratio of the miter length to the line width.

        // The default miter limit is 10.0.

        renderer.setMeterLimit(limit)
    }
}
