// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SelectObject TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SelectObject.java 10515 2007-02-06 18:42:34Z duns $
 */
class SelectObject() : EMFTag(37, 1) {
    private var index = 0

    constructor(index: Int) : this() {
        this.index = index
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SelectObject(emf.readDWORD())
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
        val gdiObject: GDIObject?

        if (index < 0) {
            gdiObject = StockObjects.getStockObject(index)
        } else {
            gdiObject = renderer.getGDIObject(index)
        }

        if (gdiObject != null) {
            // render that object
            gdiObject.render(renderer)
        } else {
        }
    }
}
