package com.wxiwei.office.fc.xls.Reader

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.SSConstant
import com.wxiwei.office.constant.wp.WPAttrConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.ppt.attribute.ParaAttr
import com.wxiwei.office.fc.ppt.attribute.RunAttr
import com.wxiwei.office.simpletext.model.*
import com.wxiwei.office.ss.model.baseModel.*
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.util.ReferenceUtil

class CellReader private constructor() {
    companion object {
        private const val CELLTYPE_BOOLEAN: Short = 0
        private const val CELLTYPE_NUMBER: Short = 1
        private const val CELLTYPE_ERROR: Short = 2
        private const val CELLTYPE_SHAREDSTRING: Short = 3
        private const val CELLTYPE_STRING: Short = 4
        private const val CELLTYPE_INLINESTRING: Short = 5
        private val reader = CellReader()
        @JvmStatic fun instance(): CellReader = reader
    }

    private var offset = 0
    private var paraElem: ParagraphElement? = null
    private var attrLayout: AttributeSetImpl? = null
    private var leaf: LeafElement? = null

    private fun isValidateCell(sheet: Sheet, cellElement: Element): Boolean {
        if (cellElement.attributeValue("t") != null || cellElement.element("v") != null || cellElement.element("f") != null) return true
        val book = sheet.getWorkbook() ?: return false
        val style = cellElement.attributeValue("s")
        if (style != null) return Workbook.isValidateStyle(book.getCellStyle(style.toInt()))
        val ref = cellElement.attributeValue("r")
        val col = ReferenceUtil.instance().getColumnIndex(ref)
        val row = sheet.getRow(ReferenceUtil.instance().getRowIndex(ref))
        return (row != null && Workbook.isValidateStyle(book.getCellStyle(row.getRowStyle()))) || Workbook.isValidateStyle(book.getCellStyle(col))
    }

    fun getCell(sheet: Sheet, cellElement: Element): Cell? {
        if (!isValidateCell(sheet, cellElement)) return null
        val type = getCellType(cellElement.attributeValue("t"))
        val cell = when (type) {
            CELLTYPE_BOOLEAN -> Cell(Cell.CELL_TYPE_BOOLEAN)
            CELLTYPE_NUMBER -> Cell(Cell.CELL_TYPE_NUMERIC)
            CELLTYPE_SHAREDSTRING, CELLTYPE_STRING, CELLTYPE_INLINESTRING -> Cell(Cell.CELL_TYPE_STRING)
            CELLTYPE_ERROR -> Cell(Cell.CELL_TYPE_ERROR)
            else -> Cell(Cell.CELL_TYPE_BLANK)
        }
        val ref = cellElement.attributeValue("r")
        cell.setColNumber(ReferenceUtil.instance().getColumnIndex(ref))
        cell.setRowNumber(ReferenceUtil.instance().getRowIndex(ref))
        val book = sheet.getWorkbook()!!
        val style = cellElement.attributeValue("s")?.toInt() ?: sheet.getColumnStyle(cell.getColNumber())
        cell.setCellStyle(style)
        // XLSX stores normal/shared-string values in <v>, but inline strings
        // are stored in <is><t>...</t></is>.  Reading only <v> creates a Cell
        // with a null value, which the renderer correctly skips as empty.
        val valueElement = if (type == CELLTYPE_INLINESTRING) {
            cellElement.element("is")
        } else {
            cellElement.element("v")
        }
        if (valueElement != null) {
            val value = valueElement.text
            when (type) {
                CELLTYPE_SHAREDSTRING -> {
                    val item = book.getSharedItem(value.toInt())
                    if (item is Element) {
                        cell.setSheet(sheet)
                        cell.setCellValue(book.addSharedString(processComplexSST(cell, item)))
                    } else cell.setCellValue(value.toInt())
                }
                CELLTYPE_STRING -> cell.setCellValue(book.addSharedString(value))
                CELLTYPE_INLINESTRING -> {
                    val textElement = valueElement.element("t")
                    val text = textElement?.text ?: value
                    cell.setCellValue(book.addSharedString(text))
                }
                CELLTYPE_NUMBER -> cell.setCellValue(value.toDouble())
                CELLTYPE_BOOLEAN -> cell.setCellValue(value.toInt() != 0)
                CELLTYPE_ERROR -> cell.setCellValue(value.toByteOrNull() ?: 0)
                else -> cell.setCellValue(value)
            }
        }
        cell.formula = SheetReader.instance().resolveFormula(sheet, cell, cellElement.element("f"))
        if (!cell.hasValidValue()) cell.setCellType(Cell.CELL_TYPE_BLANK)
        return cell
    }

    private fun getCellType(type: String?): Short = when {
        type == null || type.equals("n", true) -> CELLTYPE_NUMBER
        type.equals("b", true) -> CELLTYPE_BOOLEAN
        type.equals("s", true) -> CELLTYPE_SHAREDSTRING
        type.equals("str", true) -> CELLTYPE_STRING
        type.equals("inlineStr", true) -> CELLTYPE_INLINESTRING
        else -> CELLTYPE_ERROR
    }

