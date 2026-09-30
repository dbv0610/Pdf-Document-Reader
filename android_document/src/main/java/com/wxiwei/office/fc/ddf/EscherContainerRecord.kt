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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.PrintWriter

/**
 * Escher container records store other escher records as children.
 * The container records themselves never store any information beyond
 * the standard header used by all escher records.  This one record is
 * used to represent many different types of records.
 * 
 * @author Glen Stampoultzis
 */
class EscherContainerRecord : EscherRecord() {
    private val _childRecords: MutableList<EscherRecord> = ArrayList<EscherRecord>()

    override fun fillFields(
        data: ByteArray?,
        pOffset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var bytesRemaining = readHeader(data, pOffset)
        var bytesWritten = 8
        var offset = pOffset + 8
        while (bytesRemaining > 0 && offset < data.size) {
            val child = recordFactory!!.createRecord(data, offset)
            val childBytesWritten = child.fillFields(data, offset, recordFactory)
            bytesWritten += childBytesWritten
            offset += childBytesWritten
            bytesRemaining -= childBytesWritten
            addChildRecord(child)
            if (offset >= data.size && bytesRemaining > 0) {
//                System.out.println("WARNING: " + bytesRemaining
//                    + " bytes remaining but no space left");
            }
        }
        return bytesWritten
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        var remainingBytes = 0
        var iterator = _childRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            remainingBytes += r.recordSize
        }
        putInt(data, offset + 4, remainingBytes)
        var pos = offset + 8
        iterator = _childRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            pos += r.serialize(pos, data, listener)
        }

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    override val recordSize: Int
        get() {
        var childRecordsSize = 0
        val iterator = _childRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            childRecordsSize += r.recordSize
        }
        return 8 + childRecordsSize
    }

    /**
     * Do any of our (top level) children have the
     * given recordId?
     */
    fun hasChildOfType(recordId: Short): Boolean {
        val iterator = _childRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            if (r.recordId == recordId) {
                return true
            }
        }
        return false
    }

    override fun getChild(index: Int): EscherRecord? {
        return _childRecords.get(index)
    }

    /**
     * @return a copy of the list of all the child records of the container.
     */
    override var childRecords: MutableList<EscherRecord>
        get() = ArrayList<EscherRecord>(_childRecords)
        /**
         * replaces the internal child list with the contents of the supplied <tt>childRecords</tt>
         */
        set(childRecords) {
            check(childRecords !== _childRecords) { "Child records private data member has escaped" }
            _childRecords.clear()
            _childRecords.addAll(childRecords)
        }

    val childIterator: MutableIterator<EscherRecord?>
        get() = ReadOnlyIterator(_childRecords)

    private class ReadOnlyIterator(private val _list: MutableList<EscherRecord>) :
        MutableIterator<EscherRecord?> {
        private var _index = 0

        override fun hasNext(): Boolean {
            return _index < _list.size
        }

        override fun next(): EscherRecord? {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            return _list.get(_index++)
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }


    fun removeChildRecord(toBeRemoved: EscherRecord?): Boolean {
        return _childRecords.remove(toBeRemoved)
    }

    val childContainers: MutableList<EscherContainerRecord?>
        /**
         * Returns all of our children which are also
         * EscherContainers (may be 0, 1, or vary rarely
         * 2 or 3)
         */
        get() {
            val containers: MutableList<EscherContainerRecord?> =
                ArrayList<EscherContainerRecord?>()
            val iterator =
                _childRecords.iterator()
            while (iterator.hasNext()) {
                val r = iterator.next()
                if (r is EscherContainerRecord) {
                    containers.add(r)
                }
            }
            return containers
        }

    override val recordName: String
        get() {
        when (recordId) {
            DGG_CONTAINER -> return "DggContainer"
            BSTORE_CONTAINER -> return "BStoreContainer"
            DG_CONTAINER -> return "DgContainer"
            SPGR_CONTAINER -> return "SpgrContainer"
            SP_CONTAINER -> return "SpContainer"
            SOLVER_CONTAINER -> return "SolverContainer"
            else -> return "Container 0x" + toHex(recordId)
        }
    }

    override fun display(w: PrintWriter, indent: Int) {
        super.display(w, indent)
        val iterator = _childRecords.iterator()
        while (iterator.hasNext()) {
            val escherRecord = iterator.next()
            escherRecord.display(w, indent + 1)
        }
    }

    fun addChildRecord(record: EscherRecord?) {
        _childRecords.add(record!!)
    }

    fun addChildBefore(record: EscherRecord?, insertBeforeRecordId: Int) {
        var i = 0
        while (i < _childRecords.size) {
            val rec = _childRecords.get(i)
            if (rec.recordId.toInt() == insertBeforeRecordId) {
                _childRecords.add(i++, record!!)
                // TODO - keep looping? Do we expect multiple matches?
            }
            i++
        }
    }

    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        val children = StringBuffer()
        if (_childRecords.size > 0) {
            children.append("  children: " + nl)

            var count = 0
            val iterator = _childRecords.iterator()
            while (iterator.hasNext()) {
                val record = iterator.next()
                children.append("   Child " + count + ":" + nl)
                var childResult = record.toString()
                childResult = childResult.replace("\n".toRegex(), "\n    ")
                children.append("    ")
                children.append(childResult)
                children.append(nl)
                count++
            }
        }

        return (javaClass.getName() + " (" + recordName + "):" + nl + "  isContainer: "
                + isContainerRecord + nl + "  options: 0x" + toHex(options) + nl
                + "  recordId: 0x" + toHex(recordId) + nl + "  numchildren: "
                + _childRecords.size + nl + children.toString())
    }

    fun <T : EscherRecord?> getChildById(recordId: Short): T? {
        for (childRecord in _childRecords) {
            if (childRecord.recordId == recordId) {
                val result = childRecord as T?
                return result
            }
        }
        return null
    }

    /**
     * Recursively find records with the specified record ID
     * 
     * @param out - list to store found records
     */
    fun getRecordsById(recordId: Short, out: MutableList<EscherRecord?>) {
        val iterator = _childRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            if (r is EscherContainerRecord) {
                val c = r
                c.getRecordsById(recordId, out)
            } else if (r.recordId == recordId) {
                out.add(r)
            }
        }
    }

    /**
     * 
     */
    override fun dispose() {
        if (_childRecords != null) {
            for (i in _childRecords.indices) {
                _childRecords.get(i).dispose()
            }
            _childRecords.clear()
        }
    }

    companion object {
        val DGG_CONTAINER: Short = 0xF000.toShort()
        @JvmField
        val BSTORE_CONTAINER: Short = 0xF001.toShort()
        @JvmField
        val DG_CONTAINER: Short = 0xF002.toShort()
        @JvmField
        val SPGR_CONTAINER: Short = 0xF003.toShort()
        @JvmField
        val SP_CONTAINER: Short = 0xF004.toShort()
        val SOLVER_CONTAINER: Short = 0xF005.toShort()
    }
}
