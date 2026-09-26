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

/**
 * 
 * Record that contains the functionality page breaks (horizontal and vertical)
 * 
 * 
 * The other two classes just specifically set the SIDS for record creation.
 * 
 * 
 * REFERENCE:  Microsoft Excel SDK page 322 and 420
 * 
 * @see HorizontalPageBreakRecord
 * 
 * @see VerticalPageBreakRecord
 * 
 * @author Danny Mui (dmui at apache dot org)
 */
abstract class PageBreakRecord : StandardRecord {
    private val _breaks: MutableList<Break>
    private val _breakMap: MutableMap<Int?, Break?>

    /**
     * Since both records store 2byte integers (short), no point in
     * differentiating it in the records.
     * 
     * 
     * The subs (rows or columns, don't seem to be able to set but excel sets
     * them automatically)
     */
    class Break {
        var main: Int
        var subFrom: Int
        var subTo: Int

        constructor(main: Int, subFrom: Int, subTo: Int) {
            this.main = main
            this.subFrom = subFrom
            this.subTo = subTo
        }

        constructor(`in`: RecordInputStream) {
            main = `in`.readUShort() - 1
            subFrom = `in`.readUShort()
            subTo = `in`.readUShort()
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(main + 1)
            out.writeShort(subFrom)
            out.writeShort(subTo)
        }

        companion object {
            const val ENCODED_SIZE: Int = 6
        }
    }

    protected constructor() {
        _breaks = ArrayList<Break>()
        _breakMap = HashMap<Int?, Break?>()
    }

    constructor(`in`: RecordInputStream) {
        val nBreaks = `in`.readShort().toInt()
        _breaks = ArrayList<Break>(nBreaks + 2)
        _breakMap = HashMap<Int?, Break?>()

        for (k in 0..<nBreaks) {
            val br = Break(`in`)
            _breaks.add(br)
            _breakMap.put(br.main, br)
        }
    }

    fun isEmpty(): Boolean {
        return _breaks.isEmpty()
    }

    override fun getDataSize(): Int {
        return 2 + _breaks.size * Break.Companion.ENCODED_SIZE
    }

    public override fun serialize(out: LittleEndianOutput) {
        val nBreaks = _breaks.size
        out.writeShort(nBreaks)
        for (i in 0..<nBreaks) {
            _breaks.get(i).serialize(out)
        }
    }

    fun getNumBreaks(): Int {
        return _breaks.size
    }

    fun getBreaksIterator(): MutableIterator<Break> {
        return _breaks.iterator()
    }

    override fun toString(): String {
        val retval = StringBuffer()

        val label: String?
        val mainLabel: String?
        val subLabel: String?

        if (getSid() == HorizontalPageBreakRecord.Companion.sid) {
            label = "HORIZONTALPAGEBREAK"
            mainLabel = "row"
            subLabel = "col"
        } else {
            label = "VERTICALPAGEBREAK"
            mainLabel = "column"
            subLabel = "row"
        }

        retval.append("[" + label + "]").append("\n")
        retval.append("     .sid        =").append(getSid().toInt()).append("\n")
        retval.append("     .numbreaks =").append(getNumBreaks()).append("\n")
        val iterator = getBreaksIterator()
        for (k in 0..<getNumBreaks()) {
            val region = iterator.next()

            retval.append("     .").append(mainLabel).append(" (zero-based) =").append(region.main)
                .append("\n")
            retval.append("     .").append(subLabel).append("From    =").append(region.subFrom)
                .append("\n")
            retval.append("     .").append(subLabel).append("To      =").append(region.subTo)
                .append("\n")
        }

        retval.append("[" + label + "]").append("\n")
        return retval.toString()
    }

    /**
     * Adds the page break at the specified parameters
     * @param main Depending on sid, will determine row or column to put page break (zero-based)
     * @param subFrom No user-interface to set (defaults to minimum, 0)
     * @param subTo No user-interface to set
     */
    fun addBreak(main: Int, subFrom: Int, subTo: Int) {
        val key = main
        var region = _breakMap.get(key)
        if (region == null) {
            region = Break(main, subFrom, subTo)
            _breakMap.put(key, region)
            _breaks.add(region)
        } else {
            region.main = main
            region.subFrom = subFrom
            region.subTo = subTo
        }
    }

    /**
     * Removes the break indicated by the parameter
     * @param main (zero-based)
     */
    fun removeBreak(main: Int) {
        val rowKey = main
        val region = _breakMap.get(rowKey)
        _breaks.remove(region)
        _breakMap.remove(rowKey)
    }

    /**
     * Retrieves the region at the row/column indicated
     * @param main FIXME: Document this!
     * @return The Break or null if no break exists at the row/col specified.
     */
    fun getBreak(main: Int): Break? {
        val rowKey = main
        return _breakMap.get(rowKey)
    }

    fun getBreaks(): IntArray {
        val count = getNumBreaks()
        if (count < 1) {
            return EMPTY_INT_ARRAY
        }
        val result = IntArray(count)
        for (i in 0..<count) {
            val breakItem = _breaks.get(i)
            result[i] = breakItem.main
        }
        return result
    }

    companion object {
        private val EMPTY_INT_ARRAY = intArrayOf()
    }
}
