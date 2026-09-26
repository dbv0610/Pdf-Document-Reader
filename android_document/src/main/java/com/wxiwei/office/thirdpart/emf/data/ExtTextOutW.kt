// Copyright 2002-2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExtTextOutW TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtTextOutW.java 10525 2007-02-12 04:08:30Z duns $
 */
class ExtTextOutW : AbstractExtTextOut, EMFConstants {
    private var text: TextW? = null

    constructor() : super(84, 1, null, 0, 1f, 1f)

    constructor(
        bounds: Rectangle?,
        mode: Int,
        xScale: Float,
        yScale: Float,
        text: TextW?
    ) : super(84, 1, bounds, mode, xScale, yScale) {
        this.text = text
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ExtTextOutW(
            emf.readRECTL(),
            emf.readDWORD(),
            emf.readFLOAT(),
            emf.readFLOAT(),
            TextW.Companion.read(emf)
        )
    }

    override fun getText(): Text? {
        return text
    }
}
