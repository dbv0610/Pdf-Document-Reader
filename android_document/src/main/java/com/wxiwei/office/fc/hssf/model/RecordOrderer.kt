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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.hssf.record.ArrayRecord
import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.BlankRecord
import com.wxiwei.office.fc.hssf.record.BoolErrRecord
import com.wxiwei.office.fc.hssf.record.CalcCountRecord
import com.wxiwei.office.fc.hssf.record.CalcModeRecord
import com.wxiwei.office.fc.hssf.record.DVALRecord
import com.wxiwei.office.fc.hssf.record.DateWindow1904Record
import com.wxiwei.office.fc.hssf.record.DefaultColWidthRecord
import com.wxiwei.office.fc.hssf.record.DefaultRowHeightRecord
import com.wxiwei.office.fc.hssf.record.DeltaRecord
import com.wxiwei.office.fc.hssf.record.DimensionsRecord
import com.wxiwei.office.fc.hssf.record.DrawingRecord
import com.wxiwei.office.fc.hssf.record.DrawingSelectionRecord
import com.wxiwei.office.fc.hssf.record.EOFRecord
import com.wxiwei.office.fc.hssf.record.FeatRecord
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.GridsetRecord
import com.wxiwei.office.fc.hssf.record.GutsRecord
import com.wxiwei.office.fc.hssf.record.HyperlinkRecord
import com.wxiwei.office.fc.hssf.record.IndexRecord
import com.wxiwei.office.fc.hssf.record.IterationRecord
import com.wxiwei.office.fc.hssf.record.LabelRecord
import com.wxiwei.office.fc.hssf.record.LabelSSTRecord
import com.wxiwei.office.fc.hssf.record.NumberRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.PaneRecord
import com.wxiwei.office.fc.hssf.record.PrecisionRecord
import com.wxiwei.office.fc.hssf.record.PrintGridlinesRecord
import com.wxiwei.office.fc.hssf.record.PrintHeadersRecord
import com.wxiwei.office.fc.hssf.record.RKRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.RefModeRecord
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.hssf.record.SCLRecord
import com.wxiwei.office.fc.hssf.record.SaveRecalcRecord
import com.wxiwei.office.fc.hssf.record.SelectionRecord
import com.wxiwei.office.fc.hssf.record.SharedFormulaRecord
import com.wxiwei.office.fc.hssf.record.TableRecord
import com.wxiwei.office.fc.hssf.record.TextObjectRecord
import com.wxiwei.office.fc.hssf.record.UncalcedRecord
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.hssf.record.WindowOneRecord
import com.wxiwei.office.fc.hssf.record.WindowTwoRecord
import com.wxiwei.office.fc.hssf.record.aggregates.ColumnInfoRecordsAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.ConditionalFormattingTable
import com.wxiwei.office.fc.hssf.record.aggregates.DataValidityTable
import com.wxiwei.office.fc.hssf.record.aggregates.MergedCellsTable
import com.wxiwei.office.fc.hssf.record.aggregates.PageSettingsBlock
import com.wxiwei.office.fc.hssf.record.aggregates.WorksheetProtectionBlock
import com.wxiwei.office.fc.hssf.record.pivottable.ViewDefinitionRecord

/**
 * Finds correct insert positions for records in workbook streams
 *
 *
 * 
 * See OOO excelfileformat.pdf sec. 4.2.5 'Record Order in a BIFF8 Workbook Stream'
 * 
 * @author Josh Micich
 */
internal object RecordOrderer {
    /**
     * Adds the specified new record in the correct place in sheet records list
     */
    fun addNewSheetRecord(sheetRecords: MutableList<RecordBase>, newRecord: RecordBase) {
        val index = findSheetInsertPos(sheetRecords, newRecord.javaClass)
        sheetRecords.add(index, newRecord)
    }

    private fun findSheetInsertPos(
        records: MutableList<RecordBase>,
        recClass: Class<out RecordBase?>
    ): Int {
        if (recClass == DataValidityTable::class.java) {
            return findDataValidationTableInsertPos(records)
        }
        if (recClass == MergedCellsTable::class.java) {
            return findInsertPosForNewMergedRecordTable(records)
        }
        if (recClass == ConditionalFormattingTable::class.java) {
            return findInsertPosForNewCondFormatTable(records)
        }
        if (recClass == GutsRecord::class.java) {
            return getGutsRecordInsertPos(records)
        }
        if (recClass == PageSettingsBlock::class.java) {
            return getPageBreakRecordInsertPos(records)
        }
        if (recClass == WorksheetProtectionBlock::class.java) {
            return getWorksheetProtectionBlockInsertPos(records)
        }
        throw RuntimeException("Unexpected record class (" + recClass.getName() + ")")
    }

