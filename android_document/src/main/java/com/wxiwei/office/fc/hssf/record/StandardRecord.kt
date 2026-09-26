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

import com.wxiwei.office.fc.util.LittleEndianByteArrayOutputStream
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Subclasses of this class (the majority of BIFF records) are non-continuable.  This allows for
 * some simplification of serialization logic
 * 
 * @author Josh Micich
 */
abstract class StandardRecord : Record() {
    protected abstract fun getDataSize(): Int
    override fun getRecordSize(): Int {
        return 4 + getDataSize()
    }

    override fun serialize(offset: Int, data: ByteArray): Int {
        val dataSize = getDataSize()
        val recSize = 4 + dataSize
        val out = LittleEndianByteArrayOutputStream(data, offset, recSize)
        out.writeShort(getSid().toInt())
        out.writeShort(dataSize)
        serialize(out)
        check(out.getWriteIndex() - offset == recSize) {
            ("Error in serialization of (" + javaClass.getName() + "): "
                    + "Incorrect number of bytes written - expected "
                    + recSize + " but got " + (out.getWriteIndex() - offset))
        }
        return recSize
    }

    /**
     * Write the data content of this BIFF record.  The 'ushort sid' and 'ushort size' header fields
     * have already been written by the superclass.<br></br>
     * 
     * The subclass must write the exact number of bytes as reported by [Record.getRecordSize]}
     */
    protected abstract fun serialize(out: LittleEndianOutput)
}
