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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.hssf.record.ArrayRecord
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.MergeCellsRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.SharedFormulaRecord
import com.wxiwei.office.fc.hssf.record.TableRecord
import com.wxiwei.office.fc.hssf.record.aggregates.MergedCellsTable
import com.wxiwei.office.fc.hssf.record.aggregates.SharedValueManager
import com.wxiwei.office.fc.ss.util.CellReference

/**
 * Segregates the 'Row Blocks' section of a single sheet into plain row/cell records and
 * shared formula records.
 * 
 * @author Josh Micich
 */
class RowBlocksReader(rs: RecordStream) {
    private val _plainRecords: MutableList<Record?>
    val sharedFormulaManager: SharedValueManager

    /**
     * Some unconventional apps place [MergeCellsRecord]s within the row block.  They
     * actually should be in the [MergedCellsTable] which is much later (see bug 45699).
     * @return any loose  <tt>MergeCellsRecord</tt>s found
     */
    val looseMergedCells: Array<MergeCellsRecord?>

    /**
     * Also collects any loose MergeCellRecords and puts them in the supplied
     * mergedCellsTable
     */
    init {
        val plainRecords: MutableList<Record?> = ArrayList<Record?>()
        val shFrmRecords: MutableList<Record?> = ArrayList<Record?>()
        val firstCellRefs: MutableList<CellReference?> = ArrayList<CellReference?>()
        val arrayRecords: MutableList<Record?> = ArrayList<Record?>()
        val tableRecords: MutableList<Record?> = ArrayList<Record?>()
        val mergeCellRecords: MutableList<Record?> = ArrayList<Record?>()

        var prevRec: Record? = null
        while (!RecordOrderer.isEndOfRowBlock(rs.peekNextSid())) {
            // End of row/cell records for the current sheet
            // Note - It is important that this code does not inadvertently add any sheet
            // records from a subsequent sheet.  For example, if SharedFormulaRecords
            // are taken from the wrong sheet, this could cause bug 44449.
            if (!rs.hasNext()) {
                throw RuntimeException("Failed to find end of row/cell records")
            }
            val rec = rs.next!!
            val dest: MutableList<Record?>?
            when (rec.getSid()) {
                MergeCellsRecord.sid -> dest = mergeCellRecords
                SharedFormulaRecord.sid -> {
                    dest = shFrmRecords
                    if (prevRec !is FormulaRecord) {
                        throw RuntimeException("Shared formula record should follow a FormulaRecord")
                    }
                    val fr = prevRec
                    firstCellRefs.add(CellReference(fr.row, fr.column))
                }

                ArrayRecord.sid -> dest = arrayRecords
                TableRecord.sid -> dest = tableRecords
                else -> dest = plainRecords
            }
            dest.add(rec)
            prevRec = rec
        }
        val sharedFormulaRecs = Array(shFrmRecords.size) { i -> shFrmRecords[i] as SharedFormulaRecord }
        val firstCells: Array<CellReference?> = firstCellRefs.toTypedArray()
        val arrayRecs: Array<ArrayRecord?> = Array(arrayRecords.size) { i -> arrayRecords[i] as ArrayRecord? }
        val tableRecs = Array(tableRecords.size) { i -> tableRecords[i] as TableRecord }

        _plainRecords = plainRecords
        this.sharedFormulaManager =
            SharedValueManager.create(sharedFormulaRecs, firstCells, arrayRecs, tableRecs)
        this.looseMergedCells = Array(mergeCellRecords.size) { i -> mergeCellRecords[i] as MergeCellsRecord? }
    }

    val plainRecordStream: RecordStream
        /**
         * @return a [RecordStream] containing all the non-[SharedFormulaRecord]
         * non-[ArrayRecord] and non-[TableRecord] Records.
         */
        get() = RecordStream(_plainRecords, 0)
}
