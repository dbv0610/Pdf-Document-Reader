/*
 * 文件名称:          ASheet.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:55:20
 */
package com.wxiwei.office.ss.model.XLSModel

import android.graphics.Path
import android.graphics.PointF
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.common.pictureefftect.PictureEffectInfoFactory
import com.wxiwei.office.common.shape.AChart
import com.wxiwei.office.common.shape.ArbitraryPolygonShape
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.GroupShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.LineShape
import com.wxiwei.office.common.shape.PictureShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.fc.hssf.model.InternalSheet
import com.wxiwei.office.fc.hssf.record.BlankRecord
import com.wxiwei.office.fc.hssf.record.DefaultRowHeightRecord
import com.wxiwei.office.fc.hssf.record.EscherAggregate
import com.wxiwei.office.fc.hssf.record.HyperlinkRecord
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFAutoShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFChart
import com.wxiwei.office.fc.hssf.usermodel.HSSFChildAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFClientAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFFreeform
import com.wxiwei.office.fc.hssf.usermodel.HSSFLine
import com.wxiwei.office.fc.hssf.usermodel.HSSFPatriarch
import com.wxiwei.office.fc.hssf.usermodel.HSSFPicture
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFShapeGroup
import com.wxiwei.office.fc.hssf.usermodel.HSSFTextbox
import com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook
import com.wxiwei.office.fc.xls.ChartConverter
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.drawing.AnchorPoint
import com.wxiwei.office.ss.model.drawing.CellAnchor
import com.wxiwei.office.ss.model.sheetProperty.ColumnInfo
import com.wxiwei.office.ss.model.sheetProperty.PaneInformation
import com.wxiwei.office.ss.util.ModelUtil
import com.wxiwei.office.ss.util.SectionElementFactory
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.AbstractReader
import com.wxiwei.office.system.IControl
import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart
import com.wxiwei.office.thirdpart.achartengine.chart.RoundChart
import com.wxiwei.office.thirdpart.achartengine.chart.XYChart
import com.wxiwei.office.thirdpart.achartengine.renderers.DefaultRenderer

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-7-20
 * 负责人:           jqin
 */
open class ASheet
/**
 * Creates an HSSFSheet representing the given Sheet object.  Should only be
 * called by HSSFWorkbook when reading in an exisiting file.
 *
 * @param workbook - The HSSF Workbook object associated with the sheet.
 * @param sheet - lowlevel Sheet object this sheet will represent
 * @see HSSFWorkbook.createSheet
 */
