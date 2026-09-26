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

/**
 * Atom that contains information that describes shape client data.
 * 
 * @author Yegor Kozlov
 */
class OEShapeAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * record data
     */
    private var _recdata: ByteArray?

    /**
     * Constructs a brand new link related atom record.
     */
    constructor() {
        _recdata = ByteArray(4)

        _header = ByteArray(8)
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _recdata!!.size)
    }

    /**
     * Constructs the link related atom record from its
     * source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the record data
        _recdata = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _recdata, 0, len - 8)
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.OEShapeAtom.typeID.toLong()
    }

    var options: Int
        /**
         * shape flags.
         * 
         * @return  shape flags.
         */
        get() = LittleEndian.getInt(_recdata!!, 0)
        /**
         * shape flags.
         * 
         * @param id  shape flags.
         */
        set(id) {
            LittleEndian.putInt(_recdata!!, 0, id)
        }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _recdata = null
    }
}
