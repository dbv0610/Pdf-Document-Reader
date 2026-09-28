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

import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.LittleEndianOutputStream
import java.io.ByteArrayOutputStream


/**
 * Subrecords are part of the OBJ class.
 */
abstract class SubRecord protected constructor() : Cloneable {
    /**
     * @return the size of the data for this record (which is always 4 bytes less than the total
     * record size).  Note however, that ushort encoded after the record sid is usually but not
     * always the data size.
     */
    abstract fun getDataSize(): Int
    fun serialize(): ByteArray {
        val size = getDataSize() + 4
        val baos = ByteArrayOutputStream(size)
        serialize(LittleEndianOutputStream(baos))
        if (baos.size() != size) {
            throw RuntimeException("write size mismatch")
        }
        return baos.toByteArray()
    }

    abstract fun serialize(out: LittleEndianOutput)
    public abstract override fun clone(): Any

    /**
     * Wether this record terminates the sub-record stream.
     * There are two cases when this method must be overridden and return `true`
     * - EndSubRecord (sid = 0x00)
     * - LbsDataSubRecord (sid = 0x12)
     * 
     * @return whether this record is the last in the sub-record stream
     */
    open fun isTerminating(): Boolean {
        return false
    }

    private class UnknownSubRecord(`in`: LittleEndianInput, sid: Int, size: Int) : SubRecord() {
        private val _sid: Int
        private val _data: ByteArray

        init {
            _sid = sid
            val buf = ByteArray(size)
            `in`.readFully(buf)
            _data = buf
        }

        override fun getDataSize(): Int {
            return _data.size
        }

        override fun serialize(out: LittleEndianOutput) {
            out.writeShort(_sid)
            out.writeShort(_data.size)
            out.write(_data)
        }

        override fun clone(): Any {
            return this
        }

        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append("sid=").append(shortToHex(_sid))
            sb.append(" size=").append(_data.size)
            sb.append(" : ").append(toHex(_data))
            sb.append("]\n")
            return sb.toString()
        }
    }

    companion object {
        /**
         * read a sub-record from the supplied stream
         * 
         * @param in    the stream to read from
         * @param cmoOt the objectType field of the containing CommonObjectDataSubRecord,
         * we need it to propagate to next sub-records as it defines what data follows
         * @return the created sub-record
         */
        fun createSubRecord(`in`: LittleEndianInput, cmoOt: Int): SubRecord {
            val sid = `in`.readUShort()
            val secondUShort =
                `in`.readUShort() // Often (but not always) the datasize for the sub-record

            when (sid.toShort()) {
                CommonObjectDataSubRecord.Companion.sid -> return CommonObjectDataSubRecord(
                    `in`,
                    secondUShort
                )

                EmbeddedObjectRefSubRecord.Companion.sid -> return EmbeddedObjectRefSubRecord(
                    `in`,
                    secondUShort
                )

                GroupMarkerSubRecord.Companion.sid -> return GroupMarkerSubRecord(
                    `in`,
                    secondUShort
                )

                EndSubRecord.Companion.sid -> return EndSubRecord(`in`, secondUShort)
                NoteStructureSubRecord.Companion.sid -> return NoteStructureSubRecord(
                    `in`,
                    secondUShort
                )

                LbsDataSubRecord.sid.toShort() -> return LbsDataSubRecord(`in`, secondUShort, cmoOt)
                FtCblsSubRecord.Companion.sid -> return FtCblsSubRecord(`in`, secondUShort)
            }
            return UnknownSubRecord(`in`, sid, secondUShort)
        }
    }
}
