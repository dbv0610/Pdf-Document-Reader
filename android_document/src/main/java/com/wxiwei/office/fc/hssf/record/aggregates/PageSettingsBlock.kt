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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.model.InternalSheet
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.BottomMarginRecord
import com.wxiwei.office.fc.hssf.record.ContinueRecord
import com.wxiwei.office.fc.hssf.record.FooterRecord
import com.wxiwei.office.fc.hssf.record.HCenterRecord
import com.wxiwei.office.fc.hssf.record.HeaderFooterRecord
import com.wxiwei.office.fc.hssf.record.HeaderRecord
import com.wxiwei.office.fc.hssf.record.HorizontalPageBreakRecord
import com.wxiwei.office.fc.hssf.record.LeftMarginRecord
import com.wxiwei.office.fc.hssf.record.Margin
import com.wxiwei.office.fc.hssf.record.PageBreakRecord
import com.wxiwei.office.fc.hssf.record.PrintSetupRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.RecordFormatException
import com.wxiwei.office.fc.hssf.record.RightMarginRecord
import com.wxiwei.office.fc.hssf.record.TopMarginRecord
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.hssf.record.UserSViewBegin
import com.wxiwei.office.fc.hssf.record.VCenterRecord
import com.wxiwei.office.fc.hssf.record.VerticalPageBreakRecord
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.contentEquals
import kotlin.collections.indices

/**
 * Groups the page settings records for a worksheet.
 *
 *
 * 
 * See OOO excelfileformat.pdf sec 4.4 'Page Settings Block'
 * 
 * @author Josh Micich
 */
class PageSettingsBlock : RecordAggregate {
    /**
     * PLS is potentially a *continued* record, but is currently uninterpreted by POI
     */
    private class PLSAggregate(rs: RecordStream) : RecordAggregate() {
        private val _pls: Record?

        /**
         * holds any continue records found after the PLS record.<br></br>
         * This would not be required if PLS was properly interpreted.
         * Currently, PLS is an [UnknownRecord] and does not automatically
         * include any trailing [ContinueRecord]s.
         */
        private var _plsContinues: Array<ContinueRecord?>

        init {
            _pls = rs.next
            if (rs.peekNextSid() == ContinueRecord.sid.toInt()) {
                val temp: MutableList<ContinueRecord?> = ArrayList<ContinueRecord?>()
                while (rs.peekNextSid() == ContinueRecord.sid.toInt()) {
                    temp.add(rs.next as ContinueRecord?)
                }
                _plsContinues = temp.toTypedArray()
            } else {
                _plsContinues = EMPTY_CONTINUE_RECORD_ARRAY
            }
        }

        override fun visitContainedRecords(rv: RecordVisitor) {
            rv.visitRecord(_pls!!)
            for (i in _plsContinues.indices) {
                rv.visitRecord(_plsContinues[i]!!)
            }
        }

        companion object {
            private val EMPTY_CONTINUE_RECORD_ARRAY = arrayOf<ContinueRecord?>()
        }
    }

    // Every one of these component records is optional
    // (The whole PageSettingsBlock may not be present)
    private var _rowBreaksRecord: PageBreakRecord? = null
    private var _columnBreaksRecord: PageBreakRecord? = null
    /**
     * Returns the HeaderRecord.
     * @return HeaderRecord for the sheet.
     */
    /**
     * Sets the HeaderRecord.
     * @param newHeader The new HeaderRecord for the sheet.
     */
    var header: HeaderRecord? = null
    /**
     * Returns the FooterRecord.
     * @return FooterRecord for the sheet.
     */
    /**
     * Sets the FooterRecord.
     * @param newFooter The new FooterRecord for the sheet.
     */
    var footer: FooterRecord? = null
    var hCenter: HCenterRecord? = null
        private set
    var vCenter: VCenterRecord? = null
        private set
    private var _leftMargin: LeftMarginRecord? = null
    private var _rightMargin: RightMarginRecord? = null
    private var _topMargin: TopMarginRecord? = null
    private var _bottomMargin: BottomMarginRecord? = null
    private val _plsRecords: MutableList<PLSAggregate>
    /**
     * Returns the PrintSetupRecord.
     * @return PrintSetupRecord for the sheet.
     */
    /**
     * Sets the PrintSetupRecord.
     * @param newPrintSetup The new PrintSetupRecord for the sheet.
     */
    var printSetup: PrintSetupRecord? = null
    private var _bitmap: Record? = null
    private var _headerFooter: HeaderFooterRecord? = null

