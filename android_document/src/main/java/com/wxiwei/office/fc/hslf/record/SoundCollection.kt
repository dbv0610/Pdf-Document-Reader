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
 * Is a container for all sound related atoms and containers. It contains:
 *  * 1. SoundCollAtom (2021)
 *  * 2. Sound (2022), for each sound, if any
 * 
 * @author Yegor Kozlov
 */
class SoundCollection protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordContainer() {
    /**
     * Record header data.
     */
    private var _header: ByteArray?

    /**
     * Set things up, and find our more interesting children
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     * 
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.SoundCollection.typeID.toLong()
    }

    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
    }
}
