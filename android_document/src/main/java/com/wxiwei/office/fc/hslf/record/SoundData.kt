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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import java.io.IOException
import java.io.OutputStream

/**
 * Storage for embedded sounds.
 * 
 * @author Yegor Kozlov
 */
class SoundData : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Returns the sound data.
     * 
     * @return the sound data
     */
    /**
     * Record data.
     */
    var data: ByteArray?
        private set

    /**
     * Constructs a new empty sound container.
     */
    protected constructor() {
        _header = ByteArray(8)
        this.data = ByteArray(0)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, data!!.size)
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
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        this.data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, this.data, 0, len - 8)
    }

    /**
     * Gets the record type.
     * 
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.SoundData.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
     * 
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        out.write(_header)
        out.write(this.data)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        this.data = null
    }
}
