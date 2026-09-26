// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * DeleteObject TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: DeleteObject.java 10367 2007-01-22 19:26:48Z duns $
 */
class DeleteObject() : EMFTag(40, 1) {
    private var index = 0

    constructor(index: Int) : this() {
        this.index = index
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return DeleteObject(emf.readDWORD())
    }

    override fun toString(): String {
        return super.toString() + "\n  index: 0x" + Integer.toHexString(index)
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.storeGDIObject(index, null)
    }
}
