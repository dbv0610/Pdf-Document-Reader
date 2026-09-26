/*
 * 文件名称:          AWorkbook.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:48:53
 */
package com.wxiwei.office.ss.model.XLSModel

import android.os.Message
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.hssf.OldExcelFormatException
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.hssf.model.InternalSheet
import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.ExtendedFormatRecord
import com.wxiwei.office.fc.hssf.record.LabelRecord
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.PaletteRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFactory
import com.wxiwei.office.fc.hssf.usermodel.HSSFDataFormat
import com.wxiwei.office.fc.hssf.usermodel.HSSFName
import com.wxiwei.office.fc.poifs.filesystem.DirectoryNode
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import com.wxiwei.office.fc.xls.SSReader
import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.model.style.CellStyle
import com.wxiwei.office.ss.util.ColorUtil
import com.wxiwei.office.system.AbstractReader
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.DocumentCoroutines
import com.wxiwei.office.system.ReaderHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-7-20
 * 负责人:           jqin
 */
open class AWorkbook : Workbook, com.wxiwei.office.fc.ss.usermodel.Workbook {
    private class ShapesJob(
        private var book: AWorkbook?,
        private var sheets: Map<Int, Sheet?>?,
        private val sheetIndex: Int,
        private var iAbortListener: SSReader?
    ) {
        private var control: IControl? = iAbortListener!!.getControl()

        fun start(): Job = DocumentCoroutines.launchSuspend(control) {
            try {
                if (sheetIndex >= 0 && iAbortListener != null) {
                    iAbortListener!!.abortCurrentReading()
                    delay(50L)
                    (book!!.getSheet(sheetIndex) as ASheet).processSheet(iAbortListener!!)

                    processOtherSheets()
                }
            } catch (e: OutOfMemoryError) {
                book!!.dispose()
                iAbortListener!!.dispose()
                iAbortListener!!.getControl().getSysKit().getErrorKit().writerLog(e, true)
            } catch (e: Exception) {
                book!!.dispose()
                iAbortListener!!.dispose()
                iAbortListener!!.getControl().getSysKit().getErrorKit().writerLog(e, true)
            } finally {
                book = null
                sheets = null
                iAbortListener = null
                control = null
            }
        }

        private fun processOtherSheets() {
            var iter = sheets!!.keys.iterator()
            while (iter.hasNext()) {
                (book!!.getSheet(iter.next()) as ASheet).processSheet(iAbortListener!!)
            }

            iter = sheets!!.keys.iterator()
            while (iter.hasNext()) {
                book!!.processShapesBySheetIndex(control, iter.next())
            }
        }
    }

    private var shapesJob: Job? = null

    /**
     * The locator of user-defined functions.
     * By default includes functions from the Excel Analysis Toolpack
     */
    private var _udfFinder: UDFFinder? = UDFFinder.DEFAULT

    private var workbook: InternalWorkbook? = null

    /**
     * this holds the HSSFName objects attached to this workbook
     */
    private var names: ArrayList<HSSFName>? = null

    private var currentSheet = 0

    private var iAbortListener: SSReader? = null

