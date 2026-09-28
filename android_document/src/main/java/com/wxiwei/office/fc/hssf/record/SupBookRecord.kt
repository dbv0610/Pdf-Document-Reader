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

import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil

/**
 * Title:        Sup Book - EXTERNALBOOK (0x01AE) 
 *
 *
 * Description:  A External Workbook Description (Supplemental Book)
 * Its only a dummy record for making new ExternSheet Record <P>
 * REFERENCE:  5.38</P><P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author Andrew C. Oliver (acoliver@apache.org)
</P> */
class SupBookRecord : StandardRecord {
    private var field_1_number_of_sheets: Short
    private var field_2_encoded_url: String?
    private var field_3_sheet_names: Array<String?>?
    private var _isAddInFunctions = false


    private constructor(isAddInFuncs: Boolean, numberOfSheets: Short) {
        // else not 'External References'
        field_1_number_of_sheets = numberOfSheets
        field_2_encoded_url = null
        field_3_sheet_names = null
        _isAddInFunctions = isAddInFuncs
    }

    constructor(url: String?, sheetNames: Array<String?>) {
        field_1_number_of_sheets = sheetNames.size.toShort()
        field_2_encoded_url = url
        field_3_sheet_names = sheetNames
        _isAddInFunctions = false
    }

    fun isExternalReferences(): Boolean {
        return field_3_sheet_names != null
    }

    fun isInternalReferences(): Boolean {
        return field_3_sheet_names == null && !_isAddInFunctions
    }

    fun isAddInFunctions(): Boolean {
        return field_3_sheet_names == null && _isAddInFunctions
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/incomplete data.
     * 
     * @param in the stream to read from
     */
    constructor(`in`: RecordInputStream) {
        val recLen = `in`.remaining()

        field_1_number_of_sheets = `in`.readShort()

        if (recLen > SMALL_RECORD_SIZE) {
            // 5.38.1 External References
            _isAddInFunctions = false

            field_2_encoded_url = `in`.readString()
            val sheetNames = arrayOfNulls<String>(field_1_number_of_sheets.toInt())
            for (i in sheetNames.indices) {
                sheetNames[i] = `in`.readString()
            }
            field_3_sheet_names = sheetNames
            return
        }
        // else not 'External References'
        field_2_encoded_url = null
        field_3_sheet_names = null

        val nextShort = `in`.readShort()
        if (nextShort == TAG_INTERNAL_REFERENCES) {
            // 5.38.2 'Internal References'
            _isAddInFunctions = false
        } else if (nextShort == TAG_ADD_IN_FUNCTIONS) {
            // 5.38.3 'Add-In Functions'
            _isAddInFunctions = true
            if (field_1_number_of_sheets.toInt() != 1) {
                throw RuntimeException(
                    ("Expected 0x0001 for number of sheets field in 'Add-In Functions' but got ("
                            + field_1_number_of_sheets + ")")
                )
            }
        } else {
            throw RuntimeException(
                ("invalid EXTERNALBOOK code ("
                        + Integer.toHexString(nextShort.toInt()) + ")")
            )
        }
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(javaClass.getName()).append(" [SUPBOOK ")

        if (isExternalReferences()) {
            sb.append("External References")
            sb.append(" nSheets=").append(field_1_number_of_sheets.toInt())
            sb.append(" url=").append(field_2_encoded_url)
        } else if (_isAddInFunctions) {
            sb.append("Add-In Functions")
        } else {
            sb.append("Internal References ")
            sb.append(" nSheets= ").append(field_1_number_of_sheets.toInt())
        }
        sb.append("]")
        return sb.toString()
    }

    override fun getDataSize(): Int {
        if (!isExternalReferences()) {
            return SMALL_RECORD_SIZE.toInt()
        }
        var sum = 2 // u16 number of sheets

        sum += StringUtil.getEncodedSize(field_2_encoded_url!!)

        for (i in field_3_sheet_names!!.indices) {
            sum += StringUtil.getEncodedSize(field_3_sheet_names!![i]!!)
        }
        return sum
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_number_of_sheets.toInt())

        if (isExternalReferences()) {
            StringUtil.writeUnicodeString(out, field_2_encoded_url!!)

            for (i in field_3_sheet_names!!.indices) {
                StringUtil.writeUnicodeString(out, field_3_sheet_names!![i]!!)
            }
        } else {
            val field2val =
                (if (_isAddInFunctions) TAG_ADD_IN_FUNCTIONS else TAG_INTERNAL_REFERENCES).toInt()

            out.writeShort(field2val)
        }
    }

    fun setNumberOfSheets(number: Short) {
        field_1_number_of_sheets = number
    }

    fun getNumberOfSheets(): Short {
        return field_1_number_of_sheets
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    fun getURL(): String {
        val encodedUrl = field_2_encoded_url
        if (encodedUrl.isNullOrEmpty()) return ""
        when (encodedUrl[0].code) {
            0 -> return encodedUrl.substring(1) // will this just be empty string?
            1 -> return Companion.decodeFileName(encodedUrl)
            2 -> return encodedUrl.substring(1)

        }
        return encodedUrl
    }

    fun getSheetNames(): Array<String?>? {
        return field_3_sheet_names!!.clone()
    }

    companion object {
        const val sid: Short = 0x01AE

        private const val SMALL_RECORD_SIZE: Short = 4
        private const val TAG_INTERNAL_REFERENCES: Short = 0x0401
        private const val TAG_ADD_IN_FUNCTIONS: Short = 0x3A01

        fun createInternalReferences(numberOfSheets: Short): SupBookRecord {
            return SupBookRecord(false, numberOfSheets)
        }

        fun createAddInFunctions(): SupBookRecord {
            return SupBookRecord(
                true,
                1.toShort() /* this field MUST be 0x0001 for add-in referencing */
            )
        }

        fun createExternalReferences(url: String?, sheetNames: Array<String?>): SupBookRecord {
            return SupBookRecord(url, sheetNames)
        }

        private fun decodeFileName(encodedUrl: String): String {
            return encodedUrl.substring(1)
            // TODO the following special characters may appear in the rest of the string, and need to get interpreted
            /* see "MICROSOFT OFFICE EXCEL 97-2007  BINARY FILE FORMAT SPECIFICATION"
        chVolume  1
        chSameVolume  2
        chDownDir  3
        chUpDir  4
        chLongVolume  5
        chStartupDir  6
        chAltStartupDir 7
        chLibDir  8

        */
        }
    }
}
