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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.UserSViewBegin
import com.wxiwei.office.fc.hssf.record.UserSViewEnd

/**
 * Manages the all the records associated with a 'Custom View Settings' sub-stream.<br></br>
 * Includes the initial USERSVIEWBEGIN(0x01AA) and final USERSVIEWEND(0x01AB).
 * 
 * @author Josh Micich
 */
class CustomViewSettingsRecordAggregate(rs: RecordStream) : RecordAggregate() {
    private val _begin: Record
    private val _end: Record

    /**
     * All the records between BOF and EOF
     */
    private val _recs: MutableList<RecordBase?>
    private var _psBlock: PageSettingsBlock? = null

    init {
        _begin = rs.next!!
        check(_begin.getSid() == UserSViewBegin.sid) { "Bad begin record" }
        val temp: MutableList<RecordBase?> = ArrayList<RecordBase?>()
        while (rs.peekNextSid() != UserSViewEnd.sid.toInt()) {
            if (PageSettingsBlock.Companion.isComponentRecord(rs.peekNextSid())) {
                check(_psBlock == null) { "Found more than one PageSettingsBlock in custom view settings sub-stream" }
                _psBlock = PageSettingsBlock(rs)
                temp.add(_psBlock)
                continue
            }
            temp.add(rs.next)
        }
        _recs = temp
        _end = rs.next!! // no need to save EOF in field
        check(_end.getSid() == UserSViewEnd.sid) { "Bad custom view settings end record" }
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        if (_recs.isEmpty()) {
            return
        }
        rv.visitRecord(_begin)
        for (i in _recs.indices) {
            val rb = _recs.get(i)
            if (rb is RecordAggregate) {
                rb.visitContainedRecords(rv)
            } else {
                rv.visitRecord(rb as Record)
            }
        }
        rv.visitRecord(_end)
    }

    fun append(r: RecordBase?) {
        _recs.add(r)
    }

    companion object {
        fun isBeginRecord(sid: Int): Boolean {
            return sid == UserSViewBegin.sid.toInt()
        }
    }
}
