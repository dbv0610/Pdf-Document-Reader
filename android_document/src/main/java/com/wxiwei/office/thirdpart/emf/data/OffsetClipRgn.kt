// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * OffsetClipRgn TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: OffsetClipRgn.java 10367 2007-01-22 19:26:48Z duns $
 */
class OffsetClipRgn() : EMFTag(26, 1) {
    private var offset: Point? = null

    constructor(offset: Point?) : this() {
        this.offset = offset
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return OffsetClipRgn(emf.readPOINTL())
    }

    override fun toString(): String {
        return super.toString() + "\n  offset: " + offset
    }
}
