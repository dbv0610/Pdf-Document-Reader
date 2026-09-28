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

import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecord
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.util.IntMapper
import com.wxiwei.office.fc.util.LittleEndianConsts

/**
 * Title:        Static String Table Record (0x00FC)
 *
 *
 * 
 * Description:  This holds all the strings for LabelSSTRecords.
 * <P>
 * REFERENCE:    PG 389 Microsoft Excel 97 Developer's Kit (ISBN:
 * 1-57231-498-2)
</P> * <P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * 
 * @see LabelSSTRecord
 * 
 * @see ContinueRecord
</P> */
class SSTRecord : ContinuableRecord {
    /** union of strings in the SST and EXTSST  */
    private var field_1_num_strings: Int

    /** according to docs ONLY SST  */
    private var field_2_num_unique_strings: Int
    private val field_3_strings: IntMapper<UnicodeString?>

    private val deserializer: SSTDeserializer?

    /** Offsets from the beginning of the SST record (even across continuations)  */
    var bucketAbsoluteOffsets: IntArray = intArrayOf()

    /** Offsets relative the start of the current SST or continue record  */
    var bucketRelativeOffsets: IntArray = intArrayOf()

    constructor() {
        field_1_num_strings = 0
        field_2_num_unique_strings = 0
        field_3_strings = IntMapper<UnicodeString?>()
        deserializer = SSTDeserializer(field_3_strings)
    }

    /**
     * Add a string.
     * 
     * @param string string to be added
     * 
     * @return the index of that string in the table
     */
    fun addString(string: UnicodeString?): Int {
        field_1_num_strings++
        val ucs: UnicodeString? = if (string == null)
            EMPTY_STRING
        else
            string
        val rval: Int
        val index = field_3_strings.getIndex(ucs)

        if (index != -1) {
            rval = index
        } else {
            // This is a new string -- we didn't see it among the
            // strings we've already collected
            rval = field_3_strings.size()
            field_2_num_unique_strings++
            SSTDeserializer.Companion.addToStringTable(field_3_strings, ucs)
        }
        return rval
    }

    /**
     * @return number of strings
     */
    fun getNumStrings(): Int {
        return field_1_num_strings
    }

    /**
     * @return number of unique strings
     */
    fun getNumUniqueStrings(): Int {
        return field_2_num_unique_strings
    }


    /**
     * Get a particular string by its index
     * 
     * @param id index into the array of strings
     * 
     * @return the desired string
     */
    fun getString(id: Int): UnicodeString? {
        return field_3_strings.get(id)
    }


    /**
     * Return a debugging string representation
     * 
     * @return string representation
     */
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[SST]\n")
        buffer.append("    .numstrings     = ")
            .append(Integer.toHexString(getNumStrings())).append("\n")
        buffer.append("    .uniquestrings  = ")
            .append(Integer.toHexString(getNumUniqueStrings())).append("\n")
        for (k in 0..<field_3_strings.size()) {
            val s = field_3_strings.get(k)!!
            buffer.append("    .string_" + k + "      = ")
                .append(s.debugInfo).append("\n")
        }
        buffer.append("[/SST]\n")
        return buffer.toString()
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Fill the fields from the data
     * <P>
     * The data consists of sets of string data. This string data is
     * arranged as follows:
    </P> * <P>
     * <CODE><pre>
     * short  string_length;   // length of string data
     * byte   string_flag;     // flag specifying special string
     * // handling
     * short  run_count;       // optional count of formatting runs
     * int    extend_length;   // optional extension length
     * char[] string_data;     // string data, can be byte[] or
     * // short[] (length of array is
     * // string_length)
     * int[]  formatting_runs; // optional formatting runs (length of
     * // array is run_count)
     * byte[] extension;       // optional extension (length of array
     * // is extend_length)
    </pre></CODE> * 
    </P> * <P>
     * The string_flag is bit mapped as follows:
    </P> * <P>
    </P> * <TABLE>
     * <TR>
     * <TH>Bit number</TH>
     * <TH>Meaning if 0</TH>
     * <TH>Meaning if 1</TH>
    </TR> * <TR>
    </TR> * <TR>
     * <TD>0</TD>
     * <TD>string_data is byte[]</TD>
     * <TD>string_data is short[]
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>1</TD>
     * <TD>Should always be 0</TD>
     * <TD>string_flag is defective
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>2</TD>
     * <TD>extension is not included</TD>
     * <TD>extension is included
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>3</TD>
     * <TD>formatting run data is not included</TD>
     * <TD>formatting run data is included
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>4</TD>
     * <TD>Should always be 0</TD>
     * <TD>string_flag is defective
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>5</TD>
     * <TD>Should always be 0</TD>
     * <TD>string_flag is defective
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>6</TD>
     * <TD>Should always be 0</TD>
     * <TD>string_flag is defective
    </TD></TR> * <TR>
    </TR> * <TR>
     * <TD>7</TD>
     * <TD>Should always be 0</TD>
     * <TD>string_flag is defective
    </TD></TR> * <TR>
    </TR></TABLE> * 
     * <P>
     * We can handle eating the overhead associated with bits 2 or 3
     * (or both) being set, but we have no idea what to do with the
     * associated data. The UnicodeString class can handle the byte[]
     * vs short[] nature of the actual string data
     * 
     * @param in the RecordInputstream to read the record from
    </P> */
    constructor(`in`: RecordInputStream) {
        // this method is ALWAYS called after construction -- using
        // the nontrivial constructor, of course -- so this is where
        // we initialize our fields
        field_1_num_strings = `in`.readInt()
        field_2_num_unique_strings = `in`.readInt()
        field_3_strings = IntMapper<UnicodeString?>()
        deserializer = SSTDeserializer(field_3_strings)
        deserializer.manufactureStrings(field_2_num_unique_strings, `in`)
    }