(workbook: AWorkbook?, sheet: InternalSheet) : Sheet(), com.wxiwei.office.fc.ss.usermodel.Sheet {
    private var sheet: InternalSheet? = sheet
    private var initRowFinished = false

    init {
        book = workbook

        // merge range
        val count = sheet.getNumMergedRegions()
        for (i in 0 until count) {
            val range = sheet.getMergedRegionAt(i)
            addMergeRange(
                CellRangeAddress(
                    range.getFirstRow(),
                    range.getFirstColumn(), range.getLastRow(), range.getLastColumn()
                )
            )
        }

        // PaneInformation
        val pane = sheet.getPaneInformation()
        if (pane != null) {
            setPaneInformation(
                PaneInformation(
                    pane.getHorizontalSplitTopRow(),
                    pane.getVerticalSplitLeftColumn(), pane.isFreezePane()
                )
            )
        }

        // cloumn width, style, hidden
        val hssfColumnInfoList = sheet.getColumnInfo()
        if (hssfColumnInfoList != null) {
            val iter = hssfColumnInfoList.iterator()
            while (iter.hasNext()) {
                val hssfColumnInfo = iter.next()
                val columnInfo = ColumnInfo(
                    hssfColumnInfo.getFirstCol(),
                    hssfColumnInfo.getLastCol(),
                    (hssfColumnInfo.getColWidth() / 256.0 * SSConstant.COLUMN_CHAR_WIDTH * MainConstant.POINT_TO_PIXEL).toInt().toFloat(),
                    hssfColumnInfo.getStyle(),
                    hssfColumnInfo.isHidden()
                )

                addColumnInfo(columnInfo)
            }
        }
    }

    fun processSheet(iAbortListener: AbstractReader) {
        if (getSheetType() != Sheet.TYPE_CHARTSHEET && !initRowFinished) {
            processRowsAndCells(sheet!!, iAbortListener)

            //check merged cell validate(maybe missing some cells in merged region)
            processMergedCells()

            //hyperlink
            processHyperlinkfromSheet(sheet!!)

            initRowFinished = true
        }
    }

    /**
     * process the sheet's hyperlink
     * @param sheet
     */
    private fun processHyperlinkfromSheet(sheet: InternalSheet) {
        try {
            val it = sheet.getRecords().iterator()
            while (it.hasNext()) {
                val rec = it.next()
                if (rec is HyperlinkRecord) {
                    val linkRec = rec
                    val link = Hyperlink()
                    // Figure out the type
                    if (linkRec.isFileLink()) {
                        link.setLinkType(Hyperlink.LINK_FILE)
                    } else if (linkRec.isDocumentLink()) {
                        link.setLinkType(Hyperlink.LINK_DOCUMENT)
                    } else {
                        if (linkRec.getAddress() != null &&
                            linkRec.getAddress().startsWith("mailto:")
                        ) {
                            link.setLinkType(Hyperlink.LINK_EMAIL)
                        } else {
                            link.setLinkType(Hyperlink.LINK_URL)
                        }
                    }
                    link.setAddress(linkRec.getAddress())
                    link.setTitle(linkRec.getLabel())

                    var row = getRow(linkRec.getFirstRow())
                    if (row == null) {
                        val rowRec = RowRecord(linkRec.getFirstRow())
                        row = ARow(book!!, this, rowRec)
                        row.setRowPixelHeight(18f)

                        rows!![linkRec.getFirstRow()] = row
                    }

                    var cell = row.getCell(linkRec.getFirstColumn())
                    if (cell == null) {
                        val brec = BlankRecord()
                        brec.setRow(linkRec.getFirstRow())
                        brec.setColumn(linkRec.getFirstColumn().toShort())
                        brec.setXFIndex(row.getRowStyle().toShort())

                        cell = ACell(this, brec)
                        row.addCell(cell)
                    }
                    cell.setHyperLink(link)
                }
            }
        } catch (e: Exception) {
        }
    }

    /**
     * used internally to set the properties given a Sheet object
     */
    private fun processRowsAndCells(sheet: InternalSheet, iAbortListener: AbstractReader) {
        var row = sheet.getNextRow()
        @Suppress("UNUSED_VARIABLE")
        val rowRecordsAlreadyPresent = row != null
        //process rows
        while (row != null) {
            if (iAbortListener.isAborted()) {
                throw AbortReaderError("abort Reader")
            }
            createValidateRowFromRecord(row)
            row = sheet.getNextRow()
        }

        //create cells of all rows
        val iter = sheet.getCellValueIterator()
        var lastrow: ARow? = null
        // Add every cell to its row
        while (iter.hasNext()) {
            if (iAbortListener.isAborted()) {
                throw AbortReaderError("abort Reader")
            }

            val cval = iter.next()
            iter.remove()

            var hrow = lastrow
            if (hrow == null || hrow.getRowNumber() != cval.getRow()) {
                if (lastrow != null) {
                    lastrow.completed()
                }

                hrow = getRow(cval.getRow()) as ARow?
                lastrow = hrow
                if (hrow == null) {
                    // Some tools (like Perl module Spreadsheet::WriteExcel - bug 41187) skip the RowRecords
                    // Excel, OpenOffice.org and GoogleDocs are all OK with this, so POI should be too.
//                    if (rowRecordsAlreadyPresent)
//                    {
//                        // if at least one row record is present, all should be present.
//                        throw new RuntimeException(
//                            "Unexpected missing row when some rows already present");
//                    }
                    // create the row record on the fly now.
                    val rowRec = RowRecord(cval.getRow())
//                    sheet.addRow(rowRec);
                    hrow = createRowFromRecord(rowRec)
                }
            }
            hrow.createCellFromRecord(cval)
        }

        if (lastrow != null) {
            lastrow.completed()
        }
    }

    private fun isValidateRow(row: RowRecord): Boolean {
        if (row.getFirstCol() != row.getLastCol() || row.getHeight() != DefaultRowHeightRecord.DEFAULT_ROW_HEIGHT) {
            return true
        } else {
            var styleIndex = row.getXFIndex().toInt()
            if (styleIndex > book!!.getNumStyles()) {
                styleIndex = styleIndex and 0xFF
            }

            if (Workbook.isValidateStyle(book!!.getCellStyle(styleIndex))) {
                return true
            }
        }

        return false
    }

    /**
     *
     * @param rowRec
     * @return
     */
    private fun createValidateRowFromRecord(rowRec: RowRecord): ARow? {
        val row = getRow(rowRec.getRowNumber())
        if (row != null) {
            return row as ARow
        }

        if (isValidateRow(rowRec)) {
            val hrow = ARow(book!!, this, rowRec)
            addRow(hrow)
            return hrow
        } else {
            return null
        }
    }

    /**
     * Used internally to create a high level Row object from a low level row object.
     * USed when reading an existing file
     * @param rowRec  low level record to represent as a high level Row and add to sheet
     * @return HSSFRow high level representation
     */
    private fun createRowFromRecord(rowRec: RowRecord): ARow {
        val row = getRow(rowRec.getRowNumber())
        if (row != null) {
            return row as ARow
        }

        val hrow = ARow(book!!, this, rowRec)
        addRow(hrow)
        return hrow
    }

    /**
     * make sure merged region validate(not missing merged cells)
     */
    private fun processMergedCells() {
        var row: Row?
        var cell: Cell?
        val count = getMergeRangeCount()
        for (i in 0 until count) {
            val cr = getMergeRange(i)!!
            if (cr.getLastRow() - cr.getFirstRow() == Workbook.MAXROW_03 - 1
                || cr.getLastColumn() - cr.getFirstColumn() == Workbook.MAXCOLUMN_03 - 1
            ) {
                continue
            }

            for (j in cr.getFirstRow()..cr.getLastRow()) {
                row = getRow(j)
                if (row == null) {
                    val rowRec = RowRecord(j)
                    row = ARow(book!!, this, rowRec)
                    row.setRowPixelHeight(18f)
                    addRow(row)
                }

                for (k in cr.getFirstColumn()..cr.getLastColumn()) {
                    cell = row.getCell(k)
                    if (cell == null) {
                        val brec = BlankRecord()
                        brec.setRow(j)
                        brec.setColumn(k.toShort())
                        brec.setXFIndex(row.getRowStyle().toShort())

                        cell = ACell(this, brec)
                        row.addCell(cell)
                    }
                    cell.setRangeAddressIndex(i)
                }
            }
        }
    }

    /**
     * Returns the agregate escher records for this sheet,
     *  it there is one.
     * WARNING - calling this will trigger a parsing of the
     *  associated escher records. Any that aren't supported
     *  (such as charts and complex drawing types) will almost
     *  certainly be lost or corrupted when written out.
     */
    fun getDrawingEscherAggregate(sheet: InternalSheet?): EscherAggregate? {
        val internalWorkbook = (book as AWorkbook).getInternalWorkbook()!!
        internalWorkbook.findDrawingGroup()

        // If there's now no drawing manager, then there's
        //  no drawing escher records on the workbook
        if (internalWorkbook.getDrawingManager() == null) {
            return null
        }

        val found = sheet!!.aggregateDrawingRecords(internalWorkbook.getDrawingManager(), false)
        if (found == -1) {
            // Workbook has drawing stuff, but this sheet doesn't
            return null
        }

        // Grab our aggregate record, and wire it up
        val agg = sheet.findFirstRecordBySid(EscherAggregate.sid) as EscherAggregate?
        return agg
    }

    /**
     * Returns the top-level drawing patriach, if there is
     *  one.
     * This will hold any graphics or charts for the sheet.
     * WARNING - calling this will trigger a parsing of the
     *  associated escher records. Any that aren't supported
     *  (such as charts and complex drawing types) will almost
     *  certainly be lost or corrupted when written out. Only
     *  use this with simple drawings, otherwise call
     *  HSSFSheet.createDrawingPatriarch() and
     *  start from scratch!
     */
    fun getDrawingPatriarch(sheet: InternalSheet?): HSSFPatriarch? {
        val agg = getDrawingEscherAggregate(sheet)
        if (agg == null)
            return null

        val patriarch = HSSFPatriarch(this, agg)
        agg.setPatriarch(patriarch)

        // Have it process the records into high level objects
        //  as best it can do (this step may eat anything
        //  that isn't supported, you were warned...)
        agg.convertRecordsToUserModel(getAWorkbook())

        // Return what we could cope with
        return patriarch
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    fun rowIterator(): Iterator<Row> {
        // can this clumsy generic syntax be improved?
        val result: Iterator<Row> = rows!!.values.iterator()
        return result
    }

    /**
     *
     */
    private fun converFill(shape: HSSFShape?, control: IControl): BackgroundAndFill? {
        var bgFill: BackgroundAndFill? = null
        if (shape != null) {
            if (shape.isGradientTile()) {
                return shape.getGradientTileBackground(book as AWorkbook, control)
            }

            val type = shape.getFillType()
            if (type == BackgroundAndFill.FILL_PICTURE.toInt()) {
                val picData = shape.getBGPictureData()
                if (picData != null) {
                    val pic = Picture()
                    pic.setData(picData)
                    val picIndex = control.getSysKit().getPictureManage().addPicture(pic)
                    bgFill = BackgroundAndFill()
                    bgFill.setFillType(BackgroundAndFill.FILL_PICTURE)
                    bgFill.setPictureIndex(picIndex)
                }
            } else {
                bgFill = BackgroundAndFill()
                bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                bgFill.setForegroundColor(shape.getFillColor())
            }
        }
        return bgFill
    }

    fun processRotationAndFlip(shape: HSSFShape, autoShape: IShape) {
        var angle = shape.getRotation().toFloat()
        if (shape.getFlipH()) {
            autoShape.setFlipHorizontal(true)
            angle = -angle
        }
        if (shape.getFlipV()) {
            autoShape.setFlipVertical(true)
            angle = -angle
        }

        if (autoShape is LineShape) {
            if ((angle == 45f || angle == 135f || angle == 225f)
                && !autoShape.getFlipHorizontal()
                && !autoShape.getFlipVertical()
            ) {
                angle -= 90f
            }
        }

        autoShape.setRotation(angle)
    }

    /**
     *
     * @param control
     */
    fun processSheetShapes(control: IControl) {
        val type = getSheetType().toInt()
        if (type == Sheet.TYPE_WORKSHEET.toInt()) {
            var patriarch = getDrawingPatriarch(sheet)
            if (patriarch != null) {
                val shapeList = patriarch.getChildren()
                for (shape in shapeList) {
                    if ((book as AWorkbook).getAbstractReader()!!.isAborted()) {
                        throw AbortReaderError("abort Reader")
                    }

                    processShape(control, null, null, shape, null)
                }
                patriarch.dispose()
                @Suppress("UNUSED_VALUE")
                patriarch = null
            }

            sheet = null
        } else if (type == Sheet.TYPE_CHARTSHEET.toInt()) {
            if ((book as AWorkbook).getAbstractReader()!!.isAborted()) {
                throw AbortReaderError("abort Reader")
            }

            val chart = sheet!!.getChart()
            val achart = AChart()
            val abstractChart = ChartConverter.instance().converter(this, chart)
            if (abstractChart != null) {
                var renderer: DefaultRenderer? = null
                if (abstractChart is XYChart) {
                    renderer = abstractChart.getRenderer()
                } else if (abstractChart is RoundChart) {
                    renderer = abstractChart.getRenderer()
                }

                if (renderer != null) {
                    if (!chart.isNoBorder()) {
                        renderer.setChartFrame(chart.getLine())
                    }

//                     if (!chart.isNoFill())
//                     {
//                    	 renderer.setBackgroundAndFill(converFill(chart, control));
//                     }
                }

                achart.setAChart(abstractChart)
                shapesList!!.add(achart)
            }
        }
    }

    /**
     *
     * @param anchor
     * @return
     */
    private fun ClientAnchorToTwoCellAnchor(anchor: HSSFClientAnchor): CellAnchor {
        val from = AnchorPoint()
        val end = AnchorPoint()

        from.setColumn(anchor.getCol1())
        from.setRow(anchor.getRow1())

        end.setRow(anchor.getRow2())
        end.setColumn(anchor.getCol2())

        //dx
        var colWidth = getColumnPixelWidth(anchor.getCol1().toInt())
        from.setDX(Math.round(anchor.getDx1() / 1024f * colWidth))

        colWidth = getColumnPixelWidth(anchor.getCol2().toInt())
        end.setDX(Math.round(anchor.getDx2() / 1024f * colWidth))

        //dy
        var row = getRow(anchor.getRow1())
        var rowHeight = if (row == null) getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
        from.setDY(Math.round(anchor.getDy1() / 256f * rowHeight))

        row = getRow(anchor.getRow2())
        rowHeight = if (row == null) getDefaultRowHeight().toFloat() else row.getRowPixelHeight()
        end.setDY(Math.round(anchor.getDy2() / 256f * rowHeight))

        val cellAnchor = CellAnchor(CellAnchor.TWOCELLANCHOR)
        cellAnchor.setStart(from)
        cellAnchor.setEnd(end)

        return cellAnchor
    }

    private fun processShape(
        control: IControl, parent: GroupShape?, hssfParent: HSSFShapeGroup?,
        shape: HSSFShape, parentRect: Rectangle?
    ) {
        var rect: Rectangle? = null
        if (getSheetType() == Sheet.TYPE_WORKSHEET) {
            if (parent == null) {
                val anchor = shape.getAnchor() as HSSFClientAnchor?
                if (anchor == null) {
                    return
                }
                rect = ModelUtil.instance().getCellAnchor(this, ClientAnchorToTwoCellAnchor(anchor))
                if (rect != null) {
                    rect = ModelUtil.processRect(rect, shape.getRotation().toFloat())
                }
            } else {
                //
                val anchor = shape.getAnchor() as HSSFChildAnchor?
                if (anchor == null) {
                    return
                }
                rect = Rectangle()
                val hp = hssfParent!!
                val pr = parentRect!!
                rect.x = pr.x + Math.round((anchor.getDx1() - hp.getX1()) / (hp.getX2() - hp.getX1()).toFloat() * pr.width)
                rect.y = pr.y + Math.round((anchor.getDy1() - hp.getY1()) / (hp.getY2() - hp.getY1()).toFloat() * pr.height)
                rect.width = Math.round((anchor.getDx2() - anchor.getDx1()) / (hp.getX2() - hp.getX1()).toFloat() * pr.width)
                rect.height = Math.round((anchor.getDy2() - anchor.getDy1()) / (hp.getY2() - hp.getY1()).toFloat() * pr.height)

                rect = ModelUtil.processRect(rect, shape.getRotation().toFloat())
            }

            val type = shape.getShapeType()
            if (type != ShapeTypes.Line && type != ShapeTypes.StraightConnector1 && (rect!!.width == 0 || rect.height == 0)) {
                return
            }
        }

        if (shape is HSSFShapeGroup) {
            val groupShape = GroupShape()
            groupShape.setBounds(rect)
            val shapes = shape.getChildren()
            for (item in shapes) {
                processShape(control, groupShape, shape, item, rect)
            }

            if (parent == null) {
                shapesList!!.add(groupShape)
            } else {
                parent.appendShapes(groupShape)
            }
        } else {
            processSingleShape(control, parent, shape, rect)
        }
    }

    private fun processSingleShape(control: IControl, parent: GroupShape?, shape: HSSFShape, rect: Rectangle?) {
        if (shape is HSSFPicture) {
            val picture = shape
            val picData = picture.getPictureData()
            if (picData != null) {
                val data = picData.getData()
                if (data != null) {
                    val pic = Picture()
                    pic.setData(data)
                    var type = Picture.PNG
                    when (picData.getFormat()) {
                        HSSFWorkbook.PICTURE_TYPE_EMF -> type = Picture.EMF

                        HSSFWorkbook.PICTURE_TYPE_WMF -> type = Picture.WMF
                    }
                    pic.setPictureType(type)
                    val picIndex = control.getSysKit().getPictureManage().addPicture(pic)

                    val picShape = PictureShape()
                    picShape.setPictureIndex(picIndex)
                    picShape.setBounds(rect)
                    picShape.setPictureEffectInfor(PictureEffectInfoFactory.getPictureEffectInfor(picture.getEscherOptRecord()))
                    processRotationAndFlip(shape, picShape)
                    // border
                    if (!shape.isNoBorder()) {
                        picShape.setLine(shape.getLine())
                    }
                    if (!shape.isNoFill()) {
                        picShape.setBackgroundAndFill(converFill(shape, control))
                    }

                    if (parent == null) {
                        shapesList!!.add(picShape)
                    } else {
                        parent.appendShapes(picShape)
                    }
                }
            } else if (!shape.isNoBorder() || !shape.isNoFill()) {
                val autoShape = AutoShape(ShapeTypes.Rectangle)
                autoShape.setAuotShape07(false)
                autoShape.setBounds(rect)
                // border
                if (!shape.isNoBorder()) {
                    autoShape.setLine(shape.getLine())
                }
                if (!shape.isNoFill()) {
                    autoShape.setBackgroundAndFill(converFill(shape, control))
                }
                processRotationAndFlip(shape, autoShape)

                if (parent == null) {
                    shapesList!!.add(autoShape)
                } else {
                    parent.appendShapes(autoShape)
                }
            }
        } else if (shape is HSSFChart) {
            val chart = shape
//            if (ChartConverter.instance().getChartType(chart) != AbstractChart.CHART_UNKOWN)
            run {
                val achart = AChart()
                achart.setBounds(rect)

                val abstractChart: AbstractChart? = ChartConverter.instance().converter(this, chart)
                if (abstractChart != null) {
                    var renderer: DefaultRenderer? = null
                    if (abstractChart is XYChart) {
                        renderer = abstractChart.getRenderer()
                    } else if (abstractChart is RoundChart) {
                        renderer = abstractChart.getRenderer()
                    }

                    if (renderer != null) {
                        if (!chart.isNoBorder()) {
                            renderer.setChartFrame(chart.getLine())
                        }

                        if (!chart.isNoFill()) {
                            renderer.setBackgroundAndFill(converFill(chart, control))
                        }
                    }

                    achart.setAChart(abstractChart)
                    if (parent == null) {
                        shapesList!!.add(achart)
                    } else {
                        parent.appendShapes(achart)
                    }
                }
            }
        } else if (shape is HSSFLine) {
            if (!shape.isNoBorder()) {
                val lineShape = LineShape()
                lineShape.setAuotShape07(false)
                lineShape.setShapeType(shape.getShapeType())
                lineShape.setBounds(rect)
                lineShape.setLine(shape.getLine())

                val adj = shape.getAdjustmentValue()
                if (lineShape.getShapeType() == ShapeTypes.BentConnector2 && adj == null) {
                    lineShape.setAdjustData(arrayOf(1.0f))
                } else {
                    lineShape.setAdjustData(adj)
                }

                if (shape.getStartArrowType() > 0) {
                    lineShape.createStartArrow(
                        shape.getStartArrowType().toByte(),
                        shape.getStartArrowWidth(),
                        shape.getStartArrowLength()
                    )
                }

                if (shape.getEndArrowType() > 0) {
                    lineShape.createEndArrow(
                        shape.getEndArrowType().toByte(),
                        shape.getEndArrowWidth(),
                        shape.getEndArrowLength()
                    )
                }

                processRotationAndFlip(shape, lineShape)

                if (parent == null) {
                    shapesList!!.add(lineShape)
                } else {
                    parent.appendShapes(lineShape)
                }
            }
        } else if (shape is HSSFFreeform) {
            if (!shape.isNoBorder() || !shape.isNoFill()) {
                val arbitraryPolygonShape = ArbitraryPolygonShape()
                arbitraryPolygonShape.setShapeType(ShapeTypes.ArbitraryPolygon)
                arbitraryPolygonShape.setBounds(rect)
                val line = shape.getLine()

                var startArrowTailCenter: PointF? = null
                var endArrowTailCenter: PointF? = null

                val startArrowType = shape.getStartArrowType()
                if (startArrowType > 0) {
                    val arrowPathAndTail = shape.getStartArrowPath(rect)
                    if (arrowPathAndTail != null && arrowPathAndTail.arrowPath != null) {
                        startArrowTailCenter = arrowPathAndTail.arrowTailCenter

                        val pathExtend = ExtendPath()
                        pathExtend.path = arrowPathAndTail.arrowPath
                        pathExtend.setArrowFlag(true)
                        if (startArrowType != Arrow.Arrow_Arrow.toInt()) {
                            var fill: BackgroundAndFill? = null
                            if (shape.isNoFill()) {
                                fill = BackgroundAndFill()
                                fill.setFillType(BackgroundAndFill.FILL_SOLID)
                                fill.setForegroundColor(shape.getLineStyleColor())
                            } else if (line != null) {
                                fill = line.getBackgroundAndFill()
                            }
                            pathExtend.backgroundAndFill = fill
                        } else {
                            pathExtend.setLine(line)
                        }
                        arbitraryPolygonShape.appendPath(pathExtend)
                    }
                }

                val endArrowType = shape.getEndArrowType()
                if (endArrowType > 0) {
                    val arrowPathAndTail = shape.getEndArrowPath(rect)
                    if (arrowPathAndTail != null && arrowPathAndTail.arrowPath != null) {
                        endArrowTailCenter = arrowPathAndTail.arrowTailCenter

                        val pathExtend = ExtendPath()
                        pathExtend.path = arrowPathAndTail.arrowPath
                        pathExtend.setArrowFlag(true)
                        if (endArrowType != Arrow.Arrow_Arrow.toInt()) {
                            var fill: BackgroundAndFill? = null
                            if (shape.isNoFill()) {
                                fill = BackgroundAndFill()
                                fill.setFillType(BackgroundAndFill.FILL_SOLID)
                                fill.setForegroundColor(shape.getLineStyleColor())
                            } else if (line != null) {
                                fill = line.getBackgroundAndFill()
                            }
                            pathExtend.backgroundAndFill = fill
                        } else {
                            pathExtend.setLine(line)
                        }
                        arbitraryPolygonShape.appendPath(pathExtend)
                    }
                }

                val paths: Array<Path> = shape.getFreeformPath(
                    rect, startArrowTailCenter, startArrowType.toByte(),
                    endArrowTailCenter, endArrowType.toByte()
                )
                for (i in paths.indices) {
                    val pathExtend = ExtendPath()
                    pathExtend.path = paths[i]
                    if (!shape.isNoBorder()) {
                        pathExtend.setLine(line)
                    }
                    if (!shape.isNoFill()) {
                        pathExtend.backgroundAndFill = converFill(shape, control)
                    }
                    arbitraryPolygonShape.appendPath(pathExtend)
                }

                processRotationAndFlip(shape, arbitraryPolygonShape)

                if (parent == null) {
                    shapesList!!.add(arbitraryPolygonShape)
                } else {
                    parent.appendShapes(arbitraryPolygonShape)
                }
            }
        } else if (shape is HSSFAutoShape) {
            var autoShape: AutoShape? = null
            if (!shape.isNoBorder() || !shape.isNoFill()) {
                autoShape = AutoShape(shape.getShapeType())
                autoShape.setAuotShape07(false)
                autoShape.setBounds(rect)
                // border
                if (!shape.isNoBorder()) {
                    autoShape.setLine(shape.getLine())
                }
                if (!shape.isNoFill()) {
                    autoShape.setBackgroundAndFill(converFill(shape, control))
                }

                // adjust data
                if (shape.getShapeType() != ShapeTypes.TextBox) {
                    autoShape.setAdjustData(shape.getAdjustmentValue())
                }

                processRotationAndFlip(shape, autoShape)

                if (parent == null) {
                    shapesList!!.add(autoShape)
                } else {
                    parent.appendShapes(autoShape)
                }
            }

            // text
            val textbox = shape as HSSFTextbox
            val richTextString = textbox.getString()
            if (richTextString != null) {
                val str = richTextString.getString()
                if (str != null && str.length > 0) {
                    val tb = TextBox()
                    tb.setElement(SectionElementFactory.getSectionElement(book, textbox, rect))
                    tb.setWrapLine(textbox.isTextboxWrapLine())
                    tb.setBounds(rect)
                    processRotationAndFlip(shape, tb)

                    if (parent == null) {
                        shapesList!!.add(tb)
                    } else {
                        parent.appendShapes(tb)
                    }
                }
            }
        }
    }

    fun getAWorkbook(): AWorkbook? {
        return book as AWorkbook?
    }

    fun getInternalSheet(): InternalSheet? {
        return sheet
    }

    /**
     *
     */
    override fun dispose() {
        super.dispose()
        sheet!!.dispose()
        sheet = null
    }
}
