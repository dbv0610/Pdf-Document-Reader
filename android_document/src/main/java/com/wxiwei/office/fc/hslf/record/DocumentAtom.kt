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

import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import java.io.IOException
import java.io.OutputStream

/**
 * A Document Atom (type 1001). Holds misc information on the PowerPoint
 * document, lots of them size and scale related.
 * 
 * @author Nick Burch
 */
class DocumentAtom protected constructor(source: ByteArray, start: Int, len: Int) : RecordAtom() {
    private var _header: ByteArray?
    var slideSizeX: Long // PointAtom, assume 1st 4 bytes = X
    var slideSizeY: Long // PointAtom, assume 2nd 4 bytes = Y
    var notesSizeX: Long // PointAtom, assume 1st 4 bytes = X
    var notesSizeY: Long // PointAtom, assume 2nd 4 bytes = Y
    var serverZoomFrom: Long // RatioAtom, assume 1st 4 bytes = from
    var serverZoomTo: Long // RatioAtom, assume 2nd 4 bytes = to

    /** Returns a reference to the NotesMaster, or 0 if none  */
    val notesMasterPersist: Long // ref to NotesMaster, 0 if none

    /** Returns a reference to the HandoutMaster, or 0 if none  */
    val handoutMasterPersist: Long // ref to HandoutMaster, 0 if none

    val firstSlideNum: Int

    /** The Size of the Document's slides, @see DocumentAtom.SlideSize for values  */
    val slideSizeType: Int // see DocumentAtom.SlideSize

    private val saveWithFonts: Byte
    private val omitTitlePlace: Byte
    private val rightToLeft: Byte
    private val showComments: Byte

    private var reserved: ByteArray?

    /** Was the document saved with True Type fonts embeded?  */
    fun getSaveWithFonts(): Boolean {
        return saveWithFonts.toInt() != 0
    }

    /** Have the placeholders on the title slide been omitted?  */
    fun getOmitTitlePlace(): Boolean {
        return omitTitlePlace.toInt() != 0
    }

    /** Is this a Bi-Directional PPT Doc?  */
    fun getRightToLeft(): Boolean {
        return rightToLeft.toInt() != 0
    }

    /** Are comment shapes visible?  */
    fun getShowComments(): Boolean {
        return showComments.toInt() != 0
    }

    /* *************** record code follows ********************** */ /**
     * For the Document Atom
     */
    init {
        // Sanity Checking
        var len = len
        if (len < 48) {
            len = 48
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the sizes and zoom ratios
        slideSizeX = getInt(source, start + 0 + 8).toLong()
        slideSizeY = getInt(source, start + 4 + 8).toLong()
        notesSizeX = getInt(source, start + 8 + 8).toLong()
        notesSizeY = getInt(source, start + 12 + 8).toLong()
        serverZoomFrom = getInt(source, start + 16 + 8).toLong()
        serverZoomTo = getInt(source, start + 20 + 8).toLong()

        // Get the master persists
        notesMasterPersist = getInt(source, start + 24 + 8).toLong()
        handoutMasterPersist = getInt(source, start + 28 + 8).toLong()

        // Get the ID of the first slide
        firstSlideNum = getShort(source, start + 32 + 8).toInt()

        // Get the slide size type
        slideSizeType = getShort(source, start + 34 + 8).toInt()

        // Get the booleans as bytes
        saveWithFonts = source[start + 36 + 8]
        omitTitlePlace = source[start + 37 + 8]
        rightToLeft = source[start + 38 + 8]
        showComments = source[start + 39 + 8]

        // If there's any other bits of data, keep them about
        reserved = ByteArray(len - 40 - 8)
        System.arraycopy(source, start + 48, reserved, 0, reserved!!.size)
    }

    /**
     * We are of type 1001
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        // Header
        out.write(_header)

        // The sizes and zoom ratios
        writeLittleEndian(slideSizeX.toInt(), out)
        writeLittleEndian(slideSizeY.toInt(), out)
        writeLittleEndian(notesSizeX.toInt(), out)
        writeLittleEndian(notesSizeY.toInt(), out)
        writeLittleEndian(serverZoomFrom.toInt(), out)
        writeLittleEndian(serverZoomTo.toInt(), out)

        // The master persists
        writeLittleEndian(notesMasterPersist.toInt(), out)
        writeLittleEndian(handoutMasterPersist.toInt(), out)

        // The ID of the first slide
        writeLittleEndian(firstSlideNum.toShort(), out)

        // The slide size type
        writeLittleEndian(slideSizeType.toShort(), out)

        // The booleans as bytes
        out.write(saveWithFonts.toInt())
        out.write(omitTitlePlace.toInt())
        out.write(rightToLeft.toInt())
        out.write(showComments.toInt())

        // Reserved data
        out.write(reserved)
    }

    /**
     * Holds the different Slide Size values
     */
    object SlideSize {
        const val ON_SCREEN: Int = 0
        const val LETTER_SIZED_PAPER: Int = 1
        const val A4_SIZED_PAPER: Int = 2
        const val ON_35MM: Int = 3
        const val OVERHEAD: Int = 4
        const val BANNER: Int = 5
        const val CUSTOM: Int = 6
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        reserved = null
    }

    companion object {
        private const val _type = 1001L
    }
}
