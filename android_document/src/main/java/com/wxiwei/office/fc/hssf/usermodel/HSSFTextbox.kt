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

import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ss.usermodel.RichTextString
import kotlin.math.roundToInt

/**
 * A textbox is a shape that may hold a rich text string.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
open class HSSFTextbox(
    escherContainer: EscherContainerRecord?,
    parent: HSSFShape?,
    anchor: HSSFAnchor?
) : HSSFSimpleShape(escherContainer, parent, anchor) {
    private var string: HSSFRichTextString = HSSFRichTextString("")

    /**
     * @return  the rich text string for this textbox.
     */
    open fun getString(): HSSFRichTextString {
        return string
    }

    /**
     * @param string    Sets the rich text string used by this object.
     */
    open fun setString(string: RichTextString?) {
        val rtr = string as HSSFRichTextString

        // If font is not set we must set the default one
        if (rtr.numFormattingRuns() == 0) rtr.applyFont(0.toShort())

        this.string = rtr
    }

    /**
     * 
     */

    /**
     * @return  Returns the left margin within the textbox.
     */
    /**
     * Sets the left margin within the textbox.
     */
    @get:JvmName("getMarginLeftProperty")
    var marginLeft: Int = 0
    fun getMarginLeft(): Int = marginLeft

    @get:JvmName("getMarginRightProperty")
    var marginRight: Int = 0
    fun getMarginRight(): Int = marginRight

    @get:JvmName("getMarginTopProperty")
    var marginTop: Int = 0
    fun getMarginTop(): Int = marginTop

    @get:JvmName("getMarginBottomProperty")
    var marginBottom: Int = 0
    fun getMarginBottom(): Int = marginBottom

    @get:JvmName("getHorizontalAlignmentProperty")
    var horizontalAlignment: Short = 0
    fun getHorizontalAlignment(): Short = horizontalAlignment

    @get:JvmName("getVerticalAlignmentProperty")
    var verticalAlignment: Short = 0
    fun getVerticalAlignment(): Short = verticalAlignment

    /**
     * word wrap text in AutoShape
     * @param escherContainer
     * @return
     */
    val isTextboxWrapLine: Boolean
    var isWordArt: Boolean = false
    var fontColor: Int = 0

    /**
     * Construct a new textbox with the given parent and anchor.
     * @param parent
     * @param anchor  One of HSSFClientAnchor or HSSFChildAnchor
     */
    init {
        shapeType = OBJECT_TYPE_TEXT.toInt()

        this.horizontalAlignment = HORIZONTAL_ALIGNMENT_LEFT
        this.verticalAlignment = VERTICAL_ALIGNMENT_TOP

        marginLeft = ShapeKit.getTextboxMarginLeft(escherContainer).roundToInt()
        marginTop = ShapeKit.getTextboxMarginTop(escherContainer).roundToInt()
        marginRight = ShapeKit.getTextboxMarginRight(escherContainer).roundToInt()
        marginBottom = ShapeKit.getTextboxMarginBottom(escherContainer).roundToInt()
        this.isTextboxWrapLine = ShapeKit.isTextboxWrapLine(escherContainer)
    }

    companion object {
        const val OBJECT_TYPE_TEXT: Short = 6

        /**
         * How to align text horizontally
         */
        const val HORIZONTAL_ALIGNMENT_LEFT: Short = 1
        const val HORIZONTAL_ALIGNMENT_CENTERED: Short = 2
        const val HORIZONTAL_ALIGNMENT_RIGHT: Short = 3
        const val HORIZONTAL_ALIGNMENT_JUSTIFIED: Short = 4
        const val HORIZONTAL_ALIGNMENT_DISTRIBUTED: Short = 7

        /**
         * How to align text vertically
         */
        const val VERTICAL_ALIGNMENT_TOP: Short = 1
        const val VERTICAL_ALIGNMENT_CENTER: Short = 2
        const val VERTICAL_ALIGNMENT_BOTTOM: Short = 3
        const val VERTICAL_ALIGNMENT_JUSTIFY: Short = 4
        const val VERTICAL_ALIGNMENT_DISTRIBUTED: Short = 7
    }
}
