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

import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.record.CRNCountRecord
import com.wxiwei.office.fc.hssf.record.CRNRecord
import com.wxiwei.office.fc.hssf.record.CountryRecord
import com.wxiwei.office.fc.hssf.record.ExternSheetRecord
import com.wxiwei.office.fc.hssf.record.ExternalNameRecord
import com.wxiwei.office.fc.hssf.record.NameCommentRecord
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.SupBookRecord

/**
 * Link Table (OOO pdf reference: 4.10.3 ) 
 *
 *
 * 
 * The main data of all types of references is stored in the Link Table inside the Workbook Globals
 * Substream (4.2.5). The Link Table itself is optional and occurs only, if  there are any
 * references in the document.
 * 
 * 
 * 
 * In BIFF8 the Link Table consists of
 * 
 *  * zero or more EXTERNALBOOK Blocks
 *
 *
 * each consisting of
 * 
 *  * exactly one EXTERNALBOOK (0x01AE) record
 *  * zero or more EXTERNALNAME (0x0023) records
 *  * zero or more CRN Blocks
 *
 *
 * each consisting of
 * 
 *  * exactly one XCT (0x0059)record
 *  * zero or more CRN (0x005A) records (documentation says one or more)
 * 
 * 
 * 
 * 
 *  * zero or one EXTERNSHEET (0x0017) record
 *  * zero or more DEFINEDNAME (0x0018) records
 * 
 * 
 * 
 * @author Josh Micich
 */
internal class LinkTable {
    // TODO make this class into a record aggregate
    private class CRNBlock(rs: RecordStream) {
        private val _countRecord: CRNCountRecord
        private val _crns: Array<CRNRecord?>

        init {
            _countRecord = rs.next as CRNCountRecord
            val nCRNs = _countRecord.numberOfCRNs
            val crns = arrayOfNulls<CRNRecord>(nCRNs)
            for (i in crns.indices) {
                crns[i] = rs.next as CRNRecord?
            }
            _crns = crns
        }

        val crns: Array<CRNRecord?>?
            get() = _crns.clone()
    }

    private class ExternalBookBlock {
        val externalBookRecord: SupBookRecord
        private var _externalNameRecords: Array<ExternalNameRecord?>
        private val _crnBlocks: Array<CRNBlock?>

        constructor(rs: RecordStream) {
            this.externalBookRecord = rs.next as SupBookRecord
            val temp: MutableList<Any?> = ArrayList<Any?>()
            while (rs.peekNextClass() == ExternalNameRecord::class.java) {
                temp.add(rs.next)
            }
            _externalNameRecords = Array(temp.size) { i -> temp[i] as ExternalNameRecord? }

            temp.clear()

            while (rs.peekNextClass() == CRNCountRecord::class.java) {
                temp.add(CRNBlock(rs))
            }
            _crnBlocks = Array(temp.size) { i -> temp[i] as CRNBlock? }
        }

        /**
         * Create a new block for internal references. It is called when constructing a new LinkTable.
         * 
         * @see LinkTable.LinkTable
         */
        constructor(numberOfSheets: Int) {
            this.externalBookRecord =
                SupBookRecord.createInternalReferences(numberOfSheets.toShort())
            _externalNameRecords = arrayOfNulls<ExternalNameRecord>(0)
            _crnBlocks = arrayOfNulls<CRNBlock>(0)
        }

        /**
         * Create a new block for registering add-in functions
         * 
         * @see addNameXPtg
         */
        constructor() {
            this.externalBookRecord = SupBookRecord.createAddInFunctions()
            _externalNameRecords = arrayOfNulls<ExternalNameRecord>(0)
            _crnBlocks = arrayOfNulls<CRNBlock>(0)
        }

        fun getNameText(definedNameIndex: Int): String? {
            return _externalNameRecords[definedNameIndex]!!.getText()
        }

        fun getNameIx(definedNameIndex: Int): Int {
            return _externalNameRecords[definedNameIndex]!!.getIx().toInt()
        }

        /**
         * Performs case-insensitive search
         * @return -1 if not found
         */
        fun getIndexOfName(name: String?): Int {
            for (i in _externalNameRecords.indices) {
                if (_externalNameRecords[i]!!.getText().equals(name, ignoreCase = true)) {
                    return i
                }
            }
            return -1
        }

        val numberOfNames: Int
            get() = _externalNameRecords.size

        fun addExternalName(rec: ExternalNameRecord?): Int {
            val tmp = arrayOfNulls<ExternalNameRecord>(_externalNameRecords.size + 1)
            System.arraycopy(_externalNameRecords, 0, tmp, 0, _externalNameRecords.size)
            tmp[tmp.size - 1] = rec
            _externalNameRecords = tmp
            return _externalNameRecords.size - 1
        }
    }

