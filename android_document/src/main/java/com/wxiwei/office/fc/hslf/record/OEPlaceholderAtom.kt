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
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getUnsignedByte

/**
 * OEPlaceholderAtom (3011).
 * 
 * 
 * An atom record that specifies whether a shape is a placeholder shape.
 * 
 * 
 * @author Yegor Kozlov
 */
class OEPlaceholderAtom : RecordAtom {
    private var _header: ByteArray?

    /**
     * Returns the placement Id.
     * 
     * 
     * The placement Id is a number assigned to the placeholder. It goes from -1 to the number of placeholders.
     * It SHOULD be unique among all PlacholderAtom records contained in the corresponding slide.
     * The value 0xFFFFFFFF specifies that the corresponding shape is not a placeholder shape.
     * 
     * 
     * @return the placement Id.
     */
    /**
     * Sets the placement Id.
     * 
     * 
     * The placement Id is a number assigned to the placeholder. It goes from -1 to the number of placeholders.
     * It SHOULD be unique among all PlacholderAtom records contained in the corresponding slide.
     * The value 0xFFFFFFFF specifies that the corresponding shape is not a placeholder shape.
     * 
     * 
     * @param id the placement Id.
     */
    var placementId: Int

    /**
     * Returns the placeholder Id.
     * 
     * 
     * 
     * placeholder Id specifies the type of the placeholder shape.
     * The value MUST be one of the static constants defined in this class
     * 
     * 
     * @return the placeholder Id.
     */
    var placeholderId: Int
        private set

    /**
     * Returns the placeholder size.
     * Must be one of the PLACEHOLDER_* static constants defined in this class.
     * 
     * @return the placeholder size.
     */
    var placeholderSize: Int
        private set

    /**
     * Create a new instance of `OEPlaceholderAtom`
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 0)
        LittleEndian.putUShort(_header!!, 2, getRecordType().toInt())
        LittleEndian.putInt(_header!!, 4, 8)

        placementId = 0
        placeholderId = 0
        placeholderSize = 0
    }

    /**
     * Build an instance of `OEPlaceholderAtom` from on-disk data
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        _header = ByteArray(8)
        var offset = start
        System.arraycopy(source, start, _header, 0, 8)
        offset += _header!!.size

        placementId = getInt(source, offset)
        offset += 4
        placeholderId = getUnsignedByte(source, offset)
        offset++
        placeholderSize = getUnsignedByte(source, offset)
        offset++
    }

    /**
     * @return type of this record [RecordTypes.OEPlaceholderAtom].
     */
    public override fun getRecordType(): Long {
        return RecordTypes.OEPlaceholderAtom.typeID.toLong()
    }

    /**
     * Sets the placeholder Id.
     * 
     * 
     * 
     * placeholder Id specifies the type of the placeholder shape.
     * The value MUST be one of the static constants defined in this class
     * 
     * @param id the placeholder Id.
     */
    fun setPlaceholderId(id: Byte) {
        placeholderId = id.toInt()
    }

    /**
     * Sets the placeholder size.
     * Must be one of the PLACEHOLDER_* static constants defined in this class.
     * 
     * @param size the placeholder size.
     */
    fun setPlaceholderSize(size: Byte) {
        placeholderSize = size.toInt()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }

    companion object {
        /**
         * The full size of the master body text placeholder shape.
         */
        const val PLACEHOLDER_FULLSIZE: Int = 0

        /**
         * Half of the size of the master body text placeholder shape.
         */
        const val PLACEHOLDER_HALFSIZE: Int = 1

        /**
         * A quarter of the size of the master body text placeholder shape.
         */
        const val PLACEHOLDER_QUARTSIZE: Int = 2

        /**
         * MUST NOT be used for this field.
         */
        const val None: Byte = 0

        /**
         * The corresponding shape contains the master title text.
         * The corresponding slide MUST be a main master slide.
         */
        const val MasterTitle: Byte = 1

        /**
         * The corresponding shape contains the master body text.
         * The corresponding slide MUST be a main master slide.
         */
        const val MasterBody: Byte = 2

        /**
         * The corresponding shape contains the master center title text.
         * The corresponding slide MUST be a title master slide.
         */
        const val MasterCenteredTitle: Byte = 3

        /**
         * The corresponding shape contains the master sub-title text.
         * The corresponding slide MUST be a title master slide.
         */
        const val MasterSubTitle: Byte = 4

        /**
         * The corresponding shape contains the shared properties for slide image shapes.
         * The corresponding slide MUST be a notes master slide.
         */
        const val MasterNotesSlideImage: Byte = 5

        /**
         * The corresponding shape contains the master body text.
         * The corresponding slide MUST be a notes master slide.
         */
        const val MasterNotesBody: Byte = 6

        /**
         * The corresponding shape contains the date text field.
         * The corresponding slide MUST be a main master slide, title master slide, notes master slide, or handout master slide.
         */
        const val MasterDate: Byte = 7

        /**
         * The corresponding shape contains a slide number text field.
         * The corresponding slide MUST be a main master slide, title master slide, notes master slide, or handout master slide.
         */
        const val MasterSlideNumber: Byte = 8

        /**
         * The corresponding shape contains a footer text field.
         * The corresponding slide MUST be a main master slide, title master slide, notes master slide, or handout master slide.
         */
        const val MasterFooter: Byte = 9

        /**
         * The corresponding shape contains a header text field.
         * The corresponding slide must be a notes master slide or handout master slide.
         */
        const val MasterHeader: Byte = 10

        /**
         * The corresponding shape contains a presentation slide image.
         * The corresponding slide MUST be a notes slide.
         */
        const val NotesSlideImage: Byte = 11

        /**
         * The corresponding shape contains the notes text.
         * The corresponding slide MUST be a notes slide.
         */
        const val NotesBody: Byte = 12

        /**
         * The corresponding shape contains the title text.
         * The corresponding slide MUST be a presentation slide.
         */
        const val Title: Byte = 13

        /**
         * The corresponding shape contains the body text.
         * The corresponding slide MUST be a presentation slide.
         */
        const val Body: Byte = 14

        /**
         * The corresponding shape contains the title text.
         * The corresponding slide MUST be a presentation slide.
         */
        const val CenteredTitle: Byte = 15

        /**
         * The corresponding shape contains the sub-title text.
         * The corresponding slide MUST be a presentation slide.
         */
        const val Subtitle: Byte = 16

        /**
         * The corresponding shape contains the title text with vertical text flow.
         * The corresponding slide MUST be a presentation slide.
         */
        const val VerticalTextTitle: Byte = 17

        /**
         * The corresponding shape contains the body text with vertical text flow.
         * The corresponding slide MUST be a presentation slide.
         */
        const val VerticalTextBody: Byte = 18

        /**
         * The corresponding shape contains a generic object.
         * The corresponding slide MUST be a presentation slide.
         */
        const val Object: Byte = 19

        /**
         * The corresponding shape contains a chart object.
         * The corresponding slide MUST be a presentation slide.
         */
        const val Graph: Byte = 20

        /**
         * The corresponding shape contains a table object.
         * The corresponding slide MUST be a presentation slide.
         */
        const val Table: Byte = 21

        /**
         * The corresponding shape contains a clipart object.
         * The corresponding slide MUST be a presentation slide.
         */
        const val ClipArt: Byte = 22

        /**
         * The corresponding shape contains an organization chart object.
         * The corresponding slide MUST be a presentation slide.
         */
        const val OrganizationChart: Byte = 23

        /**
         * The corresponding shape contains a media object.
         * The corresponding slide MUST be a presentation slide.
         */
        const val MediaClip: Byte = 24
    }
}
