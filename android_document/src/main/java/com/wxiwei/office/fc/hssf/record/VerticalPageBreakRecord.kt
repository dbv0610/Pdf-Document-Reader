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
package com.wxiwei.office.fc.hssf.record

/**
 * VerticalPageBreak (0x001A) record that stores page breaks at columns
 *
 *
 * 
 * @see PageBreakRecord
 * 
 * @author Danny Mui (dmui at apache dot org)
 */
class VerticalPageBreakRecord : PageBreakRecord {
    /**
     * Creates an empty vertical page break record
     */
    constructor()

    /**
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) : super(`in`)

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun clone(): Any {
        val result: PageBreakRecord = VerticalPageBreakRecord()
        val iterator: MutableIterator<*> = getBreaksIterator()
        while (iterator.hasNext()) {
            val original = iterator.next() as Break
            result.addBreak(original.main, original.subFrom, original.subTo)
        }
        return result
    }

    companion object {
        const val sid: Short = 0x001A
    }
}
