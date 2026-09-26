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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte

/**
 * Title:        Font Record (0x0031) 
 *
 *
 * - describes a font in the workbook (index = 0-3,5-infinity - skip 4)<P>
 * Description:  An element in the Font Table</P><P>
 * REFERENCE:  PG 315 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
</P> */
class FontRecord : StandardRecord {
    private var field_1_font_height: Short = 0 // in units of .05 of a point
    private var field_2_attributes: Short = 0

    // 7-6 - reserved bits must be 0
    // the rest is unused
    private var field_3_color_palette_index: Short = 0
    private var field_4_bold_weight: Short = 0
    private var field_5_super_sub_script: Short = 0 // 00none/01super/02sub
    private var field_6_underline: Byte =
        0 // 00none/01single/02double/21singleaccounting/22doubleaccounting
    private var field_7_family: Byte = 0 // ?? defined by windows api logfont structure?
    private var field_8_charset: Byte = 0 // ?? defined by windows api logfont structure?
    private var field_9_zero: Byte = 0 // must be 0

    /** possibly empty string never `null`  */
    private var field_11_font_name: String? = null

    constructor()

    constructor(`in`: RecordInputStream) {
        field_1_font_height = `in`.readShort()
        field_2_attributes = `in`.readShort()
        field_3_color_palette_index = `in`.readShort()
        field_4_bold_weight = `in`.readShort()
        field_5_super_sub_script = `in`.readShort()
        field_6_underline = `in`.readByte()
        field_7_family = `in`.readByte()
        field_8_charset = `in`.readByte()
        field_9_zero = `in`.readByte()
        val field_10_font_name_len = `in`.readUByte()
        val unicodeFlags =
            `in`.readUByte() // options byte present always (even if no character data)

        if (field_10_font_name_len > 0) {
            if (unicodeFlags == 0) {   // is compressed unicode
                field_11_font_name = `in`.readCompressedUnicode(field_10_font_name_len)
            } else {   // is not compressed unicode
                field_11_font_name = `in`.readUnicodeLEString(field_10_font_name_len)
            }
        } else {
            field_11_font_name = ""
        }
    }

    /**
     * sets the height of the font in 1/20th point units
     * 
     * @param height  fontheight (in points/20)
     */
    fun setFontHeight(height: Short) {
        field_1_font_height = height
    }

    @get:JvmName("getFontHeightProperty")
    @set:JvmName("setFontHeightProperty")
    var fontHeight: Short get() = getFontHeight(); set(v) = setFontHeight(v)
    @get:JvmName("getFontNameProperty")
    @set:JvmName("setFontNameProperty")
    var fontName: String? get() = getFontName(); set(v) = setFontName(v)
    @get:JvmName("getColorPaletteIndexProperty")
    @set:JvmName("setColorPaletteIndexProperty")
    var colorPaletteIndex: Short get() = getColorPaletteIndex(); set(v) = setColorPaletteIndex(v)
    @get:JvmName("getBoldWeightProperty")
    @set:JvmName("setBoldWeightProperty")
    var boldWeight: Short get() = getBoldWeight(); set(v) = setBoldWeight(v)
    @get:JvmName("getSuperSubScriptProperty")
    @set:JvmName("setSuperSubScriptProperty")
    var superSubScript: Short get() = getSuperSubScript(); set(v) = setSuperSubScript(v)
    @get:JvmName("getUnderlineProperty")
    @set:JvmName("setUnderlineProperty")
    var underline: Byte get() = getUnderline(); set(v) = setUnderline(v)
    @get:JvmName("getCharsetProperty")
    @set:JvmName("setCharsetProperty")
    var charset: Byte get() = getCharset(); set(v) = setCharset(v)

    /**
     * set the font attributes (see individual bit setters that reference this method)
     * 
     * @param attributes    the bitmask to set
     */
    fun setAttributes(attributes: Short) {
        field_2_attributes = attributes
    }

    // attributes bitfields
    /**
     * set the font to be italics or not
     * 
     * @param italics - whether the font is italics or not
     * @see .setAttributes
     */
    fun setItalic(italics: Boolean) {
        field_2_attributes = italic.setShortBoolean(field_2_attributes, italics)
    }

    /**
     * set the font to be stricken out or not
     * 
     * @param strike - whether the font is stricken out or not
     * @see .setAttributes
     */
    fun setStrikeout(strike: Boolean) {
        field_2_attributes = strikeout.setShortBoolean(field_2_attributes, strike)
    }

    /**
     * whether to use the mac outline font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     * 
     * @param mac - whether to do that mac font outline thing or not
     * @see .setAttributes
     */
    fun setMacoutline(mac: Boolean) {
        field_2_attributes = macoutline.setShortBoolean(field_2_attributes, mac)
    }

    /**
     * whether to use the mac shado font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     * 
     * @param mac - whether to do that mac font shadow thing or not
     * @see .setAttributes
     */
    fun setMacshadow(mac: Boolean) {
        field_2_attributes = macshadow.setShortBoolean(field_2_attributes, mac)
    }

