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
package com.wxiwei.office.fc.hssf.record.common

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Title: FeatSmartTag (Smart Tag Shared Feature) common record part
 * <P>
 * This record part specifies Smart Tag data for a sheet, stored as part
 * of a Shared Feature. It can be found in records such as  [FeatRecord].
 * It is made up of a hash, and a set of Factoid Data that makes up
 * the smart tags.
 * For more details, see page 669 of the Excel binary file
 * format documentation.
</P> */
class FeatSmartTag : SharedFeature {
    // TODO - process
    private val data: ByteArray

    constructor() {
        data = ByteArray(0)
    }

    constructor(`in`: RecordInputStream) {
        data = `in`.readRemainder()
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(" [FEATURE SMART TAGS]\n")
        buffer.append(" [/FEATURE SMART TAGS]\n")
        return buffer.toString()
    }

    override val dataSize: Int
        get() = data.size

    override fun serialize(out: LittleEndianOutput) {
        out.write(data)
    }
}
