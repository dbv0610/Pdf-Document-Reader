/*
 * 文件名称:          CellLeafElement.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:43:29
 */
package com.wxiwei.office.simpletext.model

import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Workbook

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2012-4-27
 *
 * 负责人:           jqin
 */
class CellLeafElement(cell: Cell, private val offStart: Int, private val offEnd: Int) : LeafElement(null) {

    private var book: Workbook? = cell.getSheet()!!.getWorkbook()

    private val sharedStringIndex: Int = cell.getStringCellValueIndex()

    private var appendNewline = false

    /**
     *
     */
    override fun getText(doc: IDocument?): String? {
        if (appendNewline) {
            return book!!.getSharedString(sharedStringIndex)!!.substring(offStart, offEnd) + "\n"
        } else {
            return book!!.getSharedString(sharedStringIndex)!!.substring(offStart, offEnd)
        }
    }

//    /**
//     *
//     *
//     */
//    public long getEndOffset()
//    {
//        if(appendNewline)
//        {
//            return end + 1;
//        }
//        else
//        {
//            return end;
//        }
//
//    }

    /**
     *
     */
    fun appendNewlineFlag() {
        appendNewline = true
    }

    /**
     *
     */
    override fun dispose() {
        book = null
    }
}