    /**
     * set the font's color palette index
     * 
     * @param cpi - font color index
     */
    fun setColorPaletteIndex(cpi: Short) {
        field_3_color_palette_index = cpi
    }

    /**
     * set the bold weight for this font (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     * 
     * @param bw - a number between 100-1000 for the fonts "boldness"
     */
    fun setBoldWeight(bw: Short) {
        field_4_bold_weight = bw
    }

    /**
     * set the type of super or subscript for the font
     * 
     * @param sss  super or subscript option
     * @see .SS_NONE
     * 
     * @see .SS_SUPER
     * 
     * @see .SS_SUB
     */
    fun setSuperSubScript(sss: Short) {
        field_5_super_sub_script = sss
    }

    /**
     * set the type of underlining for the font
     * 
     * @param u  super or subscript option
     * 
     * @see .U_NONE
     * 
     * @see .U_SINGLE
     * 
     * @see .U_DOUBLE
     * 
     * @see .U_SINGLE_ACCOUNTING
     * 
     * @see .U_DOUBLE_ACCOUNTING
     */
    fun setUnderline(u: Byte) {
        field_6_underline = u
    }

    /**
     * set the font family (TODO)
     * 
     * @param f family
     */
    fun setFamily(f: Byte) {
        field_7_family = f
    }

    /**
     * set the character set
     * 
     * @param charset - character set
     */
    fun setCharset(charset: Byte) {
        field_8_charset = charset
    }


    /**
     * set the name of the font
     * 
     * @param fn - name of the font (i.e. "Arial")
     */
    fun setFontName(fn: String?) {
        field_11_font_name = fn
    }

    /**
     * gets the height of the font in 1/20th point units
     * 
     * @return fontheight (in points/20)
     */
    fun getFontHeight(): Short {
        return field_1_font_height
    }

    /**
     * get the font attributes (see individual bit getters that reference this method)
     * 
     * @return attribute - the bitmask
     */
    fun getAttributes(): Short {
        return field_2_attributes
    }

    /**
     * get whether the font is to be italics or not
     * 
     * @return italics - whether the font is italics or not
     * @see .getAttributes
     */
    fun isItalic(): Boolean {
        return italic.isSet(field_2_attributes.toInt())
    }

    /**
     * get whether the font is to be stricken out or not
     * 
     * @return strike - whether the font is stricken out or not
     * @see .getAttributes
     */
    fun isStruckout(): Boolean {
        return strikeout.isSet(field_2_attributes.toInt())
    }

    /**
     * whether to use the mac outline font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     * 
     * @return mac - whether to do that mac font outline thing or not
     * @see .getAttributes
     */
    fun isMacoutlined(): Boolean {
        return macoutline.isSet(field_2_attributes.toInt())
    }

    /**
     * whether to use the mac shado font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     * 
     * @return mac - whether to do that mac font shadow thing or not
     * @see .getAttributes
     */
    fun isMacshadowed(): Boolean {
        return macshadow.isSet(field_2_attributes.toInt())
    }

    /**
     * get the font's color palette index
     * 
     * @return cpi - font color index
     */
    fun getColorPaletteIndex(): Short {
        return field_3_color_palette_index
    }

    /**
     * get the bold weight for this font (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     * 
     * @return bw - a number between 100-1000 for the fonts "boldness"
     */
    fun getBoldWeight(): Short {
        return field_4_bold_weight
    }

    /**
     * get the type of super or subscript for the font
     * 
     * @return super or subscript option
     * @see .SS_NONE
     * 
     * @see .SS_SUPER
     * 
     * @see .SS_SUB
     */
    fun getSuperSubScript(): Short {
        return field_5_super_sub_script
    }

    /**
     * get the type of underlining for the font
     * 
     * @return super or subscript option
     * 
     * @see .U_NONE
     * 
     * @see .U_SINGLE
     * 
     * @see .U_DOUBLE
     * 
     * @see .U_SINGLE_ACCOUNTING
     * 
     * @see .U_DOUBLE_ACCOUNTING
     */
    fun getUnderline(): Byte {
        return field_6_underline
    }

    /**
     * get the font family (TODO)
     * 
     * @return family
     */
    fun getFamily(): Byte {
        return field_7_family
    }

    /**
     * get the character set
     * 
     * @return charset - character set
     */
    fun getCharset(): Byte {
        return field_8_charset
    }

