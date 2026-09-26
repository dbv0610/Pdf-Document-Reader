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

import com.wxiwei.office.fc.hssf.record.common.FeatFormulaErr2
import com.wxiwei.office.fc.hssf.record.common.FeatProtection
import com.wxiwei.office.fc.hssf.record.common.FeatSmartTag
import com.wxiwei.office.fc.hssf.record.common.FtrHeader
import com.wxiwei.office.fc.hssf.record.common.SharedFeature
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title: Feat (Feature) Record
 * <P>
 * This record specifies Shared Features data. It is normally paired
 * up with a [FeatHdrRecord].
</P> */
class FeatRecord : StandardRecord {
    private val futureHeader: FtrHeader

    /**
     * See SHAREDFEATURES_* on [FeatHdrRecord]
     */
    private var isf_sharedFeatureType = 0
    private var reserved1: Byte = 0 // Should always be zero
    private var reserved2: Long = 0 // Should always be zero

    /** Only matters if type is ISFFEC2  */
    private var cbFeatData: Long = 0
    private var reserved3 = 0 // Should always be zero
    private var cellRefs: Array<HSSFCellRangeAddress?> = emptyArray()

    /**
     * Contents depends on isf_sharedFeatureType :
     * ISFPROTECTION -> FeatProtection
     * ISFFEC2       -> FeatFormulaErr2
     * ISFFACTOID    -> FeatSmartTag
     */
    private var sharedFeature: SharedFeature? = null

    constructor() {
        futureHeader = FtrHeader()
        futureHeader.recordType = Companion.sid
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    constructor(`in`: RecordInputStream) {
        futureHeader = FtrHeader(`in`)

        isf_sharedFeatureType = `in`.readShort().toInt()
        reserved1 = `in`.readByte()
        reserved2 = `in`.readInt().toLong()
        val cref = `in`.readUShort()
        cbFeatData = `in`.readInt().toLong()
        reserved3 = `in`.readShort().toInt()

        cellRefs = arrayOfNulls<HSSFCellRangeAddress>(cref)
        for (i in cellRefs.indices) {
            cellRefs[i] = HSSFCellRangeAddress(`in`)
        }

        when (isf_sharedFeatureType) {
            FeatHdrRecord.Companion.SHAREDFEATURES_ISFPROTECTION -> sharedFeature =
                FeatProtection(`in`)

            FeatHdrRecord.Companion.SHAREDFEATURES_ISFFEC2 -> sharedFeature = FeatFormulaErr2(`in`)
            FeatHdrRecord.Companion.SHAREDFEATURES_ISFFACTOID -> sharedFeature = FeatSmartTag(`in`)
            else -> System.err.println("Unknown Shared Feature " + isf_sharedFeatureType + " found!")
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[SHARED FEATURE]\n")


        // TODO ...
        buffer.append("[/SHARED FEATURE]\n")
        return buffer.toString()
    }

    public override fun serialize(out: LittleEndianOutput) {
        futureHeader.serialize(out)

        out.writeShort(isf_sharedFeatureType)
        out.writeByte(reserved1.toInt())
        out.writeInt(reserved2.toInt())
        out.writeShort(cellRefs.size)
        out.writeInt(cbFeatData.toInt())
        out.writeShort(reserved3)

        for (i in cellRefs.indices) {
            cellRefs[i]!!.serialize(out)
        }

        sharedFeature!!.serialize(out)
    }

    override fun getDataSize(): Int {
        return (12 + 2 + 1 + 4 + 2 + 4 + 2 +
                (cellRefs.size * HSSFCellRangeAddress.ENCODED_SIZE)
                + sharedFeature!!.dataSize)
    }

    fun getIsf_sharedFeatureType(): Int {
        return isf_sharedFeatureType
    }

    fun getCbFeatData(): Long {
        return cbFeatData
    }

    fun setCbFeatData(cbFeatData: Long) {
        this.cbFeatData = cbFeatData
    }

    fun getCellRefs(): Array<HSSFCellRangeAddress?> {
        return cellRefs
    }

    fun setCellRefs(cellRefs: Array<HSSFCellRangeAddress?>) {
        this.cellRefs = cellRefs
    }

    fun getSharedFeature(): SharedFeature {
        return sharedFeature!!
    }

    fun setSharedFeature(feature: SharedFeature) {
        this.sharedFeature = feature

        if (feature is FeatProtection) {
            isf_sharedFeatureType = FeatHdrRecord.Companion.SHAREDFEATURES_ISFPROTECTION
        }
        if (feature is FeatFormulaErr2) {
            isf_sharedFeatureType = FeatHdrRecord.Companion.SHAREDFEATURES_ISFFEC2
        }
        if (feature is FeatSmartTag) {
            isf_sharedFeatureType = FeatHdrRecord.Companion.SHAREDFEATURES_ISFFACTOID
        }

        if (isf_sharedFeatureType == FeatHdrRecord.Companion.SHAREDFEATURES_ISFFEC2) {
            cbFeatData = sharedFeature!!.dataSize.toLong()
        } else {
            cbFeatData = 0
        }
    }


    //HACK: do a "cheat" clone, see Record.java for more information
    override fun clone(): Any {
        return cloneViaReserialise()
    }


    companion object {
        const val sid: Short = 0x0868
    }
}
