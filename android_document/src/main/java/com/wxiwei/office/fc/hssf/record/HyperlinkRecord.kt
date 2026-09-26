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

import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.longToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.HexRead.readFromString
import com.wxiwei.office.fc.util.LittleEndianByteArrayInputStream
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * The `HyperlinkRecord` (0x01B8) wraps an HLINK-record
 * from the Excel-97 format.
 * Supports only external links for now (eg http://)
 * 
 * @author      Mark Hissink Muller [](mailto:mark@hissinkmuller.nl >mark&064;hissinkmuller.nl</a>
@author      Yegor Kozlov (yegor at apache dot org)
) */
class HyperlinkRecord : StandardRecord {
    private val logger = getLogger(javaClass)

    class GUID(d1: Int, d2: Int, d3: Int, d4: Long) {
        /** 4 bytes - little endian  */
        private val _d1: Int

        /** 2 bytes - little endian  */
        private val _d2: Int

        /** 2 bytes - little endian  */
        private val _d3: Int

        /**
         * 8 bytes - serialized as big endian,  stored with inverted endianness here
         */
        private val _d4: Long

        constructor(`in`: LittleEndianInput) : this(
            `in`.readInt(),
            `in`.readUShort(),
            `in`.readUShort(),
            `in`.readLong()
        )

        init {
            _d1 = d1
            _d2 = d2
            _d3 = d3
            _d4 = d4
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeInt(_d1)
            out.writeShort(_d2)
            out.writeShort(_d3)
            out.writeLong(_d4)
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is GUID) return false
            return _d1 == obj._d1 && _d2 == obj._d2 && _d3 == obj._d3 && _d4 == obj._d4
        }

        fun getD1(): Int {
            return _d1
        }

        fun getD2(): Int {
            return _d2
        }

        fun getD3(): Int {
            return _d3
        }

        fun getD4(): Long {
            //
            val baos = ByteArrayOutputStream(8)
            try {
                DataOutputStream(baos).writeLong(_d4)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            val buf = baos.toByteArray()
            return LittleEndianByteArrayInputStream(buf).readLong()
        }

        fun formatAsString(): String {
            val sb = StringBuilder(36)

            val PREFIX_LEN = "0x".length
            sb.appendRange(intToHex(_d1), PREFIX_LEN, PREFIX_LEN + 8)
            sb.append("-")
            sb.appendRange(shortToHex(_d2), PREFIX_LEN, PREFIX_LEN + 4)
            sb.append("-")
            sb.appendRange(shortToHex(_d3), PREFIX_LEN, PREFIX_LEN + 4)
            sb.append("-")
            val d4Chars = longToHex(getD4())
            sb.appendRange(d4Chars, PREFIX_LEN, PREFIX_LEN + 4)
            sb.append("-")
            sb.appendRange(d4Chars, PREFIX_LEN + 4, PREFIX_LEN + 4 + 12)
            return sb.toString()
        }

        override fun toString(): String {
            val sb = StringBuilder(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(formatAsString())
            sb.append("]")
            return sb.toString()
        }

        companion object {
            /*
		 * this class is currently only used here, but could be moved to a
		 * common package if needed
		 */
            private const val TEXT_FORMAT_LENGTH = 36

            const val ENCODED_SIZE: Int = 16

            /**
             * Read a GUID in standard text form e.g.<br></br>
             * 13579BDF-0246-8ACE-0123-456789ABCDEF
             * <br></br> -&gt; <br></br>
             * 0x13579BDF, 0x0246, 0x8ACE 0x0123456789ABCDEF
             */
            fun parse(rep: String): GUID {
                val cc = rep.toCharArray()
                if (cc.size != TEXT_FORMAT_LENGTH) {
                    throw RecordFormatException("supplied text is the wrong length for a GUID")
                }
                val d0: Int = (parseShort(cc, 0) shl 16) + (parseShort(cc, 4) shl 0)
                val d1: Int = parseShort(cc, 9)
                val d2: Int = parseShort(cc, 14)
                for (i in 23 downTo 20) {
                    cc[i] = cc[i - 1]
                }
                val d3: Long = parseLELong(cc, 20)

                return GUID(d0, d1, d2, d3)
            }

            private fun parseLELong(cc: CharArray, startIndex: Int): Long {
                var acc: Long = 0
                var i = startIndex + 14
                while (i >= startIndex) {
                    acc = acc shl 4
                    acc += parseHexChar(cc[i + 0]).toLong()
                    acc = acc shl 4
                    acc += parseHexChar(cc[i + 1]).toLong()
                    i -= 2
                }
                return acc
            }

            private fun parseShort(cc: CharArray, startIndex: Int): Int {
                var acc = 0
                for (i in 0..3) {
                    acc = acc shl 4
                    acc += parseHexChar(cc[startIndex + i])
                }
                return acc
            }

            private fun parseHexChar(c: Char): Int {
                if (c >= '0' && c <= '9') {
                    return c.code - '0'.code
                }
                if (c >= 'A' && c <= 'F') {
                    return c.code - 'A'.code + 10
                }
                if (c >= 'a' && c <= 'f') {
                    return c.code - 'a'.code + 10
                }
                throw RecordFormatException("Bad hex char '" + c + "'")
            }
        }
    }

    /** cell range of this hyperlink  */
    private var _range: HSSFCellRangeAddress? = null

    /** 16-byte GUID  */
    private var _guid: GUID? = null

    /** Some sort of options for file links.  */
    private var _fileOpts = 0

    /** Link options. Can include any of HLINK_* flags.  */
    private var _linkOpts = 0

    /** Test label  */
    private var _label: String? = null

    private var _targetFrame: String? = null

    /** Moniker. Makes sense only for URL and file links  */
    private var _moniker: GUID? = null

    /** in 8:3 DOS format No Unicode string header,
     * always 8-bit characters, zero-terminated  */
    private var _shortFilename: String? = null

    /** Link  */
    private var _address: String? = null

    /**
     * Text describing a place in document.  In Excel UI, this is appended to the
     * address, (after a '#' delimiter).<br></br>
     * This field is optional.  If present, the [.HLINK_PLACE] must be set.
     */
    private var _textMark: String? = null

    private var _uninterpretedTail: ByteArray? = null

    /**
     * Create a new hyperlink
     */
    constructor()

    /**
     * @return the 0-based column of the first cell that contains this hyperlink
     */
    fun getFirstColumn(): Int {
        return _range!!.getFirstColumn()
    }

    /**
     * Set the first column (zero-based)of the range that contains this hyperlink
     */
    fun setFirstColumn(col: Int) {
        _range!!.setFirstColumn(col)
    }

    /**
     * @return the 0-based column of the last cell that contains this hyperlink
     */
    fun getLastColumn(): Int {
        return _range!!.getLastColumn()
    }

    /**
     * Set the last column (zero-based)of the range that contains this hyperlink
     */
    fun setLastColumn(col: Int) {
        _range!!.setLastColumn(col)
    }

    /**
     * @return the 0-based row of the first cell that contains this hyperlink
     */
    fun getFirstRow(): Int {
        return _range!!.getFirstRow()
    }

    /**
     * Set the first row (zero-based)of the range that contains this hyperlink
     */
    fun setFirstRow(col: Int) {
        _range!!.setFirstRow(col)
    }

    /**
     * @return the 0-based row of the last cell that contains this hyperlink
     */
    fun getLastRow(): Int {
        return _range!!.getLastRow()
    }

    /**
     * Set the last row (zero-based)of the range that contains this hyperlink
     */
    fun setLastRow(col: Int) {
        _range!!.setLastRow(col)
    }

    /**
     * @return 16-byte guid identifier Seems to always equal [.STD_MONIKER]
     */
    fun getGuid(): GUID {
        return _guid!!
    }

    /**
     * @return 16-byte moniker
     */
    fun getMoniker(): GUID? {
        return _moniker
    }

    /**
     * Return text label for this hyperlink
     * 
     * @return  text to display
     */
    fun getLabel(): String? {
        return cleanString(_label)
    }

    /**
     * Sets text label for this hyperlink
     * 
     * @param label text label for this hyperlink
     */
    fun setLabel(label: String?) {
        _label = appendNullTerm(label)
    }

    fun getTargetFrame(): String? {
        return cleanString(_targetFrame)
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     * 
     * @return  the address of this hyperlink
     */
    fun getAddress(): String? {
        if ((_linkOpts and HLINK_URL) != 0 && FILE_MONIKER == _moniker) return cleanString(if (_address != null) _address else _shortFilename)
        else if ((_linkOpts and HLINK_PLACE) != 0) return cleanString(_textMark)
        else return cleanString(_address)
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     * 
     * @param address  the address of this hyperlink
     */
    fun setAddress(address: String?) {
        if ((_linkOpts and HLINK_URL) != 0 && FILE_MONIKER == _moniker) _shortFilename =
            appendNullTerm(address)
        else if ((_linkOpts and HLINK_PLACE) != 0) _textMark = appendNullTerm(address)
        else _address = appendNullTerm(address)
    }

    fun getShortFilename(): String? {
        return cleanString(_shortFilename)
    }

    fun setShortFilename(shortFilename: String?) {
        _shortFilename = appendNullTerm(shortFilename)
    }

    fun getTextMark(): String? {
        return cleanString(_textMark)
    }

    fun setTextMark(textMark: String?) {
        _textMark = appendNullTerm(textMark)
    }


    /**
     * Link options. Must be a combination of HLINK_* constants.
     * For testing only
     */
    fun getLinkOptions(): Int {
        return _linkOpts
    }

    /**
     * Label options
     */
    fun getLabelOptions(): Int {
        return 2 // always 2
    }

    /**
     * Options for a file link
     */
    fun getFileOptions(): Int {
        return _fileOpts
    }


    constructor(`in`: RecordInputStream) {
        _range = HSSFCellRangeAddress(`in`)

        _guid = GUID(`in`)

        /**
         * streamVersion (4 bytes): An unsigned integer that specifies the version number
         * of the serialization implementation used to save this structure. This value MUST equal 2.
         */
        val streamVersion = `in`.readInt()
        if (streamVersion != 0x00000002) {
            throw RecordFormatException("Stream Version must be 0x2 but found " + streamVersion)
        }
        _linkOpts = `in`.readInt()

        if ((_linkOpts and HLINK_LABEL) != 0) {
            val label_len = `in`.readInt()
            _label = `in`.readUnicodeLEString(label_len)
        }

        if ((_linkOpts and HLINK_TARGET_FRAME) != 0) {
            val len = `in`.readInt()
            _targetFrame = `in`.readUnicodeLEString(len)
        }

        if ((_linkOpts and HLINK_URL) != 0 && (_linkOpts and HLINK_UNC_PATH) != 0) {
            _moniker = null
            val nChars = `in`.readInt()
            _address = `in`.readUnicodeLEString(nChars)
        }

        if ((_linkOpts and HLINK_URL) != 0 && (_linkOpts and HLINK_UNC_PATH) == 0) {
            _moniker = GUID(`in`)

            if (URL_MONIKER == _moniker) {
                val length = `in`.readInt()

                /**
                 * The value of `length` be either the byte size of the url field
                 * (including the terminating NULL character) or the byte size of the url field plus 24.
                 * If the value of this field is set to the byte size of the url field,
                 * then the tail bytes fields are not present.
                `` */
                val remaining = `in`.remaining()
                if (length == remaining) {
                    val nChars = length / 2
                    _address = `in`.readUnicodeLEString(nChars)
                } else {
                    val nChars: Int = (length - TAIL_SIZE) / 2
                    _address = `in`.readUnicodeLEString(nChars)
                    /**
                     * TODO: make sense of the remaining bytes
                     * According to the spec they consist of:
                     * 1. 16-byte  GUID: This field MUST equal
                     * {0xF4815879, 0x1D3B, 0x487F, 0xAF, 0x2C, 0x82, 0x5D, 0xC4, 0x85, 0x27, 0x63}
                     * 2. Serial version, this field MUST equal 0 if present.
                     * 3. URI Flags
                     */
                    _uninterpretedTail = readTail(URL_TAIL, `in`)
                }
            } else if (FILE_MONIKER == _moniker) {
                _fileOpts = `in`.readShort().toInt()

                val len = `in`.readInt()
                _shortFilename = readCompressedUnicode(`in`, len)
                _uninterpretedTail = readTail(FILE_TAIL, `in`)
                val size = `in`.readInt()
                if (size > 0) {
                    val charDataSize = `in`.readInt()

                    //From the spec: An optional unsigned integer that MUST be 3 if present
                    val optFlags = `in`.readUShort()
                    if (optFlags != 0x0003) {
                        throw RecordFormatException("Expected 0x3 but found " + optFlags)
                    }
                    _address = readUnicodeLE(`in`, charDataSize / 2)
                } else {
                    _address = null
                }
            } else if (STD_MONIKER == _moniker) {
                _fileOpts = `in`.readShort().toInt()

                val len = `in`.readInt()

                val path_bytes = ByteArray(len)
                `in`.readFully(path_bytes)

                _address = String(path_bytes)
            }
        }

        if ((_linkOpts and HLINK_PLACE) != 0) {
            val len = `in`.readInt()
            _textMark = `in`.readUnicodeLEString(len)
        }

        if (`in`.remaining() > 0) {
            logger.log(
                POILogger.WARN,
                "Hyperlink data remains: " + `in`.remaining() +
                        " : " + toHex(`in`.readRemainder())
            )
        }
    }

    public override fun serialize(out: LittleEndianOutput) {
        _range!!.serialize(out)

        _guid!!.serialize(out)
        out.writeInt(0x00000002) // TODO const
        out.writeInt(_linkOpts)

        if ((_linkOpts and HLINK_LABEL) != 0) {
            out.writeInt(_label!!.length)
            StringUtil.putUnicodeLE(_label!!, out)
        }
        if ((_linkOpts and HLINK_TARGET_FRAME) != 0) {
            out.writeInt(_targetFrame!!.length)
            StringUtil.putUnicodeLE(_targetFrame!!, out)
        }

        if ((_linkOpts and HLINK_URL) != 0 && (_linkOpts and HLINK_UNC_PATH) != 0) {
            out.writeInt(_address!!.length)
            StringUtil.putUnicodeLE(_address!!, out)
        }

        if ((_linkOpts and HLINK_URL) != 0 && (_linkOpts and HLINK_UNC_PATH) == 0) {
            _moniker!!.serialize(out)
            if (URL_MONIKER == _moniker) {
                if (_uninterpretedTail == null) {
                    out.writeInt(_address!!.length * 2)
                    StringUtil.putUnicodeLE(_address!!, out)
                } else {
                    out.writeInt(_address!!.length * 2 + TAIL_SIZE)
                    StringUtil.putUnicodeLE(_address!!, out)
                    Companion.writeTail(_uninterpretedTail!!, out)
                }
            } else if (FILE_MONIKER == _moniker) {
                out.writeShort(_fileOpts)
                out.writeInt(_shortFilename!!.length)
                StringUtil.putCompressedUnicode(_shortFilename!!, out)
                Companion.writeTail(_uninterpretedTail!!, out)
                if (_address == null) {
                    out.writeInt(0)
                } else {
                    val addrLen = _address!!.length * 2
                    out.writeInt(addrLen + 6)
                    out.writeInt(addrLen)
                    out.writeShort(0x0003) // TODO const
                    StringUtil.putUnicodeLE(_address!!, out)
                }
            }
        }
        if ((_linkOpts and HLINK_PLACE) != 0) {
            out.writeInt(_textMark!!.length)
            StringUtil.putUnicodeLE(_textMark!!, out)
        }
    }

    override fun getDataSize(): Int {
        var size = 0
        size += 2 + 2 + 2 + 2 //rwFirst, rwLast, colFirst, colLast
        size += GUID.Companion.ENCODED_SIZE
        size += 4 //label_opts
        size += 4 //link_opts
        if ((_linkOpts and HLINK_LABEL) != 0) {
            size += 4 //link length
            size += _label!!.length * 2
        }
        if ((_linkOpts and HLINK_TARGET_FRAME) != 0) {
            size += 4 // int nChars
            size += _targetFrame!!.length * 2
        }
        if ((_linkOpts and HLINK_URL) != 0 && (_linkOpts and HLINK_UNC_PATH) != 0) {
            size += 4 // int nChars
            size += _address!!.length * 2
        }
        if ((_linkOpts and HLINK_URL) != 0 && (_linkOpts and HLINK_UNC_PATH) == 0) {
            size += GUID.Companion.ENCODED_SIZE
            if (URL_MONIKER == _moniker) {
                size += 4 //address length
                size += _address!!.length * 2
                if (_uninterpretedTail != null) {
                    size += TAIL_SIZE
                }
            } else if (FILE_MONIKER == _moniker) {
                size += 2 //file_opts
                size += 4 //address length
                size += _shortFilename!!.length
                size += TAIL_SIZE
                size += 4
                if (_address != null) {
                    size += 6
                    size += _address!!.length * 2
                }
            }
        }
        if ((_linkOpts and HLINK_PLACE) != 0) {
            size += 4 //address length
            size += _textMark!!.length * 2
        }
        return size
    }


    override fun getSid(): Short {
        return Companion.sid
    }


    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[HYPERLINK RECORD]\n")
        buffer.append("    .range   = ").append(_range!!.formatAsString()).append("\n")
        buffer.append("    .guid    = ").append(_guid!!.formatAsString()).append("\n")
        buffer.append("    .linkOpts= ").append(intToHex(_linkOpts)).append("\n")
        buffer.append("    .label   = ").append(getLabel()).append("\n")
        if ((_linkOpts and HLINK_TARGET_FRAME) != 0) {
            buffer.append("    .targetFrame= ").append(getTargetFrame()).append("\n")
        }
        if ((_linkOpts and HLINK_URL) != 0 && _moniker != null) {
            buffer.append("    .moniker   = ").append(_moniker!!.formatAsString()).append("\n")
        }
        if ((_linkOpts and HLINK_PLACE) != 0) {
            buffer.append("    .textMark= ").append(getTextMark()).append("\n")
        }
        buffer.append("    .address   = ").append(getAddress()).append("\n")
        buffer.append("[/HYPERLINK RECORD]\n")
        return buffer.toString()
    }

    /**
     * Based on the link options, is this a url?
     */
    fun isUrlLink(): Boolean {
        return (_linkOpts and HLINK_URL) > 0
                && (_linkOpts and HLINK_ABS) > 0
    }

    /**
     * Based on the link options, is this a file?
     */
    fun isFileLink(): Boolean {
        return (_linkOpts and HLINK_URL) > 0
                && (_linkOpts and HLINK_ABS) == 0
    }

    /**
     * Based on the link options, is this a document?
     */
    fun isDocumentLink(): Boolean {
        return (_linkOpts and HLINK_PLACE) > 0
    }

    /**
     * Initialize a new url link
     */
    fun newUrlLink() {
        _range = HSSFCellRangeAddress(0, 0, 0, 0)
        _guid = STD_MONIKER
        _linkOpts = HLINK_URL or HLINK_ABS or HLINK_LABEL
        setLabel("")
        _moniker = URL_MONIKER
        setAddress("")
        _uninterpretedTail = URL_TAIL
    }

    /**
     * Initialize a new file link
     */
    fun newFileLink() {
        _range = HSSFCellRangeAddress(0, 0, 0, 0)
        _guid = STD_MONIKER
        _linkOpts = HLINK_URL or HLINK_LABEL
        _fileOpts = 0
        setLabel("")
        _moniker = FILE_MONIKER
        setAddress(null)
        setShortFilename("")
        _uninterpretedTail = FILE_TAIL
    }

    /**
     * Initialize a new document link
     */
    fun newDocumentLink() {
        _range = HSSFCellRangeAddress(0, 0, 0, 0)
        _guid = STD_MONIKER
        _linkOpts = HLINK_LABEL or HLINK_PLACE
        setLabel("")
        _moniker = FILE_MONIKER
        setAddress("")
        setTextMark("")
    }

    override fun clone(): Any {
        val rec = HyperlinkRecord()
        rec._range = _range!!.copy()
        rec._guid = _guid
        rec._linkOpts = _linkOpts
        rec._fileOpts = _fileOpts
        rec._label = _label
        rec._address = _address
        rec._moniker = _moniker
        rec._shortFilename = _shortFilename
        rec._targetFrame = _targetFrame
        rec._textMark = _textMark
        rec._uninterpretedTail = _uninterpretedTail
        return rec
    }

    companion object {
        const val sid: Short = 0x01B8

        /**
         * Link flags
         */
        const val HLINK_URL: Int = 0x01 // File link or URL.
        const val HLINK_ABS: Int = 0x02 // Absolute path.
        const val HLINK_LABEL: Int = 0x14 // Has label/description.

        /** Place in worksheet. If set, the [._textMark] field will be present  */
        const val HLINK_PLACE: Int = 0x08
        private const val HLINK_TARGET_FRAME = 0x80 // has 'target frame'
        private const val HLINK_UNC_PATH = 0x100 // has UNC path

        val STD_MONIKER: GUID = GUID.Companion.parse("79EAC9D0-BAF9-11CE-8C82-00AA004BA90B")
        val URL_MONIKER: GUID = GUID.Companion.parse("79EAC9E0-BAF9-11CE-8C82-00AA004BA90B")
        val FILE_MONIKER: GUID = GUID.Companion.parse("00000303-0000-0000-C000-000000000046")

        /** expected Tail of a URL link  */
        private val URL_TAIL =
            readFromString("79 58 81 F4  3B 1D 7F 48   AF 2C 82 5D  C4 85 27 63   00 00 00 00  A5 AB 00 00")

        /** expected Tail of a file link  */
        private val FILE_TAIL =
            readFromString("FF FF AD DE  00 00 00 00   00 00 00 00  00 00 00 00   00 00 00 00  00 00 00 00")

        private val TAIL_SIZE: Int = FILE_TAIL.size

        private fun cleanString(s: String?): String? {
            if (s == null) {
                return null
            }
            val idx = s.indexOf('\u0000')
            if (idx < 0) {
                return s
            }
            return s.substring(0, idx)
        }

        private fun appendNullTerm(s: String?): String? {
            if (s == null) {
                return null
            }
            return s + '\u0000'
        }

        private fun readTail(expectedTail: ByteArray, `in`: LittleEndianInput): ByteArray {
            val result = ByteArray(TAIL_SIZE)
            `in`.readFully(result)
            if (false) { // Quite a few examples in the unit tests which don't have the exact expected tail
                for (i in expectedTail.indices) {
                    if (expectedTail[i] != result[i]) {
                        System.err.println(
                            ("Mismatch in tail byte [" + i + "]"
                                    + "expected " + (expectedTail[i].toInt() and 0xFF) + " but got " + (result[i].toInt() and 0xFF))
                        )
                    }
                }
            }
            return result
        }

        private fun writeTail(tail: ByteArray, out: LittleEndianOutput) {
            out.write(tail)
        }
    }
}
