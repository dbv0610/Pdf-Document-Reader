/*
 * 文件名称:           TableReader.java
 *  
 * 编译器:             android2.2
 * 时间:               上午9:20:42
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.common.shape.TableCell
import com.wxiwei.office.common.shape.TableShape
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.fc.ppt.ShapeManage.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.ParaAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr.Companion.instance
import com.wxiwei.office.fc.ppt.attribute.SectionAttr
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Rectanglef
import com.wxiwei.office.pg.model.PGMaster
import com.wxiwei.office.pg.model.PGModel
import com.wxiwei.office.pg.model.tableStyle.TableCellStyle
import com.wxiwei.office.pg.model.tableStyle.TableStyle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IAttributeSet
import com.wxiwei.office.simpletext.model.ParagraphElement
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.system.IControl

/**
 * 解析table
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-4-16
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class TableReader {
    /**
     * 
     * @param master
     * @param style
     * @param tbl
     * @param rect
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTable(
        control: IControl?, zipPackage: ZipPackage?, packagePart: PackagePart?, pgModel: PGModel,
        master: PGMaster?, tbl: Element, rect: Rectangle
    ): TableShape? {
        RunAttr.instance().setTable(true)
        var table: TableShape? = null

        val tblGrid = tbl.element("tblGrid")
        if (tblGrid != null) {
            // column widths
            var t = 0
            val gridCols: MutableList<Element> = tblGrid.elements("gridCol") as MutableList<Element>
            val colWidths = IntArray(gridCols.size)
            for (gridCol in gridCols) {
                val colWidth = (gridCol.attributeValue("w")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()
                if (colWidth > 0) {
                    colWidths[t++] = colWidth
                } else {
                    colWidths[t++] = (DEFAULT_CELL_WIDTH * MainConstant.POINT_TO_PIXEL).toInt()
                }
            }


            // row heights
            t = 0
            val trs: MutableList<Element> = tbl.elements("tr") as MutableList<Element>
            val rowHeights = IntArray(trs.size)
            for (tr in trs) {
                val rowHeight = (tr.attributeValue("h")
                    .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH).toInt()
                if (rowHeight > 0) {
                    rowHeights[t++] = rowHeight
                } else {
                    rowHeights[t++] = (DEFAULT_CELL_HEIGHT * MainConstant.POINT_TO_PIXEL).toInt()
                }
            }

            table = TableShape(rowHeights.size, colWidths.size)

            val tblPr = tbl.element("tblPr")
            val tableStyleId = tblPr.element("tableStyleId")

            var tableStyle: TableStyle? = null
            if (tableStyleId != null) {
                tableStyle = pgModel.getTableStyle(tableStyleId.getText())

                table.setFirstRow("1".equals(tblPr.attributeValue("firstRow"), ignoreCase = true))
                table.setLastRow("1".equals(tblPr.attributeValue("lastRow"), ignoreCase = true))

                table.setFirstCol("1".equals(tblPr.attributeValue("firstCol"), ignoreCase = true))
                table.setLastCol("1".equals(tblPr.attributeValue("lastCol"), ignoreCase = true))

                table.setBandRow("1".equals(tblPr.attributeValue("bandRow"), ignoreCase = true))
                table.setBandCol("1".equals(tblPr.attributeValue("bandCol"), ignoreCase = true))
            }

            processTable(
                control, zipPackage, packagePart, master,
                trs, rect, table, colWidths, rowHeights, tableStyle
            )
        }
        RunAttr.instance().setTable(false)
        return table
    }

    private fun processLine(
        control: IControl?,
        zipPackage: ZipPackage?,
        packagePart: PackagePart?,
        master: PGMaster?,
        tableStyle: TableStyle?,
        ln: Element?,
        styleColor: Int
    ): Line? {
        try {
            var lineFill: BackgroundAndFill? = null
            var lineWidth = 1
            var dash = false
            if (ln != null && ln.element("noFill") == null) {
                //line width
                if (ln.attributeValue("w") != null) {
                    lineWidth = Math.round(
                        ln.attributeValue("w")
                            .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH
                    )
                }

                val prstDash = ln.element("prstDash")
                if (prstDash != null && !"solid".equals(
                        prstDash.attributeValue("val"),
                        ignoreCase = true
                    )
                ) {
                    dash = true
                }

                lineFill = BackgroundReader.Companion.instance()
                    .processBackground(control!!, zipPackage!!, packagePart!!, master, ln)
            } else {
                lineFill = BackgroundAndFill()
                lineFill.setForegroundColor(styleColor)
            }

            val line = Line()
            line.setBackgroundAndFill(lineFill)
            line.setLineWidth(lineWidth)
            line.setDash(dash)

            return line
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(Exception::class)
    private fun processTable(
        control: IControl?, zipPackage: ZipPackage?, packagePart: PackagePart?, master: PGMaster?,
        trs: MutableList<Element>, rect: Rectangle, table: TableShape,
        colWidths: IntArray, rowHeights: IntArray, tableStyle: TableStyle?
    ) {
        // process by row
        var i = 0
        for (tr in trs) {
            var j = 0
            val tcs: MutableList<Element> = tr.elements("tc") as MutableList<Element>
            for (tc in tcs) {
                if (tc.attribute("hMerge") == null && tc.attribute("vMerge") == null) {
                    val cell = TableCell()


                    // anchor
                    val anchor = Rectanglef(rect.x.toFloat(), rect.y.toFloat(), 0f, 0f)
                    for (t in 0..<j) {
                        anchor.x = anchor.x + colWidths[t]
                    }

                    for (t in 0..<i) {
                        anchor.y = anchor.y + rowHeights[t]
                    }

                    var w = colWidths[j]
                    var h = rowHeights[i]
                    if (tc.attribute("rowSpan") != null) {
                        val rowSpan = tc.attributeValue("rowSpan").toInt()
                        for (t in 1..<rowSpan) {
                            h += rowHeights[i + t]
                        }
                    }
                    if (tc.attribute("gridSpan") != null) {
                        val gridSpan = tc.attributeValue("gridSpan").toInt()
                        for (t in 1..<gridSpan) {
                            w += colWidths[j + t]
                        }
                    }
                    anchor.width = w.toFloat()
                    anchor.height = h.toFloat()
                    cell.setBounds(anchor)

                    var cellStyle = getTableCellBorders(tableStyle, i, j, table)


                    // border and cell background
                    val tcPr = tc.element("tcPr")
                    if (tcPr != null) {
                        var temp = tcPr.element("lnL")
                        cell.setLeftLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, temp,
                                getTableCellLeftBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle!!,
                                    cellStyle!!
                                )
                            )
                        )

                        temp = tcPr.element("lnR")
                        cell.setRightLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, temp,
                                getTableCellRightBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle,
                                    cellStyle
                                )
                            )
                        )

                        temp = tcPr.element("lnT")
                        cell.setTopLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, temp,
                                getTableCellTopBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle,
                                    cellStyle
                                )
                            )
                        )

                        temp = tcPr.element("lnB")
                        cell.setBottomLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, temp,
                                getTableCellBottomBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle,
                                    cellStyle
                                )
                            )
                        )
                    } else if (cellStyle != null) {
                        cell.setLeftLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, null,
                                getTableCellLeftBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle!!,
                                    cellStyle
                                )
                            )
                        )

                        cell.setRightLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, null,
                                getTableCellRightBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle,
                                    cellStyle
                                )
                            )
                        )

                        cell.setTopLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, null,
                                getTableCellTopBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle,
                                    cellStyle
                                )
                            )
                        )

                        cell.setBottomLine(
                            processLine(
                                control, zipPackage, packagePart, master, tableStyle, null,
                                getTableCellBottomBorderColor(
                                    zipPackage,
                                    packagePart,
                                    master,
                                    tableStyle,
                                    cellStyle
                                )
                            )
                        )
                    } else {
                        val line = processLine(
                            control,
                            zipPackage,
                            packagePart,
                            master,
                            tableStyle,
                            null,
                            -0x1000000
                        )
                        cell.setLeftLine(line)
                        cell.setRightLine(line)
                        cell.setTopLine(line)
                        cell.setBottomLine(line)
                    }

                    var fill: BackgroundAndFill? = BackgroundReader.Companion.instance()
                        .processBackground(control!!, zipPackage!!, packagePart!!, master, tcPr)
                    if (fill == null && cellStyle != null) {
                        fill = getTableCellFill(
                            control,
                            zipPackage,
                            packagePart,
                            master,
                            tableStyle!!,
                            cellStyle
                        )
                    }
                    cell.setBackgroundAndFill(fill)


                    // text
                    val textBox = TextBox()
                    val r = Rectangle(
                        anchor.x.toInt(),
                        anchor.y.toInt(),
                        anchor.width.toInt(),
                        anchor.height.toInt()
                    )
                    textBox.setBounds(r)
                    if (tableStyle != null && (cellStyle == null || cellStyle.getFontAttributeSet() == null)) {
                        cellStyle = tableStyle.getWholeTable()
                    }
                    processCellSection(control, master, textBox, r, tc, cellStyle)
                    cell.setText(textBox)

                    table.addCell(i * colWidths.size + j, cell)
                }
                j++
            }
            i++
        }
    }

    private fun getTableCellFill(
        control: IControl?, zipPackage: ZipPackage?, packagePart: PackagePart?, master: PGMaster?,
        tableStyle: TableStyle, cellStyle: TableCellStyle
    ): BackgroundAndFill? {
        try {
            var fill = cellStyle.getTableCellBgFill()
            if (fill == null) {
                fill = tableStyle.getWholeTable()!!.getTableCellBgFill()
            }

            return BackgroundReader.Companion.instance()
                .processBackground(control!!, zipPackage!!, packagePart!!, master, fill, true)
        } catch (e: Exception) {
            return null
        }
    }

    private fun getTableCellLeftBorderColor(
        zipPackage: ZipPackage?, packagePart: PackagePart?, master: PGMaster?,
        tableStyle: TableStyle, cellStyle: TableCellStyle
    ): Int {
        try {
            var left: Element? = null
            var borders = cellStyle.getTableCellBorders()
            if (borders == null) {
                borders = tableStyle.getWholeTable()!!.getTableCellBorders()
                left = borders!!.getLeftBorder()
            } else {
                left = borders!!.getLeftBorder()
                if (left == null) {
                    left = tableStyle.getWholeTable()!!.getTableCellBorders()!!.getLeftBorder()
                }
            }

            return BackgroundReader.Companion.instance()
                .getBackgroundColor(zipPackage, packagePart, master, left, true)
        } catch (e: Exception) {
            return -0x1000000
        }
    }

    private fun getTableCellRightBorderColor(
        zipPackage: ZipPackage?, packagePart: PackagePart?, master: PGMaster?,
        tableStyle: TableStyle, cellStyle: TableCellStyle
    ): Int {
        try {
            var right: Element? = null
            var borders = cellStyle.getTableCellBorders()
            if (borders == null) {
                borders = tableStyle.getWholeTable()!!.getTableCellBorders()
                right = borders!!.getRightBorder()
            } else {
                right = borders!!.getRightBorder()
                if (right == null) {
                    right = tableStyle.getWholeTable()!!.getTableCellBorders()!!.getRightBorder()
                }
            }

            return BackgroundReader.Companion.instance()
                .getBackgroundColor(zipPackage, packagePart, master, right, true)
        } catch (e: Exception) {
            return -0x1000000
        }
    }

    private fun getTableCellTopBorderColor(
        zipPackage: ZipPackage?, packagePart: PackagePart?, master: PGMaster?,
        tableStyle: TableStyle, cellStyle: TableCellStyle
    ): Int {
        try {
            var top: Element? = null
            var borders = cellStyle.getTableCellBorders()
            if (borders == null) {
                borders = tableStyle.getWholeTable()!!.getTableCellBorders()
                top = borders!!.getTopBorder()
            } else {
                top = borders!!.getTopBorder()
                if (top == null) {
                    top = tableStyle.getWholeTable()!!.getTableCellBorders()!!.getTopBorder()
                }
            }

            return BackgroundReader.Companion.instance()
                .getBackgroundColor(zipPackage, packagePart, master, top, true)
        } catch (e: Exception) {
            return -0x1000000
        }
    }

    private fun getTableCellBottomBorderColor(
        zipPackage: ZipPackage?, packagePart: PackagePart?, master: PGMaster?,
        tableStyle: TableStyle, cellStyle: TableCellStyle
    ): Int {
        try {
            var bottom: Element? = null
            var borders = cellStyle.getTableCellBorders()
            if (borders == null) {
                borders = tableStyle.getWholeTable()!!.getTableCellBorders()
                bottom = borders!!.getBottomBorder()
            } else {
                bottom = borders!!.getBottomBorder()
                if (bottom == null) {
                    bottom = tableStyle.getWholeTable()!!.getTableCellBorders()!!.getBottomBorder()
                }
            }

            return BackgroundReader.Companion.instance()
                .getBackgroundColor(zipPackage, packagePart, master, bottom, true)
        } catch (e: Exception) {
            return -0x1000000
        }
    }

    private fun getTableCellBorders(
        tableStyle: TableStyle?,
        row: Int,
        col: Int,
        table: TableShape
    ): TableCellStyle? {
        var cellStyle: TableCellStyle? = null

        if (tableStyle == null) {
            return null
        }

        if (table.isFirstRow() && table.isFirstCol()) {
            cellStyle = getTableCellBorders_FirstRowFirstColumn(tableStyle, row, col, table)
        } else if (table.isFirstRow() && !table.isFirstCol()) {
            cellStyle = getTableCellBorders_FirstRow(tableStyle, row, col, table)
        } else if (!table.isFirstRow() && table.isFirstCol()) {
            cellStyle = getTableCellBorders_FirstColumn(tableStyle, row, col, table)
        } else if (!table.isFirstRow() && !table.isFirstCol()) {
            cellStyle = getTableCellBorders_NotFirstRowFirstColumn(tableStyle, row, col, table)
        }

        return cellStyle
    }

    /**
     * is FirstRow and FirstColumn property
     * @param tableStyle
     * @param row
     * @param col
     * @param table
     * @return
     */
    private fun getTableCellBorders_FirstRowFirstColumn(
        tableStyle: TableStyle,
        row: Int,
        col: Int,
        table: TableShape
    ): TableCellStyle? {
        var cellStyle: TableCellStyle? = null
        if (row == 0) {
            cellStyle = tableStyle.getFirstRow()
        } else if (table.isLastRow() && row == table.getRowCount() - 1) {
            cellStyle = tableStyle.getLastRow()
        } else if (col == 0) {
            cellStyle = tableStyle.getFirstCol()
        } else if (table.isLastCol() && col == table.getColumnCount() - 1) {
            cellStyle = tableStyle.getLastCol()
        } else if (table.isBandRow()) {
            if (row % 2 != 0) {
                cellStyle = tableStyle.getBand1H()
            } else if (table.isBandCol() && col % 2 != 0) {
                cellStyle = tableStyle.getBand1V()
            }
        } else if (table.isBandCol() && col % 2 != 0) {
            cellStyle = tableStyle.getBand1V()
        }

        if (cellStyle == null) {
            cellStyle = tableStyle.getWholeTable()
        }

        return cellStyle
    }

    /**
     * is just FirstRow property, not FirstCol property
     * @param tableStyle
     * @param row
     * @param col
     * @param table
     * @return
     */
    private fun getTableCellBorders_FirstRow(
        tableStyle: TableStyle,
        row: Int,
        col: Int,
        table: TableShape
    ): TableCellStyle? {
        var cellStyle: TableCellStyle? = null
        if (row == 0) {
            cellStyle = tableStyle.getFirstRow()
        } else if (table.isLastRow() && row == table.getRowCount() - 1) {
            cellStyle = tableStyle.getLastRow()
        } else if (table.isLastCol() && col == table.getColumnCount() - 1) {
            cellStyle = tableStyle.getLastCol()
        } else if (table.isBandRow()) {
            if (row % 2 != 0) {
                cellStyle = tableStyle.getBand1H()
            } else if (table.isBandCol() && col % 2 == 0) {
                cellStyle = tableStyle.getBand1V()
            }
        } else if (table.isBandCol() && col % 2 == 0) {
            cellStyle = tableStyle.getBand1V()
        }

        if (cellStyle == null) {
            cellStyle = tableStyle.getWholeTable()
        }

        return cellStyle
    }

    /**
     * is just FirstCol, not FirstRow
     * @param tableStyle
     * @param row
     * @param col
     * @param table
     * @return
     */
    private fun getTableCellBorders_FirstColumn(
        tableStyle: TableStyle,
        row: Int,
        col: Int,
        table: TableShape
    ): TableCellStyle? {
        var cellStyle: TableCellStyle? = null
        if (table.isLastRow() && row == table.getRowCount() - 1) {
            cellStyle = tableStyle.getLastRow()
        } else if (col == 0) {
            cellStyle = tableStyle.getFirstCol()
        } else if (table.isLastCol() && col == table.getColumnCount() - 1) {
            cellStyle = tableStyle.getLastCol()
        } else if (table.isBandRow()) {
            if (row % 2 == 0) {
                cellStyle = tableStyle.getBand1H()
            } else if (table.isBandCol() && col % 2 != 0) {
                cellStyle = tableStyle.getBand1V()
            }
        } else if (table.isBandCol() && col % 2 != 0) {
            cellStyle = tableStyle.getBand1V()
        }

        if (cellStyle == null) {
            cellStyle = tableStyle.getWholeTable()
        }

        return cellStyle
    }

    /**
     * is not FirstRow or FirstCol
     * @param tableStyle
     * @param row
     * @param col
     * @param table
     * @return
     */
    private fun getTableCellBorders_NotFirstRowFirstColumn(
        tableStyle: TableStyle,
        row: Int,
        col: Int,
        table: TableShape
    ): TableCellStyle? {
        var cellStyle: TableCellStyle? = null
        if (table.isLastRow() && row == table.getRowCount() - 1) {
            cellStyle = tableStyle.getLastRow()
        } else if (table.isLastCol() && col == table.getColumnCount() - 1) {
            cellStyle = tableStyle.getLastCol()
        } else if (table.isBandRow()) {
            if (row % 2 == 0) {
                cellStyle = tableStyle.getBand1H()
            } else if (table.isBandCol() && col % 2 == 0) {
                cellStyle = tableStyle.getBand1V()
            }
        } else if (table.isBandCol() && col % 2 == 0) {
            cellStyle = tableStyle.getBand1V()
        }

        if (cellStyle == null) {
            cellStyle = tableStyle.getWholeTable()
        }

        return cellStyle
    }

    /**
     * 
     * @param tb
     * @param rect
     * @param tc
     */
    fun processCellSection(
        control: IControl?,
        master: PGMaster?,
        tb: TextBox,
        rect: Rectangle,
        tc: Element,
        cellStyle: TableCellStyle?
    ) {
        // 建立章节
        val secElem = SectionElement()
        // 开始Offset
        secElem.setStartOffset(0)
        tb.setElement(secElem)
        // 属性
        val attr = secElem.getAttribute()
        // 宽度
        AttrManage.instance().setPageWidth(attr, (rect.width * MainConstant.PIXEL_TO_TWIPS).toInt())
        // 高度
        AttrManage.instance()
            .setPageHeight(attr, (rect.height * MainConstant.PIXEL_TO_TWIPS).toInt())

        var leftMargin = ShapeKit.DefaultMargin_Twip * 2
        var topMargin = ShapeKit.DefaultMargin_Twip
        var rightMargin = ShapeKit.DefaultMargin_Twip * 2
        var bottomMargin = ShapeKit.DefaultMargin_Twip

        val temp = tc.element("txBody")
        if (temp != null) {
            SectionAttr.Companion.instance()
                .setSectionAttribute(temp.element("bodyPr"), attr, null, null, true)
            // vertical alignment
            var `val`: String? = ""
            var verAlign = WPAttrConstant.PAGE_V_TOP
            val horAlign = WPAttrConstant.PAGE_H_LEFT

            val tcPr = tc.element("tcPr")
            if (tcPr != null) {
                //left margin
                if (tcPr.attributeValue("marL") != null) {
                    leftMargin = (tcPr.attributeValue("marL")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH * MainConstant.PIXEL_TO_TWIPS).toInt()
                }
                //top margin
                if (tcPr.attributeValue("marT") != null) {
                    topMargin = (tcPr.attributeValue("marT")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH * MainConstant.PIXEL_TO_TWIPS).toInt()
                }
                //right margin
                if (tcPr.attributeValue("marR") != null) {
                    rightMargin = (tcPr.attributeValue("marR")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH * MainConstant.PIXEL_TO_TWIPS).toInt()
                }
                //bottom margin
                if (tcPr.attributeValue("marB") != null) {
                    bottomMargin = (tcPr.attributeValue("marB")
                        .toInt() * MainConstant.PIXEL_DPI / MainConstant.EMU_PER_INCH * MainConstant.PIXEL_TO_TWIPS).toInt()
                }


                // 上边距
                AttrManage.instance().setPageMarginTop(attr, topMargin)
                // 下边距
                AttrManage.instance().setPageMarginBottom(attr, bottomMargin)
                // 左边距
                AttrManage.instance().setPageMarginLeft(attr, leftMargin)
                // 右边距
                AttrManage.instance().setPageMarginRight(attr, rightMargin)


                //alignment in vertical
                if ((tcPr.attributeValue("anchor").also { `val` = it }) != null) {
                    if (`val` == "t") {
                        verAlign = WPAttrConstant.PAGE_V_TOP
                    } else if (`val` == "ctr") {
                        verAlign = WPAttrConstant.PAGE_V_CENTER
                    } else if (`val` == "b") {
                        verAlign = WPAttrConstant.PAGE_V_BOTTOM
                    } else if (`val` == "just") {
                        verAlign = WPAttrConstant.PAGE_V_CENTER
                    } else if (`val` == "dist") {
                        verAlign = WPAttrConstant.PAGE_V_CENTER
                    }
                    AttrManage.instance().setPageVerticalAlign(attr, verAlign)
                }


                //alignment in horizontal
                if ((tcPr.attributeValue("anchorCtr").also { `val` = it }) != null) {
                    if (pptXmlBoolean(`val`)) {
                        AttrManage.instance()
                            .setPageHorizontalAlign(attr, WPAttrConstant.PAGE_H_CENTER)
                    }
                }
            }


            // wrap line
            val wrap = temp.element("bodyPr")
            if (wrap != null) {
                // 文本框内自动换行
                val value = wrap.attributeValue("wrap")
                tb.setWrapLine(value == null || "square".equals(value, ignoreCase = true))
            }

            val offset = processParagraph(control, master, secElem, temp, cellStyle)
            secElem.setEndOffset(offset.toLong())
        }
    }

    /**
     * 
     * @param secElem
     * @param txBody
     * @return
     */
    fun processParagraph(
        control: IControl?,
        master: PGMaster?,
        secElem: SectionElement,
        txBody: Element,
        cellStyle: TableCellStyle?
    ): Int {
        var offset = 0
        var lnSpcReduction = 0
        val bodyPr = txBody.element("bodyPr")
        if (bodyPr != null) {
            val normAutofit = bodyPr.element("normAutofit")
            if (normAutofit != null && normAutofit.attribute("lnSpcReduction") != null) {
                val `val` = normAutofit.attributeValue("lnSpcReduction")
                if (`val` != null && `val`.length > 0) {
                    lnSpcReduction = `val`.toInt()
                }
            }
        }
        val ps: MutableList<Element> = txBody.elements("p") as MutableList<Element>
        for (i in ps.indices) {
            val p = ps.get(i)
            val paraElem = ParagraphElement()
            paraElem.setStartOffset(offset.toLong())
            ParaAttr.instance().setParaAttribute(
                control, p.element("pPr"), paraElem.getAttribute(), null,
                -1, -1, lnSpcReduction, true, false
            )
            var attrLayout: IAttributeSet? = null
            if (cellStyle != null) {
                attrLayout = cellStyle.getFontAttributeSet()
            }
            offset = RunAttr.instance().processRun(master, paraElem, p, attrLayout, offset, 100, -1)


            // 处理以行为单位的段前段后
            ParaAttr.instance().processParaWithPct(p.element("pPr"), paraElem.getAttribute())
            // special settings of table
            if (i == 0) {
                AttrManage.instance().setParaBefore(paraElem.getAttribute(), 0)
            } else if (i == ps.size - 1) {
                AttrManage.instance().setParaAfter(paraElem.getAttribute(), 0)
            }

            paraElem.setEndOffset(offset.toLong())
            secElem.appendParagraph(paraElem, WPModelConstant.MAIN)
        }
        return offset
    }

    companion object {
        // default table cell width and height
        const val DEFAULT_CELL_WIDTH: Int = 100
        const val DEFAULT_CELL_HEIGHT: Int = 40

        private val kit = TableReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): TableReader {
            return kit
        }
    }
}
