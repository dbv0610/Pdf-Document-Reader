// Copyright 2002-2003, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream

/**
 * EMF BitmapInfoHeader
 * 
 * @author Mark Donszelmann
 * @version $Id: BlendFunction.java 10363 2007-01-20 15:30:50Z duns $
 */
class BlendFunction : EMFConstants {
    private val blendOp: Int

    private val blendFlags: Int

    val sourceConstantAlpha: Int

    val alphaFormat: Int

    constructor(blendOp: Int, blendFlags: Int, sourceConstantAlpha: Int, alphaFormat: Int) {
        this.blendOp = blendOp
        this.blendFlags = blendFlags
        this.sourceConstantAlpha = sourceConstantAlpha
        this.alphaFormat = alphaFormat
    }

    constructor(emf: EMFInputStream) {
        blendOp = emf.readUnsignedByte()
        blendFlags = emf.readUnsignedByte()
        sourceConstantAlpha = emf.readUnsignedByte()
        alphaFormat = emf.readUnsignedByte()
    }

    override fun toString(): String {
        return "BlendFunction"
    }

    companion object {
        const val size: Int = 4
    }
}
