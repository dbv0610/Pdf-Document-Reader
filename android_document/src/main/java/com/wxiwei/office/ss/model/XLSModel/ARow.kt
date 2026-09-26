/*
 * 文件名称:          ARow.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.ss.model.XLSModel

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.baseModel.Row
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-7-23
 * 负责人:           jqin
 */
open class ARow
/**
 * Creates an ARow from a low level RowRecord object.  Only ASheet should do
 * this.  ASheet uses this when an existing file is read in.
 *
 * @param book low-level Workbook object containing the sheet that contains this row
 * @param sheet low-level Sheet object that contains this Row
 * @param record the low level api object this row should represent
 * @see com.wxiwei.office.fc.hssf.usermodel.HSSFSheet.createRow
 */
(book: Workbook, sheet: Sheet?, record: RowRecord) :
    Row(record.getLastCol() - record.getFirstCol() + INITIAL_CAPACITY) {

    init {
        setSheet(sheet)
        record.setEmpty()

        // row number
        rowNumber = record.getRowNumber()

        // first column
        firstCol = record.getFirstCol()

        // last column
        lastCol = Math.max(lastCol, record.getLastCol())

        this.styleIndex = record.getXFIndex().toInt()
//        if((styleIndex & 0xFFFF) > book.getNumStyles())
//        {
//            styleIndex = 15;
//        }
        var t = 0
        while ((styleIndex and (0xFFFF shr t)) > book.getNumStyles()) {
            t += 1
        }
        styleIndex = styleIndex and (0xFFFF shr t)

        // zero height
        setZeroHeight(record.getZeroHeight())

        // row height of pixel
        var height = record.getHeight()
        //The low-order 15 bits contain the row height.
        //The 0x8000 bit indicates that the row is standard height (optional)
        if ((height.toInt() and 0x8000) != 0) {
            height = 0xFF
        } else {
            height = (height.toInt() and 0x7FFF).toShort()
        }
        setRowPixelHeight((height / 20 * MainConstant.POINT_TO_PIXEL).toInt().toFloat())
    }

    private fun isValidateCell(cval: CellValueRecordInterface): Boolean {
        val cellType = ACell.determineType(cval)
        if (cellType != Cell.CELL_TYPE_BLANK.toInt()) {
            return true
        }

        val book = sheet!!.getWorkbook()!!
        return (Workbook.isValidateStyle(book.getCellStyle(cval.getXFIndex().toInt()))
                || Workbook.isValidateStyle(book.getCellStyle(getRowStyle()))
                || Workbook.isValidateStyle(book.getCellStyle(sheet!!.getColumnStyle(cval.getColumn().toInt()))))
    }

    /**
     * create a high level ACell object from an existing low level record.  Should
     * only be called from ASheet or AFRow itself.
     * @param cellRec low level cell to create the high level representation from
     * @return ACell representing the low level record passed in
     */
    @Suppress("UNCHECKED_CAST")
    fun createCellFromRecord(cellRec: CellValueRecordInterface): ACell? {
        // NOTE: the original Java looked the cell up with a boxed Short key
        // (cells.get(cellRec.getColumn())) in a Hashtable<Integer, Cell>, which never matches;
        // the lookup is kept identical here to preserve behavior.
        val cell = (cells as Map<Any?, Cell?>)[cellRec.getColumn()]
        if (cell != null) {
            return cell as ACell
        }

        if (isValidateCell(cellRec)) {
            val acell = ACell(sheet, cellRec)
            val colIx = cellRec.getColumn().toInt()
            if (colIx < firstCol) {
                firstCol = colIx
            } else if (colIx > lastCol) {
                lastCol = colIx
            }

            addCell(acell)

            // TODO - RowRecord column boundaries need to be updated for cell comments too
            return acell
        }

        return null
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    fun cellIterator(): Iterator<Cell> {
        // can this clumsy generic syntax be improved?
        val result: Iterator<Cell> = cells!!.values.iterator()
        return result
    }

    companion object {
        // used for collections
        const val INITIAL_CAPACITY = 5
    }
}