    /**
     * HeaderFooterRecord records belonging to preceding CustomViewSettingsRecordAggregates.
     * The indicator of such records is a non-zero GUID,
     * see [HeaderFooterRecord.getGuid]
     */
    private val _sviewHeaderFooters: MutableList<HeaderFooterRecord> =
        ArrayList<HeaderFooterRecord>()
    private var _printSize: Record? = null

    constructor(rs: RecordStream) {
        _plsRecords = ArrayList<PLSAggregate>()
        while (true) {
            if (!readARecord(rs)) {
                break
            }
        }
    }

    /**
     * Creates a PageSettingsBlock with default settings
     */
    constructor() {
        _plsRecords = ArrayList<PLSAggregate>()
        _rowBreaksRecord = HorizontalPageBreakRecord()
        _columnBreaksRecord = VerticalPageBreakRecord()
        this.header = HeaderRecord("")
        this.footer = FooterRecord("")
        this.hCenter = createHCenter()
        this.vCenter = createVCenter()
        this.printSetup = createPrintSetup()
    }

    private fun readARecord(rs: RecordStream): Boolean {
        when (rs.peekNextSid()) {
            HorizontalPageBreakRecord.sid.toInt() -> {
                checkNotPresent(_rowBreaksRecord)
                _rowBreaksRecord = rs.next as PageBreakRecord?
            }

            VerticalPageBreakRecord.sid.toInt() -> {
                checkNotPresent(_columnBreaksRecord)
                _columnBreaksRecord = rs.next as PageBreakRecord?
            }

            HeaderRecord.sid.toInt() -> {
                checkNotPresent(this.header)
                this.header = rs.next as HeaderRecord?
            }

            FooterRecord.sid.toInt() -> {
                checkNotPresent(this.footer)
                this.footer = rs.next as FooterRecord?
            }

            HCenterRecord.sid.toInt() -> {
                checkNotPresent(this.hCenter)
                this.hCenter = rs.next as HCenterRecord?
            }

            VCenterRecord.sid.toInt() -> {
                checkNotPresent(this.vCenter)
                this.vCenter = rs.next as VCenterRecord?
            }

            LeftMarginRecord.sid.toInt() -> {
                checkNotPresent(_leftMargin)
                _leftMargin = rs.next as LeftMarginRecord?
            }

            RightMarginRecord.sid.toInt() -> {
                checkNotPresent(_rightMargin)
                _rightMargin = rs.next as RightMarginRecord?
            }

            TopMarginRecord.sid.toInt() -> {
                checkNotPresent(_topMargin)
                _topMargin = rs.next as TopMarginRecord?
            }

            BottomMarginRecord.sid.toInt() -> {
                checkNotPresent(_bottomMargin)
                _bottomMargin = rs.next as BottomMarginRecord?
            }

            UnknownRecord.PLS_004D -> _plsRecords.add(PLSAggregate(rs))
            PrintSetupRecord.sid.toInt() -> {
                checkNotPresent(this.printSetup)
                this.printSetup = rs.next as PrintSetupRecord?
            }

            UnknownRecord.BITMAP_00E9 -> {
                checkNotPresent(_bitmap)
                _bitmap = rs.next
            }

            UnknownRecord.PRINTSIZE_0033 -> {
                checkNotPresent(_printSize)
                _printSize = rs.next
            }

            HeaderFooterRecord.sid.toInt() -> {
                //there can be multiple HeaderFooterRecord records belonging to different sheet views
                val hf = rs.next as HeaderFooterRecord
                if (hf.isCurrentSheet()) _headerFooter = hf
                else {
                    _sviewHeaderFooters.add(hf)
                }
            }

            else ->                // all other record types are not part of the PageSettingsBlock
                return false
        }
        return true
    }

    private fun checkNotPresent(rec: Record?) {
        if (rec != null) {
            throw RecordFormatException(
                ("Duplicate PageSettingsBlock record (sid=0x"
                        + Integer.toHexString(rec.getSid().toInt()) + ")")
            )
        }
    }

    private val rowBreaksRecord: PageBreakRecord
        get() {
            if (_rowBreaksRecord == null) {
                _rowBreaksRecord = HorizontalPageBreakRecord()
            }
            return _rowBreaksRecord!!
        }

    private val columnBreaksRecord: PageBreakRecord
        get() {
            if (_columnBreaksRecord == null) {
                _columnBreaksRecord = VerticalPageBreakRecord()
            }
            return _columnBreaksRecord!!
        }


