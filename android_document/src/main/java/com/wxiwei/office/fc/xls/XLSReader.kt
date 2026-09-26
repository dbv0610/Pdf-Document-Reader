package com.wxiwei.office.fc.xls

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.model.InternalSheet
import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.BoolErrRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.NumberRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFactory
import com.wxiwei.office.fc.poifs.filesystem.DirectoryNode
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import com.wxiwei.office.ss.model.XLSModel.ACell
import com.wxiwei.office.ss.model.XLSModel.AWorkbook
import com.wxiwei.office.fc.hssf.record.crypto.Biff8EncryptionKey
import com.wxiwei.office.system.DocumentPasswords
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.IControl
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class XLSReader(control: IControl, filePath: String) : SSReader() {
    private var filePath: String? = null

    init {
        this.control = control
        this.filePath = filePath
    }

    override fun getModel(): Any? {
        val input = FileInputStream(filePath!!)
        // BIFF8 decryption reads the password from a thread-local while the workbook is parsed
        Biff8EncryptionKey.setCurrentUserPassword(DocumentPasswords.get(filePath))
        try {
            return AWorkbook(input, this)
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null)
        }
    }

    override fun searchContent(file: File?, key: String): Boolean {
        if(file == null) return false else return try {
            val searchKey = key.lowercase()
            val input = FileInputStream(file.absolutePath)
            val directory: DirectoryNode = POIFSFileSystem(input).root
            val workbookName = AWorkbook.getWorkbookDirEntryName(directory)
            val stream: InputStream = directory.createDocumentInputStream(workbookName)
            val records: List<Record> = RecordFactory.createRecords(stream, this)
            val workbook = InternalWorkbook.createWorkbook(records, this)

            var sheetIndex = 0
            while (sheetIndex < workbook.numSheets) {
                if (workbook.getSheetName(sheetIndex++).lowercase().contains(searchKey)) {
                    return true
                }
            }

            val size = workbook.getSSTUniqueStringSize()
            for (i in 0 until size) {
                checkAbortReader()
                if (workbook.getSSTString(i).string.lowercase().contains(searchKey)) {
                    return true
                }
            }

            val recordOffset = workbook.numRecords
            val recordStream = RecordStream(records, recordOffset)
            while (recordStream.hasNext()) {
                val internalSheet = InternalSheet.createSheet(recordStream, this)
                if (searchSheet(internalSheet, searchKey)) {
                    return true
                }
            }

            for (i in 0 until workbook.numNames) {
                val nameRecord: NameRecord = workbook.getNameRecord(i)
                if (nameRecord.nameText.lowercase().contains(searchKey)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun searchSheet(sheet: InternalSheet, key: String): Boolean {
        val iterator = sheet.cellValueIterator
        while (iterator.hasNext()) {
            val cell = iterator.next()
            checkAbortReader()
            if (searchCell(cell, key)) {
                return true
            }
        }
        return false
    }

    private fun searchCell(cell: CellValueRecordInterface, key: String): Boolean {
        val cellType = ACell.determineType(cell).toShort()
        return when (cellType.toInt()) {
            Cell.CELL_TYPE_NUMERIC.toInt() -> (cell as NumberRecord).value.toString().contains(key)
            Cell.CELL_TYPE_STRING.toInt(), Cell.CELL_TYPE_BLANK.toInt(), Cell.CELL_TYPE_FORMULA.toInt() -> false
            Cell.CELL_TYPE_BOOLEAN.toInt() -> (cell as BoolErrRecord).booleanValue.toString().lowercase().contains(key)
            Cell.CELL_TYPE_ERROR.toInt() -> ErrorEval.getText((cell as BoolErrRecord).errorValue.toInt()).lowercase().contains(key)
            else -> false
        }
    }

    private fun checkAbortReader() {
        if (abortReader) {
            throw AbortReaderError("abort Reader")
        }
    }

    override fun dispose() {
        super.dispose()
        filePath = null
    }

}
