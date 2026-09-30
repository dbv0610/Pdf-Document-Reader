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

/**
 * An atom record that specifies that a shape is a header or footer placeholder shape
 * 
 * @since  PowerPoint 2007
 * @author Yegor Kozlov
 */
class RoundTripHFPlaceholder12 protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordAtom() {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Specifies the placeholder shape ID.
     * 
     * MUST be [OEPlaceholderAtom.MasterDate],  [OEPlaceholderAtom.MasterSlideNumber],
     * [OEPlaceholderAtom.MasterFooter], or [OEPlaceholderAtom.MasterHeader]
     */
    private var _placeholderId: Byte

    /**
     * Constructs the comment atom record from its source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    init {
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        _placeholderId = source[start + 8]
    }

    var placeholderId: Int
        /**
         * Gets the comment number (note - each user normally has their own count).
         * @return the comment number.
         */
        get() = _placeholderId.toInt()
        /**
         * Sets the comment number (note - each user normally has their own count).
         * @param number the comment number.
         */
        set(number) {
            _placeholderId = number.toByte()
        }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.RoundTripHFPlaceholder12.typeID.toLong()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }
}
