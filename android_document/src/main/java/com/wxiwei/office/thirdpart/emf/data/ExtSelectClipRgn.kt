// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExtSelectClipRgn TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtSelectClipRgn.java 10515 2007-02-06 18:42:34Z duns $
 */
class ExtSelectClipRgn : AbstractClipPath {
    private var rgn: Region? = null

    constructor() : super(75, 1, EMFConstants.RGN_COPY)

    constructor(mode: Int, rgn: Region?) : super(75, 1, mode) {
        this.rgn = rgn
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val length = emf.readDWORD()
        val mode = emf.readDWORD()
        return ExtSelectClipRgn(mode, if (length > 8) Region(emf) else null)
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        if (rgn == null || rgn!!.bounds == null) {
            return
        }

        render(renderer, rgn!!.bounds)
    }
}
