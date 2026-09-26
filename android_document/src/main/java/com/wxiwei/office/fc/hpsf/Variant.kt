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

import java.util.Collections

/**
 * 
 * The *Variant* types as defined by Microsoft's COM. I
 * found this information in [
 * http://www.marin.clara.net/COM/variant_type_definitions.htm](http://www.marin.clara.net/COM/variant_type_definitions.htm).
 * 
 * 
 * In the variant types descriptions the following shortcuts are
 * used: ** [V]** - may appear in a VARIANT,
 * **[T]** - may appear in a TYPEDESC,
 * **[P]** - may appear in an OLE property set,
 * **[S]** - may appear in a Safe Array.
 * 
 * @author Rainer Klute (klute@rainer-klute.de)
 */
open class Variant {
    companion object {
        /**
         * 
         * [V][P] Nothing, i.e. not a single byte of data.
         */
        const val VT_EMPTY: Int = 0

        /**
         * 
         * [V][P] SQL style Null.
         */
        const val VT_NULL: Int = 1

        /**
         * 
         * [V][T][P][S] 2 byte signed int.
         */
        const val VT_I2: Int = 2

        /**
         * 
         * [V][T][P][S] 4 byte signed int.
         */
        const val VT_I4: Int = 3

        /**
         * 
         * [V][T][P][S] 4 byte real.
         */
        const val VT_R4: Int = 4

        /**
         * 
         * [V][T][P][S] 8 byte real.
         */
        const val VT_R8: Int = 5

        /**
         * 
         * [V][T][P][S] currency. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_CY: Int = 6

        /**
         * 
         * [V][T][P][S] date. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_DATE: Int = 7

        /**
         * 
         * [V][T][P][S] OLE Automation string. <span style="background-color: #ffff00">How long is this? How is it
         * to be interpreted?</span>
         */
        const val VT_BSTR: Int = 8

        /**
         * 
         * [V][T][P][S] IDispatch *. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_DISPATCH: Int = 9

        /**
         * 
         * [V][T][S] SCODE. <span style="background-color: #ffff00">How
         * long is this? How is it to be interpreted?</span>
         */
        const val VT_ERROR: Int = 10

        /**
         * 
         * [V][T][P][S] True=-1, False=0.
         */
        const val VT_BOOL: Int = 11

        /**
         * 
         * [V][T][P][S] VARIANT *. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_VARIANT: Int = 12

        /**
         * 
         * [V][T][S] IUnknown *. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_UNKNOWN: Int = 13

        /**
         * 
         * [V][T][S] 16 byte fixed point.
         */
        const val VT_DECIMAL: Int = 14

        /**
         * 
         * [T] signed char.
         */
        const val VT_I1: Int = 16

        /**
         * 
         * [V][T][P][S] unsigned char.
         */
        const val VT_UI1: Int = 17

        /**
         * 
         * [T][P] unsigned short.
         */
        const val VT_UI2: Int = 18

        /**
         * 
         * [T][P] unsigned int.
         */
        const val VT_UI4: Int = 19

        /**
         * 
         * [T][P] signed 64-bit int.
         */
        const val VT_I8: Int = 20

        /**
         * 
         * [T][P] unsigned 64-bit int.
         */
        const val VT_UI8: Int = 21

        /**
         * 
         * [T] signed machine int.
         */
        const val VT_INT: Int = 22

        /**
         * 
         * [T] unsigned machine int.
         */
        const val VT_UINT: Int = 23

        /**
         * 
         * [T] C style void.
         */
        const val VT_VOID: Int = 24

        /**
         * 
         * [T] Standard return type. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_HRESULT: Int = 25

        /**
         * 
         * [T] pointer type. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_PTR: Int = 26

        /**
         * 
         * [T] (use VT_ARRAY in VARIANT).
         */
        const val VT_SAFEARRAY: Int = 27

        /**
         * 
         * [T] C style array. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_CARRAY: Int = 28

        /**
         * 
         * [T] user defined type. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_USERDEFINED: Int = 29

        /**
         * 
         * [T][P] null terminated string.
         */
        const val VT_LPSTR: Int = 30

        /**
         * 
         * [T][P] wide (Unicode) null terminated string.
         */
        const val VT_LPWSTR: Int = 31

        /**
         * 
         * [P] FILETIME. The FILETIME structure holds a date and time
         * associated with a file. The structure identifies a 64-bit
         * integer specifying the number of 100-nanosecond intervals which
         * have passed since January 1, 1601. This 64-bit value is split
         * into the two dwords stored in the structure.
         */
        const val VT_FILETIME: Int = 64

        /**
         * 
         * [P] Length prefixed bytes.
         */
        const val VT_BLOB: Int = 65

        /**
         * 
         * [P] Name of the stream follows.
         */
        const val VT_STREAM: Int = 66

        /**
         * 
         * [P] Name of the storage follows.
         */
        const val VT_STORAGE: Int = 67

        /**
         * 
         * [P] Stream contains an object. <span style="background-color: #ffff00"> How long is this? How is it
         * to be interpreted?</span>
         */
        const val VT_STREAMED_OBJECT: Int = 68

        /**
         * 
         * [P] Storage contains an object. <span style="background-color: #ffff00"> How long is this? How is it
         * to be interpreted?</span>
         */
        const val VT_STORED_OBJECT: Int = 69

        /**
         * 
         * [P] Blob contains an object. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_BLOB_OBJECT: Int = 70

        /**
         * 
         * [P] Clipboard format. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_CF: Int = 71

        /**
         * 
         * [P] A Class ID.
         * 
         * 
         * It consists of a 32 bit unsigned integer indicating the size
         * of the structure, a 32 bit signed integer indicating (Clipboard
         * Format Tag) indicating the type of data that it contains, and
         * then a byte array containing the data.
         * 
         * 
         * The valid Clipboard Format Tags are:
         * 
         * 
         *  * [Thumbnail.CFTAG_WINDOWS]
         *  * [Thumbnail.CFTAG_MACINTOSH]
         *  * [Thumbnail.CFTAG_NODATA]
         *  * [Thumbnail.CFTAG_FMTID]
         * 
         * 
         * <pre>typedef struct tagCLIPDATA {
         * // cbSize is the size of the buffer pointed to
         * // by pClipData, plus sizeof(ulClipFmt)
         * ULONG              cbSize;
         * long               ulClipFmt;
         * BYTE*              pClipData;
         * } CLIPDATA;</pre>
         * 
         * 
         * See [
         * msdn.microsoft.com/library/en-us/com/stgrstrc_0uwk.asp](msdn.microsoft.com/library/en-us/com/stgrstrc_0uwk.asp).
         */
        const val VT_CLSID: Int = 72

        /**
         * 
         * [P] simple counted array. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_VECTOR: Int = 0x1000

        /**
         * 
         * [V] SAFEARRAY*. <span style="background-color: #ffff00">How
         * long is this? How is it to be interpreted?</span>
         */
        const val VT_ARRAY: Int = 0x2000

        /**
         * 
         * [V] void* for local use. <span style="background-color:
        #ffff00">How long is this? How is it to be
         * interpreted?</span>
         */
        const val VT_BYREF: Int = 0x4000

        /**
         * 
         * FIXME (3): Document this!
         */
        const val VT_RESERVED: Int = 0x8000

        /**
         * 
         * FIXME (3): Document this!
         */
        const val VT_ILLEGAL: Int = 0xFFFF

        /**
         * 
         * FIXME (3): Document this!
         */
        const val VT_ILLEGALMASKED: Int = 0xFFF

        /**
         * 
         * FIXME (3): Document this!
         */
        const val VT_TYPEMASK: Int = 0xFFF


        /**
         * 
         * Maps the numbers denoting the variant types to their corresponding
         * variant type names.
         */
        private val numberToName: Map<Long, String>

        private val numberToLength: Map<Long, Int>

        /**
         * 
         * Denotes a variant type with a length that is unknown to HPSF yet.
         */
        val LENGTH_UNKNOWN: Int = -2

        /**
         * 
         * Denotes a variant type with a variable length.
         */
        val LENGTH_VARIABLE: Int = -1

        /**
         * 
         * Denotes a variant type with a length of 0 bytes.
         */
        const val LENGTH_0: Int = 0

        /**
         * 
         * Denotes a variant type with a length of 2 bytes.
         */
        const val LENGTH_2: Int = 2

        /**
         * 
         * Denotes a variant type with a length of 4 bytes.
         */
        const val LENGTH_4: Int = 4

        /**
         * 
         * Denotes a variant type with a length of 8 bytes.
         */
        const val LENGTH_8: Int = 8


        init {
            /* Initialize the number-to-name map: */
            val tm1: MutableMap<Long, String> = HashMap()
            tm1.put(0L, "VT_EMPTY")
            tm1.put(1L, "VT_NULL")
            tm1.put(2L, "VT_I2")
            tm1.put(3L, "VT_I4")
            tm1.put(4L, "VT_R4")
            tm1.put(5L, "VT_R8")
            tm1.put(6L, "VT_CY")
            tm1.put(7L, "VT_DATE")
            tm1.put(8L, "VT_BSTR")
            tm1.put(9L, "VT_DISPATCH")
            tm1.put(10L, "VT_ERROR")
            tm1.put(11L, "VT_BOOL")
            tm1.put(12L, "VT_VARIANT")
            tm1.put(13L, "VT_UNKNOWN")
            tm1.put(14L, "VT_DECIMAL")
            tm1.put(16L, "VT_I1")
            tm1.put(17L, "VT_UI1")
            tm1.put(18L, "VT_UI2")
            tm1.put(19L, "VT_UI4")
            tm1.put(20L, "VT_I8")
            tm1.put(21L, "VT_UI8")
            tm1.put(22L, "VT_INT")
            tm1.put(23L, "VT_UINT")
            tm1.put(24L, "VT_VOID")
            tm1.put(25L, "VT_HRESULT")
            tm1.put(26L, "VT_PTR")
            tm1.put(27L, "VT_SAFEARRAY")
            tm1.put(28L, "VT_CARRAY")
            tm1.put(29L, "VT_USERDEFINED")
            tm1.put(30L, "VT_LPSTR")
            tm1.put(31L, "VT_LPWSTR")
            tm1.put(64L, "VT_FILETIME")
            tm1.put(65L, "VT_BLOB")
            tm1.put(66L, "VT_STREAM")
            tm1.put(67L, "VT_STORAGE")
            tm1.put(68L, "VT_STREAMED_OBJECT")
            tm1.put(69L, "VT_STORED_OBJECT")
            tm1.put(70L, "VT_BLOB_OBJECT")
            tm1.put(71L, "VT_CF")
            tm1.put(72L, "VT_CLSID")
            val tm2: MutableMap<Long, String> = HashMap(tm1.size, 1.0f)
            tm2.putAll(tm1)
            numberToName = Collections.unmodifiableMap(tm2)

            /* Initialize the number-to-length map: */
            val tl1: MutableMap<Long, Int> = HashMap()
            tl1.put(0L, LENGTH_0)
            tl1.put(1L, LENGTH_UNKNOWN)
            tl1.put(2L, LENGTH_2)
            tl1.put(3L, LENGTH_4)
            tl1.put(4L, LENGTH_4)
            tl1.put(5L, LENGTH_8)
            tl1.put(6L, LENGTH_UNKNOWN)
            tl1.put(7L, LENGTH_UNKNOWN)
            tl1.put(8L, LENGTH_UNKNOWN)
            tl1.put(9L, LENGTH_UNKNOWN)
            tl1.put(10L, LENGTH_UNKNOWN)
            tl1.put(11L, LENGTH_UNKNOWN)
            tl1.put(12L, LENGTH_UNKNOWN)
            tl1.put(13L, LENGTH_UNKNOWN)
            tl1.put(14L, LENGTH_UNKNOWN)
            tl1.put(16L, LENGTH_UNKNOWN)
            tl1.put(17L, LENGTH_UNKNOWN)
            tl1.put(18L, LENGTH_UNKNOWN)
            tl1.put(19L, LENGTH_UNKNOWN)
            tl1.put(20L, LENGTH_UNKNOWN)
            tl1.put(21L, LENGTH_UNKNOWN)
            tl1.put(22L, LENGTH_UNKNOWN)
            tl1.put(23L, LENGTH_UNKNOWN)
            tl1.put(24L, LENGTH_UNKNOWN)
            tl1.put(25L, LENGTH_UNKNOWN)
            tl1.put(26L, LENGTH_UNKNOWN)
            tl1.put(27L, LENGTH_UNKNOWN)
            tl1.put(28L, LENGTH_UNKNOWN)
            tl1.put(29L, LENGTH_UNKNOWN)
            tl1.put(30L, LENGTH_VARIABLE)
            tl1.put(31L, LENGTH_UNKNOWN)
            tl1.put(64L, LENGTH_8)
            tl1.put(65L, LENGTH_UNKNOWN)
            tl1.put(66L, LENGTH_UNKNOWN)
            tl1.put(67L, LENGTH_UNKNOWN)
            tl1.put(68L, LENGTH_UNKNOWN)
            tl1.put(69L, LENGTH_UNKNOWN)
            tl1.put(70L, LENGTH_UNKNOWN)
            tl1.put(71L, LENGTH_UNKNOWN)
            tl1.put(72L, LENGTH_UNKNOWN)
            val tl2: MutableMap<Long, Int> = HashMap(tl1.size, 1.0f)
            tl2.putAll(tl1)
            numberToLength = Collections.unmodifiableMap(tl2)
        }


        /**
         * 
         * Returns the variant type name associated with a variant type
         * number.
         * 
         * @param variantType The variant type number
         * @return The variant type name or the string "unknown variant type"
         */
        fun getVariantName(variantType: Long): String {
            val name = numberToName[variantType]
            return if (name != null) name else "unknown variant type"
        }

        /**
         * 
         * Returns a variant type's length.
         * 
         * @param variantType The variant type number
         * @return The length of the variant type's data in bytes. If the length is
         * variable, i.e. the length of a string, -1 is returned. If HPSF does not
         * know the length, -2 is returned. The latter usually indicates an
         * unsupported variant type.
         */
        fun getVariantLength(variantType: Long): Int {
            val key = variantType.toInt().toLong()
            val length = numberToLength[key]
            if (length == null) return -2
            return length.toInt()
        }
    }
}