    /**
     * @return an iterator of the strings we hold. All instances are
     * UnicodeStrings
     */
    fun getStrings(): MutableIterator<UnicodeString?> {
        return field_3_strings.iterator()
    }

    /**
     * @return count of the strings we hold.
     */
    fun countStrings(): Int {
        return field_3_strings.size()
    }

    override fun serialize(out: ContinuableRecordOutput) {
        val serializer = SSTSerializer(field_3_strings, getNumStrings(), getNumUniqueStrings())
        serializer.serialize(out)
        bucketAbsoluteOffsets = serializer.getBucketAbsoluteOffsets()
        bucketRelativeOffsets = serializer.getBucketRelativeOffsets()
    }

    fun getDeserializer(): SSTDeserializer? {
        return deserializer
    }

    /**
     * Creates an extended string record based on the current contents of
     * the current SST record.  The offset within the stream to the SST record
     * is required because the extended string record points directly to the
     * strings in the SST record.
     * 
     * 
     * NOTE: THIS FUNCTION MUST ONLY BE CALLED AFTER THE SST RECORD HAS BEEN
     * SERIALIZED.
     * 
     * @param sstOffset     The offset in the stream to the start of the
     * SST record.
     * @return  The new SST record.
     */
    fun createExtSSTRecord(sstOffset: Int): ExtSSTRecord {
        check(!(bucketAbsoluteOffsets == null || bucketAbsoluteOffsets == null)) { "SST record has not yet been serialized." }

        val extSST = ExtSSTRecord()
        extSST.setNumStringsPerBucket(8.toShort())
        val absoluteOffsets = bucketAbsoluteOffsets.clone()
        val relativeOffsets = bucketRelativeOffsets.clone()
        for (i in absoluteOffsets.indices) absoluteOffsets[i] += sstOffset
        extSST.setBucketOffsets(absoluteOffsets, relativeOffsets)
        return extSST
    }

    /**
     * Calculates the size in bytes of the EXTSST record as it would be if the
     * record was serialized.
     * 
     * @return  The size of the ExtSST record in bytes.
     */
    fun calcExtSSTRecordSize(): Int {
        return ExtSSTRecord.Companion.getRecordSizeForStrings(field_3_strings.size())
    }

    companion object {
        const val sid: Short = 0x00FC

        private val EMPTY_STRING = UnicodeString("")

        // TODO - move these constants to test class (the only consumer)
        /** standard record overhead: two shorts (record id plus data space size) */
        val STD_RECORD_OVERHEAD: Int = 2 * LittleEndianConsts.SHORT_SIZE

        /** SST overhead: the standard record overhead, plus the number of strings and the number of unique strings -- two ints  */
        val SST_RECORD_OVERHEAD: Int = STD_RECORD_OVERHEAD + 2 * LittleEndianConsts.INT_SIZE

        /** how much data can we stuff into an SST record? That would be _max minus the standard SST record overhead  */
        val MAX_DATA_SPACE: Int = RecordInputStream.Companion.MAX_RECORD_DATA_SIZE - 8
    }
}
