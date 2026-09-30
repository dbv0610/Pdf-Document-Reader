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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.ddf.EscherRecord.EscherRecordHeader
import java.lang.reflect.Constructor

/**
 * Generates escher records when provided the byte array containing those records.
 * 
 * @author Glen Stampoultzis
 * @author Nick Burch   (nick at torchbox . com)
 * 
 * @see EscherRecordFactory
 */
open class DefaultEscherRecordFactory
/**
 * Creates an instance of the escher record factory
 */
    : EscherRecordFactory {
    /**
     * Generates an escher record including the any children contained under that record.
     * An exception is thrown if the record could not be generated.
     * 
     * @param data   The byte array containing the records
     * @param offset The starting offset into the byte array
     * @return The generated escher record
     */
    override fun createRecord(data: ByteArray?, offset: Int): EscherRecord {
        val header: EscherRecordHeader = EscherRecordHeader.Companion.readHeader(
            data!!,
            offset
        )

        // Options of 0x000F means container record
        // However, EscherTextboxRecord are containers of records for the
        //  host application, not of other Escher records, so treat them
        //  differently
        if ((header.options.toInt() and 0x000F.toShort().toInt()) == 0x000F.toShort().toInt()
            && header.recordId != EscherTextboxRecord.Companion.RECORD_ID
        ) {
            val r = EscherContainerRecord()
            r.recordId = header.recordId
            r.options = header.options
            return r
        }

        if (header.recordId >= EscherBlipRecord.Companion.RECORD_ID_START
            && header.recordId <= EscherBlipRecord.Companion.RECORD_ID_END
        ) {
            val r: EscherBlipRecord
            if (header.recordId == EscherBitmapBlip.Companion.RECORD_ID_DIB || header.recordId == EscherBitmapBlip.Companion.RECORD_ID_JPEG || header.recordId == EscherBitmapBlip.Companion.RECORD_ID_PNG) {
                r = EscherBitmapBlip()
            } else if (header.recordId == EscherMetafileBlip.Companion.RECORD_ID_EMF || header.recordId == EscherMetafileBlip.Companion.RECORD_ID_WMF || header.recordId == EscherMetafileBlip.Companion.RECORD_ID_PICT) {
                r = EscherMetafileBlip()
            } else {
                r = EscherBlipRecord()
            }
            r.recordId = header.recordId
            r.options = header.options
            return r
        }

        val recordConstructor: Constructor<out EscherRecord?>? =
            recordsMap.get(header.recordId)
        val escherRecord: EscherRecord
        if (recordConstructor == null) {
            return UnknownEscherRecord()
        }
        try {
            escherRecord = recordConstructor.newInstance(*arrayOf<Any?>())!!
        } catch (e: Exception) {
            return UnknownEscherRecord()
        }
        escherRecord.recordId = header.recordId
        escherRecord.options = header.options
        return escherRecord
    }

    companion object {
        private val escherRecordClasses = arrayOf<Class<*>?>(
            EscherBSERecord::class.java,
            EscherOptRecord::class.java,
            EscherTertiaryOptRecord::class.java,
            EscherClientAnchorRecord::class.java,
            EscherDgRecord::class.java,
            EscherSpgrRecord::class.java,
            EscherSpRecord::class.java,
            EscherClientDataRecord::class.java,
            EscherDggRecord::class.java,
            EscherSplitMenuColorsRecord::class.java,
            EscherChildAnchorRecord::class.java,
            EscherTextboxRecord::class.java,
            EscherBinaryTagRecord::class.java
        )
        private val recordsMap: MutableMap<Short?, Constructor<out EscherRecord?>?> =
            recordsToMap(escherRecordClasses)

        /**
         * Converts from a list of classes into a map that contains the record id as the key and
         * the Constructor in the value part of the map.  It does this by using reflection to look up
         * the RECORD_ID field then using reflection again to find a reference to the constructor.
         * 
         * @param recClasses The records to convert
         * @return The map containing the id/constructor pairs.
         */
        private fun recordsToMap(
            recClasses: Array<Class<*>?>
        ): MutableMap<Short?, Constructor<out EscherRecord?>?> {
            val result: MutableMap<Short?, Constructor<out EscherRecord?>?> =
                HashMap<Short?, Constructor<out EscherRecord?>?>()
            val EMPTY_CLASS_ARRAY = arrayOfNulls<Class<*>>(0)

            for (i in recClasses.indices) {
                val recCls = recClasses[i] as Class<out EscherRecord?>
                val sid: Short
                try {
                    sid = recCls.getField("RECORD_ID").getShort(null)
                } catch (e: IllegalArgumentException) {
                    throw RuntimeException(e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                } catch (e: NoSuchFieldException) {
                    throw RuntimeException(e)
                }
                val constructor: Constructor<out EscherRecord?>?
                try {
                    constructor = recCls.getConstructor(*EMPTY_CLASS_ARRAY)
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException(e)
                }
                result.put(sid, constructor)
            }
            return result
        }
    }
}
