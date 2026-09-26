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

/**
 * Atom storing information for an OLE object.
 * 
 * 
 * 
 * @author Daniel Noll
 */
class ExOleObjAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Constructs a brand new link related atom record.
     */
    constructor() {
        _header = ByteArray(8)
        _data = ByteArray(24)

        LittleEndian.putShort(_header!!, 0, 1.toShort()) //MUST be 0x1
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _data!!.size)
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
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, len - 8)

        // Must be at least 24 bytes long
        require(_data!!.size >= 24) { "The length of the data for a ExOleObjAtom must be at least 24 bytes, but was only " + _data!!.size }
    }

    var drawAspect: Int
        /**
         * Gets whether the object can be completely seen, or if only the
         * icon is visible.
         * 
         * @return the draw aspect, one of the `DRAW_ASPECT_*` constants.
         */
        get() = LittleEndian.getInt(_data!!, 0)
        /**
         * Sets whether the object can be completely seen, or if only the
         * icon is visible.
         * 
         * @param aspect the draw aspect, one of the `DRAW_ASPECT_*` constants.
         */
        set(aspect) {
            LittleEndian.putInt(_data!!, 0, aspect)
        }

    var type: Int
        /**
         * Gets whether the object is embedded or linked.
         * 
         * @return the type, one of the `TYPE_EMBEDDED_*` constants.
         */
        get() = LittleEndian.getInt(_data!!, 4)
        /**
         * Sets whether the object is embedded or linked.
         * 
         * @param type the type, one of the `TYPE_EMBEDDED_*` constants.
         */
        set(type) {
            LittleEndian.putInt(_data!!, 4, type)
        }

    var objID: Int
        /**
         * Gets the unique identifier for the OLE object.
         * 
         * @return the object ID.
         */
        get() = LittleEndian.getInt(_data!!, 8)
        /**
         * Sets the unique identifier for the OLE object.
         * 
         * @param id the object ID.
         */
        set(id) {
            LittleEndian.putInt(_data!!, 8, id)
        }

    var subType: Int
        /**
         * Gets the type of OLE object.
         * 
         * @return the sub-type, one of the `SUBTYPE_*` constants.
         */
        get() = LittleEndian.getInt(_data!!, 12)
        /**
         * Sets the type of OLE object.
         * 
         * @param type the sub-type, one of the `SUBTYPE_*` constants.
         */
        set(type) {
            LittleEndian.putInt(_data!!, 12, type)
        }

    var objStgDataRef: Int
        /**
         * Gets the reference to the persistent object
         * 
         * @return the reference to the persistent object, corresponds with an
         * `ExOleObjStg` storage container.
         */
        get() = LittleEndian.getInt(_data!!, 16)
        /**
         * Sets the reference to the persistent object
         * 
         * @param ref the reference to the persistent object, corresponds with an
         * `ExOleObjStg` storage container.
         */
        set(ref) {
            LittleEndian.putInt(_data!!, 16, ref)
        }

    val isBlank: Boolean
        /**
         * Gets whether the object's image is blank.
         * 
         * @return `true` if the object's image is blank.
         */
        get() =// Even though this is a mere boolean, KOffice's code says it's an int.
            LittleEndian.getInt(_data!!, 20) != 0

    var options: Int
        /**
         * Gets misc options (the last four bytes in the atom).
         * 
         * @return `true` if the object's image is blank.
         */
        get() =// Even though this is a mere boolean, KOffice's code says it's an int.
            LittleEndian.getInt(_data!!, 20)
        /**
         * Sets misc options (the last four bytes in the atom).
         */
        set(opts) {
            // Even though this is a mere boolean, KOffice's code says it's an int.
            LittleEndian.putInt(_data!!, 20, opts)
        }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExOleObjAtom.typeID.toLong()
    }


    override fun toString(): String {
        val buf = StringBuffer()
        buf.append("ExOleObjAtom\n")
        buf.append("  drawAspect: " + this.drawAspect + "\n")
        buf.append("  type: " + this.type + "\n")
        buf.append("  objID: " + this.objID + "\n")
        buf.append("  subType: " + this.subType + "\n")
        buf.append("  objStgDataRef: " + this.objStgDataRef + "\n")
        buf.append("  options: " + this.options + "\n")
        return buf.toString()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }

    companion object {
        /**
         * The object) is displayed as an embedded object inside of a container,
         */
        const val DRAW_ASPECT_VISIBLE: Int = 1

        /**
         * The object is displayed as a thumbnail image.
         */
        const val DRAW_ASPECT_THUMBNAIL: Int = 2

        /**
         * The object is displayed as an icon.
         */
        const val DRAW_ASPECT_ICON: Int = 4

        /**
         * The object is displayed on the screen as though it were printed to a printer.
         */
        const val DRAW_ASPECT_DOCPRINT: Int = 8

        /**
         * An embedded OLE object; the object is serialized and saved within the file.
         */
        const val TYPE_EMBEDDED: Int = 0

        /**
         * A linked OLE object; the object is saved outside of the file.
         */
        const val TYPE_LINKED: Int = 1

        /**
         * The OLE object is an ActiveX control.
         */
        const val TYPE_CONTROL: Int = 2

        const val SUBTYPE_DEFAULT: Int = 0
        const val SUBTYPE_CLIPART_GALLERY: Int = 1
        const val SUBTYPE_WORD_TABLE: Int = 2
        const val SUBTYPE_EXCEL: Int = 3
        const val SUBTYPE_GRAPH: Int = 4
        const val SUBTYPE_ORGANIZATION_CHART: Int = 5
        const val SUBTYPE_EQUATION: Int = 6
        const val SUBTYPE_WORDART: Int = 7
        const val SUBTYPE_SOUND: Int = 8
        const val SUBTYPE_IMAGE: Int = 9
        const val SUBTYPE_POWERPOINT_PRESENTATION: Int = 10
        const val SUBTYPE_POWERPOINT_SLIDE: Int = 11
        const val SUBTYPE_PROJECT: Int = 12
        const val SUBTYPE_NOTEIT: Int = 13
        const val SUBTYPE_EXCEL_CHART: Int = 14
        const val SUBTYPE_MEDIA_PLAYER: Int = 15
    }
}
