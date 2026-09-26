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

import com.wxiwei.office.fc.util.LittleEndianConsts
import com.wxiwei.office.fc.hssf.record.crypto.Biff8DecryptingStream
import com.wxiwei.office.fc.hssf.record.crypto.Biff8EncryptionKey
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale
import kotlin.Boolean
import kotlin.Byte
import kotlin.ByteArray
import kotlin.Char
import kotlin.CharArray
import kotlin.Deprecated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.RuntimeException
import kotlin.Short
import kotlin.String
import kotlin.Throws
import kotlin.byteArrayOf
import kotlin.check
import kotlin.math.min
import kotlin.require
import kotlin.toUShort

/**
 * Title:  Record Input Stream<P>
 * Description:  Wraps a stream and provides helper methods for the construction of records.</P><P>
 * 
 * @author Jason Height (jheight @ apache dot org)
</P> */
class RecordInputStream @JvmOverloads constructor(
    `in`: InputStream,
    key: Biff8EncryptionKey? = null,
    initialOffset: Int = 0
) : LittleEndianInput {
    /**
     * For use in [BiffViewer] which may construct [Record]s that don't completely
     * read all available data.  This exception should never be thrown otherwise.
     */
    class LeftoverDataException(sid: Int, remainingByteCount: Int) : RuntimeException(
        ("Initialisation of record 0x" + Integer.toHexString(sid).uppercase(
            Locale.getDefault()
        )
                + " left " + remainingByteCount + " bytes remaining still to be read.")
    )

    /** Header [LittleEndianInput] facet of the wrapped [InputStream]  */
    private val _bhi: BiffHeaderInput

    /** Data [LittleEndianInput] facet of the wrapped [InputStream]  */
    private val _dataInput: LittleEndianInput

    /** the record identifier of the BIFF record currently being read  */
    private var _currentSid = 0

    /**
     * Length of the data section of the current BIFF record (always 4 less than the total record size).
     * When uninitialised, this field is set to [.DATA_LEN_NEEDS_TO_BE_READ].
     */
    private var _currentDataLength = 0

    /**
     * The BIFF record identifier for the next record is read when just as the current record
     * is finished.
     * This field is only really valid during the time that ([._currentDataLength] ==
     * [.DATA_LEN_NEEDS_TO_BE_READ]).  At most other times its value is not really the
     * 'sid of the next record'.  Wwhile mid-record, this field coincidentally holds the sid
     * of the current record.
     */
    private var _nextSid: Int

    /**
     * index within the data section of the current BIFF record
     */
    private var _currentDataOffset = 0

    private class SimpleHeaderInput(`in`: InputStream) : BiffHeaderInput {
        private val _lei: LittleEndianInput

        init {
            _lei = getLEI(`in`)
        }

        override fun available(): Int {
            return _lei.available()
        }

        override fun readDataSize(): Int {
            return _lei.readUShort()
        }

        override fun readRecordSID(): Int {
            return _lei.readUShort()
        }
    }

    init {
        if (key == null) {
            _dataInput = getLEI(`in`)
            _bhi = SimpleHeaderInput(`in`)
        } else {
            val bds = Biff8DecryptingStream(`in`, initialOffset, key)
            _bhi = bds
            _dataInput = bds
        }
        _nextSid = readNextSid()
    }

    /**
     * @return the number of bytes available in the current BIFF record
     * @see .remaining
     */
    override fun available(): Int {
        return remaining()
    }

    fun read(b: ByteArray, off: Int, len: Int): Int {
        val limit = min(len, remaining())
        if (limit == 0) {
            return 0
        }
        readFully(b, off, limit)
        return limit
    }

    fun getSid(): Short {
        return _currentSid.toShort()
    }

    /**
     * Note - this method is expected to be called only when completed reading the current BIFF
     * record.
     * @throws LeftoverDataException if this method is called before reaching the end of the
     * current record.
     */
    @Throws(LeftoverDataException::class)
    fun hasNextRecord(): Boolean {
        if (_currentDataLength != -1 && _currentDataLength != _currentDataOffset) {
            throw LeftoverDataException(_currentSid, remaining())
        }
        if (_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ) {
            _nextSid = readNextSid()
        }
        return _nextSid != INVALID_SID_VALUE
    }

    /**
     * @return the sid of the next record or [.INVALID_SID_VALUE] if at end of stream
     */
    private fun readNextSid(): Int {
        val nAvailable = _bhi.available()
        if (nAvailable < EOFRecord.Companion.ENCODED_SIZE) {
            if (nAvailable > 0) {
                // some scrap left over?
                // ex45582-22397.xls has one extra byte after the last record
                // Excel reads that file OK
            }
            return INVALID_SID_VALUE
        }
        val result = _bhi.readRecordSID()
        if (result == INVALID_SID_VALUE) {
            throw RecordFormatException("Found invalid sid (" + result + ")")
        }
        _currentDataLength = DATA_LEN_NEEDS_TO_BE_READ
        return result
    }

    /** Moves to the next record in the stream.
     * 
     * *Note: The auto continue flag is reset to true*
     */
    @Throws(RecordFormatException::class)
    fun nextRecord() {
        check(_nextSid != INVALID_SID_VALUE) { "EOF - next record not available" }
        check(_currentDataLength == DATA_LEN_NEEDS_TO_BE_READ) { "Cannot call nextRecord() without checking hasNextRecord() first" }
        _currentSid = _nextSid
        _currentDataOffset = 0
        _currentDataLength = _bhi.readDataSize()
        if (_currentDataLength > MAX_RECORD_DATA_SIZE) {
            throw RecordFormatException(
                ("The content of an excel record cannot exceed "
                        + MAX_RECORD_DATA_SIZE + " bytes")
            )
        }
    }

    private fun checkRecordPosition(requiredByteCount: Int) {
        val nAvailable = remaining()
        if (nAvailable >= requiredByteCount) {
            // all OK
            return
        }
        if (nAvailable == 0 && isContinueNext()) {
            nextRecord()
            return
        }
        throw RecordFormatException(
            ("Not enough data (" + nAvailable
                    + ") to read requested (" + requiredByteCount + ") bytes")
        )
    }

    /**
     * Reads an 8 bit, signed value
     */
    override fun readByte(): Byte {
        checkRecordPosition(LittleEndianConsts.BYTE_SIZE)
        _currentDataOffset += LittleEndianConsts.BYTE_SIZE
        return _dataInput.readByte()
    }

    /**
     * Reads a 16 bit, signed value
     */
    override fun readShort(): Short {
        checkRecordPosition(LittleEndianConsts.SHORT_SIZE)
        _currentDataOffset += LittleEndianConsts.SHORT_SIZE
        return _dataInput.readShort()
    }

    /**
     * Reads a 32 bit, signed value
     */
    override fun readInt(): Int {
        checkRecordPosition(LittleEndianConsts.INT_SIZE)
        _currentDataOffset += LittleEndianConsts.INT_SIZE
        return _dataInput.readInt()
    }

    /**
     * Reads a 64 bit, signed value
     */
    override fun readLong(): Long {
        checkRecordPosition(LittleEndianConsts.LONG_SIZE)
        _currentDataOffset += LittleEndianConsts.LONG_SIZE
        return _dataInput.readLong()
    }

    /**
     * Reads an 8 bit, unsigned value
     */
    override fun readUByte(): Int {
        return readByte().toInt() and 0x00FF
    }

    /**
     * Reads a 16 bit, unsigned value.
     */
    override fun readUShort(): Int {
        checkRecordPosition(LittleEndianConsts.SHORT_SIZE)
        _currentDataOffset += LittleEndianConsts.SHORT_SIZE
        return _dataInput.readUShort()
    }

    override fun readDouble(): Double {
        val valueLongBits = readLong()
        val result = java.lang.Double.longBitsToDouble(valueLongBits)
        if (result.isNaN()) {
            // YK: Excel doesn't write NaN but instead converts the cell type into CELL_TYPE_ERROR.
            // HSSF prior to version 3.7 had a bug: it could write Double.NaN but could not read such a file back.
            // This behavior was fixed in POI-3.7.
            //throw new RuntimeException("Did not expect to read NaN"); // (Because Excel typically doesn't write NaN
        }
        return result
    }

    override fun readFully(buf: ByteArray) {
        readFully(buf, 0, buf.size)
    }

    override fun readFully(buf: ByteArray, off: Int, len: Int) {
        checkRecordPosition(len)
        _dataInput.readFully(buf, off, len)
        _currentDataOffset += len
    }

    fun readString(): String {
        val requestedLength = readUShort()
        val compressFlag = readByte()
        return readStringCommon(requestedLength, compressFlag.toInt() == 0)
    }

    /**
     * given a byte array of 16-bit unicode characters, compress to 8-bit and
     * return a string
     * 
     * { 0x16, 0x00 } -0x16
     * 
     * @param requestedLength the length of the final string
     * @return                                     the converted string
     * @exception  IllegalArgumentException        if len is too large (i.e.,
     * there is not enough data in string to create a String of that
     * length)
     */
    fun readUnicodeLEString(requestedLength: Int): String {
        return readStringCommon(requestedLength, false)
    }

    fun readCompressedUnicode(requestedLength: Int): String {
        return readStringCommon(requestedLength, true)
    }

    private fun readStringCommon(requestedLength: Int, pIsCompressedEncoding: Boolean): String {
        // Sanity check to detect garbage string lengths
        require(!(requestedLength < 0 || requestedLength > 0x100000)) { "Bad requested string length (" + requestedLength + ")" }
        val buf = CharArray(requestedLength)
        var isCompressedEncoding = pIsCompressedEncoding
        var curLen = 0
        while (true) {
            var availableChars =
                if (isCompressedEncoding) remaining() else remaining() / LittleEndianConsts.SHORT_SIZE
            if (requestedLength - curLen <= availableChars) {
                // enough space in current record, so just read it out
                while (curLen < requestedLength) {
                    val ch: Char
                    if (isCompressedEncoding) {
                        ch = readUByte().toChar()
                    } else {
                        ch = Char(readShort().toUShort())
                    }
                    buf[curLen] = ch
                    curLen++
                }
                return String(buf)
            }
            // else string has been spilled into next continue record
            // so read what's left of the current record
            while (availableChars > 0) {
                val ch: Char
                if (isCompressedEncoding) {
                    ch = readUByte().toChar()
                } else {
                    ch = Char(readShort().toUShort())
                }
                buf[curLen] = ch
                curLen++
                availableChars--
            }
            if (!isContinueNext()) {
                throw RecordFormatException(
                    ("Expected to find a ContinueRecord in order to read remaining "
                            + (requestedLength - curLen) + " of " + requestedLength + " chars")
                )
            }
            if (remaining() != 0) {
                throw RecordFormatException("Odd number of bytes(" + remaining() + ") left behind")
            }
            nextRecord()
            // note - the compressed flag may change on the fly
            val compressFlag = readByte()
            isCompressedEncoding = (compressFlag.toInt() == 0)
        }
    }

    /** Returns the remaining bytes for the current record.
     * 
     * @return The remaining bytes of the current record.
     */
    fun readRemainder(): ByteArray {
        val size = remaining()
        if (size == 0) {
            return EMPTY_BYTE_ARRAY
        }
        val result = ByteArray(size)
        readFully(result)
        return result
    }

    /** Reads all byte data for the current record, including any
     * that overlaps into any following continue records.
     * 
     */
    @Deprecated(
        """Best to write a input stream that wraps this one where there is
     special sub record that may overlap continue records."""
    )
    fun readAllContinuedRemainder(): ByteArray {
        //Using a ByteArrayOutputStream is just an easy way to get a
        //growable array of the data.
        val out = ByteArrayOutputStream(2 * MAX_RECORD_DATA_SIZE)

        while (true) {
            val b = readRemainder()
            out.write(b, 0, b.size)
            if (!isContinueNext()) {
                break
            }
            nextRecord()
        }
        return out.toByteArray()
    }

    /** The remaining number of bytes in the *current* record.
     * 
     * @return The number of bytes remaining in the current record
     */
    fun remaining(): Int {
        if (_currentDataLength == DATA_LEN_NEEDS_TO_BE_READ) {
            // already read sid of next record. so current one is finished
            return 0
        }
        return _currentDataLength - _currentDataOffset
    }

    /**
     * 
     * @return `true` when a [ContinueRecord] is next.
     */
    private fun isContinueNext(): Boolean {
        check(!(_currentDataLength != DATA_LEN_NEEDS_TO_BE_READ && _currentDataOffset != _currentDataLength)) { "Should never be called before end of current record" }
        if (!hasNextRecord()) {
            return false
        }
        // At what point are records continued?
        //  - Often from within the char data of long strings (caller is within readStringCommon()).
        //  - From UnicodeString construction (many different points - call via checkRecordPosition)
        //  - During TextObjectRecord construction (just before the text, perhaps within the text,
        //    and before the formatting run data)
        return _nextSid == ContinueRecord.Companion.sid.toInt()
    }

    /**
     * @return sid of next record. Can be called after hasNextRecord()
     */
    fun getNextSid(): Int {
        return _nextSid
    }

    companion object {
        /** Maximum size of a single record (minus the 4 byte header) without a continue */
        const val MAX_RECORD_DATA_SIZE: Short = 8224
        private val INVALID_SID_VALUE = -1

        /**
         * When [._currentDataLength] has this value, it means that the previous BIFF record is
         * finished, the next sid has been properly read, but the data size field has not been read yet.
         */
        private val DATA_LEN_NEEDS_TO_BE_READ = -1
        private val EMPTY_BYTE_ARRAY = byteArrayOf()

        fun getLEI(`is`: InputStream): LittleEndianInput {
            if (`is` is LittleEndianInput) {
                // accessing directly is an optimisation
                return `is` as LittleEndianInput
            }
            // less optimal, but should work OK just the same. Often occurs in junit tests.
            return LittleEndianInputStream(`is`)
        }
    }
}
