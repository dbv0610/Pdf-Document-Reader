package com.wxiwei.office.fc.xls.Reader

import android.util.Log
import android.util.Xml
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.fc.dom4j.*
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.*
import com.wxiwei.office.fc.ppt.reader.PictureReader
import com.wxiwei.office.fc.xls.Reader.drawing.DrawingReader
import com.wxiwei.office.fc.xls.Reader.table.TableReader
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.*
import com.wxiwei.office.ss.model.sheetProperty.ColumnInfo
import com.wxiwei.office.ss.model.sheetProperty.PaneInformation
import com.wxiwei.office.ss.model.table.SSTable
import com.wxiwei.office.ss.util.ReferenceUtil
import com.wxiwei.office.system.*
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class SheetReader private constructor() {
    companion object {
        private val reader = SheetReader()
        private const val STREAMING_THRESHOLD = 1500
        private const val INITIAL_ROW_BATCH = 240
        private const val CONTINUATION_ROW_BATCH = 480
        @JvmStatic fun instance(): SheetReader = reader
    }

    @Throws(Exception::class)
    fun getSheet(control: IControl, zipPackage: ZipPackage, sheet: Sheet, sheetPart: PackagePart, iReader: IReader) {
        this.sheet = sheet
        this.iReader = iReader
        // The old DOM4J path built an XML Element for every worksheet node.
        // Large worksheets therefore exhausted the Android heap before the
        // model was complete. Parse the worksheet as a forward-only stream.
        streamInput = sheetPart.inputStream
        streamParser = Xml.newPullParser().also { it.setInput(streamInput, null) }
        streamTarget = sheet
        // Small sheets are parsed in one pass. Repeatedly changing the sheet
        // state and invalidating the view for a 200-row sheet only adds UI
        // work and makes scrolling feel worse. Large sheets retain the
        // progressive path to protect the heap.
        val complete = parseWorksheetStreaming(sheet, if (shouldReadInOnePass(sheetPart)) Int.MAX_VALUE else INITIAL_ROW_BATCH)
        if (!complete) {
            sheet.setState(Sheet.State_Reading)
            return
        }
        finishSheet(control, zipPackage, sheet, sheetPart)
    }

    @Throws(Exception::class)
    fun continueSheet(control: IControl, zipPackage: ZipPackage, sheet: Sheet, sheetPart: PackagePart, iReader: IReader): Boolean {
        this.sheet = sheet
        this.iReader = iReader
        if (streamTarget !== sheet) {
            streamInput = sheetPart.inputStream
            streamParser = Xml.newPullParser().also { it.setInput(streamInput, null) }
            streamTarget = sheet
        }
        val complete = parseWorksheetStreaming(sheet, CONTINUATION_ROW_BATCH)
        if (complete) finishSheet(control, zipPackage, sheet, sheetPart)
        return complete
    }

    fun isStreaming(sheet: Sheet): Boolean = streamTarget === sheet && streamParser != null

    private fun shouldReadInOnePass(sheetPart: PackagePart): Boolean {
        val input = sheetPart.inputStream
        var onePass = false
        try {
            val parser = Xml.newPullParser()
            parser.setInput(input, null)
            scan@ while (true) {
                when (parser.next()) {
                    XmlPullParser.END_DOCUMENT -> break@scan
                    XmlPullParser.START_TAG -> if (parser.name == "dimension") {
                        val ref = parser.attr("ref") ?: break@scan
                        val parts = ref.replace("$", "").split(":")
                        val first = cellAddress(parts.first()) ?: break@scan
                        val last = cellAddress(parts.last()) ?: break@scan
                        onePass = (last.first - first.first + 1) <= STREAMING_THRESHOLD &&
                            (last.second - first.second + 1) <= STREAMING_THRESHOLD
                        break@scan
                    }
                }
            }
        } finally {
            try { input.close() } catch (_: Exception) {}
        }
        return onePass
    }

    private fun cellAddress(value: String): Pair<Int, Int>? {
        val match = Regex("([A-Za-z]+)([0-9]+)").matchEntire(value.trim()) ?: return null
        val column = match.groupValues[1].uppercase().fold(0) { result, char ->
            result * 26 + (char - 'A' + 1)
        } - 1
        val row = match.groupValues[2].toIntOrNull()?.minus(1) ?: return null
        return row to column
    }

    private fun finishSheet(control: IControl, zipPackage: ZipPackage, sheet: Sheet, sheetPart: PackagePart) {
        val tableRelations = sheetPart.getRelationshipsByType(PackageRelationshipTypes.TABLE_PART)
        for (relation in tableRelations) TableReader.instance().read(control, zipPackage.getPart(relation.targetURI), sheet)
        val drawingRelations = sheetPart.getRelationshipsByType(PackageRelationshipTypes.DRAWING_PART)
        if (drawingRelations.size() > 0) {
            DrawingReader.instance().read(control, zipPackage, zipPackage.getPart(drawingRelations.getRelationship(0).targetURI), sheet)
        }
        DrawingReader.instance().processOLEPicture(control, zipPackage, sheetPart, sheet, null)
        PictureReader.instance().dispose()
        checkTableCell(sheet)
        sheet.setState(Sheet.State_Accomplished)
        OpenTrace.d("excel.sheet.accomplished sheet=${sheet.getSheetName()} rows=${sheet.getPhysicalNumberOfRows()}")
        dispose()
    }

    private fun parseWorksheetStreaming(target: Sheet, maxRows: Int): Boolean {
        val parser = streamParser ?: return true
        val input = streamInput ?: return true
        var row: Row? = null
        var cellRef: String? = null
        var cellType: String? = null
        var cellStyle = 0
        var cellText: StringBuilder? = null
        var formulaText: StringBuilder? = null
        var formulaSi: String? = null
        var captureFormula = false
        var captureValue = false
        var rowHasMetadata = false
        var rowsRead = 0

        try {
            while (true) {
                if (iReader?.isAborted() == true) throw AbortReaderError("abort Reader")
                when (parser.next()) {
                    XmlPullParser.END_DOCUMENT -> {
                        OpenTrace.d("excel.progress sheet=${target.getSheetName()} rows=${target.getPhysicalNumberOfRows()} lastRow=${target.getLastRowNum()}")
                        return true
                    }
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "sheetFormatPr" -> {
                            parser.attr("defaultRowHeight")?.let {
                                defaultRowHeight = (it.toDouble() * MainConstant.POINT_TO_PIXEL).toInt()
                                target.setDefaultRowHeight(defaultRowHeight)
                            }
                            parser.attr("defaultColWidth")?.let {
                                defaultColWidth = (it.toDouble() * SSConstant.COLUMN_CHAR_WIDTH * MainConstant.POINT_TO_PIXEL).toInt()
                                target.setDefaultColWidth(defaultColWidth)
                            }
                        }
                        "col" -> {
                            val min = parser.attr("min")?.toIntOrNull()?.minus(1) ?: 0
                            val max = parser.attr("max")?.toIntOrNull()?.minus(1) ?: min
                            val width = parser.attr("width")?.toDoubleOrNull()
                                ?.let { it * SSConstant.COLUMN_CHAR_WIDTH * MainConstant.POINT_TO_PIXEL }
                                ?: 0.0
                            val hidden = parser.attr("hidden")?.toIntOrNull() == 1
                            val style = parser.attr("style")?.toIntOrNull() ?: 0
                            target.addColumnInfo(ColumnInfo(min, max, width.toFloat(), style, hidden))
                        }
                        "pane" -> {
                            val pane = PaneInformation()
                            parser.attr("xSplit")?.toIntOrNull()?.let { pane.setVerticalSplitLeftColumn(it.toShort()) }
                            parser.attr("ySplit")?.toIntOrNull()?.let { pane.setHorizontalSplitTopRow(it.toShort()) }
                            target.setPaneInformation(pane)
                        }
                        "row" -> {
                            val rowIndex = parser.attr("r")?.toIntOrNull()?.minus(1) ?: 0
                            row = Row(parser.attr("spans")?.let { getEndBySpans(it) } ?: 0)
                            row!!.setRowNumber(rowIndex)
                            row!!.setSheet(target)
                            row!!.setRowPixelHeight(
                                parser.attr("ht")?.toFloatOrNull()?.times(MainConstant.POINT_TO_PIXEL)
                                    ?: defaultRowHeight.toFloat()
                            )
                            val hidden = parser.attr("hidden")?.toIntOrNull() == 1
                            row!!.setZeroHeight(hidden)
                            row!!.setRowStyle(parser.attr("s")?.toIntOrNull() ?: 0)
                            rowHasMetadata = parser.attr("ht") != null || hidden || parser.attr("s") != null
                        }
                        "c" -> {
                            cellRef = parser.attr("r")
                            cellType = parser.attr("t")
                            val col = ReferenceUtil.instance().getColumnIndex(cellRef ?: "A1")
                            cellStyle = parser.attr("s")?.toIntOrNull() ?: target.getColumnStyle(col)
                            cellText = StringBuilder()
                            formulaText = null
                            formulaSi = null
                        }
                        "f" -> { formulaText = StringBuilder(); formulaSi = parser.attr("si"); captureFormula = true }
                        "v", "t" -> if (cellText != null) captureValue = true
                        "mergeCell" -> parser.attr("ref")?.let { addMergeRange(target, it) }
                    }
                    XmlPullParser.TEXT, XmlPullParser.CDSECT -> if (captureValue) {
                        cellText?.append(parser.text)
                    } else if (captureFormula) { formulaText?.append(parser.text) }
                    XmlPullParser.END_TAG -> when (parser.name) {
                        "f" -> captureFormula = false
                        "v", "t" -> captureValue = false
                        "c" -> {
                            val ref = cellRef
                            val text = cellText?.toString()
                            if (row != null && ref != null && text != null) {
                                val cell = Cell(cellTypeToModelType(cellType))
                                cell.setSheet(target)
                                cell.setRowNumber(ReferenceUtil.instance().getRowIndex(ref))
                                cell.setColNumber(ReferenceUtil.instance().getColumnIndex(ref))
                                cell.setCellStyle(cellStyle)
                                val workbook = target.getWorkbook()!!
                                if (text.isEmpty()) {
                                    cell.setCellType(Cell.CELL_TYPE_BLANK)
                                } else when (cellType) {
                                    "s" -> cell.setCellValue(text.toIntOrNull() ?: -1)
                                    "str", "inlineStr", "d" -> cell.setCellValue(workbook.addSharedString(text))
                                    "b" -> cell.setCellValue(text == "1")
                                    "e" -> cell.setCellValue(text.toByteOrNull() ?: 0)
                                    else -> {
                                        val number = text.toDoubleOrNull()
                                        if (number != null) cell.setCellValue(number) else {
                                            cell.setCellType(Cell.CELL_TYPE_STRING)
                                            cell.setCellValue(workbook.addSharedString(text))
                                        }
                                    }
                                }
                                cell.formula = formulaText?.let { resolveFormula(target, cell, it.toString(), formulaSi) }
                                row!!.addCell(cell)
                            }
                            cellRef = null
                            cellType = null
                            cellText = null
                            captureValue = false
                        }
                        "row" -> {
                            val completedRow = row
                            if (completedRow != null &&
                                (completedRow.getPhysicalNumberOfCells() > 0 || rowHasMetadata)) {
                                completedRow.completed()
                                target.addRow(completedRow)
                            }
                            row = null
                            rowHasMetadata = false
                            rowsRead++
                            if (rowsRead >= maxRows) {
                                OpenTrace.d("excel.progress sheet=${target.getSheetName()} rows=${target.getPhysicalNumberOfRows()} lastRow=${target.getLastRowNum()}")
                                return false
                            }
                        }
                    }
                }
            }
        } catch (error: Throwable) {
            try { input.close() } catch (_: Exception) {}
            streamInput = null
            streamParser = null
            streamTarget = null
            throw error
        }
        return true
    }

    private fun cellTypeToModelType(type: String?): Short = when (type) {
        "s", "str", "inlineStr", "d" -> Cell.CELL_TYPE_STRING
        "b" -> Cell.CELL_TYPE_BOOLEAN
        "e" -> Cell.CELL_TYPE_ERROR
        else -> Cell.CELL_TYPE_NUMERIC
    }

    private fun addMergeRange(target: Sheet, ref: String) {
        val parts = ref.split(":")
        if (parts.size != 2) return
        val range = CellRangeAddress(
            ReferenceUtil.instance().getRowIndex(parts[0]),
            ReferenceUtil.instance().getColumnIndex(parts[0]),
            ReferenceUtil.instance().getRowIndex(parts[1]),
            ReferenceUtil.instance().getColumnIndex(parts[1])
        )
        val index = target.addMergeRange(range) - 1
        for (rowIndex in range.getFirstRow()..range.getLastRow()) {
            var row = target.getRow(rowIndex)
            if (row == null) {
                row = Row(range.getLastColumn() - range.getFirstColumn() + 1)
                row.setSheet(target)
                row.setRowNumber(rowIndex)
                target.addRow(row)
            }
            for (columnIndex in range.getFirstColumn()..range.getLastColumn()) {
                var cell = row.getCell(columnIndex, false)
                if (cell == null) {
                    cell = Cell(Cell.CELL_TYPE_BLANK)
                    cell.setRowNumber(rowIndex)
                    cell.setColNumber(columnIndex)
                    cell.setSheet(target)
                    cell.setCellStyle(row.getRowStyle())
                    row.addCell(cell)
                }
                cell.setRangeAddressIndex(index)
            }
        }
    }

    private fun XmlPullParser.attr(name: String): String? = getAttributeValue(null, name)

    private fun stringToInt(number: String): Int {
        try { return number.toInt() } catch (e: NumberFormatException) { Log.e("HungHoai", "stringToInt: $number") }
        try { return number.toFloat().toInt() } catch (e: NumberFormatException) { Log.e("HungHoai", "stringToInt: $number") }
        return 0
    }

    @Throws(Exception::class)
    private fun getSheetHyperlinkByRelation(sheetPart: PackagePart): MutableMap<String, String> {
        val relations = sheetPart.getRelationshipsByType(PackageRelationshipTypes.HYPERLINK_PART)
        val result = HashMap<String, String>(relations.size())
        for (relation in relations) result[relation.id] = relation.targetURI.toString()
        return result
    }

    private fun getSheetHyperlink(sheet: Sheet, targets: Map<String, String>, hyperlinks: Element?) {
        if (hyperlinks == null) return
        val iterator = hyperlinks.elementIterator()
        while (iterator.hasNext()) {
            val element = iterator.next() as Element
            val row = sheet.getRow(ReferenceUtil.instance().getRowIndex(element.attributeValue("ref")))
            val cell = row?.getCell(ReferenceUtil.instance().getColumnIndex(element.attributeValue("ref"))) ?: continue
            val hyperlink = Hyperlink()
            val target = targets[element.attributeValue("id")]
            val address: String?
            if (target == null) {
                hyperlink.setLinkType(Hyperlink.LINK_DOCUMENT)
                address = element.attributeValue("location")
            } else {
                hyperlink.setLinkType(if (target.contains("mailto")) Hyperlink.LINK_EMAIL else if (target.contains("http")) Hyperlink.LINK_URL else Hyperlink.LINK_FILE)
                address = target
            }
            hyperlink.setAddress(address)
            cell.setHyperLink(hyperlink)
        }
    }

    private fun setColumnProperty(col: Element) {
        val min = col.attributeValue("min").toInt() - 1
        val max = col.attributeValue("max").toInt() - 1
        val width = col.attributeValue("width")?.toDouble()?.let { it * SSConstant.COLUMN_CHAR_WIDTH * MainConstant.POINT_TO_PIXEL } ?: 0.0
        // Missing hidden means visible.  `null != 0` was evaluating to true,
        // which hid every column in normal XLSX files.
        val hidden = col.attributeValue("hidden")?.toIntOrNull() == 1
        val style = col.attributeValue("style")?.toInt() ?: 0
        sheet!!.addColumnInfo(ColumnInfo(min, max, width.toFloat(), style, hidden))
    }

    private fun getSheetMergerdCells(mergedCell: Element) {
        val range = getCellRangeAddress(mergedCell.attributeValue("ref"))
        if (range.getLastRow() - range.getFirstRow() == Workbook.MAXROW_07 - 1 || range.getLastColumn() - range.getFirstColumn() == Workbook.MAXCOLUMN_07 - 1) return
        val currentSheet = sheet!!
        val index = currentSheet.addMergeRange(range) - 1
        for (i in range.getFirstRow()..range.getLastRow()) {
            var row = currentSheet.getRow(i)
            if (row == null) {
                row = Row(range.getLastColumn() - range.getFirstColumn())
                row.setSheet(currentSheet)
                row.setRowNumber(i)
                currentSheet.addRow(row)
            }
            for (j in range.getFirstColumn()..range.getLastColumn()) {
                var cell = row.getCell(j)
                if (cell == null) {
                    cell = Cell(Cell.CELL_TYPE_BLANK)
                    cell.setRowNumber(i)
                    cell.setColNumber(j)
                    cell.setSheet(currentSheet)
                    cell.setCellStyle(row.getRowStyle())
                    row.addCell(cell)
                }
                cell.setRangeAddressIndex(index)
            }
        }
    }

    private fun getCellRangeAddress(region: String): CellRangeAddress {
        val parts = region.split(":")
        return CellRangeAddress(ReferenceUtil.instance().getRowIndex(parts[0]), ReferenceUtil.instance().getColumnIndex(parts[0]), ReferenceUtil.instance().getRowIndex(parts[1]), ReferenceUtil.instance().getColumnIndex(parts[1]))
    }

    private fun checkTableCell(sheet: Sheet) {
        val tables = sheet.getTables() ?: return
        for (table in tables) {
            val range = table.getTableReference() ?: continue
            for (i in range.getFirstRow()..range.getLastRow()) {
                var row = sheet.getRow(i)
                if (row == null) {
                    row = Row(range.getLastColumn() - range.getFirstColumn() + 1)
                    row.setSheet(sheet)
                    row.setRowNumber(i)
                    row.setFirstCol(range.getFirstColumn())
                    row.setLastCol(range.getLastColumn())
                    row.setInitExpandedRangeAddress(true)
                    sheet.addRow(row)
                }
                for (j in range.getFirstColumn()..range.getLastColumn()) {
                    var cell = row.getCell(j)
                    if (cell == null) {
                        cell = Cell(Cell.CELL_TYPE_BLANK)
                        cell.setColNumber(j)
                        cell.setRowNumber(row.getRowNumber())
                        cell.setSheet(sheet)
                        cell.setCellStyle(row.getRowStyle())
                        row.addCell(cell)
                    }
                    cell.setTableInfo(table)
                }
            }
        }
    }

    @Throws(Exception::class)
    fun searchContent(zipPackage: ZipPackage, iReader: IReader, sheetPart: PackagePart, key: String): Boolean {
        this.key = key
        searched = false
        this.iReader = iReader
        val saxReader = SAXReader()
        try {
            saxReader.addHandler("/worksheet/sheetData/row/c", XLSXSearchSaxHandler())
            val input = sheetPart.inputStream
            saxReader.read(input)
            input.close()
        } catch (e: StopReaderError) {
            return true
        } finally {
            saxReader.resetHandlers()
        }
        return searched
    }

    private inner class XLSXSaxHandler : ElementHandler {
        override fun onStart(elementPath: ElementPath) {}
        override fun onEnd(elementPath: ElementPath) {
            if (iReader?.isAborted() == true) throw AbortReaderError("abort Reader")
            val elem = elementPath.current
            when (elem.name) {
                "sheetFormatPr" -> {
                    elem.attributeValue("defaultRowHeight")?.let { defaultRowHeight = (it.toDouble() * MainConstant.POINT_TO_PIXEL).toInt(); sheet!!.setDefaultRowHeight(defaultRowHeight) }
                    elem.attributeValue("defaultColWidth")?.let { defaultColWidth = (it.toDouble() * SSConstant.COLUMN_CHAR_WIDTH * MainConstant.POINT_TO_PIXEL).toInt(); sheet!!.setDefaultColWidth(defaultColWidth) }
                }
                "col" -> setColumnProperty(elem)
                "row" -> {
                    val rowIndex = elem.attributeValue("r").toInt() - 1
                    val currentSheet = sheet!!
                    val old = currentSheet.getRow(rowIndex)
                    if (old == null) currentSheet.addRow(createRow(elem, defaultRowHeight)) else modifyRow(old, elem, defaultRowHeight)
                }
                "c" -> {
                    val ref = elem.attributeValue("r")
                    val rowIndex = ReferenceUtil.instance().getRowIndex(ref)
                    val colIndex = ReferenceUtil.instance().getColumnIndex(ref)
                    val currentSheet = sheet!!
                    var row = currentSheet.getRow(rowIndex)
                    var cell: Cell? = row?.getCell(colIndex, false)
                    if (row == null) { row = Row(colIndex); row.setRowNumber(rowIndex); row.setSheet(currentSheet); currentSheet.addRow(row) }
                    if (cell == null) cell = CellReader.instance().getCell(currentSheet, elem)
                    if (cell != null) { cell.setSheet(currentSheet); row.addCell(cell) }
                }
                "mergeCell" -> getSheetMergerdCells(elem)
            }
            elem.detach()
        }
    }

    private fun isValidateRow(rowElement: Element): Boolean {
        if (rowElement.attributeValue("ht") != null) return true
        val style = rowElement.attributeValue("s")?.toInt() ?: return false
        return Workbook.isValidateStyle(sheet!!.getWorkbook()!!.getCellStyle(style))
    }

    private fun createRow(rowElement: Element, defaultRowHeight: Int): Row? {
        if (!isValidateRow(rowElement)) return null
        val rowIndex = rowElement.attributeValue("r").toInt() - 1
        val height = rowElement.attributeValue("ht")?.let { it.toFloat() * MainConstant.POINT_TO_PIXEL } ?: defaultRowHeight.toFloat()
        // The attribute is optional; only an explicit hidden="1" hides a row.
        val hidden = rowElement.attributeValue("hidden")?.toIntOrNull() == 1
        val style = rowElement.attributeValue("s")?.toInt() ?: 0
        val row = Row(getEndBySpans(rowElement.attributeValue("spans")))
        row.setRowNumber(rowIndex)
        row.setRowPixelHeight(height.toFloat())
        row.setZeroHeight(hidden)
        row.setSheet(sheet)
        row.setRowStyle(style)
        row.completed()
        return row
    }

    private fun modifyRow(row: Row, rowElement: Element, defaultRowHeight: Int) {
        val height = rowElement.attributeValue("ht")?.let { (it.toDouble() * MainConstant.POINT_TO_PIXEL).toInt() } ?: defaultRowHeight
        row.setRowPixelHeight(height.toFloat())
        row.setZeroHeight(rowElement.attributeValue("hidden")?.toIntOrNull() == 1)
        row.setRowStyle(rowElement.attributeValue("s")?.toInt() ?: 0)
        row.completed()
    }

    private fun getEndBySpans(spans: String?): Int {
        if (spans == null) return 0
        val last = spans.split(" ").last().split(":")
        return last[1].toInt(16) - 1
    }

    private inner class XLSXSearchSaxHandler : ElementHandler {
        override fun onStart(elementPath: ElementPath) {}
        override fun onEnd(elementPath: ElementPath) {
            if (iReader?.isAborted() == true) throw AbortReaderError("abort Reader")
            val elem = elementPath.current
            if (elem.name == "c" && CellReader.instance().searchContent(elem, key ?: "")) searched = true
            elem.detach()
            if (searched) throw StopReaderError("stop")
        }
    }

    fun dispose() {
        try { streamInput?.close() } catch (_: Exception) {}
        streamInput = null
        streamParser = null
        streamTarget = null
        sheet = null
        iReader = null
        key = null
    }

    private data class Master(val text: String, val row: Int, val col: Int)
    private val sharedFormulas = java.util.WeakHashMap<Sheet, MutableMap<String, Master>>()
    fun resolveFormula(sheet: Sheet, cell: Cell, element: Element?): String? {
        if (element == null) return null
        return resolveFormula(sheet, cell, element.text, element.attributeValue("si"))
    }
    private fun resolveFormula(sheet: Sheet, cell: Cell, text: String, si: String?): String {
        if (si == null) return text
        val masters = sharedFormulas.getOrPut(sheet) { HashMap() }
        if (text.isNotEmpty()) {
            masters[si] = Master(text, cell.getRowNumber(), cell.getColNumber())
            return text
        }
        val master = masters[si] ?: return "#REF!"
        return com.wxiwei.office.editor.xlsx.A1FormulaShifter.shift(master.text,
            cell.getRowNumber() - master.row, cell.getColNumber() - master.col)
    }

    private var sheet: Sheet? = null
    private var iReader: IReader? = null
    private var defaultRowHeight = 0
    private var defaultColWidth = 0
    private var key: String? = null
    private var searched = false
    private var streamInput: InputStream? = null
    private var streamParser: XmlPullParser? = null
    private var streamTarget: Sheet? = null

}
