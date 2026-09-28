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

import com.wxiwei.office.fc.ddf.EscherTextboxRecord

/**
 * A wrapper around a DDF (Escher) EscherTextbox Record. Causes the DDF
 * Record to be accessible as if it were a HSLF record.
 * Note: when asked to write out, will simply put any child records correctly
 * into the Escher layer. A call to the escher layer to write out (by the
 * parent PPDrawing) will do the actual write out
 * 
 * @author Nick Burch
 */
class EscherTextboxWrapper : RecordContainer {
    /**
     * Returns the underlying DDF Escher Record
     */
    var escherRecord: EscherTextboxRecord?
        private set
    private var _type: Long = 0
    /**
     * @return  Shape ID
     */
    /**
     * @param id Shape ID
     */
    var shapeId: Int = 0

    /**
     * Creates the wrapper for the given DDF Escher Record and children
     */
    constructor(textbox: EscherTextboxRecord?) {
        this.escherRecord = textbox
        _type = escherRecord!!.recordId.toLong()

        // Find the child records in the escher data
        val data = escherRecord!!.data
        _children = Record.findChildRecords(data!!, 0, data.size)
    }

    /**
     * Creates a new, empty wrapper for DDF Escher Records and their children
     */
    constructor() {
        this.escherRecord = EscherTextboxRecord()
        escherRecord!!.recordId = EscherTextboxRecord.RECORD_ID
        escherRecord!!.options = 15.toShort()

        _children = emptyArray()
    }

    /**
     * Return the type of the escher record (normally in the 0xFnnn range)
     */
    public override fun getRecordType(): Long {
        return _type
    }

    public override fun dispose() {
        super.dispose()
        if (this.escherRecord != null) {
            escherRecord!!.dispose()
            this.escherRecord = null
        }
    }
}