    /**
     * get the name of the font
     * 
     * @return fn - name of the font (i.e. "Arial")
     */
    fun getFontName(): String? {
        return field_11_font_name
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[FONT]\n")
        sb.append("    .fontheight    = ").append(shortToHex(getFontHeight().toInt())).append("\n")
        sb.append("    .attributes    = ").append(shortToHex(getAttributes().toInt())).append("\n")
        sb.append("       .italic     = ").append(isItalic()).append("\n")
        sb.append("       .strikout   = ").append(isStruckout()).append("\n")
        sb.append("       .macoutlined= ").append(isMacoutlined()).append("\n")
        sb.append("       .macshadowed= ").append(isMacshadowed()).append("\n")
        sb.append("    .colorpalette  = ").append(shortToHex(getColorPaletteIndex().toInt()))
            .append("\n")
        sb.append("    .boldweight    = ").append(shortToHex(getBoldWeight().toInt())).append("\n")
        sb.append("    .supersubscript= ").append(shortToHex(getSuperSubScript().toInt()))
            .append("\n")
        sb.append("    .underline     = ").append(byteToHex(getUnderline().toInt())).append("\n")
        sb.append("    .family        = ").append(byteToHex(getFamily().toInt())).append("\n")
        sb.append("    .charset       = ").append(byteToHex(getCharset().toInt())).append("\n")
        sb.append("    .fontname      = ").append(getFontName()).append("\n")
        sb.append("[/FONT]\n")
        return sb.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(getFontHeight().toInt())
        out.writeShort(getAttributes().toInt())
        out.writeShort(getColorPaletteIndex().toInt())
        out.writeShort(getBoldWeight().toInt())
        out.writeShort(getSuperSubScript().toInt())
        out.writeByte(getUnderline().toInt())
        out.writeByte(getFamily().toInt())
        out.writeByte(getCharset().toInt())
        out.writeByte(field_9_zero.toInt())
        val fontNameLen = field_11_font_name!!.length
        out.writeByte(fontNameLen)
        val hasMultibyte = hasMultibyte(field_11_font_name)
        out.writeByte(if (hasMultibyte) 0x01 else 0x00)
        if (fontNameLen > 0) {
            if (hasMultibyte) {
                StringUtil.putUnicodeLE(field_11_font_name!!, out)
            } else {
                StringUtil.putCompressedUnicode(field_11_font_name!!, out)
            }
        }
    }

    override fun getDataSize(): Int {
        val size = 16 // 5 shorts + 6 bytes
        val fontNameLen = field_11_font_name!!.length
        if (fontNameLen < 1) {
            return size
        }

        val hasMultibyte = hasMultibyte(field_11_font_name)
        return size + fontNameLen * (if (hasMultibyte) 2 else 1)
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Clones all the font style information from another
     * FontRecord, onto this one. This
     * will then hold all the same font style options.
     */
    fun cloneStyleFrom(source: FontRecord) {
        field_1_font_height = source.field_1_font_height
        field_2_attributes = source.field_2_attributes
        field_3_color_palette_index = source.field_3_color_palette_index
        field_4_bold_weight = source.field_4_bold_weight
        field_5_super_sub_script = source.field_5_super_sub_script
        field_6_underline = source.field_6_underline
        field_7_family = source.field_7_family
        field_8_charset = source.field_8_charset
        field_9_zero = source.field_9_zero
        field_11_font_name = source.field_11_font_name
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = (prime
                * result
                + (if (field_11_font_name == null) 0 else field_11_font_name
            .hashCode()))
        result = prime * result + field_1_font_height
        result = prime * result + field_2_attributes
        result = prime * result + field_3_color_palette_index
        result = prime * result + field_4_bold_weight
        result = prime * result + field_5_super_sub_script
        result = prime * result + field_6_underline
        result = prime * result + field_7_family
        result = prime * result + field_8_charset
        result = prime * result + field_9_zero
        return result
    }

    /**
     * Does this FontRecord have all the same font
     * properties as the supplied FontRecord?
     * Note that [.equals] will check
     * for exact objects, while this will check
     * for exact contents, because normally the
     * font record's position makes a big
     * difference too.
     */
    fun sameProperties(other: FontRecord): Boolean {
        return field_1_font_height == other.field_1_font_height && field_2_attributes == other.field_2_attributes && field_3_color_palette_index == other.field_3_color_palette_index && field_4_bold_weight == other.field_4_bold_weight && field_5_super_sub_script == other.field_5_super_sub_script && field_6_underline == other.field_6_underline && field_7_family == other.field_7_family && field_8_charset == other.field_8_charset && field_9_zero == other.field_9_zero &&
                field_11_font_name == other.field_11_font_name
    }

    companion object {
        const val sid: Short =
            0x0031 // docs are wrong (0x231 Microsoft Support site article Q184647)
        const val SS_NONE: Short = 0
        const val SS_SUPER: Short = 1
        const val SS_SUB: Short = 2
        const val U_NONE: Byte = 0
        const val U_SINGLE: Byte = 1
        const val U_DOUBLE: Byte = 2
        const val U_SINGLE_ACCOUNTING: Byte = 0x21
        const val U_DOUBLE_ACCOUNTING: Byte = 0x22

        // 0 0x01 - Reserved bit must be 0
        private val italic = getInstance(0x02) // is this font in italics

        // 2 0x04 - reserved bit must be 0
        private val strikeout = getInstance(0x08) // is this font has a line through the center
        private val macoutline =
            getInstance(0x10) // some weird macintosh thing....but who understands those mac people anyhow
        private val macshadow =
            getInstance(0x20) // some weird macintosh thing....but who understands those mac people anyhow
    }
}
