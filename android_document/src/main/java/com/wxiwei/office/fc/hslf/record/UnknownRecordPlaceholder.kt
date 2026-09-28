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
 * If we come across a record we don't know about, we create one of
 * these. It allows us to keep track of what it contains, so we can
 * write it back out to disk unchanged
 * 
 * @author Nick Burch
 */
class UnknownRecordPlaceholder protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordAtom() {
    private var _contents: ByteArray?
    private val _type: Long

    /**
     * Create a new holder for a record we don't grok
     */
    init {
        // Sanity Checking - including whole header, so treat
        //  length as based of 0, not 8 (including header size based)
        var len = len
        if (len < 0) {
            len = 0
        }

        // Treat as an atom, grab and hold everything
        _contents = ByteArray(len)
        System.arraycopy(source, start, _contents, 0, len)
        _type = LittleEndian.getUShort(_contents!!, 2).toLong()
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
    public override fun dispose() {
        _contents = null
    }
}
