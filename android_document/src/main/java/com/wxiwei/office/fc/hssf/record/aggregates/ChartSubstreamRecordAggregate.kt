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
import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.EOFRecord
import com.wxiwei.office.fc.hssf.record.HeaderFooterRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase

/**
 * Manages the all the records associated with a chart sub-stream.<br></br>
 * Includes the initial [BOFRecord] and final [EOFRecord].
 * 
 * @author Josh Micich
 */
class ChartSubstreamRecordAggregate(rs: RecordStream) : RecordAggregate() {
    private val _bofRec: BOFRecord

    /**
     * All the records between BOF and EOF
     */
    private val _recs: MutableList<RecordBase?>
    private var _psBlock: PageSettingsBlock? = null

    init {
        _bofRec = rs.next as BOFRecord
        val temp: MutableList<RecordBase?> = ArrayList<RecordBase?>()
        while (rs.peekNextClass() != EOFRecord::class.java) {
            if (PageSettingsBlock.Companion.isComponentRecord(rs.peekNextSid())) {
                if (_psBlock != null) {
                    if (rs.peekNextSid() == HeaderFooterRecord.sid.toInt()) {
                        // test samples: 45538_classic_Footer.xls, 45538_classic_Header.xls
                        _psBlock!!.addLateHeaderFooter(rs.next as HeaderFooterRecord)
                        continue
                    }
                    throw IllegalStateException(
                        "Found more than one PageSettingsBlock in chart sub-stream"
                    )
                }
                _psBlock = PageSettingsBlock(rs)
                temp.add(_psBlock)
                continue
            }
            temp.add(rs.next)
        }
        _recs = temp
        val eof: Record? = rs.next // no need to save EOF in field
        check(eof is EOFRecord) { "Bad chart EOF" }
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        if (_recs.isEmpty()) {
            return
        }
        rv.visitRecord(_bofRec)
        for (i in _recs.indices) {
            val rb = _recs.get(i)
            if (rb is RecordAggregate) {
                rb.visitContainedRecords(rv)
            } else {
                rv.visitRecord(rb as Record)
            }
        }
        rv.visitRecord(EOFRecord.instance)
    }
}
