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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import java.io.IOException
import java.io.OutputStream

/**
 * The special info runs contained in this text.
 * Special info runs consist of character properties which don?t follow styles.
 * 
 * @author Yegor Kozlov
 */
class TextSpecInfoAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordAtom() {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Constructs the link related atom record from its
     * source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    init {
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, len - 8)
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.TextSpecInfoAtom.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     * 
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        out.write(_header)
        out.write(_data)
    }

    /**
     * Update the text length
     * 
     * @param size the text length
     */
    fun setTextSize(size: Int) {
        LittleEndian.putInt(_data!!, 0, size)
    }

    /**
     * Reset the content to one info run with the default values
     * @param size  the site of parent text
     */
    fun reset(size: Int) {
        _data = ByteArray(10)
        // 01 00 00 00
        LittleEndian.putInt(_data!!, 0, size)
        // 01 00 00 00
        LittleEndian.putInt(_data!!, 4, 1) //mask
        // 00 00
        LittleEndian.putShort(_data!!, 8, 0.toShort()) //langId

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header!!, 4, _data!!.size)
    }

    val charactersCovered: Int
        /**
         * Get the number of characters covered by this records
         * 
         * @return the number of characters covered by this records
         */
        get() {
            var covered = 0
            val runs = this.textSpecInfoRuns
            for (i in runs.indices) covered += runs[i]!!.len
            return covered
        }

    val textSpecInfoRuns: Array<TextSpecInfoRun?>
        get() {
            val lst =
                ArrayList<TextSpecInfoRun?>()
            var pos = 0
            val bits = intArrayOf(1, 0, 2)
            while (pos < _data!!.size) {
                val run = TextSpecInfoRun()
                run.len = LittleEndian.getInt(_data!!, pos)
                pos += 4
                run.mask = LittleEndian.getInt(_data!!, pos)
                pos += 4
                for (i in bits.indices) {
                    if ((run.mask and (1 shl bits[i])) != 0) {
                        when (bits[i]) {
                            0 -> {
                                run.spellInfo =
                                    LittleEndian.getShort(_data!!, pos)
                                pos += 2
                            }

                            1 -> {
                                run.langId =
                                    LittleEndian.getShort(_data!!, pos)
                                pos += 2
                            }

                            2 -> {
                                run.altLangId =
                                    LittleEndian.getShort(_data!!, pos)
                                pos += 2
                            }
                        }
                    }
                }
                lst.add(run)
            }
            return lst.toTypedArray<TextSpecInfoRun?>()
        }

    class TextSpecInfoRun {
        //Length of special info run.
        var len: Int = 0

        //Special info mask of this run;
        var mask: Int = 0

        /**
         * Spelling status of this text. See Spell Info table below.
         * 
         * 
         * Spell Info Types:
         *  * 0    Unchecked
         *  * 1    Previously incorrect, needs rechecking
         *  * 2    Correct
         *  * 3    Incorrect
         * 
         * @return Spelling status of this text
         */
        // info fields as indicated by the mask.
        // -1 means the bit is not set
        var spellInfo: Short = -1
        @get:JvmName("getLangIdProperty")
        var langId: Short = -1

        /**
         * Alternate Windows LANGID of this text;
         * must be a valid non-East Asian LANGID if the text has an East Asian language,
         * otherwise may be an East Asian LANGID or language neutral (zero).
         * 
         * @return  Alternate Windows LANGID of this text
         */
        var altLangId: Short = -1

        /**
         * Windows LANGID for this text.
         * 
         * @return Windows LANGID for this text.
         */
        fun getLangId(): Short {
            return spellInfo
        }

        /**
         * @return Length of special info run.
         */
        fun length(): Int {
            return len
        }
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }
}
