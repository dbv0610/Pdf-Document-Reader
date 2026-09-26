// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExcludeClipRect TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExcludeClipRect.java 10367 2007-01-22 19:26:48Z duns $
 */
class ExcludeClipRect() : EMFTag(29, 1) {
    private var bounds: Rectangle? = null

    constructor(bounds: Rectangle?) : this() {
        this.bounds = bounds
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ExcludeClipRect(emf.readRECTL())
    }

    override fun toString(): String {
        return super.toString() + "\n  bounds: " + bounds
    }
}
