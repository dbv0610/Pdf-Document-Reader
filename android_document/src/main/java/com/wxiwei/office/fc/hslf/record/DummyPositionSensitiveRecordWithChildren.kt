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
 * If we come across a record we know has children of (potential)
 * interest, but where the record itself is boring, but where other
 * records may care about where this one lives, we create one
 * of these. It allows us to get at the children, and track where on
 * disk this is, but not much else.
 * Anything done using this should quite quickly be transitioned to its
 * own proper record class!
 * 
 * @author Nick Burch
 */
class DummyPositionSensitiveRecordWithChildren protected constructor(
    source: ByteArray,
    start: Int,
    len: Int
) : PositionDependentRecordContainer() {
    private var _header: ByteArray?
    private val _type: Long

    /**
     * Create a new holder for a boring record with children, but with
     * position dependent characteristics
     */
    init {
        // Just grab the header, not the whole contents
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)
        _type = LittleEndian.getUShort(_header!!, 2).toLong()

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
    }

    /**
     * Return the value we were given at creation
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
        _header = null
    }
}
