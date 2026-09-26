// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ResizePalette TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ResizePalette.java 10367 2007-01-22 19:26:48Z duns $
 */
class ResizePalette() : EMFTag(51, 1) {
    private var index = 0
    private var entries = 0

    constructor(index: Int, entries: Int) : this() {
        this.index = index
        this.entries = entries
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ResizePalette(emf.readDWORD(), emf.readDWORD())
    }

    override fun toString(): String {
        return (super.toString() + "\n  index: 0x" + Integer.toHexString(index) + "\n  entries: "
                + entries)
    }
}
