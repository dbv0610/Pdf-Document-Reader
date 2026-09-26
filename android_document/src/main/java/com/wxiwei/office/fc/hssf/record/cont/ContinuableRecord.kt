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
package com.wxiwei.office.fc.hssf.record.cont

import com.wxiwei.office.fc.hssf.record.ContinueRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.util.LittleEndianByteArrayOutputStream
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Common superclass of all records that can produce [ContinueRecord]s while being serialized.
 * 
 * @author Josh Micich
 */
abstract class ContinuableRecord protected constructor() : Record() {
    /**
     * Serializes this record's content to the supplied data output.<br></br>
     * The standard BIFF header (ushort sid, ushort size) has been handled by the superclass, so
     * only BIFF data should be written by this method.  Simple data types can be written with the
     * standard [LittleEndianOutput] methods.  Methods from [ContinuableRecordOutput]
     * can be used to serialize strings (with [ContinueRecord]s being written as required).
     * If necessary, implementors can explicitly start [ContinueRecord]s (regardless of the
     * amount of remaining space).
     * 
     * @param out a data output stream
     */
    protected abstract fun serialize(out: ContinuableRecordOutput)


    /**
     * @return the total length of the encoded record(s)
     * (Note - if any [ContinueRecord] is required, this result includes the
     * size of those too)
     */
    override fun getRecordSize(): Int {
        val out: ContinuableRecordOutput = ContinuableRecordOutput.Companion.createForCountingOnly()
        serialize(out)
        out.terminate()
        return out.totalSize
    }

    override fun serialize(offset: Int, data: ByteArray): Int {
        val leo: LittleEndianOutput = LittleEndianByteArrayOutputStream(data, offset)
        val out = ContinuableRecordOutput(leo, getSid().toInt())
        serialize(out)
        out.terminate()
        return out.totalSize
    }
}