    /**
     * Finds the index where the protection block should be inserted
     * @param records the records for this sheet
     * <pre>
     * + BOF
     * o INDEX
     * o Calculation Settings Block
     * o PRINTHEADERS
     * o PRINTGRIDLINES
     * o GRIDSET
     * o GUTS
     * o DEFAULTROWHEIGHT
     * o SHEETPR
     * o Page Settings Block
     * o Worksheet Protection Block
     * o DEFCOLWIDTH
     * oo COLINFO
     * o SORT
     * + DIMENSION
    </pre> * 
     */
    private fun getWorksheetProtectionBlockInsertPos(records: MutableList<RecordBase>): Int {
        var i = getDimensionsIndex(records)
        while (i > 0) {
            i--
            val rb: Any? = records.get(i)
            if (!isProtectionSubsequentRecord(rb)) {
                return i + 1
            }
        }
        throw IllegalStateException("did not find insert pos for protection block")
    }


    /**
     * These records may occur between the 'Worksheet Protection Block' and DIMENSION:
     * <pre>
     * o DEFCOLWIDTH
     * oo COLINFO
     * o SORT
    </pre> * 
     */
    private fun isProtectionSubsequentRecord(rb: Any?): Boolean {
        if (rb is ColumnInfoRecordsAggregate) {
            return true // oo COLINFO
        }
        if (rb is Record) {
            val record = rb
            when (record.getSid().toInt()) {
                DefaultColWidthRecord.sid.toInt(), UnknownRecord.SORT_0090 -> return true
            }
        }
        return false
    }

    private fun getPageBreakRecordInsertPos(records: MutableList<RecordBase>): Int {
        val dimensionsIndex = getDimensionsIndex(records)
        var i = dimensionsIndex - 1
        while (i > 0) {
            i--
            val rb: Any? = records.get(i)
            if (isPageBreakPriorRecord(rb)) {
                return i + 1
            }
        }
        throw RuntimeException("Did not find insert point for GUTS")
    }

    private fun isPageBreakPriorRecord(rb: Any?): Boolean {
        if (rb is Record) {
            val record = rb
            when (record.getSid().toInt()) {
                BOFRecord.sid.toInt(), IndexRecord.sid.toInt(), UncalcedRecord.sid.toInt(), CalcCountRecord.sid.toInt(), CalcModeRecord.sid.toInt(), PrecisionRecord.sid.toInt(), RefModeRecord.sid.toInt(), DeltaRecord.sid.toInt(), IterationRecord.sid.toInt(), DateWindow1904Record.sid.toInt(), SaveRecalcRecord.sid.toInt(), PrintHeadersRecord.sid.toInt(), PrintGridlinesRecord.sid.toInt(), GridsetRecord.sid.toInt(), DefaultRowHeightRecord.sid.toInt(), UnknownRecord.SHEETPR_0081 -> return true
            }
        }
        return false
    }

    /**
     * Find correct position to add new CFHeader record
     */
    private fun findInsertPosForNewCondFormatTable(records: MutableList<RecordBase>): Int {
        for (i in records.size - 2 downTo 0) { // -2 to skip EOF record
            val rb: Any? = records.get(i)
            if (rb is MergedCellsTable) {
                return i + 1
            }
            if (rb is DataValidityTable) {
                continue
            }

            val rec = rb as Record
            when (rec.getSid().toInt()) {
                WindowTwoRecord.sid.toInt(), SCLRecord.sid.toInt(), PaneRecord.sid.toInt(), SelectionRecord.sid.toInt(), UnknownRecord.STANDARDWIDTH_0099, UnknownRecord.LABELRANGES_015F, UnknownRecord.PHONETICPR_00EF ->                    // ConditionalFormattingTable goes here
                    return i + 1
            }
        }
        throw RuntimeException("Did not find Window2 record")
    }

    private fun findInsertPosForNewMergedRecordTable(records: MutableList<RecordBase>): Int {
        for (i in records.size - 2 downTo 0) { // -2 to skip EOF record
            val rb: Any? = records.get(i)
            if (rb !is Record) {
                // DataValidityTable, ConditionalFormattingTable,
                // even PageSettingsBlock (which doesn't normally appear after 'View Settings')
                continue
            }
            val rec = rb
            when (rec.getSid().toInt()) {
                WindowTwoRecord.sid.toInt(), SCLRecord.sid.toInt(), PaneRecord.sid.toInt(), SelectionRecord.sid.toInt(), UnknownRecord.STANDARDWIDTH_0099 -> return i + 1
            }
        }
        throw RuntimeException("Did not find Window2 record")
    }