    /**
     * Sets a page break at the indicated column
     * 
     */
    fun setColumnBreak(column: Short, fromRow: Short, toRow: Short) {
        this.columnBreaksRecord.addBreak(column.toInt(), fromRow.toInt(), toRow.toInt())
    }

    /**
     * Removes a page break at the indicated column
     * 
     */
    fun removeColumnBreak(column: Int) {
        this.columnBreaksRecord.removeBreak(column)
    }


    override fun visitContainedRecords(rv: RecordVisitor) {
        // Replicates record order from Excel 2007, though this is not critical

        Companion.visitIfPresent(_rowBreaksRecord, rv)
        Companion.visitIfPresent(_columnBreaksRecord, rv)
        // Write out empty header / footer records if these are missing
        if (this.header == null) {
            rv.visitRecord(HeaderRecord(""))
        } else {
            rv.visitRecord(this.header!!)
        }
        if (this.footer == null) {
            rv.visitRecord(FooterRecord(""))
        } else {
            rv.visitRecord(this.footer!!)
        }
        visitIfPresent(this.hCenter, rv)
        visitIfPresent(this.vCenter, rv)
        visitIfPresent(_leftMargin, rv)
        visitIfPresent(_rightMargin, rv)
        visitIfPresent(_topMargin, rv)
        visitIfPresent(_bottomMargin, rv)
        for (pls in _plsRecords) {
            pls.visitContainedRecords(rv)
        }
        visitIfPresent(this.printSetup, rv)
        visitIfPresent(_bitmap, rv)
        visitIfPresent(_printSize, rv)
        visitIfPresent(_headerFooter, rv)
    }


    private fun getMarginRec(marginIndex: Int): Margin? {
        when (marginIndex) {
            InternalSheet.LeftMargin.toInt() -> return _leftMargin
            InternalSheet.RightMargin.toInt() -> return _rightMargin
            InternalSheet.TopMargin.toInt() -> return _topMargin
            InternalSheet.BottomMargin.toInt() -> return _bottomMargin
        }
        throw IllegalArgumentException("Unknown margin constant:  " + marginIndex)
    }


    /**
     * Gets the size of the margin in inches.
     * @param margin which margin to get
     * @return the size of the margin
     */
    fun getMargin(margin: Short): Double {
        val m = getMarginRec(margin.toInt())
        if (m != null) {
            return m.getMargin()
        }
        when (margin) {
            InternalSheet.LeftMargin -> return .75
            InternalSheet.RightMargin -> return .75
            InternalSheet.TopMargin -> return 1.0
            InternalSheet.BottomMargin -> return 1.0
        }
        throw IllegalArgumentException("Unknown margin constant:  " + margin)
    }

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
    fun setMargin(margin: Short, size: Double) {
        var m = getMarginRec(margin.toInt())
        if (m == null) {
            when (margin) {
                InternalSheet.LeftMargin -> {
                    _leftMargin = LeftMarginRecord()
                    m = _leftMargin
                }

                InternalSheet.RightMargin -> {
                    _rightMargin = RightMarginRecord()
                    m = _rightMargin
                }

                InternalSheet.TopMargin -> {
                    _topMargin = TopMarginRecord()
                    m = _topMargin
                }

                InternalSheet.BottomMargin -> {
                    _bottomMargin = BottomMarginRecord()
                    m = _bottomMargin
                }

                else -> throw IllegalArgumentException("Unknown margin constant:  " + margin)
            }
        }
        m!!.setMargin(size)
    }

    /**
     * Sets a page break at the indicated row
     * @param row
     */
    fun setRowBreak(row: Int, fromCol: Short, toCol: Short) {
        this.rowBreaksRecord.addBreak(row.toShort().toInt(), fromCol.toInt(), toCol.toInt())
    }

    /**
     * Removes a page break at the indicated row
     * @param row
     */
    fun removeRowBreak(row: Int) {
        require(this.rowBreaksRecord.getBreaks().size >= 1) { "Sheet does not define any row breaks" }
        this.rowBreaksRecord.removeBreak(row.toShort().toInt())
    }

    /**
     * Queries if the specified row has a page break
     * @param row
     * @return true if the specified row has a page break
     */
    fun isRowBroken(row: Int): Boolean {
        return this.rowBreaksRecord.getBreak(row) != null
    }


    /**
     * Queries if the specified column has a page break
     * 
     * @return `true` if the specified column has a page break
     */
    fun isColumnBroken(column: Int): Boolean {
        return this.columnBreaksRecord.getBreak(column) != null
    }

