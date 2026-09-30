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
 * The atom that holds metadata on a specific embedded object in the document.
 * 
 * 
 * 
 * @author Daniel Noll
 */
class ExEmbedAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Constructs a brand new embedded object atom record.
     */
    constructor() {
        _header = ByteArray(8)
        _data = ByteArray(7)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _data!!.size)

        // It is fine for the other values to be zero
    }

    /**
     * Constructs the embedded object atom record from its source data.
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
        require(_data!!.size >= 7) {
            ("The length of the data for a ExEmbedAtom must be at least 4 bytes, but was only "
                    + _data!!.size)
        }
    }

    val followColorScheme: Int
        /**
         * Gets whether the object follows the color scheme.
         * 
         * @return one of [.DOES_NOT_FOLLOW_COLOR_SCHEME],
         * [.FOLLOWS_ENTIRE_COLOR_SCHEME], or
         * [.FOLLOWS_TEXT_AND_BACKGROUND_SCHEME].
         */
        get() = LittleEndian.getInt(_data!!, 0)

    val cantLockServerB: Boolean
        /**
         * Gets whether the embedded server cannot be locked.
         * 
         * @return `true` if the embedded server cannot be locked.
         */
        get() = _data!![4].toInt() != 0

    val noSizeToServerB: Boolean
        /**
         * Gets whether it is not required to send the dimensions to the embedded object.
         * 
         * @return `true` if the embedded server does not require the object dimensions.
         */
        get() = _data!![5].toInt() != 0

    val isTable: Boolean
        /**
         * Getswhether the object is a Word table.
         * 
         * @return `true` if the object is a Word table.
         */
        get() = _data!![6].toInt() != 0

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExEmbedAtom.typeID.toLong()
    }

    /**
     * 
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }

    companion object {
        /**
         * Embedded document does not follow the color scheme.
         */
        const val DOES_NOT_FOLLOW_COLOR_SCHEME: Int = 0

        /**
         * Embedded document follows the entire color scheme.
         */
        const val FOLLOWS_ENTIRE_COLOR_SCHEME: Int = 1

        /**
         * Embedded document follows the text and background scheme.
         */
        const val FOLLOWS_TEXT_AND_BACKGROUND_SCHEME: Int = 2
    }
}
