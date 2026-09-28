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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.java.awt.Rectangle

/**
 * Represents a cell in a ppt table
 * 
 * @author Yegor Kozlov
 */
class TableCell : TextBox {
    private var borderLeft: Line? = null
    private var borderRight: Line? = null
    private var borderTop: Line? = null
    private var borderBottom: Line? = null

    /**
     * Create a TableCell object and initialize it from the supplied Record container.
     * 
     * @param escherRecord       `EscherSpContainer` container which holds information about this shape
     * @param parent    the parent of the shape
     */
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    /**
     * Create a new TableCell. This constructor is used when a new shape is created.
     * 
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    constructor(parent: Shape?) : super(parent) {
        shapeType = ShapeTypes.Rectangle
        //_txtrun.setRunType(TextHeaderAtom.HALF_BODY_TYPE);
        //_txtrun.getRichTextRuns()[0].setFlag(false, 0, false);
    }

    override fun createSpContainer(isChild: Boolean): EscherContainerRecord? {
        spContainer = super.createSpContainer(isChild)
        val opt = ShapeKit.getEscherChild(
            spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        Shape.setEscherProperty(opt, EscherProperties.TEXT__TEXTID, 0)
        Shape.setEscherProperty(
            opt,
            EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE,
            0x20000
        )
        Shape.setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150001)
        Shape.setEscherProperty(opt, EscherProperties.SHADOWSTYLE__SHADOWOBSURED, 0x20000)
        Shape.setEscherProperty(
            opt,
            EscherProperties.PROTECTION__LOCKAGAINSTGROUPING,
            0x40000
        )

        return spContainer
    }

    protected fun anchorBorder(type: Int, line: Line) {
        val cellAnchor = anchor
        val lineAnchor = Rectangle()
        when (type) {
            Table.BORDER_TOP -> {
                lineAnchor.x = cellAnchor.x
                lineAnchor.y = cellAnchor.y
                lineAnchor.width = cellAnchor.width
                lineAnchor.height = 0
            }

            Table.BORDER_RIGHT -> {
                lineAnchor.x = cellAnchor.x + cellAnchor.width
                lineAnchor.y = cellAnchor.y
                lineAnchor.width = 0
                lineAnchor.height = cellAnchor.height
            }

            Table.BORDER_BOTTOM -> {
                lineAnchor.x = cellAnchor.x
                lineAnchor.y = cellAnchor.y + cellAnchor.height
                lineAnchor.width = cellAnchor.width
                lineAnchor.height = 0
            }

            Table.BORDER_LEFT -> {
                lineAnchor.x = cellAnchor.x
                lineAnchor.y = cellAnchor.y
                lineAnchor.width = 0
                lineAnchor.height = cellAnchor.height
            }

            else -> throw IllegalArgumentException("Unknown border type: " + type)
        }
        line.setAnchor(lineAnchor)
    }

    fun getBorderLeft(): Line? {
        return borderLeft
    }

    fun setBorderLeft(line: Line?) {
        if (line != null) anchorBorder(Table.BORDER_LEFT, line)
        this.borderLeft = line
    }

    fun getBorderRight(): Line? {
        return borderRight
    }

    fun setBorderRight(line: Line?) {
        if (line != null) anchorBorder(Table.BORDER_RIGHT, line)
        this.borderRight = line
    }

    fun getBorderTop(): Line? {
        return borderTop
    }

    fun setBorderTop(line: Line?) {
        if (line != null) anchorBorder(Table.BORDER_TOP, line)
        this.borderTop = line
    }

    fun getBorderBottom(): Line? {
        return borderBottom
    }

    fun setBorderBottom(line: Line?) {
        if (line != null) anchorBorder(Table.BORDER_BOTTOM, line)
        this.borderBottom = line
    }

    fun setAnchor(anchor: Rectangle) {
        super.setAnchor(anchor)

        if (borderTop != null) anchorBorder(Table.BORDER_TOP, borderTop!!)
        if (borderRight != null) anchorBorder(Table.BORDER_RIGHT, borderRight!!)
        if (borderBottom != null) anchorBorder(Table.BORDER_BOTTOM, borderBottom!!)
        if (borderLeft != null) anchorBorder(Table.BORDER_LEFT, borderLeft!!)
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 100
        const val DEFAULT_HEIGHT: Int = 40
    }
}