    private var _externalBookBlocks: Array<ExternalBookBlock?>
    private val _externSheetRecord: ExternSheetRecord?
    private val _definedNames: MutableList<NameRecord>

    /**
     * TODO - would not be required if calling code used RecordStream or similar
     */
    val recordCount: Int
    private val _workbookRecordList: WorkbookRecordList // TODO - would be nice to remove this

    constructor(
        inputList: List<Record>,
        startIndex: Int,
        workbookRecordList: WorkbookRecordList,
        commentRecords: MutableMap<String?, NameCommentRecord?>
    ) {
        _workbookRecordList = workbookRecordList
        val rs = RecordStream(inputList, startIndex)

        val temp: MutableList<ExternalBookBlock?> = ArrayList<ExternalBookBlock?>()
        while (rs.peekNextClass() == SupBookRecord::class.java) {
            temp.add(ExternalBookBlock(rs))
        }

        _externalBookBlocks = temp.toTypedArray()
        temp.clear()

        if (_externalBookBlocks.size > 0) {
            // If any ExternalBookBlock present, there is always 1 of ExternSheetRecord
            if (rs.peekNextClass() != ExternSheetRecord::class.java) {
                // not quite - if written by google docs
                _externSheetRecord = null
            } else {
                _externSheetRecord = readExtSheetRecord(rs)
            }
        } else {
            _externSheetRecord = null
        }

        _definedNames = ArrayList<NameRecord>()
        // collect zero or more DEFINEDNAMEs id=0x18,
        //  with their comments if present
        while (true) {
            val nextClass: Class<*>? = rs.peekNextClass()
            if (nextClass == NameRecord::class.java) {
                val nr = rs.next as NameRecord?
                _definedNames.add(nr!!)
            } else if (nextClass == NameCommentRecord::class.java) {
                val ncr = rs.next as NameCommentRecord
                commentRecords.put(ncr.getNameText(), ncr)
            } else {
                break
            }
        }

        this.recordCount = rs.countRead
        _workbookRecordList.records
            .addAll(inputList.subList(startIndex, startIndex + this.recordCount))
    }

    constructor(numberOfSheets: Int, workbookRecordList: WorkbookRecordList) {
        _workbookRecordList = workbookRecordList
        _definedNames = ArrayList<NameRecord>()
        _externalBookBlocks = arrayOf<ExternalBookBlock?>(
            ExternalBookBlock(numberOfSheets),
        )
        _externSheetRecord = ExternSheetRecord()
        this.recordCount = 2

        // tell _workbookRecordList about the 2 new records
        val supbook = _externalBookBlocks[0]!!.externalBookRecord

        val idx = findFirstRecordLocBySid(CountryRecord.sid)
        if (idx < 0) {
            throw RuntimeException("CountryRecord not found")
        }
        _workbookRecordList.add(idx + 1, _externSheetRecord)
        _workbookRecordList.add(idx + 1, supbook)
    }


    /**
     * @param builtInCode a BUILTIN_~ constant from [NameRecord]
     * @param sheetNumber 1-based sheet number
     */
    fun getSpecificBuiltinRecord(builtInCode: Byte, sheetNumber: Int): NameRecord? {
        val iterator: MutableIterator<*> = _definedNames.iterator()
        while (iterator.hasNext()) {
            val record = iterator.next() as NameRecord

            //print areas are one based
            if (record.getBuiltInName() == builtInCode && record.getSheetNumber() == sheetNumber) {
                return record
            }
        }

        return null
    }

    fun removeBuiltinRecord(name: Byte, sheetIndex: Int) {
        //the name array is smaller so searching through it should be faster than
        //using the findFirstXXXX methods
        val record = getSpecificBuiltinRecord(name, sheetIndex)
        if (record != null) {
            _definedNames.remove(record)
        }
        // TODO - do we need "Workbook.records.remove(...);" similar to that in Workbook.removeName(int namenum) {}?
    }

    val numNames: Int
        get() = _definedNames.size

    fun getNameRecord(index: Int): NameRecord {
        return _definedNames.get(index)
    }

    fun addName(name: NameRecord?) {
        _definedNames.add(name!!)

        // TODO - this is messy
        // Not the most efficient way but the other way was causing too many bugs
        var idx = findFirstRecordLocBySid(ExternSheetRecord.sid)
        if (idx == -1) idx = findFirstRecordLocBySid(SupBookRecord.sid)
        if (idx == -1) idx = findFirstRecordLocBySid(CountryRecord.sid)
        val countNames = _definedNames.size
        _workbookRecordList.add(idx + countNames, name)
    }

    fun removeName(namenum: Int) {
        _definedNames.removeAt(namenum)
    }

