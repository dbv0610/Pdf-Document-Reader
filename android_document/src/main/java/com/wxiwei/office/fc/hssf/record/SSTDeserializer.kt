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

import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.util.IntMapper


/**
 * Handles the task of deserializing a SST string.  The two main entry points are
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at apache.org)
 */
class SSTDeserializer
    (strings: IntMapper<UnicodeString?>) {
    private val strings: IntMapper<UnicodeString?>

    init {
        this.strings = strings
    }

    /**
     * This is the starting point where strings are constructed.  Note that
     * strings may span across multiple continuations. Read the SST record
     * carefully before beginning to hack.
     */
    fun manufactureStrings(stringCount: Int, `in`: RecordInputStream) {
        for (i in 0..<stringCount) {
            // Extract exactly the count of strings from the SST record.
            val str: UnicodeString?
            if (`in`.available() == 0 && !`in`.hasNextRecord()) {
                System.err.println("Ran out of data before creating all the strings! String at index " + i + "")
                str = UnicodeString("")
            } else {
                str = UnicodeString(`in`)
            }
            addToStringTable(strings, str)
        }
    }

    companion object {
        fun addToStringTable(strings: IntMapper<UnicodeString?>, string: UnicodeString?) {
            strings.add(string)
        }
    }
}
