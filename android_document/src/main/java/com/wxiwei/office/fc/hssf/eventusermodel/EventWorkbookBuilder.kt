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

import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.record.BoundSheetRecord
import com.wxiwei.office.fc.hssf.record.EOFRecord
import com.wxiwei.office.fc.hssf.record.ExternSheetRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.SSTRecord
import com.wxiwei.office.fc.hssf.record.SupBookRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook

/**
 * When working with the EventUserModel, if you want to
 * process formulas, you need an instance of
 * [InternalWorkbook] to pass to a [HSSFWorkbook],
 * to finally give to [HSSFFormulaParser],
 * and this will build you stub ones.
 * Since you're working with the EventUserModel, you
 * wouldn't want to get a full [InternalWorkbook] and
 * [HSSFWorkbook], as they would eat too much memory.
 * Instead, you should collect a few key records as they
 * go past, then call this once you have them to build a
 * stub [InternalWorkbook], and from that a stub
 * [HSSFWorkbook], to use with the [HSSFFormulaParser].
 * 
 * The records you should collect are:
 * * [ExternSheetRecord]
 * * [BoundSheetRecord]
 * You should probably also collect [SSTRecord],
 * but it's not required to pass this in.
 * 
 * To help, this class includes a HSSFListener wrapper
 * that will do the collecting for you.
 */
object EventWorkbookBuilder {
    /**
     * Creates a stub Workbook from the supplied records,
     * suitable for use with the [HSSFFormulaParser]
     * @param externs The ExternSheetRecords in your file
     * @param bounds The BoundSheetRecords in your file
     * @param sst The SSTRecord in your file.
     * @return A stub Workbook suitable for use with [HSSFFormulaParser]
     */
    fun createStubWorkbook(
        externs: Array<ExternSheetRecord>?,
        bounds: Array<BoundSheetRecord>?, sst: SSTRecord?
    ): InternalWorkbook {
        val wbRecords: MutableList<Record> = ArrayList<Record>()

        // Core Workbook records go first
        if (bounds != null) {
            for (i in bounds.indices) {
                wbRecords.add(bounds[i])
            }
        }
        if (sst != null) {
            wbRecords.add(sst)
        }

        // Now we can have the ExternSheetRecords,
        //  preceded by a SupBookRecord
        if (externs != null) {
            wbRecords.add(
                SupBookRecord.createInternalReferences(
                    externs.size.toShort()
                )
            )
            for (i in externs.indices) {
                wbRecords.add(externs[i])
            }
        }

        // Finally we need an EoF record
        wbRecords.add(EOFRecord.instance)

        return InternalWorkbook.createWorkbook(wbRecords)
    }

    /**
     * Creates a stub workbook from the supplied records,
     * suitable for use with the [HSSFFormulaParser]
     * @param externs The ExternSheetRecords in your file
     * @param bounds The BoundSheetRecords in your file
     * @return A stub Workbook suitable for use with [HSSFFormulaParser]
     */
    fun createStubWorkbook(
        externs: Array<ExternSheetRecord>?,
        bounds: Array<BoundSheetRecord>?
    ): InternalWorkbook {
        return createStubWorkbook(externs, bounds, null)
    }


    /**
     * A wrapping HSSFListener which will collect
     * [BoundSheetRecord]s and [ExternSheetRecord]s as
     * they go past, so you can create a Stub [InternalWorkbook] from
     * them once required.
     */
    class SheetRecordCollectingListener(private val childListener: HSSFListener) : HSSFListener {
        private val boundSheetRecords: MutableList<BoundSheetRecord> = ArrayList<BoundSheetRecord>()
        private val externSheetRecords: MutableList<ExternSheetRecord> = ArrayList<ExternSheetRecord>()
        var sSTRecord: SSTRecord? = null
            private set


        fun getBoundSheetRecords(): Array<BoundSheetRecord> {
            return boundSheetRecords.toTypedArray()
        }

        fun getExternSheetRecords(): Array<ExternSheetRecord> {
            return externSheetRecords.toTypedArray()
        }

        val stubHSSFWorkbook: HSSFWorkbook
            get() = HSSFWorkbook.create(this.stubWorkbook)
        val stubWorkbook: InternalWorkbook
            get() = createStubWorkbook(
                getExternSheetRecords(), getBoundSheetRecords(),
                this.sSTRecord
            )


        /**
         * Process this record ourselves, and then
         * pass it on to our child listener
         */
        override fun processRecord(record: Record?) {
            // Handle it ourselves
            processRecordInternally(record)

            // Now pass on to our child
            childListener.processRecord(record)
        }

        /**
         * Process the record ourselves, but do not
         * pass it on to the child Listener.
         */
        fun processRecordInternally(record: Record?) {
            if (record is BoundSheetRecord) {
                boundSheetRecords.add(record)
            } else if (record is ExternSheetRecord) {
                externSheetRecords.add(record)
            } else if (record is SSTRecord) {
                this.sSTRecord = record
            }
        }
    }
}
