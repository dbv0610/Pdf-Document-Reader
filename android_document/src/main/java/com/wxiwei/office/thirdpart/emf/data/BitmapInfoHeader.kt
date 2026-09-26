// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream

/**
 * EMF BitmapInfoHeader
 * 
 * @author Mark Donszelmann
 * @version $Id: BitmapInfoHeader.java 10515 2007-02-06 18:42:34Z duns $
 */
class BitmapInfoHeader : EMFConstants {
    val width: Int

    val height: Int

    private val planes: Int

    val bitCount: Int

    val compression: Int

    private val sizeImage: Int

    private val xPelsPerMeter: Int

    private val yPelsPerMeter: Int

    val clrUsed: Int

    private val clrImportant: Int

    constructor(
        width: Int, height: Int, bitCount: Int, compression: Int, sizeImage: Int,
        xPelsPerMeter: Int, yPelsPerMeter: Int, clrUsed: Int, clrImportant: Int
    ) {
        this.width = width
        this.height = height
        this.planes = 1
        this.bitCount = bitCount
        this.compression = compression
        this.sizeImage = sizeImage
        this.xPelsPerMeter = xPelsPerMeter
        this.yPelsPerMeter = yPelsPerMeter
        this.clrUsed = clrUsed
        this.clrImportant = clrImportant
    }

    constructor(emf: EMFInputStream) {
        /*int len = */
        emf.readDWORD() // seems fixed
        width = emf.readLONG()
        height = emf.readLONG()
        planes = emf.readWORD()
        bitCount = emf.readWORD()
        compression = emf.readDWORD()
        sizeImage = emf.readDWORD()
        xPelsPerMeter = emf.readLONG()
        yPelsPerMeter = emf.readLONG()
        clrUsed = emf.readDWORD()
        clrImportant = emf.readDWORD()
    }

    override fun toString(): String {
        return ("    size: " + size + "\n    width: " + width + "\n    height: " + height
                + "\n    planes: " + planes + "\n    bitCount: " + bitCount + "\n    compression: "
                + compression + "\n    sizeImage: " + sizeImage + "\n    xPelsPerMeter: "
                + xPelsPerMeter + "\n    yPelsPerMeter: " + yPelsPerMeter + "\n    clrUsed: " + clrUsed
                + "\n    clrImportant: " + clrImportant)
    }

    companion object {
        const val size: Int = 40
    }
}