    /**
     * checks if the given name is already included in the linkTable
     */
    fun nameAlreadyExists(name: NameRecord): Boolean {
        // Check to ensure no other names have the same case-insensitive name
        for (i in this.numNames - 1 downTo 0) {
            val rec = getNameRecord(i)
            if (rec != name) {
                if (isDuplicatedNames(name, rec)) return true
            }
        }
        return false
    }

    fun getExternalBookAndSheetName(extRefIndex: Int): Array<String?>? {
        val ebIx = _externSheetRecord!!.getExtbookIndexFromRefIndex(extRefIndex)
        val ebr = _externalBookBlocks[ebIx]!!.externalBookRecord
        if (!ebr.isExternalReferences()) {
            return null
        }
        // Sheet name only applies if not a global reference
        val shIx = _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex)
        var usSheetName: String? = null
        if (shIx >= 0) {
            usSheetName = ebr.getSheetNames()!![shIx]
        }
        return arrayOf<String?>(
            ebr.getURL(),
            usSheetName,
        )
    }

    fun getExternalSheetIndex(workbookName: String?, sheetName: String?): Int {
        var ebrTarget: SupBookRecord? = null
        var externalBookIndex = -1
        for (i in _externalBookBlocks.indices) {
            val ebr = _externalBookBlocks[i]!!.externalBookRecord
            if (!ebr.isExternalReferences()) {
                continue
            }
            if (workbookName == ebr.getURL()) { // not sure if 'equals()' works when url has a directory
                ebrTarget = ebr
                externalBookIndex = i
                break
            }
        }
        if (ebrTarget == null) {
            throw RuntimeException("No external workbook with name '" + workbookName + "'")
        }
        val sheetIndex: Int = getSheetIndex(ebrTarget.getSheetNames()!!, sheetName)

        val result = _externSheetRecord!!.getRefIxForSheet(externalBookIndex, sheetIndex)
        if (result < 0) {
            throw RuntimeException(
                ("ExternSheetRecord does not contain combination ("
                        + externalBookIndex + ", " + sheetIndex + ")")
            )
        }
        return result
    }

    /**
     * @param extRefIndex as from a [Ref3DPtg] or [Area3DPtg]
     * @return -1 if the reference is to an external book
     */
    fun getIndexToInternalSheet(extRefIndex: Int): Int {
        return _externSheetRecord!!.getFirstSheetIndexFromRefIndex(extRefIndex)
    }

    fun getSheetIndexFromExternSheetIndex(extRefIndex: Int): Int {
        if (extRefIndex >= _externSheetRecord!!.getNumOfRefs()) {
            return -1
        }
        return _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex)
    }

    fun checkExternSheet(sheetIndex: Int): Int {
        var thisWbIndex = -1 // this is probably always zero
        for (i in _externalBookBlocks.indices) {
            val ebr = _externalBookBlocks[i]!!.externalBookRecord
            if (ebr.isInternalReferences()) {
                thisWbIndex = i
                break
            }
        }
        if (thisWbIndex < 0) {
            throw RuntimeException("Could not find 'internal references' EXTERNALBOOK")
        }

        //Trying to find reference to this sheet
        val i = _externSheetRecord!!.getRefIxForSheet(thisWbIndex, sheetIndex)
        if (i >= 0) {
            return i
        }
        //We haven't found reference to this sheet
        return _externSheetRecord.addRef(thisWbIndex, sheetIndex, sheetIndex)
    }


    /**
     * copied from Workbook
     */
    private fun findFirstRecordLocBySid(sid: Short): Int {
        var index = 0
        val iterator: MutableIterator<*> = _workbookRecordList.iterator()
        while (iterator.hasNext()) {
            val record = iterator.next() as Record

            if (record.getSid() == sid) {
                return index
            }
            index++
        }
        return -1
    }

    fun resolveNameXText(refIndex: Int, definedNameIndex: Int): String? {
        val extBookIndex = _externSheetRecord!!.getExtbookIndexFromRefIndex(refIndex)
        return _externalBookBlocks[extBookIndex]!!.getNameText(definedNameIndex)
    }

    fun resolveNameXIx(refIndex: Int, definedNameIndex: Int): Int {
        val extBookIndex = _externSheetRecord!!.getExtbookIndexFromRefIndex(refIndex)
        return _externalBookBlocks[extBookIndex]!!.getNameIx(definedNameIndex)
    }

    fun getNameXPtg(name: String?): NameXPtg? {
        // first find any external book block that contains the name:
        for (i in _externalBookBlocks.indices) {
            val definedNameIndex = _externalBookBlocks[i]!!.getIndexOfName(name)
            if (definedNameIndex < 0) {
                continue
            }
            // found it.
            val sheetRefIndex = findRefIndexFromExtBookIndex(i)
            if (sheetRefIndex >= 0) {
                return NameXPtg(sheetRefIndex, definedNameIndex)
            }
        }
        return null
    }

    /**
     * Register an external name in this workbook
     * 
     * @param name  the name to register
     * @return a NameXPtg describing this name
     */
    fun addNameXPtg(name: String?): NameXPtg {
        var extBlockIndex = -1
        var extBlock: ExternalBookBlock? = null

        // find ExternalBlock for Add-In functions and remember its index
        for (i in _externalBookBlocks.indices) {
            val ebr = _externalBookBlocks[i]!!.externalBookRecord
            if (ebr.isAddInFunctions()) {
                extBlock = _externalBookBlocks[i]
                extBlockIndex = i
                break
            }
        }
        // An ExternalBlock for Add-In functions was not found. Create a new one.
        if (extBlock == null) {
            extBlock = ExternalBookBlock()

            val tmp: Array<ExternalBookBlock?> =
                arrayOfNulls<ExternalBookBlock>(_externalBookBlocks.size + 1)
            System.arraycopy(_externalBookBlocks, 0, tmp, 0, _externalBookBlocks.size)
            tmp[tmp.size - 1] = extBlock
            _externalBookBlocks = tmp

            extBlockIndex = _externalBookBlocks.size - 1

            // add the created SupBookRecord before ExternSheetRecord
            val idx = findFirstRecordLocBySid(ExternSheetRecord.sid)
            _workbookRecordList.add(idx, extBlock.externalBookRecord)

            // register the SupBookRecord in the ExternSheetRecord
            // -2 means that the scope of this name is Workbook and the reference applies to the entire workbook.
            _externSheetRecord!!.addRef(_externalBookBlocks.size - 1, -2, -2)
        }

        // create a ExternalNameRecord that will describe this name
        val extNameRecord = ExternalNameRecord()
        extNameRecord.setText(name!!)
        // The docs don't explain why Excel set the formula to #REF!
        extNameRecord.setParsedExpression(arrayOf<Ptg?>(ErrPtg.REF_INVALID))

        val nameIndex = extBlock.addExternalName(extNameRecord)
        var supLinkIndex = 0
        // find the posistion of the Add-In SupBookRecord in the workbook stream,
        // the created ExternalNameRecord will be appended to it
        val iterator: MutableIterator<*> = _workbookRecordList.iterator()
        while (iterator.hasNext()) {
            val record = iterator.next() as Record?
            if (record is SupBookRecord) {
                if (record.isAddInFunctions()) break
            }
            supLinkIndex++
        }
        val numberOfNames = extBlock.numberOfNames
        // a new name is inserted in the end of the SupBookRecord, after the last name
        _workbookRecordList.add(supLinkIndex + numberOfNames, extNameRecord)
        val ix = _externSheetRecord!!.getRefIxForSheet(extBlockIndex, -2 /* the scope is workbook*/)
        return NameXPtg(ix, nameIndex)
    }

    private fun findRefIndexFromExtBookIndex(extBookIndex: Int): Int {
        return _externSheetRecord!!.findRefIndexFromExtBookIndex(extBookIndex)
    }

    companion object {
        private fun readExtSheetRecord(rs: RecordStream): ExternSheetRecord? {
            val temp: MutableList<ExternSheetRecord?> = ArrayList<ExternSheetRecord?>(2)
            while (rs.peekNextClass() == ExternSheetRecord::class.java) {
                temp.add(rs.next as ExternSheetRecord?)
            }

            val nItems = temp.size
            if (nItems < 1) {
                throw RuntimeException(
                    ("Expected an EXTERNSHEET record but got ("
                            + rs.peekNextClass()!!.name + ")")
                )
            }
            if (nItems == 1) {
                // this is the normal case. There should be just one ExternSheetRecord
                return temp.get(0)
            }
            // Some apps generate multiple ExternSheetRecords (see bug 45698).
            // It seems like the best thing to do might be to combine these into one
            val esrs = Array(nItems) { i -> temp[i]!! }
            return ExternSheetRecord.combine(esrs)
        }

        private fun isDuplicatedNames(firstName: NameRecord, lastName: NameRecord): Boolean {
            return lastName.getNameText().equals(firstName.getNameText(), ignoreCase = true)
                    && isSameSheetNames(firstName, lastName)
        }

        private fun isSameSheetNames(firstName: NameRecord, lastName: NameRecord): Boolean {
            return lastName.getSheetNumber() == firstName.getSheetNumber()
        }

        private fun getSheetIndex(sheetNames: Array<String?>, sheetName: String?): Int {
            for (i in sheetNames.indices) {
                if (sheetNames[i] == sheetName) {
                    return i
                }
            }
            throw RuntimeException("External workbook does not contain sheet '" + sheetName + "'")
        }
    }
}
