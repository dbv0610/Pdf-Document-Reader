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

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherArrayProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.java.awt.Rectangle
import java.util.Arrays
import kotlin.math.max

/**
 * Represents a table in a PowerPoint presentation
 * 
 * @author Yegor Kozlov
 */
class Table : ShapeGroup {
    protected var cells: Array<Array<TableCell?>>? = null

    /**
     * get table borders
     * @return
     */
    var tableBorders: Array<Line?>? = null
        protected set

    /**
     * Create a new Table of the given number of rows and columns
     * 
     * @param numrows the number of rows
     * @param numcols the number of columns
     */
    constructor(numrows: Int, numcols: Int) : super() {
        require(numrows >= 1) { "The number of rows must be greater than 1" }
        require(numcols >= 1) { "The number of columns must be greater than 1" }

        var x = 0
        var y = 0
        var tblWidth = 0
        var tblHeight = 0
        cells = Array<Array<TableCell?>>(numrows) { arrayOfNulls<TableCell>(numcols) }
        for (i in cells!!.indices) {
            x = 0
            for (j in cells!![i].indices) {
                cells!![i][j] = TableCell(this)
                val anchor = Rectangle(
                    x, y, TableCell.DEFAULT_WIDTH,
                    TableCell.DEFAULT_HEIGHT
                )
                cells!![i][j]!!.setAnchor(anchor)
                x += TableCell.DEFAULT_WIDTH
            }
            y += TableCell.DEFAULT_HEIGHT
        }
        tblWidth = x
        tblHeight = y
        setAnchor(Rectangle(0, 0, tblWidth, tblHeight))

        val spCont = spContainer!!.getChild(0) as EscherContainerRecord?
        val opt = EscherOptRecord()
        opt.recordId = 0xF122.toShort()
        opt.addEscherProperty(EscherSimpleProperty(0x39F.toShort(), 1))
        val p = EscherArrayProperty(0x43A0.toShort(), false, null)
        p.setSizeOfElements(0x0004)
        p.numberOfElementsInArray = numrows
        p.numberOfElementsInMemory = numrows
        opt.addEscherProperty(p)
        val lst = spCont!!.childRecords
        lst.add(lst.size - 1, opt)
        spCont.childRecords = lst
    }

    /**
     * Create a Table object and initilize it from the supplied Record container.
     * 
     * @param escherRecord `EscherSpContainer` container which holds information about this shape
     * @param parent       the parent of the shape
     */
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    /**
     * Gets a cell
     * 
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @return the cell
     */
    fun getCell(row: Int, col: Int): TableCell? {
        return cells!![row][col]
    }

    val numberOfColumns: Int
        get() = cells!![0].size

    val numberOfRows: Int
        get() = cells!!.size

