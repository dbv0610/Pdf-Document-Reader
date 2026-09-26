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
package com.wxiwei.office.fc.hpsf

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getDouble
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getLong
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.getUInt
import com.wxiwei.office.fc.util.LittleEndianConsts
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.Date
import java.util.LinkedList
import kotlin.Any
import kotlin.ByteArray
import kotlin.ClassCastException
import kotlin.Double
import kotlin.Int
import kotlin.IntArray
import kotlin.Long
import kotlin.String
import kotlin.Throws
import kotlin.code
import kotlin.intArrayOf

/**
 * 
 * Supports reading and writing of variant data.
 * 
 * 
 * **FIXME (3):** Reading and writing should be made more
 * uniform than it is now. The following items should be resolved:
 * 
 * 
 * 
 *  * 
 *
 *Reading requires a length parameter that is 4 byte greater than the
 * actual data, because the variant type field is included. 
 * 
 *  * 
 *
 *Reading reads from a byte array while writing writes to an byte array
 * output stream.
 * 
 * 
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
class VariantSupport : Variant() {
    /**
     * 
     * Checks whether HPSF supports the specified variant type. Unsupported
     * types should be implemented included in the [.SUPPORTED_TYPES]
     * array.
     * 
     * @see Variant
     * 
     * @param variantType the variant type to check
     * @return `true` if HPFS supports this type, else
     * `false`
     */
    fun isSupportedType(variantType: Int): Boolean {
        for (i in SUPPORTED_TYPES.indices) if (variantType == SUPPORTED_TYPES[i]) return true
        return false
    }


    companion object {
        /**
         * 
         * Checks whether logging of unsupported variant types warning is turned
         * on or off.
         * 
         * @return `true` if logging is turned on, else
         * `false`.
         */
        /**
         * 
         * Specifies whether warnings about unsupported variant types are to be
         * written to `System.err` or not.
         * 
         * @param logUnsupportedTypes If `true` warnings will be written,
         * if `false` they won't.
         */
        var isLogUnsupportedTypes: Boolean = false


        /**
         * 
         * Keeps a list of the variant types an "unsupported" message has already
         * been issued for.
         */
        private var unsupportedMessage: MutableList<Long>? = null

        /**
         * 
         * Writes a warning to `System.err` that a variant type is
         * unsupported by HPSF. Such a warning is written only once for each variant
         * type. Log messages can be turned on or off by 
         * 
         * @param ex The exception to log
         */
        fun writeUnsupportedTypeMessage(ex: UnsupportedVariantTypeException) {
            if (isLogUnsupportedTypes) {
                if (unsupportedMessage == null) unsupportedMessage = LinkedList<Long>()
                val vt = ex.variantType
                if (!unsupportedMessage!!.contains(vt)) {
                    System.err.println(ex.message)
                    unsupportedMessage!!.add(vt)
                }
            }
        }


        /**
         * 
         * HPSF is able to read these [Variant] types.
         */
        val SUPPORTED_TYPES: IntArray = intArrayOf(
            Variant.Companion.VT_EMPTY,
            Variant.Companion.VT_I2,
            Variant.Companion.VT_I4,
            Variant.Companion.VT_I8,
            Variant.Companion.VT_R8,
            Variant.Companion.VT_FILETIME,
            Variant.Companion.VT_LPSTR,
            Variant.Companion.VT_LPWSTR,
            Variant.Companion.VT_CF,
            Variant.Companion.VT_BOOL
        )


        /**
         * 
         * Reads a variant type from a byte array.
         * 
         * @param src The byte array
         * @param offset The offset in the byte array where the variant starts
         * @param length The length of the variant including the variant type field
         * @param type The variant type to read
         * @param codepage The codepage to use for non-wide strings
         * @return A Java object that corresponds best to the variant field. For
         * example, a VT_I4 is returned as a [Long], a VT_LPSTR as a
         * [String].
         * @exception ReadingNotSupportedException if a property is to be written
         * who's variant type HPSF does not yet support
         * @exception UnsupportedEncodingException if the specified codepage is not
         * supported.
         * @see Variant
         */
        @Throws(ReadingNotSupportedException::class, UnsupportedEncodingException::class)
        fun read(
            src: ByteArray,
            offset: Int,
            length: Int,
            type: Long,
            codepage: Int
        ): Any? {
            val value: Any?
            var o1 = offset
            var l1: Int = length - LittleEndianConsts.INT_SIZE
            var lType = type

            /* Instead of trying to read 8-bit characters from a Unicode string,
         * read 16-bit characters. */
            if (codepage == Constants.CP_UNICODE && type == Variant.Companion.VT_LPSTR.toLong()) lType =
                Variant.Companion.VT_LPWSTR.toLong()

            when (lType.toInt()) {
                Variant.Companion.VT_EMPTY -> {
                    value = null
                }

                Variant.Companion.VT_I2 -> {
                    /*
                                    * Read a short. In Java it is represented as an
                                    * Integer object.
                                    */
                    value = getShort(src, o1)
                }

                Variant.Companion.VT_I4 -> {
                    /*
                                    * Read a word. In Java it is represented as an
                                    * Integer object.
                                    */
                    value = getInt(src, o1)
                }

                Variant.Companion.VT_I8 -> {
                    /*
                                    * Read a double word. In Java it is represented as a
                                    * Long object.
                                    */
                    value = getLong(src, o1)
                }

                Variant.Companion.VT_R8 -> {
                    /*
                                    * Read an eight-byte double value. In Java it is represented as
                                    * a Double object.
                                    */
                    value = getDouble(src, o1)
                }

                Variant.Companion.VT_FILETIME -> {
                    /*
                                    * Read a FILETIME object. In Java it is represented
                                    * as a Date object.
                                    */
                    val low = getUInt(src, o1)
                    o1 += LittleEndianConsts.INT_SIZE
                    val high = getUInt(src, o1)
                    value = Util.filetimeToDate(high.toInt(), low.toInt())
                }

                Variant.Companion.VT_LPSTR -> {
                    /*
                                    * Read a byte string. In Java it is represented as a
                                    * String object. The 0x00 bytes at the end must be
                                    * stripped.
                                    */
                    val first: Int = o1 + LittleEndianConsts.INT_SIZE
                    var last = first + getUInt(src, o1) - 1
                    o1 += LittleEndianConsts.INT_SIZE
                    while (src[last.toInt()].toInt() == 0 && first <= last) last--
                    val l = (last - first + 1).toInt()
                    value = if (codepage != -1) String(
                        src, first, l,
                        charset(codepageToEncoding(codepage))
                    ) else String(src, first, l)
                }

                Variant.Companion.VT_LPWSTR -> {
                    /*
                                    * Read a Unicode string. In Java it is represented as
                                    * a String object. The 0x00 bytes at the end must be
                                    * stripped.
                                    */
                    val first: Int = o1 + LittleEndianConsts.INT_SIZE
                    val last = first + getUInt(src, o1) - 1
                    val l = last - first
                    o1 += LittleEndianConsts.INT_SIZE
                    val b = StringBuffer((last - first).toInt())
                    var i = 0
                    while (i <= l) {
                        val i1 = o1 + (i * 2)
                        val i2 = i1 + 1
                        val high = src[i2].toInt() shl 8
                        val low = src[i1].toInt() and 0x00ff
                        val c = (high or low).toChar()
                        b.append(c)
                        i++
                    }
                    /* Strip 0x00 characters from the end of the string: */
                    while (b.length > 0 && b.get(b.length - 1).code == 0x00) b.setLength(b.length - 1)
                    value = b.toString()
                }

                Variant.Companion.VT_CF -> {
                    if (l1 < 0) {
                        /**
                         * YK: reading the ClipboardData packet (VT_CF) is not quite correct.
                         * The size of the data is determined by the first four bytes of the packet
                         * while the current implementation calculates it in the Section constructor.
                         * Test files in Bugzilla 42726 and 45583 clearly show that this approach does not always work.
                         * The workaround below attempts to gracefully handle such cases instead of throwing exceptions.
                         * 
                         * August 20, 2009
                         */
                        l1 = getInt(src, o1)
                        o1 += LittleEndianConsts.INT_SIZE
                    }
                    val v = ByteArray(l1)
                    System.arraycopy(src, o1, v, 0, v.size)
                    value = v
                }

                Variant.Companion.VT_BOOL -> {
                    /*
                                    * The first four bytes in src, from src[offset] to
                                    * src[offset + 3] contain the DWord for VT_BOOL, so
                                    * skip it, we don't need it.
                                    */
                    // final int first = offset + LittleEndianConsts.INT_SIZE;
                    val bool = getUInt(src, o1)
                    if (bool != 0L) value = true
                    else value = false
                }

                else -> {
                    val v = ByteArray(l1)
                    var i = 0
                    while (i < l1) {
                        v[i] = src[(o1 + i)]
                        i++
                    }
                    throw ReadingNotSupportedException(type, v)
                }
            }
            return value
        }


        /**
         * 
         * Turns a codepage number into the equivalent character encoding's
         * name.
         * 
         * @param codepage The codepage number
         * 
         * @return The character encoding's name. If the codepage number is 65001,
         * the encoding name is "UTF-8". All other positive numbers are mapped to
         * "cp" followed by the number, e.g. if the codepage number is 1252 the
         * returned character encoding name will be "cp1252".
         * 
         * @exception UnsupportedEncodingException if the specified codepage is
         * less than zero.
         */
        @Throws(UnsupportedEncodingException::class)
        fun codepageToEncoding(codepage: Int): String {
            if (codepage <= 0) throw UnsupportedEncodingException("Codepage number may not be " + codepage)
            when (codepage) {
                Constants.CP_UTF16 -> return "UTF-16"
                Constants.CP_UTF16_BE -> return "UTF-16BE"
                Constants.CP_UTF8 -> return "UTF-8"
                Constants.CP_037 -> return "cp037"
                Constants.CP_GBK -> return "GBK"
                Constants.CP_MS949 -> return "ms949"
                Constants.CP_WINDOWS_1250 -> return "windows-1250"
                Constants.CP_WINDOWS_1251 -> return "windows-1251"
                Constants.CP_WINDOWS_1252 -> return "windows-1252"
                Constants.CP_WINDOWS_1253 -> return "windows-1253"
                Constants.CP_WINDOWS_1254 -> return "windows-1254"
                Constants.CP_WINDOWS_1255 -> return "windows-1255"
                Constants.CP_WINDOWS_1256 -> return "windows-1256"
                Constants.CP_WINDOWS_1257 -> return "windows-1257"
                Constants.CP_WINDOWS_1258 -> return "windows-1258"
                Constants.CP_JOHAB -> return "johab"
                Constants.CP_MAC_ROMAN -> return "MacRoman"
                Constants.CP_MAC_JAPAN -> return "SJIS"
                Constants.CP_MAC_CHINESE_TRADITIONAL -> return "Big5"
                Constants.CP_MAC_KOREAN -> return "EUC-KR"
                Constants.CP_MAC_ARABIC -> return "MacArabic"
                Constants.CP_MAC_HEBREW -> return "MacHebrew"
                Constants.CP_MAC_GREEK -> return "MacGreek"
                Constants.CP_MAC_CYRILLIC -> return "MacCyrillic"
                Constants.CP_MAC_CHINESE_SIMPLE -> return "EUC_CN"
                Constants.CP_MAC_ROMANIA -> return "MacRomania"
                Constants.CP_MAC_UKRAINE -> return "MacUkraine"
                Constants.CP_MAC_THAI -> return "MacThai"
                Constants.CP_MAC_CENTRAL_EUROPE -> return "MacCentralEurope"
                Constants.CP_MAC_ICELAND -> return "MacIceland"
                Constants.CP_MAC_TURKISH -> return "MacTurkish"
                Constants.CP_MAC_CROATIAN -> return "MacCroatian"
                Constants.CP_US_ACSII, Constants.CP_US_ASCII2 -> return "US-ASCII"
                Constants.CP_KOI8_R -> return "KOI8-R"
                Constants.CP_ISO_8859_1 -> return "ISO-8859-1"
                Constants.CP_ISO_8859_2 -> return "ISO-8859-2"
                Constants.CP_ISO_8859_3 -> return "ISO-8859-3"
                Constants.CP_ISO_8859_4 -> return "ISO-8859-4"
                Constants.CP_ISO_8859_5 -> return "ISO-8859-5"
                Constants.CP_ISO_8859_6 -> return "ISO-8859-6"
                Constants.CP_ISO_8859_7 -> return "ISO-8859-7"
                Constants.CP_ISO_8859_8 -> return "ISO-8859-8"
                Constants.CP_ISO_8859_9 -> return "ISO-8859-9"
                Constants.CP_ISO_2022_JP1, Constants.CP_ISO_2022_JP2, Constants.CP_ISO_2022_JP3 -> return "ISO-2022-JP"
                Constants.CP_ISO_2022_KR -> return "ISO-2022-KR"
                Constants.CP_EUC_JP -> return "EUC-JP"
                Constants.CP_EUC_KR -> return "EUC-KR"
                Constants.CP_GB2312 -> return "GB2312"
                Constants.CP_GB18030 -> return "GB18030"
                Constants.CP_SJIS -> return "SJIS"
                else -> return "cp" + codepage
            }
        }


        /**
         * 
         * Writes a variant value to an output stream. This method ensures that
         * always a multiple of 4 bytes is written.
         * 
         * 
         * If the codepage is UTF-16, which is encouraged, strings
         * **must** always be written as [VT_LPWSTR]
         * strings, not as [VT_LPSTR] strings. This method ensure this
         * by converting strings appropriately, if needed.
         * 
         * @param out The stream to write the value to.
         * @param type The variant's type.
         * @param value The variant's value.
         * @param codepage The codepage to use to write non-wide strings
         * @return The number of entities that have been written. In many cases an
         * "entity" is a byte but this is not always the case.
         * @exception IOException if an I/O exceptions occurs
         * @exception WritingNotSupportedException if a property is to be written
         * who's variant type HPSF does not yet support
         */
        @Throws(IOException::class, WritingNotSupportedException::class)
        fun write(
            out: OutputStream, type: Long,
            value: Any?, codepage: Int
        ): Int {
            var length = 0
            when (type.toInt()) {
                Variant.Companion.VT_BOOL -> {
                    val trueOrFalse: Int
                    if ((value as Boolean)) trueOrFalse = 1
                    else trueOrFalse = 0
                    length = TypeWriter.writeUIntToStream(out, trueOrFalse.toLong())
                }

                Variant.Companion.VT_LPSTR -> {
                    val bytes =
                        (if (codepage == -1) (value as String).toByteArray() else (value as String).toByteArray(
                            charset(codepageToEncoding(codepage))
                        ))
                    length = TypeWriter.writeUIntToStream(out, (bytes.size + 1).toLong())
                    val b = ByteArray(bytes.size + 1)
                    System.arraycopy(bytes, 0, b, 0, bytes.size)
                    b[b.size - 1] = 0x00
                    out.write(b)
                    length += b.size
                }

                Variant.Companion.VT_LPWSTR -> {
                    val nrOfChars = (value as String).length + 1
                    length += TypeWriter.writeUIntToStream(out, nrOfChars.toLong())
                    val s = Util.pad4(value)
                    var i = 0
                    while (i < s.size) {
                        val high = ((s[i].code and 0x0000ff00) shr 8)
                        val low = (s[i].code and 0x000000ff)
                        val highb = high.toByte()
                        val lowb = low.toByte()
                        out.write(lowb.toInt())
                        out.write(highb.toInt())
                        length += 2
                        i++
                    }
                    out.write(0x00)
                    out.write(0x00)
                    length += 2
                }

                Variant.Companion.VT_CF -> {
                    val b = value as ByteArray
                    out.write(b)
                    length = b.size
                }

                Variant.Companion.VT_EMPTY -> {
                    TypeWriter.writeUIntToStream(out, Variant.Companion.VT_EMPTY.toLong())
                    length = LittleEndianConsts.INT_SIZE
                }

                Variant.Companion.VT_I2 -> {
                    TypeWriter.writeToStream(out, (value as Int).toShort())
                    length = LittleEndianConsts.SHORT_SIZE
                }

                Variant.Companion.VT_I4 -> {
                    if (value !is Int) {
                        throw ClassCastException(
                            ("Could not cast an object to "
                                    + Int::class.java.toString() + ": "
                                    + value!!.javaClass.toString() + ", "
                                    + value.toString())
                        )
                    }
                    length += TypeWriter.writeToStream(
                        out,
                        value
                    )
                }

                Variant.Companion.VT_I8 -> {
                    TypeWriter.writeToStream(out, (value as Long))
                    length = LittleEndianConsts.LONG_SIZE
                }

                Variant.Companion.VT_R8 -> {
                    length += TypeWriter.writeToStream(
                        out,
                        (value as Double)
                    )
                }

                Variant.Companion.VT_FILETIME -> {
                    val filetime = Util.dateToFileTime(value as Date)
                    val high = ((filetime shr 32) and 0x00000000FFFFFFFFL).toInt()
                    val low = (filetime and 0x00000000FFFFFFFFL).toInt()
                    length += TypeWriter.writeUIntToStream(out, 0x0000000FFFFFFFFL and low.toLong())
                    length += TypeWriter.writeUIntToStream(
                        out,
                        0x0000000FFFFFFFFL and high.toLong()
                    )
                }

                else -> {
                    /* The variant type is not supported yet. However, if the value
                                    * is a byte array we can write it nevertheless. */
                    if (value is ByteArray) {
                        val b = value
                        out.write(b)
                        length = b.size
                        writeUnsupportedTypeMessage(WritingNotSupportedException(type, value))
                    } else throw WritingNotSupportedException(type, value)
                }
            }

            return length
        }
    }
}
