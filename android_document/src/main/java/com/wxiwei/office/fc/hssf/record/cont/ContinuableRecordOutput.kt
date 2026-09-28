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
package com.wxiwei.office.fc.hssf.record.cont

import com.wxiwei.office.fc.hssf.record.ContinueRecord
import com.wxiwei.office.fc.util.DelayableLittleEndianOutput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import kotlin.math.min


/**
 * An augmented [LittleEndianOutput] used for serialization of [ContinuableRecord]s.
 * This class keeps track of how much remaining space is available in the current BIFF record and
 * can start new [ContinueRecord]s as required.
 * 
 * @author Josh Micich
 */
class ContinuableRecordOutput(out: LittleEndianOutput, sid: Int) : LittleEndianOutput {
    private val _out: LittleEndianOutput
    private var _ulrOutput: UnknownLengthRecordOutput
    private var _totalPreviousRecordsSize: Int

    val totalSize: Int
        /**
         * @return total number of bytes written so far (including all BIFF headers)
         */
        get() = _totalPreviousRecordsSize + _ulrOutput.totalSize

    /**
     * Terminates the last record (also updates its 'ushort size' field)
     */
    fun terminate() {
        _ulrOutput.terminate()
    }

    val availableSpace: Int
        /**
         * @return number of remaining bytes of space in current record
         */
        get() = _ulrOutput.availableSpace

    /**
     * Terminates the current record and starts a new [ContinueRecord] (regardless
     * of how much space is still available in the current record).
     */
    fun writeContinue() {
        _ulrOutput.terminate()
        _totalPreviousRecordsSize += _ulrOutput.totalSize
        _ulrOutput = UnknownLengthRecordOutput(_out, ContinueRecord.sid.toInt())
    }

    /**
     * Will terminate the current record and start a new [ContinueRecord]
     * if there isn't space for the requested number of bytes
     * @param requiredContinuousSize The number of bytes that need to be written
     */
    fun writeContinueIfRequired(requiredContinuousSize: Int) {
        if (_ulrOutput.availableSpace < requiredContinuousSize) {
            writeContinue()
        }
    }

    /**
     * Writes the 'optionFlags' byte and encoded character data of a unicode string.  This includes:
     * 
     *  * byte optionFlags
     *  * encoded character data (in "ISO-8859-1" or "UTF-16LE" encoding)
     * 
     * 
     * Notes:
     * 
     *  * The value of the 'is16bitEncoded' flag is determined by the actual character data
     * of <tt>text</tt>
     *  * The string options flag is never separated (by a [ContinueRecord]) from the
     * first chunk of character data it refers to.
     *  * The 'ushort length' field is assumed to have been explicitly written earlier.  Hence,
     * there may be an intervening [ContinueRecord]
     * 
     */
    fun writeStringData(text: String) {
        val is16bitEncoded = hasMultibyte(text)
        // calculate total size of the header and first encoded char
        var keepTogetherSize = 1 + 1 // ushort len, at least one character byte
        var optionFlags = 0x00
        if (is16bitEncoded) {
            optionFlags = optionFlags or 0x01
            keepTogetherSize += 1 // one extra byte for first char
        }
        writeContinueIfRequired(keepTogetherSize)
        writeByte(optionFlags)
        writeCharacterData(text, is16bitEncoded)
    }

