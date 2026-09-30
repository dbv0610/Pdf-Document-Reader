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
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.util.IntMapper


/**
 * This class handles serialization of SST records.  It utilizes the record processor
 * class write individual records. This has been refactored from the SSTRecord class.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
internal class SSTSerializer(
    strings: IntMapper<UnicodeString?>,
    numStrings: Int,
    numUniqueStrings: Int
) {
    private val _numStrings: Int
    private val _numUniqueStrings: Int

    private val strings: IntMapper<UnicodeString?>

    /** Offsets from the beginning of the SST record (even across continuations)  */
    private val bucketAbsoluteOffsets: IntArray

    /** Offsets relative the start of the current SST or continue record  */
    private val bucketRelativeOffsets: IntArray

    init {
        this.strings = strings
        _numStrings = numStrings
        _numUniqueStrings = numUniqueStrings

        val infoRecs: Int = ExtSSTRecord.Companion.getNumberOfInfoRecsForStrings(strings.size())
        this.bucketAbsoluteOffsets = IntArray(infoRecs)
        this.bucketRelativeOffsets = IntArray(infoRecs)
    }

    fun serialize(out: ContinuableRecordOutput) {
        out.writeInt(_numStrings)
        out.writeInt(_numUniqueStrings)

        for (k in 0..<strings.size()) {
            if (k % ExtSSTRecord.Companion.DEFAULT_BUCKET_SIZE == 0) {
                val rOff = out.totalSize
                val index: Int = k / ExtSSTRecord.Companion.DEFAULT_BUCKET_SIZE
                if (index < ExtSSTRecord.Companion.MAX_BUCKETS) {
                    //Excel only indexes the first 128 buckets.
                    bucketAbsoluteOffsets[index] = rOff
                    bucketRelativeOffsets[index] = rOff
                }
            }
            val s = getUnicodeString(k)
            s.serialize(out)
        }
    }


    private fun getUnicodeString(index: Int): UnicodeString {
        return getUnicodeString(strings, index)
    }

    fun getBucketAbsoluteOffsets(): IntArray {
        return bucketAbsoluteOffsets
    }

    fun getBucketRelativeOffsets(): IntArray {
        return bucketRelativeOffsets
    }

    companion object {
        private fun getUnicodeString(
            strings: IntMapper<UnicodeString?>,
            index: Int
        ): UnicodeString {
            return (strings.get(index)!!)
        }
    }
}
