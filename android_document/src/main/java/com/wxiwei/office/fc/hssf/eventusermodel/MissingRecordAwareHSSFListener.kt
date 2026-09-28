/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
package com.wxiwei.office.fc.hssf.eventusermodel

import com.wxiwei.office.fc.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord
import com.wxiwei.office.fc.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord
import com.wxiwei.office.fc.hssf.eventusermodel.dummyrecord.MissingRowDummyRecord
import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.MulBlankRecord
import com.wxiwei.office.fc.hssf.record.MulRKRecord
import com.wxiwei.office.fc.hssf.record.NoteRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFactory
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.hssf.record.SharedFormulaRecord


/**
 * 
 * A HSSFListener which tracks rows and columns, and will
 * trigger your HSSFListener for all rows and cells,
 * even the ones that aren't actually stored in the file.
 * 
 * This allows your code to have a more "Excel" like
 * view of the data in the file, and not have to worry
 * (as much) about if a particular row/cell is in the
 * file, or was skipped from being written as it was
 * blank.
 */
class MissingRecordAwareHSSFListener(listener: HSSFListener) : HSSFListener {
    private val childListener: HSSFListener

    // Need to have different counters for cell rows and
    //  row rows, as you sometimes get a RowRecord in the
    //  middle of some cells, and that'd break everything
    private var lastRowRow = 0

    private var lastCellRow = 0
    private var lastCellColumn = 0

    /**
     * Constructs a new MissingRecordAwareHSSFListener, which
     * will fire processRecord on the supplied child
     * HSSFListener for all Records, and missing records.
     * @param listener The HSSFListener to pass records on to
     */
    init {
        resetCounts()
        childListener = listener
    }

    override fun processRecord(record: Record?) {
        var thisRow: Int
        var thisColumn: Int
        var expandedRecords: Array<out CellValueRecordInterface?>? = null

        if (record is CellValueRecordInterface) {
            val valueRec = record as CellValueRecordInterface
            thisRow = valueRec.row
            thisColumn = valueRec.column.toInt()
        } else {
            thisRow = -1
            thisColumn = -1

            when (record!!.getSid()) {
                BOFRecord.sid -> {
                    val bof = record as BOFRecord
                    if (bof.type == BOFRecord.TYPE_WORKBOOK || bof.type == BOFRecord.TYPE_WORKSHEET) {
                        // Reset the row and column counts - new workbook / worksheet
                        resetCounts()
                    }
                }

                RowRecord.sid -> {
                    val rowrec = record as RowRecord

                    //System.out.println("Row " + rowrec.getRowNumber() + " found, first column at "
                    //        + rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());

                    // If there's a jump in rows, fire off missing row records
                    if (lastRowRow + 1 < rowrec.getRowNumber()) {
                        var i = (lastRowRow + 1)
                        while (i < rowrec.getRowNumber()) {
                            val dr = MissingRowDummyRecord(i)
                            childListener.processRecord(dr)
                            i++
                        }
                    }

                    // Record this as the last row we saw
                    lastRowRow = rowrec.getRowNumber()
                }

                SharedFormulaRecord.sid -> {
                    // SharedFormulaRecord occurs after the first FormulaRecord of the cell range.
                    // There are probably (but not always) more cell records after this
                    // - so don't fire off the LastCellOfRowDummyRecord yet
                    childListener.processRecord(record)
                    return
                }

                MulBlankRecord.sid -> {
                    // These appear in the middle of the cell records, to
                    //  specify that the next bunch are empty but styled
                    // Expand this out into multiple blank cells
                    val mbr = record as MulBlankRecord
                    expandedRecords = RecordFactory.convertBlankRecords(mbr)
                }

                MulRKRecord.sid -> {
                    // This is multiple consecutive number cells in one record
                    // Exand this out into multiple regular number cells
                    val mrk = record as MulRKRecord
                    expandedRecords = RecordFactory.convertRKRecords(mrk)
                }

                NoteRecord.sid -> {
                    val nrec = record as NoteRecord
                    thisRow = nrec.getRow()
                    thisColumn = nrec.getColumn()
                }
            }
        }

        // First part of expanded record handling
        if (expandedRecords != null && expandedRecords.size > 0) {
            thisRow = expandedRecords[0]!!.row
            thisColumn = expandedRecords[0]!!.column.toInt()
        }

        // If we're on cells, and this cell isn't in the same
        //  row as the last one, then fire the
        //  dummy end-of-row records
        if (thisRow != lastCellRow && lastCellRow > -1) {
            for (i in lastCellRow..<thisRow) {
                var cols = -1
                if (i == lastCellRow) {
                    cols = lastCellColumn
                }
                childListener.processRecord(LastCellOfRowDummyRecord(i, cols))
            }
        }

        // If we've just finished with the cells, then fire the
        // final dummy end-of-row record
        if (lastCellRow != -1 && lastCellColumn != -1 && thisRow == -1) {
            childListener.processRecord(LastCellOfRowDummyRecord(lastCellRow, lastCellColumn))

            lastCellRow = -1
            lastCellColumn = -1
        }

        // If we've moved onto a new row, the ensure we re-set
        //  the column counter
        if (thisRow != lastCellRow) {
            lastCellColumn = -1
        }

        // If there's a gap in the cells, then fire
        //  the dummy cell records
        if (lastCellColumn != thisColumn - 1) {
            for (i in lastCellColumn + 1..<thisColumn) {
                childListener.processRecord(MissingCellDummyRecord(thisRow, i))
            }
        }

        // Next part of expanded record handling
        if (expandedRecords != null && expandedRecords.size > 0) {
            thisColumn = expandedRecords[expandedRecords.size - 1]!!.column.toInt()
        }


        // Update cell and row counts as needed
        if (thisColumn != -1) {
            lastCellColumn = thisColumn
            lastCellRow = thisRow
        }

        // Pass along the record(s)
        if (expandedRecords != null && expandedRecords.size > 0) {
            for (r in expandedRecords) {
                childListener.processRecord(r as Record?)
            }
        } else {
            childListener.processRecord(record)
        }
    }

    private fun resetCounts() {
        lastRowRow = -1
        lastCellRow = -1
        lastCellColumn = -1
    }
}
