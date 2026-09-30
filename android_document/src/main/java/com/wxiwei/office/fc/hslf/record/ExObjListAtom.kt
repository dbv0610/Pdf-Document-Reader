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

/**
 * Tne atom that holds the seed info used by a ExObjList
 * 
 * @author Nick Burch
 */
class ExObjListAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Constructs a brand new link related atom record.
     */
    constructor() {
        _header = ByteArray(8)
        _data = ByteArray(4)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _data!!.size)

        // It is fine for the other values to be zero
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
        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, len - 8)

        // Must be at least 4 bytes long
        require(_data!!.size >= 4) {
            ("The length of the data for a ExObjListAtom must be at least 4 bytes, but was only "
                    + _data!!.size)
        }
    }

    val objectIDSeed: Long
        /**
         * Gets the object ID seed, which will be used as the unique
         * OLE identifier for the next OLE object added
         * @return the object ID seed
         */
        get() = LittleEndian.getUInt(_data!!, 0)

    /**
     * Sets the object ID seed
     * @param seed the new ID seed
     */
    fun setObjectIDSeed(seed: Int) {
        LittleEndian.putInt(_data!!, 0, seed)
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExObjListAtom.typeID.toLong()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }
}
