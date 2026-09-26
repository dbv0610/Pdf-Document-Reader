package com.wxiwei.office.fc.xls.Reader.table

import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.ss.model.CellRangeAddress
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.table.SSTable
import com.wxiwei.office.ss.util.ReferenceUtil
import com.wxiwei.office.system.IControl

class TableReader private constructor() {
    companion object {
        private val reader = TableReader()

        @JvmStatic
        fun instance(): TableReader = reader
    }

    fun read(control: IControl, tablePart: PackagePart, sheet: Sheet) {
        val saxreader = SAXReader()
        try {
            val input = tablePart.inputStream
            val tableDocument = saxreader.read(input)
            input.close()
            val table = SSTable()
            val root = tableDocument.rootElement
            val reference = root.attributeValue("ref")
            val range = reference.split(":")
            if (range.size == 2) {
                table.setTableReference(
                    CellRangeAddress(
                        ReferenceUtil.instance().getRowIndex(range[0]),
                        ReferenceUtil.instance().getColumnIndex(range[0]),
                        ReferenceUtil.instance().getRowIndex(range[1]),
                        ReferenceUtil.instance().getColumnIndex(range[1])
                    )
                )
            }
            root.attributeValue("totalsRowDxfId")?.let { table.setTotalsRowDxfId(it.toInt()) }
            root.attributeValue("totalsRowBorderDxfId")?.let { table.setTotalsRowBorderDxfId(it.toInt()) }
            root.attributeValue("headerRowDxfId")?.let { table.setHeaderRowDxfId(it.toInt()) }
            root.attributeValue("headerRowBorderDxfId")?.let { table.setHeaderRowBorderDxfId(it.toInt()) }
            root.attributeValue("tableBorderDxfId")?.let { table.setTableBorderDxfId(it.toInt()) }

            if (root.attributeValue("headerRowCount").equals("0", ignoreCase = true)) {
                table.setHeaderRowShown(false)
            }
            val totalsRowCount = root.attributeValue("totalsRowCount") ?: "0"
            val totalsRowShown = root.attributeValue("totalsRowShown")
            if (!totalsRowShown.equals("0", ignoreCase = true) && totalsRowCount.equals("1", ignoreCase = true)) {
                table.setTotalRowShown(true)
            }

            val styleInfo = root.element("tableStyleInfo")
            if (styleInfo != null) {
                table.setName(styleInfo.attributeValue("name"))
                table.setShowFirstColumn(!styleInfo.attributeValue("showFirstColumn").equals("0", ignoreCase = true))
                table.setShowLastColumn(!styleInfo.attributeValue("showLastColumn").equals("0", ignoreCase = true))
                table.setShowRowStripes(!styleInfo.attributeValue("showRowStripes").equals("0", ignoreCase = true))
                table.setShowColumnStripes(!styleInfo.attributeValue("showColumnStripes").equals("0", ignoreCase = true))
                sheet.addTable(table)
            }
        } finally {
            saxreader.resetHandlers()
        }
    }
}
