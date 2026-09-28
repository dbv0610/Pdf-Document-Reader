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

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title: Interface End Record (0x00E2)<P>
 * Description: Shows where the Interface Records end (MMS)
 * (has no fields)</P><P>
 * REFERENCE:  PG 324 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)</P><P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
</P> */
class InterfaceEndRecord private constructor() : StandardRecord() {
    override fun toString(): String {
        return "[INTERFACEEND/]\n"
    }

    public override fun serialize(out: LittleEndianOutput) {
        // no instance data
    }

    override fun getDataSize(): Int {
        return 0
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    companion object {
        const val sid: Short = 0x00E2
        val instance: InterfaceEndRecord = InterfaceEndRecord()

        @JvmStatic
        fun create(`in`: RecordInputStream): Record? {
            when (`in`.remaining()) {
                0 -> return instance
                2 -> return InterfaceHdrRecord(`in`)
            }
            throw RecordFormatException("Invalid record data size: " + `in`.remaining())
        }
    }
}
