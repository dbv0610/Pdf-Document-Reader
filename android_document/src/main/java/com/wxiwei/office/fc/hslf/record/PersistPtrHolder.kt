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
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.POILogger
import java.util.Hashtable

/**
 * General holder for PersistPtrFullBlock and PersistPtrIncrementalBlock
 * records. We need to handle them specially, since we have to go around
 * updating UserEditAtoms if they shuffle about on disk
 * These hold references to where slides "live". If the position of a slide
 * moves, then we have update all of these. If we come up with a new version
 * of a slide, then we have to add one of these to the end of the chain
 * (via CurrentUserAtom and UserEditAtom) pointing to the new slide location
 * 
 * @author Nick Burch
 */
class PersistPtrHolder protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?
    private var _ptrData: ByteArray? // Will need to update this once we allow updates to _slideLocations
    private val _type: Long

    /**
     * Get the lookup from slide numbers to byte offsets, for the slides
     * known about by this PersistPtrHolder.
     */
    /**
     * Holds the lookup for slides to their position on disk.
     * You always need to check the most recent PersistPtrHolder
     * that knows about a given slide to find the right location
     */
    val slideLocationsLookup: Hashtable<Int?, Int?>
    /**
     * Get the lookup from slide numbers to their offsets inside
     * _ptrData, used when adding or moving slides.
     */
    /**
     * Holds the lookup from slide id to where their offset is
     * held inside _ptrData. Used when writing out, and updating
     * the positions of the slides
     */
    val slideOffsetDataLocationsLookup: Hashtable<Int?, Int?>

    val knownSlideIDs: IntArray
        /**
         * Get the list of slides that this PersistPtrHolder knows about.
         * (They will be the keys in the hashtable for looking up the positions
         * of these slides)
         */
        get() {
            val ids = IntArray(slideLocationsLookup.size)
            val e = slideLocationsLookup.keys()
            for (i in ids.indices) {
                val id = e.nextElement()
                ids[i] = id!!
            }
            return ids
        }

    /**
     * Adds a new slide, notes or similar, to be looked up by this.
     * For now, won't look for the most optimal on disk representation.
     */
    fun addSlideLookup(slideID: Int, posOnDisk: Int) {
        // PtrData grows by 8 bytes:
        //  4 bytes for the new info block
        //  4 bytes for the slide offset
        val newPtrData = ByteArray(_ptrData!!.size + 8)
        System.arraycopy(_ptrData, 0, newPtrData, 0, _ptrData!!.size)

        // Add to the slide location lookup hash
        slideLocationsLookup.put(slideID, posOnDisk)
        // Add to the ptrData offset lookup hash
        slideOffsetDataLocationsLookup.put(
            slideID,
            _ptrData!!.size + 4
        )

        // Build the info block
        // First 20 bits = offset number = slide ID
        // Remaining 12 bits = offset count = 1
        var infoBlock = slideID
        infoBlock += (1 shl 20)

        // Write out the data for this
        putInt(newPtrData, newPtrData.size - 8, infoBlock)
        putInt(newPtrData, newPtrData.size - 4, posOnDisk)

        // Save the new ptr data
        _ptrData = newPtrData

        // Update the atom header
        LittleEndian.putInt(_header!!, 4, newPtrData.size)
    }

    /**
     * Create a new holder for a PersistPtr record
     */
    init {
        // Sanity Checking - including whole header, so treat
        //  length as based of 0, not 8 (including header size based)
        var len = len
        if (len < 8) {
            len = 8
        }

        // Treat as an atom, grab and hold everything
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)
        _type = LittleEndian.getUShort(_header!!, 2).toLong()

        // Try to make sense of the data part:
        // Data part is made up of a number of these sets:
        //   32 bit info value
        //		12 bits count of # of entries
        //      base number for these entries
        //   count * 32 bit offsets
        // Repeat as many times as you have data
        this.slideLocationsLookup = Hashtable<Int?, Int?>()
        this.slideOffsetDataLocationsLookup = Hashtable<Int?, Int?>()
        _ptrData = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _ptrData, 0, _ptrData!!.size)

        var pos = 0
        while (pos < _ptrData!!.size) {
            // Grab the info field
            val info = LittleEndian.getUInt(_ptrData!!, pos)

            // First 20 bits = offset number
            // Remaining 12 bits = offset count
            val offset_count = (info shr 20).toInt()
            val offset_no = (info - (offset_count shl 20)).toInt()

            //System.out.println("Info is " + info + ", count is " + offset_count + ", number is " + offset_no);

            // Wind on by the 4 byte info header
            pos += 4

            // Grab the offsets for each of the sheets
            for (i in 0..<offset_count) {
                val sheet_no = offset_no + i
                val sheet_offset = LittleEndian.getUInt(_ptrData!!, pos)
                slideLocationsLookup.put(sheet_no, sheet_offset.toInt())
                slideOffsetDataLocationsLookup.put(sheet_no, pos)

                // Wind on by 4 bytes per sheet found
                pos += 4
            }
        }
    }

    /**
     * Return the value we were given at creation, be it 6001 or 6002
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * At write-out time, update the references to the sheets to their
     * new positions
     */
    public override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
        val slideIDs = this.knownSlideIDs

        // Loop over all the slides we know about
        // Find where they used to live, and where they now live
        // Then, update the right bit of _ptrData with their new location
        for (i in slideIDs.indices) {
            val id = slideIDs[i]
            val oldPos = slideLocationsLookup.get(id)
            var newPos = oldToNewReferencesLookup!!.get(oldPos)

            if (newPos == null) {
                logger.log(
                    POILogger.WARN,
                    "Couldn't find the new location of the \"slide\" with id " + id + " that used to be at " + oldPos
                )
                logger.log(
                    POILogger.WARN,
                    "Not updating the position of it, you probably won't be able to find it any more (if you ever could!)"
                )
                newPos = oldPos
            }

            // Write out the new location
            val dataOffset = slideOffsetDataLocationsLookup.get(id)
            LittleEndian.putInt(_ptrData!!, dataOffset!!, newPos!!)

            // Update our hashtable
            slideLocationsLookup.remove(id)
            slideLocationsLookup.put(id, newPos)
        }
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _ptrData = null
    }
}
