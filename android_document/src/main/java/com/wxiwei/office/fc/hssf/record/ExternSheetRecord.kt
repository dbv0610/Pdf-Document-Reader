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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * EXTERNSHEET (0x0017)<br></br>
 * A List of Indexes to  EXTERNALBOOK (supplemental book) Records 
 *
 *
 * 
 * @author Libin Roman (Vista Portal LDT. Developer)
 */
class ExternSheetRecord : StandardRecord {
    private val _list: MutableList<RefSubRecord>

    class RefSubRecord(
        private val _extBookIndex: Int,
        private val _firstSheetIndex: Int,
        private val _lastSheetIndex: Int
    ) {

        /**
         * @param in the RecordInputstream to read the record from
         */
        constructor(`in`: RecordInputStream) : this(
            `in`.readShort().toInt(),
            `in`.readShort().toInt(),
            `in`.readShort().toInt()
        )

        fun getExtBookIndex(): Int {
            return _extBookIndex
        }

        fun getFirstSheetIndex(): Int {
            return _firstSheetIndex
        }

        fun getLastSheetIndex(): Int {
            return _lastSheetIndex
        }

        override fun toString(): String {
            val buffer = StringBuffer()
            buffer.append("extBook=").append(_extBookIndex)
            buffer.append(" firstSheet=").append(_firstSheetIndex)
            buffer.append(" lastSheet=").append(_lastSheetIndex)
            return buffer.toString()
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(_extBookIndex)
            out.writeShort(_firstSheetIndex)
            out.writeShort(_lastSheetIndex)
        }

        companion object {
            const val ENCODED_SIZE: Int = 6
        }
    }


    constructor() {
        _list = ArrayList<RefSubRecord>()
    }

    constructor(`in`: RecordInputStream) {
        _list = ArrayList<RefSubRecord>()

        val nItems = `in`.readShort().toInt()

        for (i in 0..<nItems) {
            val rec = RefSubRecord(`in`)
            _list.add(rec)
        }
    }


    /**
     * @return number of REF structures
     */
    fun getNumOfRefs(): Int {
        return _list.size
    }

    /**
     * adds REF struct (ExternSheetSubRecord)
     * @param rec REF struct
     */
    fun addREFRecord(rec: RefSubRecord?) {
        _list.add(rec!!)
    }

    /** returns the number of REF Records, which is in model
     * @return number of REF records
     */
    fun getNumOfREFRecords(): Int {
        return _list.size
    }


    override fun toString(): String {
        val sb = StringBuffer()
        val nItems = _list.size
        sb.append("[EXTERNSHEET]\n")
        sb.append("   numOfRefs     = ").append(nItems).append("\n")
        for (i in 0..<nItems) {
            sb.append("refrec         #").append(i).append(": ")
            sb.append(getRef(i).toString())
            sb.append('\n')
        }
        sb.append("[/EXTERNSHEET]\n")


        return sb.toString()
    }

    override fun getDataSize(): Int {
        return 2 + _list.size * RefSubRecord.ENCODED_SIZE
    }

    public override fun serialize(out: LittleEndianOutput) {
        val nItems = _list.size

        out.writeShort(nItems)

        for (i in 0..<nItems) {
            getRef(i).serialize(out)
        }
    }

    private fun getRef(i: Int): RefSubRecord {
        return _list.get(i)
    }

    /**
     * return the non static version of the id for this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    fun getExtbookIndexFromRefIndex(refIndex: Int): Int {
        return getRef(refIndex).getExtBookIndex()
    }

    /**
     * @return -1 if not found
     */
    fun findRefIndexFromExtBookIndex(extBookIndex: Int): Int {
        val nItems = _list.size
        for (i in 0..<nItems) {
            if (getRef(i).getExtBookIndex() == extBookIndex) {
                return i
            }
        }
        return -1
    }

    fun getFirstSheetIndexFromRefIndex(extRefIndex: Int): Int {
        return getRef(extRefIndex).getFirstSheetIndex()
    }

    /**
     * Add a zero-based reference to a [SupBookRecord].
     * 
     * 
     * If the type of the SupBook record is same-sheet referencing, Add-In referencing,
     * DDE data source referencing, or OLE data source referencing,
     * then no scope is specified and this value *MUST* be -2. Otherwise,
     * the scope must be set as follows:
     * 
     *  1. `-2` Workbook-level reference that applies to the entire workbook.
     *  1. `-1` Sheet-level reference. 
     *  1. `>=0` Sheet-level reference. This specifies the first sheet in the reference.
     * 
     * 
     * If the SupBook type is unused or external workbook referencing,
     * then this value specifies the zero-based index of an external sheet name,
     * see [SupBookRecord.getSheetNames].
     * This referenced string specifies the name of the first sheet within the external workbook that is in scope.
     * This sheet MUST be a worksheet or macro sheet.
     * 
     * 
     * 
     * 
     * If the supporting link type is self-referencing, then this value specifies the zero-based index of a
     * [BoundSheetRecord] record in the workbook stream that specifies
     * the first sheet within the scope of this reference. This sheet MUST be a worksheet or a macro sheet.
     * 
     * 
     * 
     * 
     * @param firstSheetIndex  the scope, must be -2 for add-in references
     * @param lastSheetIndex   the scope, must be -2 for add-in references
     * @return index of newly added ref
     */
    fun addRef(extBookIndex: Int, firstSheetIndex: Int, lastSheetIndex: Int): Int {
        _list.add(RefSubRecord(extBookIndex, firstSheetIndex, lastSheetIndex))
        return _list.size - 1
    }

    fun getRefIxForSheet(externalBookIndex: Int, sheetIndex: Int): Int {
        val nItems = _list.size
        for (i in 0..<nItems) {
            val ref = getRef(i)
            if (ref.getExtBookIndex() != externalBookIndex) {
                continue
            }
            if (ref.getFirstSheetIndex() == sheetIndex && ref.getLastSheetIndex() == sheetIndex) {
                return i
            }
        }
        return -1
    }

    companion object {
        const val sid: Short = 0x0017
        fun combine(esrs: Array<ExternSheetRecord>): ExternSheetRecord {
            val result = ExternSheetRecord()
            for (i in esrs.indices) {
                val esr = esrs[i]
                val nRefs = esr.getNumOfREFRecords()
                for (j in 0..<nRefs) {
                    result.addREFRecord(esr.getRef(j))
                }
            }
            return result
        }
    }
}
