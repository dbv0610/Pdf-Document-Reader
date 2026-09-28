/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * A ColorSchemeAtom (type 2032). Holds the 8 RGB values for the different
 * colours of bits of text, that makes up a given colour scheme.
 * Slides (presumably) link to a given colour scheme atom, and that
 * defines the colours to be used
 * 
 * @author Nick Burch
 */
class ColorSchemeAtom : RecordAtom {
    private var _header: ByteArray?
    /** Fetch the RGB value for Background Colour  */
    /** Set the RGB value for Background Colour  */
    var backgroundColourRGB: Int
    /** Fetch the RGB value for Text And Lines Colour  */
    /** Set the RGB value for Text And Lines Colour  */
    var textAndLinesColourRGB: Int
    /** Fetch the RGB value for Shadows Colour  */
    /** Set the RGB value for Shadows Colour  */
    var shadowsColourRGB: Int
    /** Fetch the RGB value for Title Text Colour  */
    /** Set the RGB value for Title Text Colour  */
    var titleTextColourRGB: Int
    /** Fetch the RGB value for Fills Colour  */
    /** Set the RGB value for Fills Colour  */
    var fillsColourRGB: Int
    /** Fetch the RGB value for Accent Colour  */
    /** Set the RGB value for Accent Colour  */
    var accentColourRGB: Int
    /** Fetch the RGB value for Accent And Hyperlink Colour  */
    /** Set the RGB value for Accent And Hyperlink Colour  */
    var accentAndHyperlinkColourRGB: Int
    /** Fetch the RGB value for Accent And Following Hyperlink Colour  */
    /** Set the RGB value for Accent And Following Hyperlink Colour  */
    var accentAndFollowingHyperlinkColourRGB: Int

    /* *************** record code follows ********************** */
    /**
     * For the Colour Scheme (ColorSchem) Atom
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Sanity Checking - we're always 40 bytes long
        var len = len
        if (len < 40) {
            len = 40
            if (source.size - start < 40) {
                throw RuntimeException(
                    "Not enough data to form a ColorSchemeAtom (always 40 bytes long) - found "
                            + (source.size - start)
                )
            }
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the rgb values
        backgroundColourRGB = getInt(source, start + 8 + 0)
        textAndLinesColourRGB = getInt(source, start + 8 + 4)
        shadowsColourRGB = getInt(source, start + 8 + 8)
        titleTextColourRGB = getInt(source, start + 8 + 12)
        fillsColourRGB = getInt(source, start + 8 + 16)
        accentColourRGB = getInt(source, start + 8 + 20)
        accentAndHyperlinkColourRGB = getInt(source, start + 8 + 24)
        accentAndFollowingHyperlinkColourRGB = getInt(source, start + 8 + 28)
    }

    /**
     * Create a new ColorSchemeAtom, to go with a new Slide
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 16)
        LittleEndian.putUShort(_header!!, 2, _type.toInt())
        LittleEndian.putInt(_header!!, 4, 32)

        // Setup the default rgb values
        backgroundColourRGB = 16777215
        textAndLinesColourRGB = 0
        shadowsColourRGB = 8421504
        titleTextColourRGB = 0
        fillsColourRGB = 10079232
        accentColourRGB = 13382451
        accentAndHyperlinkColourRGB = 16764108
        accentAndFollowingHyperlinkColourRGB = 11711154
    }

    /**
     * We are of type 3999
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        // Header - size or type unchanged
        out.write(_header)

        // Write out the rgb values
        writeLittleEndian(backgroundColourRGB, out)
        writeLittleEndian(textAndLinesColourRGB, out)
        writeLittleEndian(shadowsColourRGB, out)
        writeLittleEndian(titleTextColourRGB, out)
        writeLittleEndian(fillsColourRGB, out)
        writeLittleEndian(accentColourRGB, out)
        writeLittleEndian(accentAndHyperlinkColourRGB, out)
        writeLittleEndian(accentAndFollowingHyperlinkColourRGB, out)
    }

    /**
     * Returns color by its index
     * 
     * @param idx 0-based color index
     * @return color by its index
     */
    fun getColor(idx: Int): Int {
        val clr = intArrayOf(
            backgroundColourRGB, textAndLinesColourRGB, shadowsColourRGB,
            titleTextColourRGB, fillsColourRGB, accentColourRGB, accentAndHyperlinkColourRGB,
            accentAndFollowingHyperlinkColourRGB
        )
        return clr[idx]
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }

    companion object {
        private const val _type = 2032L

        /**
         * Convert from an integer RGB value to individual R, G, B 0-255 values
         */
        fun splitRGB(rgb: Int): ByteArray {
            val ret = ByteArray(3)

            // Serialise to bytes, then grab the right ones out
            val baos = ByteArrayOutputStream()
            try {
                writeLittleEndian(rgb, baos)
            } catch (ie: IOException) {
                // Should never happen
                throw RuntimeException(ie)
            }
            val b = baos.toByteArray()
            System.arraycopy(b, 0, ret, 0, 3)

            return ret
        }

        /**
         * Convert from split R, G, B values to an integer RGB value
         */
        fun joinRGB(r: Byte, g: Byte, b: Byte): Int {
            return joinRGB(byteArrayOf(r, g, b))
        }

        /**
         * Convert from split R, G, B values to an integer RGB value
         */
        fun joinRGB(rgb: ByteArray): Int {
            if (rgb.size != 3) {
                throw RuntimeException(
                    ("joinRGB accepts a byte array of 3 values, but got one of "
                            + rgb.size + " values!")
                )
            }
            val with_zero = ByteArray(4)
            System.arraycopy(rgb, 0, with_zero, 0, 3)
            with_zero[3] = 0
            val ret = getInt(with_zero, 0)
            return ret
        }
    }
}
