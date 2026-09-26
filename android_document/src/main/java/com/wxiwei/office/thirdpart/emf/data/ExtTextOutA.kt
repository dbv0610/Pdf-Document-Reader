// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExtTextOutA TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtTextOutA.java 10377 2007-01-23 15:44:34Z duns $
 */
class ExtTextOutA : AbstractExtTextOut, EMFConstants {
    private var text: TextA? = null

    constructor() : super(83, 1, null, 0, 1f, 1f)

    constructor(
        bounds: Rectangle?,
        mode: Int,
        xScale: Float,
        yScale: Float,
        text: TextA?
    ) : super(83, 1, bounds, mode, xScale, yScale) {
        this.text = text
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ExtTextOutA(
            emf.readRECTL(),
            emf.readDWORD(),
            emf.readFLOAT(),
            emf.readFLOAT(),
            TextA.Companion.read(emf)
        )
    }

    override fun getText(): Text? {
        return text
    }
}