    /**
     * Writes a unicode string complete with header and character data.  This includes:
     * 
     *  * ushort length
     *  * byte optionFlags
     *  * ushort numberOfRichTextRuns (optional)
     *  * ushort extendedDataSize (optional)
     *  * encoded character data (in "ISO-8859-1" or "UTF-16LE" encoding)
     * 
     * 
     * The following bits of the 'optionFlags' byte will be set as appropriate:
     * <table border='1'>
     * <tr><th>Mask</th><th>Description</th></tr>
     * <tr><td>0x01</td><td>is16bitEncoded</td></tr>
     * <tr><td>0x04</td><td>hasExtendedData</td></tr>
     * <tr><td>0x08</td><td>isRichText</td></tr>
    </table> * 
     * Notes:
     * 
     *  * The value of the 'is16bitEncoded' flag is determined by the actual character data
     * of <tt>text</tt>
     *  * The string header fields are never separated (by a [ContinueRecord]) from the
     * first chunk of character data (i.e. the first character is always encoded in the same
     * record as the string header).
     * 
     */
    fun writeString(text: String, numberOfRichTextRuns: Int, extendedDataSize: Int) {
        val is16bitEncoded = hasMultibyte(text)
        // calculate total size of the header and first encoded char
        var keepTogetherSize =
            2 + 1 + 1 // ushort len, byte optionFlags, at least one character byte
        var optionFlags = 0x00
        if (is16bitEncoded) {
            optionFlags = optionFlags or 0x01
            keepTogetherSize += 1 // one extra byte for first char
        }
        if (numberOfRichTextRuns > 0) {
            optionFlags = optionFlags or 0x08
            keepTogetherSize += 2
        }
        if (extendedDataSize > 0) {
            optionFlags = optionFlags or 0x04
            keepTogetherSize += 4
        }
        writeContinueIfRequired(keepTogetherSize)
        writeShort(text.length)
        writeByte(optionFlags)
        if (numberOfRichTextRuns > 0) {
            writeShort(numberOfRichTextRuns)
        }
        if (extendedDataSize > 0) {
            writeInt(extendedDataSize)
        }
        writeCharacterData(text, is16bitEncoded)
    }


    private fun writeCharacterData(text: String, is16bitEncoded: Boolean) {
        val nChars = text.length
        var i = 0
        if (is16bitEncoded) {
            while (true) {
                var nWritableChars = min(nChars - i, _ulrOutput.availableSpace / 2)
                while (nWritableChars > 0) {
                    _ulrOutput.writeShort(text.get(i++).code)
                    nWritableChars--
                }
                if (i >= nChars) {
                    break
                }
                writeContinue()
                writeByte(0x01)
            }
        } else {
            while (true) {
                var nWritableChars = min(nChars - i, _ulrOutput.availableSpace / 1)
                while (nWritableChars > 0) {
                    _ulrOutput.writeByte(text.get(i++).code)
                    nWritableChars--
                }
                if (i >= nChars) {
                    break
                }
                writeContinue()
                writeByte(0x00)
            }
        }
    }

    override fun write(b: ByteArray) {
        writeContinueIfRequired(b.size)
        _ulrOutput.write(b)
    }

    override fun write(b: ByteArray, offset: Int, len: Int) {
        var i = 0
        while (true) {
            var nWritableChars = min(len - i, _ulrOutput.availableSpace / 1)
            while (nWritableChars > 0) {
                _ulrOutput.writeByte(b[offset + i++].toInt())
                nWritableChars--
            }
            if (i >= len) {
                break
            }
            writeContinue()
        }
    }

    override fun writeByte(v: Int) {
        writeContinueIfRequired(1)
        _ulrOutput.writeByte(v)
    }

    override fun writeDouble(v: Double) {
        writeContinueIfRequired(8)
        _ulrOutput.writeDouble(v)
    }

    override fun writeInt(v: Int) {
        writeContinueIfRequired(4)
        _ulrOutput.writeInt(v)
    }

    override fun writeLong(v: Long) {
        writeContinueIfRequired(8)
        _ulrOutput.writeLong(v)
    }

    override fun writeShort(v: Int) {
        writeContinueIfRequired(2)
        _ulrOutput.writeShort(v)
    }

    init {
        _ulrOutput = UnknownLengthRecordOutput(out, sid)
        _out = out
        _totalPreviousRecordsSize = 0
    }

    companion object {
        fun createForCountingOnly(): ContinuableRecordOutput {
            return ContinuableRecordOutput(NOPOutput, -777) // fake sid
        }

        /**
         * Allows optimised usage of [ContinuableRecordOutput] for sizing purposes only.
         */
        private val NOPOutput: LittleEndianOutput = object : DelayableLittleEndianOutput {
            override fun createDelayedOutput(size: Int): LittleEndianOutput {
                return this
            }

            override fun write(b: ByteArray) {
                // does nothing
            }

            override fun write(b: ByteArray, offset: Int, len: Int) {
                // does nothing
            }

            override fun writeByte(v: Int) {
                // does nothing
            }

            override fun writeDouble(v: Double) {
                // does nothing
            }

            override fun writeInt(v: Int) {
                // does nothing
            }

            override fun writeLong(v: Long) {
                // does nothing
            }

            override fun writeShort(v: Int) {
                // does nothing
            }
        }
    }
}
