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
import com.wxiwei.office.fc.hssf.record.DVALRecord
import com.wxiwei.office.fc.hssf.record.DVRecord
import com.wxiwei.office.fc.hssf.record.Record

/**
 * Manages the DVALRecord and DVRecords for a single sheet<br></br>
 * See OOO excelfileformat.pdf section 4.14
 * @author Josh Micich
 */
class DataValidityTable : RecordAggregate {
    private val _headerRec: DVALRecord

    /**
     * The list of data validations for the current sheet.
     * Note - this may be empty (contrary to OOO documentation)
     */
    private val _validationList: MutableList<DVRecord?>

    constructor(rs: RecordStream) {
        _headerRec = rs.next as DVALRecord
        val temp: MutableList<DVRecord?> = ArrayList<DVRecord?>()
        while (rs.peekNextClass() == DVRecord::class.java) {
            temp.add(rs.next as DVRecord?)
        }
        _validationList = temp
    }

    constructor() {
        _headerRec = DVALRecord()
        _validationList = ArrayList<DVRecord?>()
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        if (_validationList.isEmpty()) {
            return
        }
        rv.visitRecord(_headerRec)
        for (i in _validationList.indices) {
            rv.visitRecord(_validationList.get(i) as Record)
        }
    }

    fun addDataValidation(dvRecord: DVRecord?) {
        _validationList.add(dvRecord)
        _headerRec.setDVRecNo(_validationList.size)
    }
}