    @Throws(IOException::class)
    constructor(s: InputStream?, iAbortListener: SSReader?) : super(true) {
        this.iAbortListener = iAbortListener
        val directory = POIFSFileSystem(s).getRoot()
        val workbookName = getWorkbookDirEntryName(directory)

        // Grab the data from the workbook stream, however
        //  it happens to be spelled.
        val stream = directory.createDocumentInputStream(workbookName)

        var records: MutableList<Record>? = RecordFactory.createRecords(stream, iAbortListener)
        val workbook = InternalWorkbook.createWorkbook(records, iAbortListener)
        this.workbook = workbook

        val recOffset = workbook.getNumRecords()

        // shared string
        val size = workbook.getSSTUniqueStringSize()
        for (i in 0 until size) {
            addSharedString(i, workbook.getSSTString(i))
        }

        convertLabelRecords(records!!, recOffset)

        isUsing1904DateWindowing = workbook.isUsing1904DateWindowing()

        //color
        val palette = workbook.getCustomPalette()
        var index = PaletteRecord.FIRST_COLOR_INDEX.toInt()
        addColor(index++, ColorUtil.rgb(0, 0, 0))
        var color = palette.getColor(index)
        while (color != null) {
            addColor(index++, ColorUtil.rgb(color[0], color[1], color[2]))
            color = palette.getColor(index)
        }

        //cell style
        processCellStyle(workbook)

        val rs = RecordStream(records, recOffset)
        var sheetIndex = 0
        while (rs.hasNext()) {
            val internalSheet = InternalSheet.createSheet(rs, iAbortListener)
            val sheet = ASheet(this, internalSheet)
            sheet.setSheetName(workbook.getSheetName(sheetIndex))
            if (internalSheet.isChartSheet()) {
                sheet.setSheetType(Sheet.TYPE_CHARTSHEET)
            }
            sheets!![sheetIndex++] = sheet
        }

        // The view is created immediately after getModel() returns.  Load the
        // first worksheet's rows/cells before returning so the initial render
        // never observes an empty sheet while the background shape job starts.
        if (sheets!!.isNotEmpty() && iAbortListener != null) {
            (sheets!![0] as ASheet).processSheet(iAbortListener!!)
        }
        debugDump("xls-after-first-sheet")

        records.clear()
        @Suppress("UNUSED_VALUE")
        records = null

        names = ArrayList(INITIAL_CAPACITY)
        for (i in 0 until workbook.getNumNames()) {
            val nameRecord = workbook.getNameRecord(i)
            val name = HSSFName(
                this, nameRecord,
                workbook.getNameCommentRecord(nameRecord)
            )
            names!!.add(name)
        }

        //rows and shapes processing
        processSheet()
    }

    /**
     * process the index sheet
     * @param sheetIndex
     */
    private fun processShapesBySheetIndex(control: IControl?, sheetIndex: Int) {
        val sheet = sheets!![sheetIndex] as ASheet?
        try {
            if (sheet!!.getState() != Sheet.State_Accomplished) {
                sheet.processSheetShapes(control!!)
                sheet.setState(Sheet.State_Accomplished)
            }
        } catch (e: Exception) {
            sheet!!.setState(Sheet.State_Accomplished)
        }
    }

