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
package com.wxiwei.office.fc.hssf.eventusermodel

import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFactory

/**
 * An HSSFRequest object should be constructed registering an instance or multiple
 * instances of HSSFListener with each Record.sid you wish to listen for.
 * 
 * @see HSSFEventFactory
 * 
 * @see HSSFListener
 * 
 * @see HSSFUserException
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Carey Sublette (careysub@earthling.net)
 */
class HSSFRequest {
    private val _records: MutableMap<Short?, MutableList<HSSFListener?>?>

    /** Creates a new instance of HSSFRequest  */
    init {
        _records =
            HashMap<Short?, MutableList<HSSFListener?>?>(50) // most folks won't listen for too many of these
    }

    /**
     * add an event listener for a particular record type.  The trick is you have to know
     * what the records are for or just start with our examples and build on them.  Alternatively,
     * you CAN call addListenerForAllRecords and you'll receive ALL record events in one listener,
     * but if you like to squeeze every last byte of efficiency out of life you my not like this.
     * (its sure as heck what I plan to do)
     * 
     * @see .addListenerForAllRecords
     * @param lsnr for the event
     * @param sid identifier for the record type this is the .sid static member on the individual records
     * for example req.addListener(myListener, BOFRecord.sid)
     */
    fun addListener(lsnr: HSSFListener?, sid: Short) {
        var list = _records.get(sid)

        if (list == null) {
            list = ArrayList<HSSFListener?>(1) // probably most people will use one listener
            _records.put(sid, list)
        }
        list.add(lsnr)
    }

    /**
     * This is the equivalent of calling addListener(myListener, sid) for EVERY
     * record in the org.apache.poi.hssf.record package. This is for lazy
     * people like me. You can call this more than once with more than one listener, but
     * that seems like a bad thing to do from a practice-perspective unless you have a
     * compelling reason to do so (like maybe you send the event two places or log it or
     * something?).
     * 
     * @param lsnr a single listener to associate with ALL records
     */
    fun addListenerForAllRecords(lsnr: HSSFListener?) {
        val rectypes = RecordFactory.getAllKnownRecordSIDs()!!

        for (k in rectypes.indices) {
            addListener(lsnr, rectypes[k])
        }
    }

    /**
     * Called by HSSFEventFactory, passes the Record to each listener associated with
     * a record.sid.
     * 
     * Exception and return value added 2002-04-19 by Carey Sublette
     * 
     * @return numeric user-specified result code. If zero continue processing.
     * @throws HSSFUserException User exception condition
     */
    @Throws(HSSFUserException::class)
    fun processRecord(rec: Record): Short {
        val obj: Any? = _records.get(rec.getSid())
        var userCode: Short = 0

        if (obj != null) {
            val listeners = obj as MutableList<*>

            for (k in listeners.indices) {
                val listenObj: Any? = listeners.get(k)
                if (listenObj is AbortableHSSFListener) {
                    val listener = listenObj
                    userCode = listener.abortableProcessRecord(rec)
                    if (userCode.toInt() != 0) break
                } else {
                    val listener = listenObj as HSSFListener
                    listener.processRecord(rec)
                }
            }
        }
        return userCode
    }
}
