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

import com.wxiwei.office.fc.ddf.DefaultEscherRecordFactory
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherRecordFactory
import com.wxiwei.office.fc.ddf.NullEscherSerializationListener
import com.wxiwei.office.fc.hssf.util.LazilyConcatenatedByteArray
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * The escher container record is used to hold escher records.  It is abstract and
 * must be subclassed for maximum benefit.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Michael Zalewski (zalewski at optonline.net)
 */
abstract class AbstractEscherHolderRecord : Record {
    @JvmField
    val escherRecords: MutableList<EscherRecord>
    private val rawDataContainer = LazilyConcatenatedByteArray()

    constructor() {
        escherRecords = ArrayList<EscherRecord>()
    }

    constructor(`in`: RecordInputStream) {
        escherRecords = ArrayList<EscherRecord>()
        if (!DESERIALISE) {
            rawDataContainer.concatenate(`in`.readRemainder())
        } else {
            val data = `in`.readAllContinuedRemainder()
            convertToEscherRecords(0, data.size, data)
        }
    }

    protected fun convertRawBytesToEscherRecords() {
        val rawData = this.rawData!!
        convertToEscherRecords(0, rawData.size, rawData)
    }

    private fun convertToEscherRecords(offset: Int, size: Int, data: ByteArray?) {
        escherRecords.clear()
        val recordFactory: EscherRecordFactory = DefaultEscherRecordFactory()
        var pos = offset
        while (pos < offset + size) {
            val r: EscherRecord = recordFactory.createRecord(data, pos)!!
            val bytesRead = r.fillFields(data, pos, recordFactory)
            escherRecords.add(r)
            pos += bytesRead
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        val nl = System.getProperty("line.separator")
        buffer.append('['.toString() + getRecordName() + ']' + nl)
        if (escherRecords.size == 0) buffer.append("No Escher Records Decoded" + nl)
        val iterator = escherRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            buffer.append(r.toString())
        }
        buffer.append("[/" + getRecordName() + ']' + nl)

        return buffer.toString()
    }

    protected abstract fun getRecordName(): String

    override fun serialize(offset: Int, data: ByteArray): Int {
        putShort(data, 0 + offset, getSid())
        putShort(data, 2 + offset, (getRecordSize() - 4).toShort())
        val rawData = this.rawData
        if (escherRecords.size == 0 && rawData != null) {
            putShort(data, 0 + offset, getSid())
            putShort(data, 2 + offset, (getRecordSize() - 4).toShort())
            System.arraycopy(rawData, 0, data, 4 + offset, rawData.size)
            return rawData.size + 4
        }
        putShort(data, 0 + offset, getSid())
        putShort(data, 2 + offset, (getRecordSize() - 4).toShort())

        var pos = offset + 4
        val iterator = escherRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            pos += r.serialize(pos, data, NullEscherSerializationListener())
        }
        return getRecordSize()
    }

    override fun getRecordSize(): Int {
        val rawData = this.rawData
        if (escherRecords.size == 0 && rawData != null) {
            // XXX: It should be possible to derive this without concatenating the array, too.
            return rawData.size
        }
        var size = 0
        val iterator = escherRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next()
            size += r.recordSize
        }
        return size
    }


    abstract override fun getSid(): Short

    override fun clone(): Any {
        return cloneViaReserialise()
    }

    fun addEscherRecord(index: Int, element: EscherRecord) {
        escherRecords.add(index, element)
    }

    fun addEscherRecord(element: EscherRecord): Boolean {
        return escherRecords.add(element)
    }

    fun clearEscherRecords() {
        escherRecords.clear()
    }

    val escherContainer: EscherContainerRecord?
        /**
         * If we have a EscherContainerRecord as one of our
         * children (and most top level escher holders do),
         * then return that.
         */
        get() {
            val it = escherRecords.iterator()
            while (it.hasNext()) {
                val er = it.next()
                if (er is EscherContainerRecord) {
                    return er
                }
            }
            return null
        }

    /**
     * 
     * @return
     */
    fun getgetEscherContainers(): MutableList<EscherContainerRecord?> {
        val containers: MutableList<EscherContainerRecord?> = ArrayList<EscherContainerRecord?>()
        val it = escherRecords.iterator()
        while (it.hasNext()) {
            val er = it.next()
            if (er is EscherContainerRecord) {
                containers.add(er)
            }
        }

        return containers
    }

    /**
     * Descends into all our children, returning the
     * first EscherRecord with the given id, or null
     * if none found
     */
    fun findFirstWithId(id: Short): EscherRecord? {
        return findFirstWithId(id, this.escherRecords)
    }

    private fun findFirstWithId(id: Short, records: MutableList<EscherRecord>): EscherRecord? {
        // Check at our level
        run {
            val it: MutableIterator<EscherRecord> = records.iterator()
            while (it.hasNext()) {
                val r = it.next()
                if (r.recordId == id) {
                    return r
                }
            }
        }

        // Then check our children in turn
        val it: MutableIterator<EscherRecord> = records.iterator()
        while (it.hasNext()) {
            val r = it.next()
            if (r.isContainerRecord) {
                val found = findFirstWithId(id, r.childRecords)
                if (found != null) {
                    return found
                }
            }
        }

        // Not found in this lot
        return null
    }


    fun getEscherRecord(index: Int): EscherRecord? {
        return escherRecords.get(index)
    }

    /**
     * Big drawing group records are split but it's easier to deal with them
     * as a whole group so we need to join them together.
     */
    fun join(record: AbstractEscherHolderRecord) {
        rawDataContainer.concatenate(record.rawData!!)
    }

    fun processContinueRecord(record: ByteArray?) {
        rawDataContainer.concatenate(record!!)
    }

    var rawData: ByteArray?
        get() = rawDataContainer.toArray()
        set(rawData) {
            rawDataContainer.clear()
            rawDataContainer.concatenate(rawData!!)
        }

    /**
     * Convert raw data to escher records.
     */
    fun decode() {
        val rawData = this.rawData!!
        convertToEscherRecords(0, rawData.size, rawData)
    }

    companion object {
        private var DESERIALISE = false

        init {
            try {
                DESERIALISE = (System.getProperty("poi.deserialize.escher") != null)
            } catch (e: SecurityException) {
                DESERIALISE = false
            }
        }
    }
}