    /**
     *
     */
    private fun processSheet() {
        class WorkbookReaderHandler(private var book: AWorkbook?) : ReaderHandler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MainConstant.HANDLER_MESSAGE_SUCCESS -> {
                        currentSheet = msg.obj as Int
                        if (sheets!![currentSheet]!!.getState() != Sheet.State_Accomplished) {
                            shapesJob?.cancel()
                            shapesJob = ShapesJob(book, sheets, currentSheet, iAbortListener).start()
                        }
                    }

                    MainConstant.HANDLER_MESSAGE_ERROR, MainConstant.HANDLER_MESSAGE_DISPOSE -> {
                        shapesJob?.cancel()
                        shapesJob = null
                        book = null
                    }
                }
            }
        }

        readerHandler = WorkbookReaderHandler(this)

        val msg = Message()
        msg.what = MainConstant.HANDLER_MESSAGE_SUCCESS
        msg.obj = 0
        readerHandler!!.handleMessage(msg)
    }

    /**
     * This is basically a kludge to deal with the now obsolete Label records.  If
     * you have to read in a sheet that contains Label records, be aware that the rest
     * of the API doesn't deal with them, the low level structure only provides read-only
     * semi-immutable structures (the sets are there for interface conformance with NO
     * impelmentation).  In short, you need to call this function passing it a reference
     * to the Workbook object.  All labels will be converted to LabelSST records and their
     * contained strings will be written to the Shared String tabel (SSTRecord) within
     * the Workbook.
     *
     * @param records a collection of sheet's records.
     * @param offset the offset to search at
     * @see LabelRecord
     * @see com.wxiwei.office.fc.hssf.record.LabelSSTRecord
     * @see com.wxiwei.office.fc.hssf.record.SSTRecord
     */
    private fun convertLabelRecords(records: List<*>, offset: Int) {
        for (k in offset until records.size) {
            val rec = records[k] as Record
            if (rec.getSid() == LabelRecord.sid) {
                val oldrec = rec as LabelRecord
                sharedString!![sharedString!!.size] = oldrec.getValue()
            }
        }
    }

    /**
     *
     * @param workbook
     */
    private fun processCellStyle(workbook: InternalWorkbook) {
        processFont(workbook)

        var styleIndex: Short = 0
        val cellStyleCnt = workbook.getNumExFormats().toShort()
        var format: ExtendedFormatRecord?
        while (styleIndex < cellStyleCnt) {
            format = workbook.getExFormatAt(styleIndex.toInt())
            if (format == null) {
                // A malformed/sparse BIFF style table must not trap the reader at
                // the same index forever.  Keep the missing style as the default
                // style and continue with the next XF record.
                styleIndex++
                continue
            }

            val style = CellStyle()
            // style index;
            style.setIndex(styleIndex)
            // data format Index
            style.setNumberFormatID(format.getFormatIndex())
            // data format string
            style.setFormatCode(HSSFDataFormat.getFormatCode(workbook, format.getFormatIndex()))
            // fontIndex
            style.setFontIndex(format.getFontIndex())
            // hidden
            style.setHidden(format.isHidden())
            // locked
            style.setLocked(format.isLocked())
            // wrap text
            style.setWrapText(format.getWrapText())
            // horizontal alignment
            style.setHorizontalAlign(format.getAlignment())
            // vertical alignment
            style.setVerticalAlign(format.getVerticalAlignment())
            // rotation
            style.setRotation(format.getRotation())
            // indent
            style.setIndent(format.getIndent())

            // border left and color
            style.setBorderLeft(format.getBorderLeft())
            var colorIndex = format.getLeftBorderPaletteIdx()
            if (colorIndex.toInt() == AUTOMATIC_COLOR) {
                colorIndex = PaletteRecord.FIRST_COLOR_INDEX
            }
            style.setBorderLeftColorIdx(colorIndex)

            // border right and color
            style.setBorderRight(format.getBorderRight())
            colorIndex = format.getRightBorderPaletteIdx()
            if (colorIndex.toInt() == AUTOMATIC_COLOR) {
                colorIndex = PaletteRecord.FIRST_COLOR_INDEX
            }
            style.setBorderRightColorIdx(colorIndex)

            // border top and color
            style.setBorderTop(format.getBorderTop())
            colorIndex = format.getTopBorderPaletteIdx()
            if (colorIndex.toInt() == AUTOMATIC_COLOR) {
                colorIndex = PaletteRecord.FIRST_COLOR_INDEX
            }
            style.setBorderTopColorIdx(colorIndex)

            // border bottom and color
            style.setBorderBottom(format.getBorderBottom())
            colorIndex = format.getBottomBorderPaletteIdx()
            if (colorIndex.toInt() == AUTOMATIC_COLOR) {
                colorIndex = PaletteRecord.FIRST_COLOR_INDEX
            }
            style.setBorderBottomColorIdx(colorIndex)

            // background color index
            colorIndex = format.getFillBackground()
            style.setBgColor(getColor(colorIndex.toInt()))

            // foreground color index
            colorIndex = format.getFillForeground()
            if (colorIndex.toInt() == AUTOMATIC_COLOR) {
                colorIndex = (PaletteRecord.FIRST_COLOR_INDEX + 1).toShort()
            }
            style.setFgColor(getColor(colorIndex.toInt()))

            // fill color index
            style.setFillPatternType((format.getAdtlFillPattern() - 1).toByte())

            addCellStyle(styleIndex.toInt(), style)
            styleIndex++
        }
    }

    /**
     * process font fontIndex
     */
    private fun processFont(workbook: InternalWorkbook) {
        var numFont = workbook.getNumberOfFontRecords()
        if (numFont <= 4) {
            numFont -= 1
        }

        var idx = 0
        while (idx <= numFont) {
            val fontRec = workbook.getFontRecordAt(idx)
            val font = Font()
            // font index;
            font.setIndex(idx)
            // font name
            font.setName(fontRec.getFontName())
            // font size
            font.setFontSize((fontRec.getFontHeight() / 20).toShort().toDouble())
            // color index;
            var index = fontRec.getColorPaletteIndex()
            if (index.toInt() == 32767) {
                index = PaletteRecord.FIRST_COLOR_INDEX
            }
            font.setColorIndex(index.toInt())
            // Italic
            font.setItalic(fontRec.isItalic())
            // bold
            font.setBold(fontRec.getBoldWeight() > Font.BOLDWEIGHT_NORMAL)
            // superSubScript
            font.setSuperSubScript(fontRec.getSuperSubScript().toByte())
            // strike
            font.setStrikeline(fontRec.isStruckout())
            // underline
            font.setUnderline(fontRec.getUnderline().toInt())

            addFont(idx++, font)
        }
    }

    /**
     *
     * @return
     */
    fun getInternalWorkbook(): InternalWorkbook? {
        return workbook
    }

    /**
     * Returns the locator of user-defined functions.
     * The default instance extends the built-in functions with the Analysis Tool Pack
     *
     * @return the locator of user-defined functions
     */
    fun getUDFFinder(): UDFFinder? {
        return _udfFinder
    }

    /**
     * Get the number of spreadsheets in the workbook
     *
     * @return the number of sheets
     */
    override fun getNumberOfSheets(): Int {
        return sheets!!.size
    }

    /**
     * get sheet for sheet index;
     */
    override fun getSheetAt(index: Int): ASheet? {
        if (index < 0 || index >= sheets!!.size) {
            return null
        }
        return sheets!![index] as ASheet?
    }

    /** Returns the index of the sheet by his name
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    fun getSheetIndex(name: String?): Int {
        return workbook!!.getSheetIndex(name)
    }

    /** Returns the index of the given sheet
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based). <tt>-1</tt> if not found
     */
    override fun getSheetIndex(sheet: Sheet?): Int {
        for (i in 0 until sheets!!.size) {
            if (sheets!![i] === sheet) {
                return i
            }
        }
        return -1
    }

    fun getNumberOfNames(): Int {
        val result = names!!.size
        return result
    }

    fun getNameIndex(name: String?): Int {
        for (k in 0 until names!!.size) {
            val nameName = getNameName(k)
            if (nameName!!.equals(name, ignoreCase = true)) {
                return k
            }
        }
        return -1
    }

    fun getName(name: String?): HSSFName? {
        val nameIndex = getNameIndex(name)
        if (nameIndex < 0) {
            return null
        }
        return names!![nameIndex] as HSSFName
    }

    fun getNameAt(nameIndex: Int): HSSFName {
        val nNames = names!!.size
        if (nNames < 1) {
            throw IllegalStateException("There are no defined names in this workbook")
        }
        if (nameIndex < 0 || nameIndex > nNames) {
            throw IllegalArgumentException(
                "Specified name index " + nameIndex
                        + " is outside the allowable range (0.." + (nNames - 1) + ")."
            )
        }
        return names!![nameIndex] as HSSFName
    }

    fun getNameRecord(nameIndex: Int): NameRecord? {
        return workbook!!.getNameRecord(nameIndex)
    }

    /** gets the named range name
     * @param index the named range index (0 based)
     * @return named range name
     */
    fun getNameName(index: Int): String? {
        val result = getNameAt(index).getNameName()
        return result
    }

    /**
     *
     * @return
     */
    fun getAbstractReader(): AbstractReader? {
        return iAbortListener
    }

    /**
     *
     */
    override fun dispose() {
        destroy()
        workbook = null
        if (names != null && names!!.size > 0) {
            val iter = names!!.iterator()
            while (iter.hasNext()) {
                iter.next().dispose()
            }
            names!!.clear()
            names = null
        }
        _udfFinder = null
        iAbortListener = null
    }

    companion object {
        /**
         * used for compile-time performance/memory optimization.  This determines the
         * initial capacity for the sheet collection.  Its currently set to 3.
         * Changing it in this release will decrease performance
         * since you're never allowed to have more or less than three sheets!
         */
        const val INITIAL_CAPACITY = 3

        const val AUTOMATIC_COLOR = 0x40

        /**
         * Normally, the Workbook will be in a POIFS Stream
         * called "Workbook". However, some weird XLS generators use "WORKBOOK"
         */
        private val WORKBOOK_DIR_ENTRY_NAMES = arrayOf(
            "Workbook", // as per BIFF8 spec
            "WORKBOOK"
        )

        @JvmStatic
        fun getWorkbookDirEntryName(directory: DirectoryNode): String {
            val potentialNames = WORKBOOK_DIR_ENTRY_NAMES
            for (i in potentialNames.indices) {
                val wbName = potentialNames[i]
                try {
                    directory.getEntry(wbName)
                    return wbName
                } catch (e: FileNotFoundException) {
                    // continue - to try other options
                }
            }

            // check for previous version of file format
            try {
                directory.getEntry("Book")
                throw OldExcelFormatException(
                    "The supplied spreadsheet seems to be Excel 5.0/7.0 (BIFF5) format. "
                            + "POI only supports BIFF8 format (from Excel versions 97/2000/XP/2003)"
                )
            } catch (e: FileNotFoundException) {
                // fall through
            }

            throw IllegalArgumentException(
                "The supplied POIFSFileSystem does not contain a BIFF8 'Workbook' entry. "
                        + "Is it really an excel file?"
            )
        }
    }
}