    /**
     * Finds the index where the sheet validations header record should be inserted
     * @param records the records for this sheet
     * 
     * + WINDOW2
     * o SCL
     * o PANE
     * oo SELECTION
     * o STANDARDWIDTH
     * oo MERGEDCELLS
     * o LABELRANGES
     * o PHONETICPR
     * o Conditional Formatting Table
     * o Hyperlink Table
     * o Data Validity Table
     * o SHEETLAYOUT
     * o SHEETPROTECTION
     * o RANGEPROTECTION
     * + EOF
     */
    private fun findDataValidationTableInsertPos(records: MutableList<RecordBase>): Int {
        var i = records.size - 1
        check(records.get(i) is EOFRecord) { "Last sheet record should be EOFRecord" }
        while (i > 0) {
            i--
            val rb = records.get(i)
            if (isDVTPriorRecord(rb)) {
                val nextRec = records.get(i + 1) as Record
                check(isDVTSubsequentRecord(nextRec.getSid())) {
                    ("Unexpected (" + nextRec.javaClass.getName()
                            + ") found after (" + rb.javaClass.getName() + ")")
                }
                return i + 1
            }
            val rec = rb as Record
            check(isDVTSubsequentRecord(rec.getSid())) {
                ("Unexpected (" + rec.javaClass.getName()
                        + ") while looking for DV Table insert pos")
            }
        }
        return 0
    }


    private fun isDVTPriorRecord(rb: RecordBase): Boolean {
        if (rb is MergedCellsTable || rb is ConditionalFormattingTable) {
            return true
        }
        val sid = (rb as Record).getSid()
        when (sid.toInt()) {
            WindowTwoRecord.sid.toInt(), UnknownRecord.SCL_00A0, PaneRecord.sid.toInt(), SelectionRecord.sid.toInt(), UnknownRecord.STANDARDWIDTH_0099, UnknownRecord.LABELRANGES_015F, UnknownRecord.PHONETICPR_00EF, HyperlinkRecord.sid.toInt(), UnknownRecord.QUICKTIP_0800, UnknownRecord.CODENAME_1BA -> return true
        }
        return false
    }

    private fun isDVTSubsequentRecord(sid: Short): Boolean {
        when (sid.toInt()) {
            UnknownRecord.SHEETEXT_0862, UnknownRecord.SHEETPROTECTION_0867, FeatRecord.sid.toInt(), EOFRecord.sid.toInt() -> return true
        }
        return false
    }

    /**
     * DIMENSIONS record is always present
     */
    private fun getDimensionsIndex(records: MutableList<RecordBase>): Int {
        val nRecs = records.size
        for (i in 0..<nRecs) {
            if (records.get(i) is DimensionsRecord) {
                return i
            }
        }
        // worksheet stream is seriously broken
        throw RuntimeException("DimensionsRecord not found")
    }

    private fun getGutsRecordInsertPos(records: MutableList<RecordBase>): Int {
        val dimensionsIndex = getDimensionsIndex(records)
        var i = dimensionsIndex - 1
        while (i > 0) {
            i--
            val rb: RecordBase? = records.get(i)
            if (isGutsPriorRecord(rb)) {
                return i + 1
            }
        }
        throw RuntimeException("Did not find insert point for GUTS")
    }

    private fun isGutsPriorRecord(rb: RecordBase?): Boolean {
        if (rb is Record) {
            val record = rb
            when (record.getSid().toInt()) {
                BOFRecord.sid.toInt(), IndexRecord.sid.toInt(), UncalcedRecord.sid.toInt(), CalcCountRecord.sid.toInt(), CalcModeRecord.sid.toInt(), PrecisionRecord.sid.toInt(), RefModeRecord.sid.toInt(), DeltaRecord.sid.toInt(), IterationRecord.sid.toInt(), DateWindow1904Record.sid.toInt(), SaveRecalcRecord.sid.toInt(), PrintHeadersRecord.sid.toInt(), PrintGridlinesRecord.sid.toInt(), GridsetRecord.sid.toInt() -> return true
            }
        }
        return false
    }

    /**
     * @return `true` if the specified record ID terminates a sequence of Row block records
     * It is assumed that at least one row or cell value record has been found prior to the current
     * record
     */
    fun isEndOfRowBlock(sid: Int): Boolean {
        when (sid.toInt()) {
            ViewDefinitionRecord.sid.toInt(), DrawingRecord.sid.toInt(), DrawingSelectionRecord.sid.toInt(), ObjRecord.sid.toInt(), TextObjectRecord.sid.toInt(), GutsRecord.sid.toInt(), WindowOneRecord.sid.toInt(), WindowTwoRecord.sid.toInt() -> return true

            DVALRecord.sid.toInt() -> return true
            EOFRecord.sid.toInt() ->                // WINDOW2 should always be present, so shouldn't have got this far
                throw RuntimeException("Found EOFRecord before WindowTwoRecord was encountered")
        }
        return PageSettingsBlock.isComponentRecord(sid)
    }

    /**
     * @return `true` if the specified record id normally appears in the row blocks section
     * of the sheet records
     */
    fun isRowBlockRecord(sid: Int): Boolean {
        when (sid.toInt()) {
            RowRecord.sid.toInt(), BlankRecord.sid.toInt(), BoolErrRecord.sid.toInt(), FormulaRecord.sid.toInt(), LabelRecord.sid.toInt(), LabelSSTRecord.sid.toInt(), NumberRecord.sid.toInt(), RKRecord.sid.toInt(), ArrayRecord.sid.toInt(), SharedFormulaRecord.sid.toInt(), TableRecord.sid.toInt() -> return true
        }
        return false
    }
}
