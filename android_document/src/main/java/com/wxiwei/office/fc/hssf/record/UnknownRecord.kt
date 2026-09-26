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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import java.util.Locale


/**
 * Title:        Unknown Record (for debugging)
 *
 *
 * Description:  Unknown record just tells you the sid so you can figure out
 * what records you are missing.  Also helps us read/modify sheets we
 * don't know all the records to.  (HSSF leaves these alone!) 
 *
 *
 * Company:      SuperLink Software, Inc.<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Glen Stampoultzis (glens at apache.org)
</P> */
class UnknownRecord : StandardRecord {
    private var _sid: Int
    private val _rawData: ByteArray

    /**
     * @param id    id of the record -not validated, just stored for serialization
     * @param data  the data
     */
    constructor(id: Int, data: ByteArray) {
        _sid = id and 0xFFFF
        _rawData = data
    }


    /**
     * construct an unknown record.  No fields are interpreted and the record will
     * be serialized in its original form more or less
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        _sid = `in`.getSid().toInt()
        _rawData = `in`.readRemainder()
        if (false && getBiffName(_sid) == null) {
            // unknown sids in the range 0x0004-0x0013 are probably 'sub-records' of ObjectRecord
            // those sids are in a different number space.
            // TODO - put unknown OBJ sub-records in a different class
            println("Unknown record 0x" + Integer.toHexString(_sid).uppercase(Locale.getDefault()))
        }
    }

    /**
     * spit the record out AS IS. no interpretation or identification
     */
    public override fun serialize(out: LittleEndianOutput) {
        out.write(_rawData)
    }

    override fun getDataSize(): Int {
        return _rawData.size
    }

    @get:JvmName("getDataProperty")
    val data: ByteArray
        get() = _rawData

    fun getData(): ByteArray {
        return _rawData
    }

    /**
     * print a sort of string representation ([UNKNOWN RECORD] id = x [/UNKNOWN RECORD])
     */
    override fun toString(): String {
        var biffName: String? = getBiffName(_sid)
        if (biffName == null) {
            biffName = "UNKNOWNRECORD"
        }
        val sb = StringBuffer()

        sb.append("[").append(biffName).append("] (0x")
        sb.append(Integer.toHexString(_sid).uppercase(Locale.getDefault()) + ")\n")
        if (_rawData.size > 0) {
            sb.append("  rawData=").append(toHex(_rawData)).append("\n")
        }
        sb.append("[/").append(biffName).append("]\n")
        return sb.toString()
    }

    override fun getSid(): Short {
        return _sid.toShort()
    }

    override fun clone(): Any {
        // immutable - OK to return this
        return this
    }

