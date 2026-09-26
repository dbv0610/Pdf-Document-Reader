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

/**
 * 
 * Maps section format IDs to [PropertyIDMap]s. It is
 * initialized with two well-known section format IDs: those of the
 * <tt>\005SummaryInformation</tt> stream and the
 * <tt>\005DocumentSummaryInformation</tt> stream.
 * 
 * 
 * If you have a section format ID you can use it as a key to query
 * this map.  If you get a [PropertyIDMap] returned your section
 * is well-known and you can query the [PropertyIDMap] for PID
 * strings. If you get back `null` you are on your own.
 * 
 * 
 * This [java.util.Map] expects the byte arrays of section format IDs
 * as keys. A key maps to a [PropertyIDMap] describing the
 * property IDs in sections with the specified section format ID.
 * 
 * @author Rainer Klute (klute@rainer-klute.de)
 */
class SectionIDMap : HashMap<Any?, Any?>() {
    /**
     * 
     * Returns the [PropertyIDMap] for a given section format
     * ID.
     * 
     * @param sectionFormatID the section format ID
     * @return the property ID map
     */
    fun get(sectionFormatID: ByteArray?): PropertyIDMap? {
        return super.get(kotlin.text.String(sectionFormatID!!)) as PropertyIDMap?
    }


    /**
     * 
     * Returns the [PropertyIDMap] for a given section format
     * ID.
     * 
     * @param sectionFormatID A section format ID as a <tt>byte[]</tt> .
     * @return the property ID map
     */
    @Deprecated(
        """Use {@link #get(byte[])} instead!
      """
    )
    override fun get(sectionFormatID: Any?): Any? {
        return get(sectionFormatID as ByteArray?)
    }


    /**
     * 
     * Associates a section format ID with a [ ].
     * 
     * @param sectionFormatID the section format ID
     * @param propertyIDMap the property ID map
     * @return as defined by [java.util.Map.put]
     */
    fun put(
        sectionFormatID: ByteArray?,
        propertyIDMap: PropertyIDMap?
    ): Any? {
        return super.put(kotlin.text.String(sectionFormatID!!), propertyIDMap)
    }


    /**
     * @see .put
     * @param key This parameter remains undocumented since the method is
     * deprecated.
     * @param value This parameter remains undocumented since the method is
     * deprecated.
     * @return The return value remains undocumented since the method is
     * deprecated.
     */
    @Deprecated(
        """Use {@link #put(byte[], PropertyIDMap)} instead!
     
      """
    )
    override fun put(key: Any?, value: Any?): Any? {
        return put(key as ByteArray?, value as PropertyIDMap?)
    }

    companion object {
        /**
         * 
         * The SummaryInformation's section's format ID.
         */
        val SUMMARY_INFORMATION_ID: ByteArray = byteArrayOf(
            0xF2.toByte(), 0x9F.toByte(), 0x85.toByte(), 0xE0.toByte(),
            0x4F.toByte(), 0xF9.toByte(), 0x10.toByte(), 0x68.toByte(),
            0xAB.toByte(), 0x91.toByte(), 0x08.toByte(), 0x00.toByte(),
            0x2B.toByte(), 0x27.toByte(), 0xB3.toByte(), 0xD9.toByte()
        )

        /**
         * 
         * The DocumentSummaryInformation's first and second sections' format
         * ID.
         */
        val DOCUMENT_SUMMARY_INFORMATION_ID: Array<ByteArray> = arrayOf(
            byteArrayOf(
                0xD5.toByte(), 0xCD.toByte(), 0xD5.toByte(), 0x02.toByte(),
                0x2E.toByte(), 0x9C.toByte(), 0x10.toByte(), 0x1B.toByte(),
                0x93.toByte(), 0x97.toByte(), 0x08.toByte(), 0x00.toByte(),
                0x2B.toByte(), 0x2C.toByte(), 0xF9.toByte(), 0xAE.toByte()
            ),
            byteArrayOf(
                0xD5.toByte(), 0xCD.toByte(), 0xD5.toByte(), 0x05.toByte(),
                0x2E.toByte(), 0x9C.toByte(), 0x10.toByte(), 0x1B.toByte(),
                0x93.toByte(), 0x97.toByte(), 0x08.toByte(), 0x00.toByte(),
                0x2B.toByte(), 0x2C.toByte(), 0xF9.toByte(), 0xAE.toByte()
            )
        )

        /**
         * 
         * A property without a known name is described by this string.
         */
        const val UNDEFINED: String = "[undefined]"

        /**
         * 
         * The default section ID map. It maps section format IDs to
         * [PropertyIDMap]s.
         */
        private var defaultMap: SectionIDMap? = null


        val instance: SectionIDMap
            /**
             * 
             * Returns the singleton instance of the default [ ].
             * 
             * @return The instance value
             */
            get() {
                if (defaultMap == null) {
                    val m = SectionIDMap()
                    m.put(
                        SUMMARY_INFORMATION_ID,
                        PropertyIDMap.summaryInformationProperties
                    )
                    m.put(
                        DOCUMENT_SUMMARY_INFORMATION_ID[0],
                        PropertyIDMap.documentSummaryInformationProperties
                    )
                    defaultMap = m
                }
                return defaultMap!!
            }


        /**
         * 
         * Returns the property ID string that is associated with a
         * given property ID in a section format ID's namespace.
         * 
         * @param sectionFormatID Each section format ID has its own name
         * space of property ID strings and thus must be specified.
         * @param  pid The property ID
         * @return The well-known property ID string associated with the
         * property ID <var>pid</var> in the name space spanned by <var>
         * sectionFormatID</var> . If the <var>pid</var>
         * /<var>sectionFormatID </var> combination is not well-known, the
         * string "[undefined]" is returned.
         */
        fun getPIDString(
            sectionFormatID: ByteArray?,
            pid: Long
        ): String {
            val m: PropertyIDMap? = instance.get(sectionFormatID)
            if (m == null) {
                return UNDEFINED
            }
            val s = m.get(pid) as String?
            if (s == null) return UNDEFINED
            return s
        }
    }
}
