/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.wxiwei.office.fc.hssf.record.cont

import com.wxiwei.office.fc.hssf.record.ContinueRecord
import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.LittleEndianInput


/**
 * A decorated [RecordInputStream] that can read primitive data types
 * (short, int, long, etc.) spanned across a [ContinueRecord] boundary.
 * 
 * 
 * 
 * Most records construct themselves from [RecordInputStream].
 * This class assumes that a [ContinueRecord] record break always occurs at the type boundary,
 * however, it is not always so.
 * 
 * Two  attachments to [Bugzilla 50779](https://issues.apache.org/bugzilla/show_bug.cgi?id=50779)
 * demonstrate that a CONTINUE break can appear right in between two bytes of a unicode character
 * or between two bytes of a `short`. The problematic portion of the data is
 * in a Asian Phonetic Settings Block (ExtRst) of a UnicodeString.
 * 
 * 
 * [RecordInputStream] greedily requests the bytes to be read and stumbles on such files with a
 * "Not enough data (1) to read requested (2) bytes" exception.  The `ContinuableRecordInput`
 * class circumvents this "type boundary" rule and reads data byte-by-byte rolling over CONTINUE if necessary.
 * 
 * 
 * 
 * 
 * YK: For now (March 2011) this class is only used to read
 * @link org.apache.poi.hssf.record.common.UnicodeString.ExtRst} blocks of a UnicodeString.
 * 
 * 
 * 
 * @author Yegor Kozlov
 */
class ContinuableRecordInput(private val _in: RecordInputStream) : LittleEndianInput {
    override fun available(): Int {
        return _in.available()
    }

    override fun readByte(): Byte {
        return _in.readByte()
    }

    override fun readUByte(): Int {
        return _in.readUByte()
    }

    override fun readShort(): Short {
        return _in.readShort()
    }

    override fun readUShort(): Int {
        val ch1 = readUByte()
        val ch2 = readUByte()
        return (ch2 shl 8) + (ch1 shl 0)
    }

    override fun readInt(): Int {
        val ch1 = _in.readUByte()
        val ch2 = _in.readUByte()
        val ch3 = _in.readUByte()
        val ch4 = _in.readUByte()
        return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
    }

    override fun readLong(): Long {
        val b0 = _in.readUByte()
        val b1 = _in.readUByte()
        val b2 = _in.readUByte()
        val b3 = _in.readUByte()
        val b4 = _in.readUByte()
        val b5 = _in.readUByte()
        val b6 = _in.readUByte()
        val b7 = _in.readUByte()
        return ((b7.toLong() shl 56) +
                (b6.toLong() shl 48) +
                (b5.toLong() shl 40) +
                (b4.toLong() shl 32) +
                (b3.toLong() shl 24) +
                (b2 shl 16) +
                (b1 shl 8) +
                (b0 shl 0))
    }

    override fun readDouble(): Double {
        return _in.readDouble()
    }

    override fun readFully(buf: ByteArray) {
        _in.readFully(buf)
    }

    override fun readFully(buf: ByteArray, off: Int, len: Int) {
        _in.readFully(buf, off, len)
    }
}