    companion object {
        /*
	 * Some Record IDs used by POI as 'milestones' in the record stream
	 */
        /**
         * seems to be part of the [PageSettingsBlock]. Not interpreted by POI.
         * The name 'PRINTSIZE' was taken from OOO source.<br></br>
         * The few POI test samples with this record have data { 0x03, 0x00 }.
         */
        const val PRINTSIZE_0033: Int = 0x0033

        /**
         * Environment-Specific Print Record
         */
        const val PLS_004D: Int = 0x004D
        const val SHEETPR_0081: Int = 0x0081
        const val SORT_0090: Int = 0x0090
        const val STANDARDWIDTH_0099: Int = 0x0099
        const val SCL_00A0: Int = 0x00A0
        const val BITMAP_00E9: Int = 0x00E9
        const val PHONETICPR_00EF: Int = 0x00EF
        const val LABELRANGES_015F: Int = 0x015F
        const val QUICKTIP_0800: Int = 0x0800
        const val SHEETEXT_0862: Int = 0x0862 // OOO calls this SHEETLAYOUT
        const val SHEETPROTECTION_0867: Int = 0x0867
        const val HEADER_FOOTER_089C: Int = 0x089C
        const val CODENAME_1BA: Int = 0x01BA

        /**
         * These BIFF record types are known but still uninterpreted by POI
         * 
         * @return the documented name of this BIFF record type, `null` if unknown to POI
         */
        fun getBiffName(sid: Int): String? {
            // Note to POI developers:
            // Make sure you delete the corresponding entry from
            // this method any time a new Record subclass is created.
            when (sid) {
                PRINTSIZE_0033 -> return "PRINTSIZE"
                PLS_004D -> return "PLS"
                0x0050 -> return "DCON" // Data Consolidation Information
                0x007F -> return "IMDATA"
                SHEETPR_0081 -> return "SHEETPR"
                SORT_0090 -> return "SORT" // Sorting Options
                0x0094 -> return "LHRECORD" // .WK? File Conversion Information
                STANDARDWIDTH_0099 -> return "STANDARDWIDTH" //Standard Column Width
                SCL_00A0 -> return "SCL" // Window Zoom Magnification
                0x00AE -> return "SCENMAN" // Scenario Output Data

                0x00B2 -> return "SXVI" // (pivot table) View Item
                0x00B4 -> return "SXIVD" // (pivot table) Row/Column Field IDs
                0x00B5 -> return "SXLI" // (pivot table) Line Item Array

                0x00D3 -> return "OBPROJ"
                0x00DC -> return "PARAMQRY"
                0x00DE -> return "OLESIZE"
                BITMAP_00E9 -> return "BITMAP"
                PHONETICPR_00EF -> return "PHONETICPR"
                0x00F1 -> return "SXEX" // PivotTable View Extended Information

                LABELRANGES_015F -> return "LABELRANGES"
                0x01BA -> return "CODENAME"
                0x01A9 -> return "USERBVIEW"
                0x01AD -> return "QSI"

                0x01C0 -> return "EXCEL9FILE"

                0x0802 -> return "QSISXTAG" // Pivot Table and Query Table Extensions
                0x0803 -> return "DBQUERYEXT"
                0x0805 -> return "TXTQUERY"
                0x0810 -> return "SXVIEWEX9" // Pivot Table Extensions

                0x0812 -> return "CONTINUEFRT"
                QUICKTIP_0800 -> return "QUICKTIP"
                SHEETEXT_0862 -> return "SHEETEXT"
                0x0863 -> return "BOOKEXT"
                0x0864 -> return "SXADDL" // Pivot Table Additional Info
                SHEETPROTECTION_0867 -> return "SHEETPROTECTION"
                0x086B -> return "DATALABEXTCONTENTS"
                0x086C -> return "CELLWATCH"
                0x0874 -> return "DROPDOWNOBJIDS"
                0x0876 -> return "DCONN"
                0x087B -> return "CFEX"
                0x087C -> return "XFCRC"
                0x087D -> return "XFEXT"
                0x087F -> return "CONTINUEFRT12"
                0x088B -> return "PLV"
                0x088C -> return "COMPAT12"
                0x088D -> return "DXF"
                0x0892 -> return "STYLEEXT"
                0x0896 -> return "THEME"
                0x0897 -> return "GUIDTYPELIB"
                0x089A -> return "MTRSETTINGS"
                0x089B -> return "COMPRESSPICTURES"
                HEADER_FOOTER_089C -> return "HEADERFOOTER"
                0x08A1 -> return "SHAPEPROPSSTREAM"
                0x08A3 -> return "FORCEFULLCALCULATION"
                0x08A4 -> return "SHAPEPROPSSTREAM"
                0x08A5 -> return "TEXTPROPSSTREAM"
                0x08A6 -> return "RICHTEXTSTREAM"

                0x08C8 -> return "PLV{Mac Excel}"


            }
            if (isObservedButUnknown(sid)) {
                return "UNKNOWN-" + Integer.toHexString(sid).uppercase(Locale.getDefault())
            }

            return null
        }

        /**
         * @return `true` if the unknown record id has been observed in POI unit tests
         */
        private fun isObservedButUnknown(sid: Int): Boolean {
            when (sid) {
                0x0033, 0x0034, 0x01BD, 0x01C2, 0x089D, 0x089E, 0x08A7, 0x1001, 0x1006, 0x1007, 0x1009, 0x100A, 0x100B, 0x100C, 0x1014, 0x1017, 0x1018, 0x1019, 0x101A, 0x101B, 0x101D, 0x101E, 0x101F, 0x1020, 0x1021, 0x1022, 0x1024, 0x1025, 0x1026, 0x1027, 0x1032, 0x1033, 0x1034, 0x1035, 0x103A, 0x1041, 0x1043, 0x1044, 0x1045, 0x1046, 0x104A, 0x104B, 0x104E, 0x104F, 0x1051, 0x105C, 0x105D, 0x105F, 0x1060, 0x1062, 0x1063, 0x1064, 0x1065, 0x1066 -> return true
            }
            return false
        }
    }
}
