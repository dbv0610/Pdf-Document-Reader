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
package com.wxiwei.office.fc.hssf.model

/**
 * Simplifies iteration over a sequence of <tt>Record</tt> objects.
 * 
 * @author Josh Micich
 */
class RecordStream
/**
 * Creates a RecordStream bounded by startIndex and endIndex
 */(
    private val _list: List<com.wxiwei.office.fc.hssf.record.Record?>,
    private var _nextIndex: Int,
    private val _endIx: Int
) {
    var countRead: Int = 0
        private set

    constructor(
        records: List<com.wxiwei.office.fc.hssf.record.Record?>,
        startIx: Int
    ) : this(records, startIx, records.size)

    fun hasNext(): Boolean {
        return _nextIndex < _endIx
    }

    val next: com.wxiwei.office.fc.hssf.record.Record?
        get() {
            if (!hasNext()) {
                throw RuntimeException("Attempt to read past end of record stream")
            }
            this.countRead++
            return _list.get(_nextIndex++)
        }

    /**
     * @return the [Class] of the next Record. `null` if this stream is exhausted.
     */
    fun peekNextClass(): Class<out com.wxiwei.office.fc.hssf.record.Record?>? {
        if (!hasNext()) {
            return null
        }
        return _list.get(_nextIndex)!!.javaClass
    }

    /**
     * @return -1 if at end of records
     */
    fun peekNextSid(): Int {
        if (!hasNext()) {
            return -1
        }
        return (_list.get(_nextIndex) as com.wxiwei.office.fc.hssf.record.Record).getSid().toInt()
    }
}
