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
package com.wxiwei.office.fc.hssf.record.chart

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.StandardRecord
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.getFromUnicodeLE


/**
 * DATALABEXT - Chart Data Label Extension (0x086A) <br></br>
 * 
 * @author Patrick Cheng
 */
class DataLabelExtensionRecord(`in`: RecordInputStream) : StandardRecord() {
    private val rt: Int
    private val grbitFrt: Int
    private val unused = ByteArray(8)


    //	Option flags for chart data labels
    //	The grbit  field contains the following data label option flags:
    //
    //		Bits	Mask	Flag Name		Contents
    //		0		0001h	fSeriesName		=1 if the data labels contain the series name 
    //		 								=0 otherwise
    //		1		0002h	fCategoryName	=1 if the data labels contain the category name (x-value)
    //		 								=0 otherwise 
    //		2		0004h	fValue			=1 if the data labels contain the y-value
    //		 								=0 otherwise 
    //		3		0008h	fPercent		=1 if the data labels contain a percentage
    //										=0 otherwise
    //		4		0010h	fBubbleSizes	=1 if the data labels contain bubble size
    //		 								=0 otherwise
    //		15-5	FFE0h	(unused)		Reserved; must be zero
    private val grbit: Short

    //Count of characters in the separator string
    private val cchSep: Short

    //Separator string for use in chart data labels.
    //(See section titled ‘Unicode Strings in Biff8 ’ for more information about Unicode encodings.) 
    val separator: String?

    init {
        rt = `in`.readShort().toInt()
        grbitFrt = `in`.readShort().toInt()
        `in`.readFully(unused)

        grbit = `in`.readShort()
        cchSep = `in`.readShort()

        val datas = ByteArray(`in`.available())
        `in`.readFully(datas)

        this.separator = getFromUnicodeLE(datas)
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 8
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    val isShowSeriesName: Boolean
        /**
         * 
         * @return
         */
        get() = showSeriesName.isSet(grbit.toInt())

    val isShowCategoryName: Boolean
        /**
         * 
         * @return
         */
        get() = showCategoryName.isSet(grbit.toInt())

    val isShowValue: Boolean
        /**
         * 
         * @return
         */
        get() = showValue.isSet(grbit.toInt())

    val isShowPercent: Boolean
        get() = showPercent.isSet(grbit.toInt())

    val isShowBubbleSizes: Boolean
        get() = showBubbleSizes.isSet(grbit.toInt())

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(rt)
        out.writeShort(grbitFrt)
        out.write(unused)
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[DATALABEXT]\n")
        buffer.append("    .rt      =").append(shortToHex(rt)).append('\n')
        buffer.append("    .grbitFrt=").append(shortToHex(grbitFrt)).append('\n')
        buffer.append("    .unused  =").append(toHex(unused)).append('\n')

        buffer.append("[/DATALABEXT]\n")
        return buffer.toString()
    }

    companion object {
        const val sid: Short = 0x086B

        private val showSeriesName = getInstance(0x0001)
        private val showCategoryName = getInstance(0x0002)
        private val showValue = getInstance(0x0004)
        private val showPercent = getInstance(0x0008)
        private val showBubbleSizes = getInstance(0x0010)
    }
}
