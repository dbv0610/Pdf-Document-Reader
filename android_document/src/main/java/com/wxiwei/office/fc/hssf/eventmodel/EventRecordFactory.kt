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
package com.wxiwei.office.fc.hssf.eventmodel

import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFactory
import com.wxiwei.office.fc.hssf.record.RecordFormatException
import com.wxiwei.office.fc.hssf.record.RecordInputStream
import java.io.InputStream
import java.util.Arrays

/**
 * Event-based record factory.  As opposed to RecordFactory
 * this version sends [ERFListener.processRecord] messages to
 * the supplied listener.  Record notifications are sent one record behind
 * to ensure that [ContinueRecord]s are processed first.
 * 
 * @author Andrew C. Oliver (acoliver@apache.org) - probably to blame for the bugs (so yank his chain on the list)
 * @author Marc Johnson (mjohnson at apache dot org) - methods taken from RecordFactory
 * @author Glen Stampoultzis (glens at apache.org) - methods taken from RecordFactory
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 */
class EventRecordFactory(private val _listener: ERFListener, sids: ShortArray?) {
    private val _sids: ShortArray?

    /**
     * 
     * @param sids an array of Record.sid values identifying the records
     * the listener will work with.  Alternatively if this is "null" then
     * all records are passed. For all 'known' record types use [RecordFactory.getAllKnownRecordSIDs]
     */
    init {
        if (sids == null) {
            _sids = null
        } else {
            _sids = sids.clone()
            Arrays.sort(_sids) // for faster binary search
        }
    }

    private fun isSidIncluded(sid: Short): Boolean {
        if (_sids == null) {
            return true
        }
        return Arrays.binarySearch(_sids, sid) >= 0
    }


    /**
     * sends the record event to all registered listeners.
     * @param record the record to be thrown.
     * @return `false` to abort.  This aborts
     * out of the event loop should the listener return false
     */
    private fun processRecord(record: Record): Boolean {
        if (!isSidIncluded(record.getSid())) {
            return true
        }
        return _listener.processRecord(record)
    }

    /**
     * Create an array of records from an input stream
     * 
     * @param in the InputStream from which the records will be
     * obtained
     * 
     * @exception RecordFormatException on error processing the
     * InputStream
     */
    @Throws(RecordFormatException::class)
    fun processRecords(`in`: InputStream) {
        var last_record: Record? = null

        val recStream = RecordInputStream(`in`)

        while (recStream.hasNextRecord()) {
            recStream.nextRecord()
            val recs = RecordFactory.createRecord(recStream) // handle MulRK records
            if (recs.size > 1) {
                for (k in recs.indices) {
                    if (last_record != null) {
                        if (!processRecord(last_record)) {
                            return
                        }
                    }
                    last_record = recs[k] // do to keep the algorithm homogeneous...you can't
                } // actually continue a number record anyhow.
            } else {
                val record = recs[0]

                if (record != null) {
                    if (last_record != null) {
                        if (!processRecord(last_record)) {
                            return
                        }
                    }
                    last_record = record
                }
            }
        }

        if (last_record != null) {
            processRecord(last_record)
        }
    }
}