    private fun processComplexSST(cell: Cell, si: Element): SectionElement {
        val section = SectionElement()
        section.setStartOffset(0)
        val attr = section.getAttribute()
        AttrManage.instance().setPageMarginLeft(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS).toInt())
        AttrManage.instance().setPageMarginRight(attr, Math.round(SSConstant.SHEET_SPACETOBORDER * MainConstant.PIXEL_TO_TWIPS).toInt())
        AttrManage.instance().setPageMarginTop(attr, 0)
        AttrManage.instance().setPageMarginBottom(attr, 0)
        val style = cell.getCellStyle()!!
        val vertical = when (style.getVerticalAlign()) {
            CellStyle.VERTICAL_CENTER -> WPAttrConstant.PAGE_V_CENTER
            CellStyle.VERTICAL_JUSTIFY -> WPAttrConstant.PAGE_V_JUSTIFIED
            CellStyle.VERTICAL_BOTTOM -> WPAttrConstant.PAGE_V_BOTTOM
            else -> WPAttrConstant.PAGE_V_TOP
        }
        AttrManage.instance().setPageVerticalAlign(attr, vertical)
        offset = 0
        paraElem = ParagraphElement()
        paraElem!!.setStartOffset(0)
        attrLayout = AttributeSetImpl()
        ParaAttr.instance().setParaAttribute(style, paraElem!!.getAttribute(), attrLayout)
        paraElem = processRun(cell, section, si, cell.getCellStyle()!!.getFontIndex().toInt())
        paraElem!!.setEndOffset(offset.toLong())
        section.appendParagraph(paraElem, WPModelConstant.MAIN)
        section.setEndOffset(offset.toLong())
        offset = 0
        paraElem = null
        attrLayout = null
        leaf = null
        return section
    }

    private fun processRun(cell: Cell, section: SectionElement, si: Element, fontId: Int): ParagraphElement {
        val book = cell.getSheet()!!.getWorkbook()!!
        val paragraph = paraElem!!
        val runs = si.elements()
        if (runs.isEmpty()) {
            leaf = LeafElement("\n")
            RunAttr.instance().setRunAttribute(book, fontId, null, leaf!!.getAttribute(), attrLayout)
            leaf!!.setStartOffset(offset.toLong())
            offset++
            leaf!!.setEndOffset(offset.toLong())
            paragraph.appendLeaf(leaf)
            return paragraph
        }
        for (raw in runs) {
            val run = raw as Element
            if (!run.name.equals("r", true)) continue
            val textElement = run.element("t") ?: continue
            var text = textElement.text
            if (!cell.getCellStyle()!!.isWrapText()) text = text.replace("\n", "")
            if (text.contains("\n") && cell.getCellStyle()!!.isWrapText()) {
                processBreakLine(cell, section, fontId, run, text)
            } else if (text.isNotEmpty()) {
                leaf = LeafElement(text)
                RunAttr.instance().setRunAttribute(book, fontId, run.element("rPr"), leaf!!.getAttribute(), attrLayout)
                leaf!!.setStartOffset(offset.toLong())
                offset += text.length
                leaf!!.setEndOffset(offset.toLong())
                paragraph.appendLeaf(leaf)
            }
        }
        if (leaf != null) {
            leaf!!.setText(leaf!!.getText(null) + "\n")
            offset++
        }
        return paragraph
    }

    private fun processBreakLine(cell: Cell, section: SectionElement, fontId: Int, run: Element, text: String) {
        if (text.isEmpty()) return
        val book = cell.getSheet()!!.getWorkbook()!!
        val items = text.split("\n")
        for (i in items.indices) {
            if (i > 0) {
                paraElem!!.setEndOffset(offset.toLong())
                section.appendParagraph(paraElem, WPModelConstant.MAIN)
                paraElem = ParagraphElement()
                paraElem!!.setStartOffset(offset.toLong())
                attrLayout = AttributeSetImpl()
                ParaAttr.instance().setParaAttribute(cell.getCellStyle(), paraElem!!.getAttribute(), attrLayout)
            }
            if (items[i].isNotEmpty()) {
                val value = items[i] + if (i < items.lastIndex) "\n" else ""
                leaf = LeafElement(value)
                RunAttr.instance().setRunAttribute(book, fontId, run.element("rPr"), leaf!!.getAttribute(), attrLayout)
                leaf!!.setStartOffset(offset.toLong())
                offset += value.length
                leaf!!.setEndOffset(offset.toLong())
                paraElem!!.appendLeaf(leaf)
            }
        }
    }

    fun searchContent(cellElement: Element, key: String): Boolean {
        val value = cellElement.element("v")
        return value != null && getCellType(cellElement.attributeValue("t")) != CELLTYPE_SHAREDSTRING && value.text.lowercase().contains(key)
    }
}