    override fun afterInsert(sh: Sheet?) {
        super.afterInsert(sh)

        val spCont = spContainer!!.getChild(0) as EscherContainerRecord?
        val lst = spCont!!.childRecords
        val opt = lst.get(lst.size - 2) as EscherOptRecord
        val p = opt.getEscherProperty(1) as EscherArrayProperty?
        for (i in cells!!.indices) {
            val cell = cells!![i][0]!!
            val rowHeight =
                (cell.anchor.height * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
            val `val` = ByteArray(4)
            putInt(`val`, rowHeight)
            p!!.setElement(i, `val`)
            for (j in cells!![i].indices) {
                val c = cells!![i][j]!!
                addShape(c)

                val bt = c.getBorderTop()
                if (bt != null) addShape(bt)

                val br = c.getBorderRight()
                if (br != null) addShape(br)

                val bb = c.getBorderBottom()
                if (bb != null) addShape(bb)

                val bl = c.getBorderLeft()
                if (bl != null) addShape(bl)
            }
        }
    }

    protected fun initTable() {
        val sh = shapes
        Arrays.sort<Shape?>(sh, object : Comparator<Shape?> {
            override fun compare(o1: Shape?, o2: Shape?): Int {
                val anchor1 = o1!!.anchor
                val anchor2 = o2!!.anchor
                var delta = anchor1.y - anchor2.y
                if (delta == 0) delta = anchor1.x - anchor2.x
                return delta
            }
        })
        var y0 = -1
        var maxrowlen = 0
        val lst = ArrayList<ArrayList<Shape?>>()
        val lineList = ArrayList<Shape?>()
        var row: ArrayList<Shape?>? = null
        for (i in sh.indices) {
            if (sh[i] is TextShape) {
                val anchor = sh[i]!!.anchor
                if (anchor.y != y0) {
                    y0 = anchor.y
                    row = ArrayList<Shape?>()
                    lst.add(row)
                }
                row!!.add(sh[i])
                maxrowlen = max(maxrowlen, row.size)
            } else if (sh[i] is Line) {
                lineList.add(sh[i])
            }
        }
        //table cell
        cells = Array<Array<TableCell?>>(lst.size) { arrayOfNulls<TableCell>(maxrowlen) }
        for (i in lst.indices) {
            row = lst.get(i)
            for (j in row!!.indices) {
                val tx = row.get(j) as TextShape
                cells!![i][j] = TableCell(tx.spContainer, parent)
                cells!![i][j]!!.sheet = tx.sheet
            }
        }
        //table borders
        this.tableBorders = arrayOfNulls<Line>(lineList.size)
        for (i in lineList.indices) {
            this.tableBorders!![i] = lineList.get(i) as Line?
        }
    }

    /**
     * Assign the `SlideShow` this shape belongs to
     * 
     * @param sheet owner of this shape
     */
    override var sheet: Sheet?
        get() = super.sheet
        set(sheet) {
            super.sheet = sheet
            if (cells == null) initTable()
        }

    /**
     * Sets the row height.
     * 
     * @param row the row index (0-based)
     * @param height the height to set (in pixels)
     */
    fun setRowHeight(row: Int, height: Int) {
        val currentHeight = cells!![row][0]!!.anchor.height
        val dy = height - currentHeight

        for (i in row..<cells!!.size) {
            for (j in cells!![i].indices) {
                val anchor = cells!![i][j]!!.anchor
                if (i == row) anchor.height = height
                else anchor.y += dy
                cells!![i][j]!!.setAnchor(anchor)
            }
        }
        val tblanchor = anchor
        tblanchor.height += dy
        setAnchor(tblanchor)
    }

    /**
     * Sets the column width.
     * 
     * @param col the column index (0-based)
     * @param width the width to set (in pixels)
     */
    fun setColumnWidth(col: Int, width: Int) {
        val currentWidth = cells!![0][col]!!.anchor.width
        val dx = width - currentWidth
        for (i in cells!!.indices) {
            var anchor = cells!![i][col]!!.anchor
            anchor.width = width
            cells!![i][col]!!.setAnchor(anchor)

            if (col < cells!![i].size - 1) for (j in col + 1..<cells!![i].size) {
                anchor = cells!![i][j]!!.anchor
                anchor.x += dx
                cells!![i][j]!!.setAnchor(anchor)
            }
        }
        val tblanchor = anchor
        tblanchor.width += dx
        setAnchor(tblanchor)
    }

    /**
     * Format the table and apply the specified Line to all cell boundaries,
     * both outside and inside
     * 
     * @param line the border line
     */
    fun setAllBorders(line: Line) {
        for (i in cells!!.indices) {
            for (j in cells!![i].indices) {
                val cell = cells!![i][j]!!
                cell.setBorderTop(cloneBorder(line))
                cell.setBorderLeft(cloneBorder(line))
                if (j == cells!![i].size - 1) cell.setBorderRight(cloneBorder(line))
                if (i == cells!!.size - 1) cell.setBorderBottom(cloneBorder(line))
            }
        }
    }

    /**
     * Format the outside border using the specified Line object
     * 
     * @param line the border line
     */
    fun setOutsideBorders(line: Line) {
        for (i in cells!!.indices) {
            for (j in cells!![i].indices) {
                val cell = cells!![i][j]!!

                if (j == 0) cell.setBorderLeft(cloneBorder(line))
                if (j == cells!![i].size - 1) cell.setBorderRight(cloneBorder(line))
                else {
                    cell.setBorderLeft(null)
                    cell.setBorderLeft(null)
                }

                if (i == 0) cell.setBorderTop(cloneBorder(line))
                else if (i == cells!!.size - 1) cell.setBorderBottom(cloneBorder(line))
                else {
                    cell.setBorderTop(null)
                    cell.setBorderBottom(null)
                }
            }
        }
    }

    /**
     * Format the inside border using the specified Line object
     * 
     * @param line the border line
     */
    fun setInsideBorders(line: Line) {
        for (i in cells!!.indices) {
            for (j in cells!![i].indices) {
                val cell = cells!![i][j]!!

                if (j != cells!![i].size - 1) cell.setBorderRight(cloneBorder(line))
                else {
                    cell.setBorderLeft(null)
                    cell.setBorderLeft(null)
                }
                if (i != cells!!.size - 1) cell.setBorderBottom(cloneBorder(line))
                else {
                    cell.setBorderTop(null)
                    cell.setBorderBottom(null)
                }
            }
        }
    }

    private fun cloneBorder(line: Line): Line {
        val border = createBorder()
        border.setLineWidth(line.lineWidth)
        border.lineStyle = line.lineStyle
        border.lineDashing = line.lineDashing
        border.setLineColor(line.lineColor)
        return border
    }

    /**
     * Create a border to format this table
     * 
     * @return the created border
     */
    fun createBorder(): Line {
        val line = Line(this)

        val opt = ShapeKit.getEscherChild(
            line.spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        Shape.setEscherProperty(opt, EscherProperties.GEOMETRY__SHAPEPATH, -1)
        Shape.setEscherProperty(opt, EscherProperties.GEOMETRY__FILLOK, -1)
        Shape.setEscherProperty(opt, EscherProperties.SHADOWSTYLE__SHADOWOBSURED, 0x20000)
        Shape.setEscherProperty(opt, EscherProperties.THREED__LIGHTFACE, 0x80000)

        return line
    }

    companion object {
        const val BORDER_TOP: Int = 1
        const val BORDER_RIGHT: Int = 2
        const val BORDER_BOTTOM: Int = 3
        const val BORDER_LEFT: Int = 4

        protected const val BORDERS_ALL: Int = 5
        protected const val BORDERS_OUTSIDE: Int = 6
        protected const val BORDERS_INSIDE: Int = 7
        protected const val BORDERS_NONE: Int = 8
    }
}
