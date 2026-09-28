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
package com.wxiwei.office.fc.hslf.usermodel

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.hslf.model.TextRun
import com.wxiwei.office.fc.hslf.model.textproperties.BitMaskTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.CharFlagsTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.ParagraphFlagsTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextPropCollection
import com.wxiwei.office.java.awt.Color
import kotlin.math.min

/**
 * Represents a run of text, all with the same style
 * 
 */
class RichTextRun
@JvmOverloads constructor(
    parent: TextRun?, startAt: Int, len: Int, pStyle: TextPropCollection? = null,
    cStyle: TextPropCollection? = null, pShared: Boolean = false, cShared: Boolean = false
) {
    /**
     * Supply (normally default) textprops, and if they're shared,
     * when a run gets them
     */
    fun supplyTextProps(
        pStyle: TextPropCollection?, cStyle: TextPropCollection?,
        pShared: Boolean, cShared: Boolean
    ) {
        check(!(paragraphStyle != null || characterStyle != null)) { "Can't call supplyTextProps if run already has some" }
        paragraphStyle = pStyle
        characterStyle = cStyle
        sharingParagraphStyle = pShared
        sharingCharacterStyle = cShared
    }

    /**
     * Supply the SlideShow we belong to
     */
    fun supplySlideShow(ss: SlideShow?) {
        slideShow = ss
        if (_fontname != null) {
            this.fontName = _fontname
            _fontname = null
        }
    }

    @get:JvmName("getEndIndexProperty")
    val endIndex: Int
        /**
         * The ending index, exclusive.
         * 
         * @return the ending index, exclusive.
         */
        get() = this.startIndex + length

    var text: String
        /**
         * Fetch the text, in output suitable form
         */
        get() {
            val text = parentRun!!.text
            val end = min(text.length, this.startIndex + length)
            return text.substring(this.startIndex, end)
        }
        /**
         * Change the text
         */
        set(text) {
            val s = parentRun!!.normalize(text)
            this.rawText = s
        }

    var rawText: String
        /**
         * Fetch the text, in raw storage form
         */
        get() = parentRun!!.rawText.substring(this.startIndex, this.startIndex + length)
        /**
         * Change the text
         */
        set(text) {
            length = text.length
            parentRun!!.changeTextInRichTextRun(this, text)
        }

    /**
     * Tells the RichTextRun its new position in the parent TextRun
     * @param startAt
     */
    fun updateStartPosition(startAt: Int) {
        this.startIndex = startAt
    }

    // --------------- Internal helpers on rich text properties -------
    /**
     * Fetch the value of the given flag in the CharFlagsTextProp.
     * Returns false if the CharFlagsTextProp isn't present, since the
     * text property won't be set if there's no CharFlagsTextProp.
     */
    private fun isCharFlagsTextPropVal(index: Int): Boolean {
        return getFlag(true, index)
    }

    private fun getFlag(isCharacter: Boolean, index: Int): Boolean {
        val props: TextPropCollection?
        val propname: String?
        if (isCharacter) {
            props = characterStyle
            propname = CharFlagsTextProp.NAME
        } else {
            props = paragraphStyle
            propname = ParagraphFlagsTextProp.NAME
        }

        var prop: BitMaskTextProp? = null
        if (props != null) {
            prop = props.findByName(propname) as BitMaskTextProp?
        }
        if (prop == null) {
            val sheet = parentRun!!.sheet
            if (sheet != null) {
                val txtype = parentRun!!.runType
                val master = sheet.masterSheet
                if (master != null) {
                    prop = master.getStyleAttribute(
                        txtype, this.indentLevel,
                        propname, isCharacter
                    ) as BitMaskTextProp?
                }
            } else {
            }
        }

        return if (prop == null) false else prop.getSubValue(index)
    }

    /**
     * Set the value of the given flag in the CharFlagsTextProp, adding
     * it if required.
     */
    private fun setCharFlagsTextPropVal(index: Int, value: Boolean) {
        if (getFlag(true, index) != value) setFlag(true, index, value)
    }

    fun setFlag(isCharacter: Boolean, index: Int, value: Boolean) {
        var props: TextPropCollection?
        val propname: String?
        if (isCharacter) {
            props = characterStyle
            propname = CharFlagsTextProp.NAME
        } else {
            props = paragraphStyle
            propname = ParagraphFlagsTextProp.NAME
        }

        // Ensure we have the StyleTextProp atom we're going to need
        if (props == null) {
            parentRun!!.ensureStyleAtomPresent()
            props = if (isCharacter) characterStyle else paragraphStyle
        }

        val prop = fetchOrAddTextProp(props!!, propname) as BitMaskTextProp
        prop.setSubValue(value, index)
    }

    /**
     * Returns the named TextProp, either by fetching it (if it exists) or adding it
     * (if it didn't)
     * @param textPropCol The TextPropCollection to fetch from / add into
     * @param textPropName The name of the TextProp to fetch/add
     */
    private fun fetchOrAddTextProp(
        textPropCol: TextPropCollection,
        textPropName: String?
    ): TextProp {
        // Fetch / Add the TextProp
        var tp = textPropCol.findByName(textPropName)
        if (tp == null) {
            tp = textPropCol.addWithName(textPropName)
        }
        return tp
    }

    /**
     * Fetch the value of the given Character related TextProp.
     * Returns -1 if that TextProp isn't present.
     * If the TextProp isn't present, the value from the appropriate
     * Master Sheet will apply.
     */
    private fun getCharTextPropVal(propName: String): Int {
        var prop: TextProp? = null
        if (characterStyle != null) {
            prop = characterStyle!!.findByName(propName)
        }

        if (prop == null) {
            val sheet = parentRun!!.sheet
            val txtype = parentRun!!.runType
            val master = sheet!!.masterSheet
            if (master != null) {
                prop = master.getStyleAttribute(txtype, this.indentLevel, propName, true)
            }
        }
        if (prop == null && propName.equals("font.color", ignoreCase = true)) {
            return Color.BLACK.getRGB()
        }
        return if (prop == null) -1 else prop.value
    }

    /**
     * Fetch the value of the given Paragraph related TextProp.
     * Returns -1 if that TextProp isn't present.
     * If the TextProp isn't present, the value from the appropriate
     * Master Sheet will apply.
     */
    private fun getParaTextPropVal(propName: String?): Int {
        var prop: TextProp? = null
        val hardAttribute = false
        if (paragraphStyle != null) {
            prop = paragraphStyle!!.findByName(propName)

            /*BitMaskTextProp maskProp = (BitMaskTextProp)paragraphStyle
                .findByName(ParagraphFlagsTextProp.NAME);
            hardAttribute = maskProp != null && maskProp.getValue() != 0;*/
        }
        if (prop == null && !hardAttribute) {
            val sheet = parentRun!!.sheet
            val txtype = parentRun!!.runType
            val master = sheet!!.masterSheet
            if (master != null) prop = master.getStyleAttribute(
                txtype,
                this.indentLevel, propName, false
            )
        }

        return if (prop == null) -1 else prop.value
    }

    /**
     * Sets the value of the given Character TextProp, add if required
     * @param propName The name of the Character TextProp
     * @param val The value to set for the TextProp
     */
    fun setParaTextPropVal(propName: String?, `val`: Int) {
        // Ensure we have the StyleTextProp atom we're going to need
        if (paragraphStyle == null) {
            parentRun!!.ensureStyleAtomPresent()
            // paragraphStyle will now be defined
        }

        val tp = fetchOrAddTextProp(paragraphStyle!!, propName)
        tp.value = `val`
    }

    /**
     * Sets the value of the given Paragraph TextProp, add if required
     * @param propName The name of the Paragraph TextProp
     * @param val The value to set for the TextProp
     */
    fun setCharTextPropVal(propName: String?, `val`: Int) {
        // Ensure we have the StyleTextProp atom we're going to need
        if (characterStyle == null) {
            parentRun!!.ensureStyleAtomPresent()
            // characterStyle will now be defined
        }

        val tp = fetchOrAddTextProp(characterStyle!!, propName)
        tp.value = `val`
    }

    // --------------- Friendly getters / setters on rich text properties -------
    @get:JvmName("isBoldProperty")
    var isBold: Boolean
        /**
         * Is the text bold?
         */
        get() = isCharFlagsTextPropVal(CharFlagsTextProp.BOLD_IDX)
        /**
         * Is the text bold?
         */
        set(bold) {
            setCharFlagsTextPropVal(CharFlagsTextProp.BOLD_IDX, bold)
        }

    @get:JvmName("isItalicProperty")
    var isItalic: Boolean
        /**
         * Is the text italic?
         */
        get() = isCharFlagsTextPropVal(CharFlagsTextProp.ITALIC_IDX)
        /**
         * Is the text italic?
         */
        set(italic) {
            setCharFlagsTextPropVal(CharFlagsTextProp.ITALIC_IDX, italic)
        }

    @get:JvmName("isUnderlinedProperty")
    var isUnderlined: Boolean
        /**
         * Is the text underlined?
         */
        get() = isCharFlagsTextPropVal(CharFlagsTextProp.UNDERLINE_IDX)
        /**
         * Is the text underlined?
         */
        set(underlined) {
            setCharFlagsTextPropVal(CharFlagsTextProp.UNDERLINE_IDX, underlined)
        }

    var isShadowed: Boolean
        /**
         * Does the text have a shadow?
         */
        get() = isCharFlagsTextPropVal(CharFlagsTextProp.SHADOW_IDX)
        /**
         * Does the text have a shadow?
         */
        set(flag) {
            setCharFlagsTextPropVal(CharFlagsTextProp.SHADOW_IDX, flag)
        }

    var isEmbossed: Boolean
        /**
         * Is this text embossed?
         */
        get() = isCharFlagsTextPropVal(CharFlagsTextProp.RELIEF_IDX)
        /**
         * Is this text embossed?
         */
        set(flag) {
            setCharFlagsTextPropVal(CharFlagsTextProp.RELIEF_IDX, flag)
        }

    @get:JvmName("isStrikethroughProperty")
    var isStrikethrough: Boolean
        /**
         * Gets the strikethrough flag
         */
        get() = isCharFlagsTextPropVal(CharFlagsTextProp.STRIKETHROUGH_IDX)
        /**
         * Sets the strikethrough flag
         */
        set(flag) {
            setCharFlagsTextPropVal(CharFlagsTextProp.STRIKETHROUGH_IDX, flag)
        }

    @get:JvmName("getSuperscriptProperty")
    var superscript: Int
        /**
         * Gets the subscript/superscript option
         * 
         * @return the percentage of the font size. If the value is positive, it is superscript, otherwise it is subscript
         */
        get() {
            val `val` = getCharTextPropVal("superscript")
            return if (`val` == -1) 0 else `val`
        }
        /**
         * Sets the subscript/superscript option
         * 
         * @param val the percentage of the font size. If the value is positive, it is superscript, otherwise it is subscript
         */
        set(value) {
            setCharTextPropVal("superscript", value)
        }

    @get:JvmName("getFontSizeProperty")
    var fontSize: Int
        /**
         * Gets the font size
         */
        get() = getCharTextPropVal("font.size")
        /**
         * Sets the font size
         */
        set(fontSize) {
            setCharTextPropVal("font.size", fontSize)
        }

    var fontIndex: Int
        /**
         * Gets the font index
         */
        get() = getCharTextPropVal("font.index")
        /**
         * Sets the font index
         */
        set(idx) {
            setCharTextPropVal("font.index", idx)
        }

    @get:JvmName("getFontNameProperty")
    var fontName: String?
        /**
         * Gets the font name
         */
        get() {
            if (slideShow == null) {
                return _fontname
            }
            val fontIdx = getCharTextPropVal("font.index")
            if (fontIdx == -1) {
                return null
            }
            return slideShow!!.fontCollection!!.getFontWithId(fontIdx)
        }
        /**
         * Sets the font name to use
         */
        set(fontName) {
            if (slideShow == null) {
                //we can't set font since slideshow is not assigned yet
                _fontname = fontName
            } else {
                // Get the index for this font (adding if needed)
                val fontIdx = slideShow!!.fontCollection!!.addFont(fontName!!)
                setCharTextPropVal("font.index", fontIdx)
            }
        }

    @get:JvmName("getFontColorProperty")
    val fontColor: Color
        /**
         * @return font color as RGB value
         * @see Color
         */
        get() {
            var rgb = getCharTextPropVal("font.color")

            val cidx = rgb shr 24
            if (rgb % 0x1000000 == 0) {
                val ca = parentRun!!.sheet!!.colorScheme!!
                if (cidx >= 0 && cidx <= 7) rgb = ca.getColor(cidx)
            }
            val tmp = Color(rgb, true)
            return Color(tmp.getBlue(), tmp.getGreen(), tmp.getRed())
        }

    /**
     * Sets color of the text, as a int bgr.
     * (PowerPoint stores as BlueGreenRed, not the more
     * usual RedGreenBlue)
     * @see Color
     */
    fun setFontColor(bgr: Int) {
        setCharTextPropVal("font.color", bgr)
    }

    /**
     * Sets color of the text, as a java.awt.Color
     */
    fun setFontColor(color: Color) {
        // In PowerPont RGB bytes are swapped, as BGR
        val rgb = Color(color.getBlue(), color.getGreen(), color.getRed(), 254).getRGB()
        setFontColor(rgb)
    }

    var alignment: Int
        /**
         * Returns the type of horizontal alignment for the text.
         * One of the `Align*` constants defined in the `TextBox class.
         * 
         * @return the type of alignment
        ` */
        get() = getParaTextPropVal("alignment")
        /**
         * Sets the type of horizontal alignment for the text.
         * One of the `Align*` constants defined in the `TextBox` class.
         * 
         * @param align - the type of alignment
         */
        set(align) {
            setParaTextPropVal("alignment", align)
        }

    var indentLevel: Int
        /**
         * 
         * @return indentation level
         */
        get() = (if (paragraphStyle == null) 0 else paragraphStyle!!.reservedField).toInt()
        /**
         * Sets indentation level
         * 
         * @param level indentation level. Must be in the range [0, 4]
         */
        set(level) {
            if (paragraphStyle != null) paragraphStyle!!.reservedField = level.toShort()
        }

    var isBullet: Boolean
        /**
         * Returns whether this rich text run has bullets
         */
        get() = getFlag(false, ParagraphFlagsTextProp.BULLET_IDX)
        /**
         * Sets whether this rich text run has bullets
         */
        set(flag) {
            setFlag(false, ParagraphFlagsTextProp.BULLET_IDX, flag)
        }

    val isBulletHard: Boolean
        /**
         * Returns whether this rich text run has bullets
         */
        get() = getFlag(false, ParagraphFlagsTextProp.BULLET_IDX)

    var bulletChar: Char
        /**
         * Returns the bullet character
         */
        get() = getParaTextPropVal("bullet.char").toChar()
        /**
         * Sets the bullet character
         */
        set(c) {
            setParaTextPropVal("bullet.char", c.code)
        }

    var bulletOffset: Int
        /**
         * Returns the bullet offset
         */
        get() = (getParaTextPropVal("bullet.offset") * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt()
        /**
         * Sets the bullet offset
         */
        set(offset) {
            setParaTextPropVal(
                "bullet.offset",
                (offset * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
            )
        }

    var textOffset: Int
        /**
         * Returns the text offset
         */
        get() = (getParaTextPropVal("text.offset") * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt()
        /**
         * Sets the text offset
         */
        set(offset) {
            setParaTextPropVal(
                "text.offset",
                (offset * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
            )
        }

    var bulletSize: Int
        /**
         * Returns the bullet size
         */
        get() = getParaTextPropVal("bullet.size")
        /**
         * Sets the bullet size
         */
        set(size) {
            setParaTextPropVal("bullet.size", size)
        }

    var bulletColor: Color
        /**
         * Returns the bullet color
         */
        get() {
            var rgb = getParaTextPropVal("bullet.color")
            if (rgb == -1) return this.fontColor

            val cidx = rgb shr 24
            if (rgb % 0x1000000 == 0) {
                val ca = parentRun!!.sheet!!.colorScheme!!
                if (cidx >= 0 && cidx <= 7) rgb = ca.getColor(cidx)
            }
            val tmp = Color(rgb, true)
            return Color(tmp.getBlue(), tmp.getGreen(), tmp.getRed())
        }
        /**
         * Sets the bullet color
         */
        set(color) {
            val rgb = Color(
                color.getBlue(),
                color.getGreen(),
                color.getRed(),
                254
            ).getRGB()
            setParaTextPropVal("bullet.color", rgb)
        }

    var bulletFont: Int
        /**
         * Returns the bullet font
         */
        get() = getParaTextPropVal("bullet.font")
        /**
         * Sets the bullet font
         */
        set(idx) {
            setParaTextPropVal("bullet.font", idx)
            setFlag(false, ParagraphFlagsTextProp.BULLET_HARDFONT_IDX, true)
        }

    var lineSpacing: Int
        /**
         * Returns the line spacing
         * 
         * 
         * If linespacing >= 0, then linespacing is a percentage of normal line height.
         * If linespacing < 0, the absolute value of linespacing is the spacing in master coordinates.
         * 
         * 
         * @return the spacing between lines
         */
        get() {
            val `val` = getParaTextPropVal("linespacing")
            return if (`val` == -1) 0 else `val`
        }
        /**
         * Sets the line spacing.
         * 
         * 
         * If linespacing >= 0, then linespacing is a percentage of normal line height.
         * If linespacing < 0, the absolute value of linespacing is the spacing in master coordinates.
         * 
         */
        set(value) {
            setParaTextPropVal("linespacing", value)
        }

    var spaceBefore: Int
        /**
         * Returns spacing before a paragraph
         * 
         * 
         * If spacebefore >= 0, then spacebefore is a percentage of normal line height.
         * If spacebefore < 0, the absolute value of spacebefore is the spacing in master coordinates.
         * 
         * 
         * @return the spacing before a paragraph
         */
        get() {
            val `val` = getParaTextPropVal("spacebefore")
            return if (`val` == -1) 0 else `val`
        }
        /**
         * Sets spacing before a paragraph.
         * 
         * 
         * If spacebefore >= 0, then spacebefore is a percentage of normal line height.
         * If spacebefore < 0, the absolute value of spacebefore is the spacing in master coordinates.
         * 
         */
        set(value) {
            setParaTextPropVal("spacebefore", value)
        }

    var spaceAfter: Int
        /**
         * Returns spacing after a paragraph
         * 
         * 
         * If spaceafter >= 0, then spaceafter is a percentage of normal line height.
         * If spaceafter < 0, the absolute value of spaceafter is the spacing in master coordinates.
         * 
         * 
         * @return the spacing before a paragraph
         */
        get() {
            val `val` = getParaTextPropVal("spaceafter")
            return if (`val` == -1) 0 else `val`
        }
        /**
         * Sets spacing after a paragraph.
         * 
         * 
         * If spaceafter >= 0, then spaceafter is a percentage of normal line height.
         * If spaceafter < 0, the absolute value of spaceafter is the spacing in master coordinates.
         * 
         */
        set(value) {
            setParaTextPropVal("spaceafter", value)
        }

    // --------------- Internal HSLF methods, not intended for end-user use! -------
    /**
     * Internal Use Only - get the underlying paragraph style collection.
     * For normal use, use the friendly setters and getters
     */
    fun _getRawParagraphStyle(): TextPropCollection? {
        return paragraphStyle
    }

    /**
     * Internal Use Only - get the underlying character style collection.
     * For normal use, use the friendly setters and getters
     */
    fun _getRawCharacterStyle(): TextPropCollection? {
        return characterStyle
    }

    /**
     * Internal Use Only - are the Paragraph styles shared?
     */
    fun _isParagraphStyleShared(): Boolean {
        return sharingParagraphStyle
    }

    /**
     * Internal Use Only - are the Character styles shared?
     */
    fun _isCharacterStyleShared(): Boolean {
        return sharingCharacterStyle
    }

    /**
     * 
     * 
     */
    fun dispose() {
        parentRun = null
        slideShow = null
        _fontname = null
        if (paragraphStyle != null) {
            paragraphStyle!!.dispose()
            paragraphStyle = null
        }
        if (characterStyle != null) {
            characterStyle!!.dispose()
            characterStyle = null
        }
    }


    /** The TextRun we belong to  */
    private var parentRun: TextRun?

    /** The SlideShow we belong to  */
    private var slideShow: SlideShow? = null

    /**
     * The beginning index, inclusive.
     * 
     * @return the beginning index, inclusive.
     */
    /** Where in the parent TextRun we start from  */
    @get:JvmName("getStartIndexProperty")
    var startIndex: Int
        private set
    fun getStartIndex(): Int = startIndex
    fun getEndIndex(): Int = endIndex
    fun getFontName(): String? = fontName
    fun getFontSize(): Int = fontSize
    fun getFontColor(): Color? = fontColor
    fun isBold(): Boolean = isBold
    fun isItalic(): Boolean = isItalic
    fun isUnderlined(): Boolean = isUnderlined
    fun isStrikethrough(): Boolean = isStrikethrough
    fun getSuperscript(): Int = superscript

    /**
     * Get the length of the text
     */
    /** How long a string (in the parent TextRun) we represent  */
    var length: Int
        private set

    private var _fontname: String? = null

    /**
     * Our paragraph and character style.
     * Note - we may share these styles with other RichTextRuns
     */
    private var paragraphStyle: TextPropCollection?
    private var characterStyle: TextPropCollection?
    private var sharingParagraphStyle: Boolean
    private var sharingCharacterStyle: Boolean
    /**
     * Create a new wrapper around a rich text string
     * @param parent The parent TextRun
     * @param startAt The start position of this run
     * @param len The length of this run
     * @param pStyle The paragraph style property collection
     * @param cStyle The character style property collection
     * @param pShared The paragraph styles are shared with other runs
     * @param cShared The character styles are shared with other runs
     */
    //protected POILogger logger = POILogFactory.getLogger(this.getClass());
    /**
     * Create a new wrapper around a (currently not)
     * rich text string
     * @param parent
     * @param startAt
     * @param len
     */
    init {
        parentRun = parent
        this.startIndex = startAt
        length = len
        paragraphStyle = pStyle
        characterStyle = cStyle
        sharingParagraphStyle = pShared
        sharingCharacterStyle = cShared
    }
}
