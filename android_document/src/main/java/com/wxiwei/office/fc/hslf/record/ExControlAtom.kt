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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt
import java.io.IOException
import java.io.OutputStream

/**
 * An atom record that specifies an ActiveX control.
 * 
 * @author Yegor Kozlov
 */
class ExControlAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * An integer that specifies which presentation slide is associated with the ActiveX control.
     * 
     * 
     * It MUST be 0x00000000 or equal to the value of the slideId field of a SlidePersistAtom record.
     * The value 0x00000000 specifies a null reference.
     * 
     * 
     * @return an integer that specifies which presentation slide is associated with the ActiveX control
     */
    /**
     * Sets which presentation slide is associated with the ActiveX control.
     * 
     * @param id an integer that specifies which presentation slide is associated with the ActiveX control
     * 
     * 
     * It MUST be 0x00000000 or equal to the value of the slideId field of a SlidePersistAtom record.
     * The value 0x00000000 specifies a null reference.
     * 
     */
    /**
     * slideId.
     */
    var slideId: Int = 0

    /**
     * Constructs a brand new embedded object atom record.
     */
    constructor() {
        _header = ByteArray(8)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, 4)
    }

    /**
     * Constructs the ExControlAtom record from its source data.
     * 
     * @param source the source data as a byte array.
     * @param start  the start offset into the byte array.
     * @param len    the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        this.slideId = getInt(source, start + 8)
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExControlAtom.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     * 
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        out.write(_header)
        val data = ByteArray(4)
        putInt(data, this.slideId)
        out.write(data)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }
}
