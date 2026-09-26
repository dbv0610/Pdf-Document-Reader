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
 * An atom record that specifies the animation information for a shape.
 * 
 * @author Yegor Kozlov
 */
class AnimationInfoAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * record data
     */
    private var _recdata: ByteArray?

    /**
     * Constructs a brand new link related atom record.
     */
    constructor() {
        _recdata = ByteArray(28)

        _header = ByteArray(8)
        LittleEndian.putShort(_header!!, 0, 0x01.toShort())
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _recdata!!.size)
    }

    /**
     * Constructs the link related atom record from its
     * source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the record data
        _recdata = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _recdata, 0, len - 8)
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.AnimationInfoAtom.typeID.toLong()
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
        out.write(_recdata)
    }

    var dimColor: Int
        /**
         * A rgb structure that specifies a color for the dim effect after the animation is complete.
         * 
         * @return  color for the dim effect after the animation is complete
         */
        get() = LittleEndian.getInt(_recdata!!, 0)
        /**
         * A rgb structure that specifies a color for the dim effect after the animation is complete.
         * 
         * @param rgb  color for the dim effect after the animation is complete
         */
        set(rgb) {
            LittleEndian.putInt(_recdata!!, 0, rgb)
        }

    var mask: Int
        /**
         * A bit mask specifying options for displaying headers and footers
         * 
         * @return A bit mask specifying options for displaying headers and footers
         */
        get() = LittleEndian.getInt(_recdata!!, 4)
        /**
         * A bit mask specifying options for displaying video
         * 
         * @param mask A bit mask specifying options for displaying video
         */
        set(mask) {
            LittleEndian.putInt(_recdata!!, 4, mask)
        }

    /**
     * @param bit the bit to check
     * @return whether the specified flag is set
     */
    fun getFlag(bit: Int): Boolean {
        return (this.mask and bit) != 0
    }

    /**
     * @param  bit the bit to set
     * @param  value whether the specified bit is set
     */
    fun setFlag(bit: Int, value: Boolean) {
        var mask = this.mask
        if (value) mask = mask or bit
        else mask = mask and bit.inv()
        this.mask = mask
    }

    var soundIdRef: Int
        /**
         * A 4-byte unsigned integer that specifies a reference to a sound
         * in the SoundCollectionContainer record to locate the embedded audio
         * 
         * @return  reference to a sound
         */
        get() = LittleEndian.getInt(_recdata!!, 8)
        /**
         * A 4-byte unsigned integer that specifies a reference to a sound
         * in the SoundCollectionContainer record to locate the embedded audio
         * 
         * @param id reference to a sound
         */
        set(id) {
            LittleEndian.putInt(_recdata!!, 8, id)
        }

    var delayTime: Int
        /**
         * A signed integer that specifies the delay time, in milliseconds, before the animation starts to play.
         * If [.Automatic] is 0x1, this value MUST be greater than or equal to 0; otherwise, this field MUST be ignored.
         */
        get() = LittleEndian.getInt(_recdata!!, 12)
        /**
         * A signed integer that specifies the delay time, in milliseconds, before the animation starts to play.
         * If [.Automatic] is 0x1, this value MUST be greater than or equal to 0; otherwise, this field MUST be ignored.
         */
        set(id) {
            LittleEndian.putInt(_recdata!!, 12, id)
        }

    var orderID: Int
        /**
         * A signed integer that specifies the order of the animation in the slide.
         * It MUST be greater than or equal to -2. The value -2 specifies that this animation follows the order of
         * the corresponding placeholder shape on the main master slide or title master slide.
         * The value -1 SHOULD NOT <105> be used.
         */
        get() = LittleEndian.getInt(_recdata!!, 16)
        /**
         * A signed integer that specifies the order of the animation in the slide.
         * It MUST be greater than or equal to -2. The value -2 specifies that this animation follows the order of
         * the corresponding placeholder shape on the main master slide or title master slide.
         * The value -1 SHOULD NOT <105> be used.
         */
        set(id) {
            LittleEndian.putInt(_recdata!!, 16, id)
        }

    var slideCount: Int
        /**
         * An unsigned integer that specifies the number of slides that this animation continues playing.
         * This field is utilized only in conjunction with media.
         * The value 0xFFFFFFFF specifies that the animation plays for one slide.
         */
        get() = LittleEndian.getInt(_recdata!!, 18)
        /**
         * An unsigned integer that specifies the number of slides that this animation continues playing.
         * This field is utilized only in conjunction with media.
         * The value 0xFFFFFFFF specifies that the animation plays for one slide.
         */
        set(id) {
            LittleEndian.putInt(_recdata!!, 18, id)
        }

    override fun toString(): String {
        val buf = StringBuffer()
        buf.append("AnimationInfoAtom\n")
        buf.append("\tDimColor: " + this.dimColor + "\n")
        val mask = this.mask
        buf.append("\tMask: " + mask + ", 0x" + Integer.toHexString(mask) + "\n")
        buf.append("\t  Reverse: " + getFlag(Reverse) + "\n")
        buf.append("\t  Automatic: " + getFlag(Automatic) + "\n")
        buf.append("\t  Sound: " + getFlag(Sound) + "\n")
        buf.append("\t  StopSound: " + getFlag(StopSound) + "\n")
        buf.append("\t  Play: " + getFlag(Play) + "\n")
        buf.append("\t  Synchronous: " + getFlag(Synchronous) + "\n")
        buf.append("\t  Hide: " + getFlag(Hide) + "\n")
        buf.append("\t  AnimateBg: " + getFlag(AnimateBg) + "\n")
        buf.append("\tSoundIdRef: " + this.soundIdRef + "\n")
        buf.append("\tDelayTime: " + this.delayTime + "\n")
        buf.append("\tOrderID: " + this.orderID + "\n")
        buf.append("\tSlideCount: " + this.slideCount + "\n")
        return buf.toString()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _recdata = null
    }

    companion object {
        /**
         * whether the animation plays in the reverse direction
         */
        const val Reverse: Int = 1

        /**
         * whether the animation starts automatically
         */
        const val Automatic: Int = 4

        /**
         * whether the animation has an associated sound
         */
        const val Sound: Int = 16

        /**
         * whether all playing sounds are stopped when this animation begins
         */
        const val StopSound: Int = 64

        /**
         * whether an associated sound, media or action verb is activated when the shape is clicked.
         */
        const val Play: Int = 256

        /**
         * specifies that the animation, while playing, stops other slide show actions.
         */
        const val Synchronous: Int = 1024

        /**
         * whether the shape is hidden while the animation is not playing
         */
        const val Hide: Int = 4096

        /**
         * whether the background of the shape is animated
         */
        const val AnimateBg: Int = 16384
    }
}
