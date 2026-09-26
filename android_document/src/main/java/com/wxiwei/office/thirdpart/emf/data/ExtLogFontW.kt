// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer

/**
 * EMF ExtLogFontW
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtLogFontW.java 10367 2007-01-22 19:26:48Z duns $
 */
class ExtLogFontW : EMFConstants, GDIObject {
    private val font: LogFontW

    private val fullName: String?

    private val style: String?

    private val version: Int

    private val styleSize: Int

    private val match: Int

    private val vendorID: ByteArray?

    private val culture: Int

    private val panose: Panose

    constructor(
        font: LogFontW, fullName: String?, style: String?, version: Int, styleSize: Int,
        match: Int, vendorID: ByteArray?, culture: Int, panose: Panose
    ) {
        this.font = font
        this.fullName = fullName
        this.style = style
        this.version = version
        this.styleSize = styleSize
        this.match = match
        this.vendorID = vendorID
        this.culture = culture
        this.panose = panose
    }

    constructor(font: Font) {
        this.font = LogFontW(font)
        this.fullName = ""
        this.style = ""
        this.version = 0
        this.styleSize = 0
        this.match = 0
        this.vendorID = byteArrayOf(0, 0, 0, 0)
        this.culture = 0
        this.panose = Panose()
    }

    constructor(emf: EMFInputStream) {
        font = LogFontW(emf)
        fullName = emf.readWCHAR(64)
        style = emf.readWCHAR(32)
        version = emf.readDWORD()
        styleSize = emf.readDWORD()
        match = emf.readDWORD()
        emf.readDWORD()
        vendorID = emf.readBYTE(4)
        culture = emf.readDWORD()
        panose = Panose(emf)
        emf.readWORD() // Pad to 4-byte boundary
        // to avoid an eception
        emf.popBuffer()
    }

    override fun toString(): String {
        return (super.toString() + "\n  LogFontW\n" + font.toString() + "\n    fullname: "
                + fullName + "\n    style: " + style + "\n    version: " + version
                + "\n    stylesize: " + styleSize + "\n    match: " + match + "\n    vendorID: "
                + vendorID + "\n    culture: " + culture + "\n" + panose.toString())
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.setFont(font.getFont())
        renderer.setEscapement(font.escapement)
    }
}