    /**
     * Shifts the horizontal page breaks for the indicated count
     * @param startingRow
     * @param endingRow
     * @param count
     */
    fun shiftRowBreaks(startingRow: Int, endingRow: Int, count: Int) {
        shiftBreaks(this.rowBreaksRecord, startingRow, endingRow, count)
    }

    /**
     * Shifts the vertical page breaks for the indicated count
     * @param startingCol
     * @param endingCol
     * @param count
     */
    fun shiftColumnBreaks(startingCol: Short, endingCol: Short, count: Short) {
        shiftBreaks(this.columnBreaksRecord, startingCol.toInt(), endingCol.toInt(), count.toInt())
    }

    val rowBreaks: IntArray?
        /**
         * @return all the horizontal page breaks, never `null`
         */
        get() = this.rowBreaksRecord.getBreaks()

    val numRowBreaks: Int
        /**
         * @return the number of row page breaks
         */
        get() = this.rowBreaksRecord.getNumBreaks()

    val columnBreaks: IntArray?
        /**
         * @return all the column page breaks, never `null`
         */
        get() = this.columnBreaksRecord.getBreaks()

    val numColumnBreaks: Int
        /**
         * @return the number of column page breaks
         */
        get() = this.columnBreaksRecord.getNumBreaks()

    /**
     * HEADERFOOTER is new in 2007.  Some apps seem to have scattered this record long after
     * the [PageSettingsBlock] where it belongs.
     */
    fun addLateHeaderFooter(rec: HeaderFooterRecord) {
        check(_headerFooter == null) { "This page settings block already has a header/footer record" }
        if (rec.getSid() != HeaderFooterRecord.sid) {
            throw RecordFormatException(
                "Unexpected header-footer record sid: 0x" + Integer.toHexString(
                    rec.getSid().toInt()
                )
            )
        }
        _headerFooter = rec
    }

    /**
     * This method reads PageSettingsBlock records from the supplied RecordStream until the first
     * non-PageSettingsBlock record is encountered.  As each record is read, it is incorporated
     * into this PageSettingsBlock.
     * 
     * 
     * The latest Excel version seems to write the PageSettingsBlock uninterrupted. However there
     * are several examples (that Excel reads OK) where these records are not written together:
     * 
     *  * **HEADER_FOOTER(0x089C) after WINDOW2** - This record is new in 2007.  Some apps
     * seem to have scattered this record long after the PageSettingsBlock where it belongs
     * test samples: SharedFormulaTest.xls, ex44921-21902.xls, ex42570-20305.xls
     *  * **PLS, WSBOOL, PageSettingsBlock** - WSBOOL is not a PSB record.
     * This happens in the test sample file "NoGutsRecords.xls" and "WORKBOOK_in_capitals.xls"
     *  * **Margins after DIMENSION** - All of PSB should be before DIMENSION. (Bug-47199)
     * 
     * These were probably written by other applications (or earlier versions of Excel). It was
     * decided to not write specific code for detecting each of these cases.  POI now tolerates
     * PageSettingsBlock records scattered all over the sheet record stream, and in any order, but
     * does not allow duplicates of any of those records.
     * 
     * 
     * 
     * **Note** - when POI writes out this PageSettingsBlock, the records will always be written
     * in one consolidated block (in the standard ordering) regardless of how scattered the records
     * were when they were originally read.
     * 
     * @throws  RecordFormatException if any PSB record encountered has the same type (sid) as
     * a record that is already part of this PageSettingsBlock
     */
    fun addLateRecords(rs: RecordStream) {
        while (true) {
            if (!readARecord(rs)) {
                break
            }
        }
    }

