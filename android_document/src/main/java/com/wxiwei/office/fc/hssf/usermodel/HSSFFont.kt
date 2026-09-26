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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.FontRecord
import com.wxiwei.office.fc.hssf.record.PaletteRecord
import com.wxiwei.office.fc.ss.usermodel.IFont


/**
 * Represents a Font used in a workbook.
 */
class HSSFFont
constructor(
    private val index: Short,
    private val font: FontRecord?
) : IFont {
    override fun setFontName(name: String?) {
        font?.fontName = name
    }

    override fun getFontName(): String {
        return font?.fontName ?: ""
    }

    override fun getIndex(): Short {
        return index
    }

    override fun setFontHeight(height: Short) {
        font?.fontHeight = height
    }

    override fun setFontHeightInPoints(height: Short) {
        font?.fontHeight = (height * 20).toShort()
    }

    override fun getFontHeight(): Short {
        return font?.fontHeight ?: 0
    }

    override fun getFontHeightInPoints(): Short {
        return ((font?.fontHeight ?: 0) / 20).toShort()
    }

    override fun setItalic(italic: Boolean) {
        font?.setItalic(italic)
    }

    override fun getItalic(): Boolean {
        return font?.isItalic() ?: false
    }

    override fun setStrikeout(strikeout: Boolean) {
        font?.setStrikeout(strikeout)
    }

    override fun getStrikeout(): Boolean {
        return font?.isStruckout() ?: false
    }

    override fun setColor(color: Short) {
        font?.colorPaletteIndex = color
    }

    override fun getColor(): Short {
        val index: Short = font?.colorPaletteIndex ?: 0
        return (if (index.toInt() == 32767) PaletteRecord.FIRST_COLOR_INDEX else index)
    }

    override fun setBoldweight(boldweight: Short) {
        font?.boldWeight = boldweight
    }

    override fun getBoldweight(): Short {
        return font?.boldWeight ?: 0
    }

    override fun setTypeOffset(offset: Short) {
        font?.superSubScript = offset
    }

    override fun getTypeOffset(): Short {
        return font?.superSubScript ?: 0
    }

    override fun setUnderline(underline: Byte) {
        font?.underline = underline
    }

    override fun getUnderline(): Byte {
        return font?.underline ?: 0
    }

    override fun getCharSet(): Int {
        val charset: Byte = font?.charset ?: 0
        if (charset >= 0) {
            return charset.toInt()
        } else {
            return charset + 256
        }
    }

    override fun setCharSet(charset: Int) {
        var cs = charset.toByte()
        if (charset > 127) {
            cs = (charset - 256).toByte()
        }
        setCharSet(cs)
    }

    override fun setCharSet(charset: Byte) {
        font?.charset = charset
    }

    override fun toString(): String {
        return "org.apache.poi.hssf.usermodel.HSSFFont{" +
                font +
                "}"
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (font == null) 0 else font.hashCode())
        result = prime * result + index
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as HSSFFont
        if (font == null) {
            if (other.font != null) return false
        } else if (font != other.font) return false
        if (index != other.index) return false
        return true
    }

    companion object {
        const val FONT_ARIAL: String = "Arial"
        const val BOLDWEIGHT_NORMAL: Short = 0x190.toShort()
        const val BOLDWEIGHT_BOLD: Short = 0x2bc.toShort()
        const val COLOR_NORMAL: Short = 0x7fff.toShort()
        const val COLOR_RED: Short = 0xa.toShort()
        const val SS_NONE: Short = 0
        const val SS_SUPER: Short = 1
        const val SS_SUB: Short = 2
        const val U_NONE: Byte = 0
        const val U_SINGLE: Byte = 1
        const val U_DOUBLE: Byte = 2
        const val U_SINGLE_ACCOUNTING: Byte = 0x21
        const val U_DOUBLE_ACCOUNTING: Byte = 0x22
        const val ANSI_CHARSET: Byte = 0
        const val DEFAULT_CHARSET: Byte = 1
        const val SYMBOL_CHARSET: Byte = 2
    }
}
