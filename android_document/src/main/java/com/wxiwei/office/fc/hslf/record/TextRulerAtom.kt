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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import java.io.IOException
import java.io.OutputStream

/**
 * Ruler of a text as it differs from the style's ruler settings.
 * 
 * @author Yegor Kozlov
 */
class TextRulerAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Default distance between tab stops, in master coordinates (576 dpi).
     */
    //ruler internals
    var defaultTabSize: Int = 0
        private set

    /**
     * Number of indent levels (maximum 5).
     */
    var numberOfLevels: Int = 0
        private set

    /**
     * Default distance between tab stops, in master coordinates (576 dpi).
     */
    var tabStops: IntArray? = null
        private set

    /**
     * First line of paragraph's distance from shape's left margin, in master coordinates (576 dpi).
     */
    var bulletOffsets: IntArray? = intArrayOf(-1, -1, -1, -1, -1)
        private set

    /**
     * Paragraph's distance from shape's left margin, in master coordinates (576 dpi).
     */
    var textOffsets: IntArray? = intArrayOf(-1, -1, -1, -1, -1)
        private set

    /**
     * Constructs a new empty ruler atom.
     */
    constructor() {
        _header = ByteArray(8)
        _data = ByteArray(0)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _data!!.size)
    }

    /**
     * Constructs the ruler atom record from its
     * source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, len - 8)

        try {
            read()
        } catch (e: Exception) {
            //logger.log(POILogger.ERROR, "Failed to parse TextRulerAtom: " + e.getMessage());
            e.printStackTrace()
        }
    }

    /**
     * Gets the record type.
     * 
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.TextRulerAtom.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
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
     * Read the record bytes and initialize the internal variables
     */
    private fun read() {
        var pos = 0
        val mask = LittleEndian.getShort(_data!!)
        pos += 4
        var `val`: Short
        val bits = intArrayOf(1, 0, 2, 3, 8, 4, 9, 5, 10, 6, 11, 7, 12)
        for (i in bits.indices) {
            if ((mask.toInt() and (1 shl bits[i])) != 0) {
                when (bits[i]) {
                    0 -> {
                        //defaultTabSize
                        defaultTabSize = LittleEndian.getShort(_data!!, pos).toInt()
                        pos += 2
                    }

                    1 -> {
                        //numLevels
                        this.numberOfLevels = LittleEndian.getShort(_data!!, pos).toInt()
                        pos += 2
                    }

                    2 -> {
                        //tabStops
                        `val` = LittleEndian.getShort(_data!!, pos)
                        pos += 2
                        tabStops = IntArray(`val` * 2)
                        var j = 0
                        while (j < tabStops!!.size) {
                            tabStops!![j] = LittleEndian.getUShort(_data!!, pos)
                            pos += 2
                            j++
                        }
                    }

                    3, 4, 5, 6, 7 -> {
                        //bullet.offset
                        `val` = LittleEndian.getShort(_data!!, pos)
                        pos += 2
                        bulletOffsets!![bits[i] - 3] = `val`.toInt()
                    }

                    8, 9, 10, 11, 12 -> {
                        //text.offset
                        `val` = LittleEndian.getShort(_data!!, pos)
                        pos += 2
                        textOffsets!![bits[i] - 8] = `val`.toInt()
                    }
                }
            }
        }
    }

    fun setParagraphIndent(tetxOffset: Short, bulletOffset: Short) {
        LittleEndian.putShort(_data!!, 4, tetxOffset)
        LittleEndian.putShort(_data!!, 6, bulletOffset)
        LittleEndian.putShort(_data!!, 8, bulletOffset)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
        tabStops = null
        textOffsets = null
        bulletOffsets = null
    }

    companion object {
        val paragraphInstance: TextRulerAtom
            get() {
                val data = byteArrayOf(
                    0x00, 0x00, 0xA6.toByte(), 0x0F, 0x0A, 0x00, 0x00, 0x00, 0x10, 0x03,
                    0x00, 0x00, 0xF9.toByte(), 0x00, 0x41, 0x01, 0x41, 0x01
                )
                val ruler = TextRulerAtom(data, 0, data.size)
                return ruler
            }
    }
}