    /**
     * Some apps can define multiple HeaderFooterRecord records for a sheet.
     * When saving such a file Excel 2007 re-positions them according to the following rules:
     * - take a HeaderFooterRecord and read 16-byte GUID at offset 12. If it is zero,
     * it means the current sheet and the given HeaderFooterRecord belongs to this PageSettingsBlock
     * - If GUID is not zero then search in preceding CustomViewSettingsRecordAggregates.
     * Compare first 16 bytes of UserSViewBegin with the HeaderFooterRecord's GUID. If match,
     * then append the HeaderFooterRecord to this CustomViewSettingsRecordAggregates
     * 
     * @param sheetRecords the list of sheet records read so far
     */
    fun positionRecords(sheetRecords: MutableList<RecordBase>) {
        // Take a copy to loop over, so we can update the real one
        //  without concurrency issues
        val hfRecordsToIterate: MutableList<HeaderFooterRecord> =
            ArrayList<HeaderFooterRecord>(_sviewHeaderFooters)


        // loop through HeaderFooterRecord records having not-empty GUID and match them with
        // CustomViewSettingsRecordAggregate blocks having UserSViewBegin with the same GUID
        for (hf in hfRecordsToIterate) {
            for (rb in sheetRecords) {
                if (rb is CustomViewSettingsRecordAggregate) {
                    val cv = rb
                    cv.visitContainedRecords(object : RecordVisitor {
                        override fun visitRecord(r: Record) {
                            if (r.getSid() == UserSViewBegin.sid) {
                                val guid1 = (r as UserSViewBegin).getGuid()
                                val guid2 = hf.getGuid()
                                if (guid1.contentEquals(guid2)) {
                                    cv.append(hf)
                                    _sviewHeaderFooters.remove(hf)
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    companion object {
        /**
         * @return `true` if the specified Record sid is one belonging to the
         * 'Page Settings Block'.
         */
        fun isComponentRecord(sid: Int): Boolean {
            when (sid) {
                HorizontalPageBreakRecord.sid.toInt(), VerticalPageBreakRecord.sid.toInt(), HeaderRecord.sid.toInt(), FooterRecord.sid.toInt(), HCenterRecord.sid.toInt(), VCenterRecord.sid.toInt(), LeftMarginRecord.sid.toInt(), RightMarginRecord.sid.toInt(), TopMarginRecord.sid.toInt(), BottomMarginRecord.sid.toInt(), UnknownRecord.PLS_004D, PrintSetupRecord.sid.toInt(), UnknownRecord.BITMAP_00E9, UnknownRecord.PRINTSIZE_0033, HeaderFooterRecord.sid.toInt() -> return true
            }
            return false
        }

        private fun visitIfPresent(r: Record?, rv: RecordVisitor) {
            if (r != null) {
                rv.visitRecord(r)
            }
        }

        private fun visitIfPresent(r: PageBreakRecord?, rv: RecordVisitor) {
            if (r != null) {
                if (r.isEmpty()) {
                    // its OK to not serialize empty page break records
                    return
                }
                rv.visitRecord(r)
            }
        }

        /**
         * creates the HCenter Record and sets it to false (don't horizontally center)
         */
        private fun createHCenter(): HCenterRecord {
            val retval = HCenterRecord()

            retval.setHCenter(false)
            return retval
        }

        /**
         * creates the VCenter Record and sets it to false (don't horizontally center)
         */
        private fun createVCenter(): VCenterRecord {
            val retval = VCenterRecord()

            retval.setVCenter(false)
            return retval
        }

        /**
         * creates the PrintSetup Record and sets it to defaults and marks it invalid
         * @see PrintSetupRecord
         * 
         * @see Record
         * 
         * @return record containing a PrintSetupRecord
         */
        private fun createPrintSetup(): PrintSetupRecord {
            val retval = PrintSetupRecord()

            retval.setPaperSize(1.toShort())
            retval.setScale(100.toShort())
            retval.setPageStart(1.toShort())
            retval.setFitWidth(1.toShort())
            retval.setFitHeight(1.toShort())
            retval.setOptions(2.toShort())
            retval.setHResolution(300.toShort())
            retval.setVResolution(300.toShort())
            retval.setHeaderMargin(0.5)
            retval.setFooterMargin(0.5)
            retval.setCopies(1.toShort())
            return retval
        }


        /**
         * Shifts all the page breaks in the range "count" number of rows/columns
         * @param breaks The page record to be shifted
         * @param start Starting "main" value to shift breaks
         * @param stop Ending "main" value to shift breaks
         * @param count number of units (rows/columns) to shift by
         */
        private fun shiftBreaks(breaks: PageBreakRecord, start: Int, stop: Int, count: Int) {
            var iterator = breaks.getBreaksIterator()
            val shiftedBreak: MutableList<PageBreakRecord.Break> =
                ArrayList<PageBreakRecord.Break>()
            while (iterator.hasNext()) {
                val breakItem = iterator.next()
                val breakLocation = breakItem.main
                val inStart = (breakLocation >= start)
                val inEnd = (breakLocation <= stop)
                if (inStart && inEnd) shiftedBreak.add(breakItem)
            }

            iterator = shiftedBreak.iterator()
            while (iterator.hasNext()) {
                val breakItem = iterator.next()
                breaks.removeBreak(breakItem.main)
                breaks.addBreak(
                    (breakItem.main + count).toShort().toInt(),
                    breakItem.subFrom,
                    breakItem.subTo
                )
            }
        }
    }
}
