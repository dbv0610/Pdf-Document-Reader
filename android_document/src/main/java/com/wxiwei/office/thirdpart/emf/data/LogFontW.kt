// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import kotlin.math.abs

/**
 * EMF LogFontW
 * 
 * @author Mark Donszelmann
 * @version $Id: LogFontW.java 10367 2007-01-22 19:26:48Z duns $
 */
class LogFontW : EMFConstants, GDIObject {
    private val height: Int

    private val width: Int

    val escapement: Int

    private val orientation: Int

    private val weight: Int

    private val italic: Boolean

    private val underline: Boolean

    private val strikeout: Boolean

    private val charSet: Int

    private val outPrecision: Int

    private val clipPrecision: Int

    private val quality: Int

    private val pitchAndFamily: Int

    private val faceFamily: String?

    /**
     * cache for getFont()
     */
    private var font: Font? = null

    constructor(
        height: Int, width: Int, escapement: Int, orientation: Int, weight: Int,
        italic: Boolean, underline: Boolean, strikeout: Boolean, charSet: Int, outPrecision: Int,
        clipPrecision: Int, quality: Int, pitchAndFamily: Int, faceFamily: String?
    ) {
        this.height = height
        this.width = width
        this.escapement = escapement
        this.orientation = orientation
        this.weight = weight
        this.italic = italic
        this.underline = underline
        this.strikeout = strikeout
        this.charSet = charSet
        this.outPrecision = outPrecision
        this.clipPrecision = clipPrecision
        this.quality = quality
        this.pitchAndFamily = pitchAndFamily
        this.faceFamily = faceFamily
    }

    constructor(font: Font) {
        this.height = -font.getFontSize().toInt()
        this.width = 0
        this.escapement = 0
        this.orientation = 0
        this.weight = if (font.isBold()) EMFConstants.FW_BOLD else EMFConstants.FW_NORMAL
        this.italic = font.isItalic()
        this.underline = false
        this.strikeout = false
        this.charSet = 0 // ANSI_CHARSET;
        this.outPrecision = 0 // OUT_DEFAULT_PRECIS;
        this.clipPrecision = 0 // CLIP_DEFAULT_PRECIS;
        this.quality = 4 // ANTIALIASED_QUALITY;
        this.pitchAndFamily = 0
        this.faceFamily = font.getName()
    }

    constructor(emf: EMFInputStream) {
        height = emf.readLONG()
        width = emf.readLONG()
        escapement = emf.readLONG()
        orientation = emf.readLONG()
        weight = emf.readLONG()
        italic = emf.readBOOLEAN()
        underline = emf.readBOOLEAN()
        strikeout = emf.readBOOLEAN()
        charSet = emf.readBYTE()
        outPrecision = emf.readBYTE()
        clipPrecision = emf.readBYTE()
        quality = emf.readBYTE()
        pitchAndFamily = emf.readBYTE()
        faceFamily = emf.readWCHAR(32)
    }

    fun getFont(): Font {
        if (font == null) {
            var style = 0
            if (italic) {
                style = style or Font.ITALIC
            }

            // 400 is considered to be normal.
            if (weight > 400) {
                style = style or Font.BOLD
            }

            val size = abs(height)
            font = Font(faceFamily, style, size)
        }
        return font!!
    }

    override fun toString(): String {
        return ("  LogFontW\n" + "    height: " + height + "\n    width: " + width
                + "\n    orientation: " + orientation + "\n    weight: " + weight + "\n    italic: "
                + italic + "\n    underline: " + underline + "\n    strikeout: " + strikeout
                + "\n    charSet: " + charSet + "\n    outPrecision: " + outPrecision
                + "\n    clipPrecision: " + clipPrecision + "\n    quality: " + quality
                + "\n    pitchAndFamily: " + pitchAndFamily + "\n    faceFamily: " + faceFamily)
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // TODO: See if this ever happens.
        renderer.setFont(font)
    }
}
