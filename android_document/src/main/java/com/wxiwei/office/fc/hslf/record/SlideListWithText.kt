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
import java.util.Vector

/**
 * These are tricky beasts. They contain the text of potentially
 * many (normal) slides. They are made up of several sets of
 * - SlidePersistAtom
 * - TextHeaderAtom
 * - TextBytesAtom / TextCharsAtom
 * - StyleTextPropAtom (optional)
 * - TextSpecInfoAtom (optional)
 * - InteractiveInfo (optional)
 * - TxInteractiveInfoAtom (optional)
 * and then the next SlidePersistAtom.
 * 
 * Eventually, Slides will find the blocks that interest them from all
 * the SlideListWithText entries, and refere to them
 * 
 * For now, we scan through looking for interesting bits, then creating
 * the helpful Sheet from model for them
 * 
 * @author Nick Burch
 */
// For now, pretend to be an atom
class SlideListWithText : RecordContainer {
    private var _header: ByteArray?
    /**
     * Get access to the SlideAtomsSets of the children of this record
     */
    /**
     * Get access to the SlideAtomsSets of the children of this record
     */
    var slideAtomsSets: Array<SlideAtomsSet>?

    /**
     * Create a new holder for slide records
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)

        // Group our children together into SlideAtomsSets
        // That way, model layer code can just grab the sets to use,
        //  without having to try to match the children together
        val sets = Vector<SlideAtomsSet>()
        var i = 0
        while (i < _children.size) {
            if (_children[i] is SlidePersistAtom) {
                // Find where the next SlidePersistAtom is
                var endPos = i + 1
                while (endPos < _children.size
                    && _children[endPos] !is SlidePersistAtom
                ) {
                    endPos += 1
                }

                val clen = endPos - i - 1
                var emptySet = false
                if (clen == 0) {
                    emptySet = true
                }

                // Create a SlideAtomsSets, not caring if they're empty
                //if(emptySet) { continue; }
                val spaChildren: Array<Record> = _children.copyOfRange(i + 1, i + 1 + clen)
                val set = SlideAtomsSet(_children[i] as SlidePersistAtom, spaChildren)
                sets.add(set)

                // Wind on
                i += clen
            }
            i++
        }

        // Turn the vector into an array
        slideAtomsSets = sets.toTypedArray()
    }

    /**
     * Create a new, empty, SlideListWithText
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 15)
        LittleEndian.putUShort(_header!!, 2, _type.toInt())
        LittleEndian.putInt(_header!!, 4, 0)

        // We have no children to start with
        _children = emptyArray()
        slideAtomsSets = emptyArray()
    }

    /**
     * Add a new SlidePersistAtom, to the end of the current list,
     * and update the internal list of SlidePersistAtoms
     * @param spa
     */
    fun addSlidePersistAtom(spa: SlidePersistAtom) {
        // Add the new SlidePersistAtom at the end
        appendChildRecord(spa)

        val newSAS = SlideAtomsSet(spa, emptyArray())

        // Update our SlideAtomsSets with this
        slideAtomsSets = slideAtomsSets!! + newSAS
    }

    var instance: Int
        get() = LittleEndian.getShort(_header!!, 0).toInt() shr 4
        set(inst) {
            LittleEndian.putShort(
                _header!!,
                ((inst shl 4) or 0xF).toShort()
            )
        }

    /**
     * Return the value we were given at creation
     */
    public override fun getRecordType(): Long {
        return _type
    }


    /**
     * Inner class to wrap up a matching set of records that hold the
     * text for a given sheet. Contains the leading SlidePersistAtom,
     * and all of the records until the next SlidePersistAtom. This
     * includes sets of TextHeaderAtom and TextBytesAtom/TextCharsAtom,
     * along with some others.
     */
    inner class SlideAtomsSet

    /** Create one to hold the Records for one Slide's text  */(
        /** Get the SlidePersistAtom, which gives details on the Slide this text is associated with  */
        val slidePersistAtom: SlidePersistAtom?,
        /** Get the Text related records for this slide  */
        var slideRecords: Array<Record>?
    ) {
        /**
         * 
         */
        fun dispose() {
            if (slidePersistAtom != null) {
                slidePersistAtom.dispose()
            }
            val records = slideRecords
            if (records != null) {
                for (rec in records!!) {
                    rec.dispose()
                }
                slideRecords = null
            }
        }
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        val sets = slideAtomsSets
        if (sets != null) {
            for (sas in sets!!) {
                sas.dispose()
            }
            slideAtomsSets = null
        }
    }

    companion object {
        /**
         * Instance filed of the record header indicates that this SlideListWithText stores
         * references to slides
         */
        const val SLIDES: Int = 0

        /**
         * Instance filed of the record header indicates that this SlideListWithText stores
         * references to master slides
         */
        const val MASTER: Int = 1

        /**
         * Instance filed of the record header indicates that this SlideListWithText stores
         * references to notes
         */
        const val NOTES: Int = 2

        private const val _type: Long = 4080
    }
}
