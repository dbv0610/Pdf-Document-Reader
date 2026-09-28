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

import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * The common object data record is used to store all common preferences for an excel object.
 *
 *
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class CommonObjectDataSubRecord : SubRecord {
    /**
     * Get the object type field for the CommonObjectData record.
     * 
     * @return  One of
     * OBJECT_TYPE_GROUP
     * OBJECT_TYPE_LINE
     * OBJECT_TYPE_RECTANGLE
     * OBJECT_TYPE_OVAL
     * OBJECT_TYPE_ARC
     * OBJECT_TYPE_CHART
     * OBJECT_TYPE_TEXT
     * OBJECT_TYPE_BUTTON
     * OBJECT_TYPE_PICTURE
     * OBJECT_TYPE_POLYGON
     * OBJECT_TYPE_RESERVED1
     * OBJECT_TYPE_CHECKBOX
     * OBJECT_TYPE_OPTION_BUTTON
     * OBJECT_TYPE_EDIT_BOX
     * OBJECT_TYPE_LABEL
     * OBJECT_TYPE_DIALOG_BOX
     * OBJECT_TYPE_SPINNER
     * OBJECT_TYPE_SCROLL_BAR
     * OBJECT_TYPE_LIST_BOX
     * OBJECT_TYPE_GROUP_BOX
     * OBJECT_TYPE_COMBO_BOX
     * OBJECT_TYPE_RESERVED2
     * OBJECT_TYPE_RESERVED3
     * OBJECT_TYPE_RESERVED4
     * OBJECT_TYPE_RESERVED5
     * OBJECT_TYPE_COMMENT
     * OBJECT_TYPE_RESERVED6
     * OBJECT_TYPE_RESERVED7
     * OBJECT_TYPE_RESERVED8
     * OBJECT_TYPE_RESERVED9
     * OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING
     */
    /**
     * Set the object type field for the CommonObjectData record.
     * 
     * @param field_1_objectType
     * One of
     * OBJECT_TYPE_GROUP
     * OBJECT_TYPE_LINE
     * OBJECT_TYPE_RECTANGLE
     * OBJECT_TYPE_OVAL
     * OBJECT_TYPE_ARC
     * OBJECT_TYPE_CHART
     * OBJECT_TYPE_TEXT
     * OBJECT_TYPE_BUTTON
     * OBJECT_TYPE_PICTURE
     * OBJECT_TYPE_POLYGON
     * OBJECT_TYPE_RESERVED1
     * OBJECT_TYPE_CHECKBOX
     * OBJECT_TYPE_OPTION_BUTTON
     * OBJECT_TYPE_EDIT_BOX
     * OBJECT_TYPE_LABEL
     * OBJECT_TYPE_DIALOG_BOX
     * OBJECT_TYPE_SPINNER
     * OBJECT_TYPE_SCROLL_BAR
     * OBJECT_TYPE_LIST_BOX
     * OBJECT_TYPE_GROUP_BOX
     * OBJECT_TYPE_COMBO_BOX
     * OBJECT_TYPE_RESERVED2
     * OBJECT_TYPE_RESERVED3
     * OBJECT_TYPE_RESERVED4
     * OBJECT_TYPE_RESERVED5
     * OBJECT_TYPE_COMMENT
     * OBJECT_TYPE_RESERVED6
     * OBJECT_TYPE_RESERVED7
     * OBJECT_TYPE_RESERVED8
     * OBJECT_TYPE_RESERVED9
     * OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING
     */
    var objectType: Short = 0
    /**
     * Get the object id field for the CommonObjectData record.
     */
    /**
     * Set the object id field for the CommonObjectData record.
     */
    var objectId: Int = 0
    /**
     * Get the option field for the CommonObjectData record.
     */
    /**
     * Set the option field for the CommonObjectData record.
     */
    var option: Short = 0
    /**
     * Get the reserved1 field for the CommonObjectData record.
     */
    /**
     * Set the reserved1 field for the CommonObjectData record.
     */
    var reserved1: Int = 0
    /**
     * Get the reserved2 field for the CommonObjectData record.
     */
    /**
     * Set the reserved2 field for the CommonObjectData record.
     */
    var reserved2: Int = 0
    /**
     * Get the reserved3 field for the CommonObjectData record.
     */
    /**
     * Set the reserved3 field for the CommonObjectData record.
     */
    var reserved3: Int = 0


    constructor()

    constructor(`in`: LittleEndianInput, size: Int) {
        if (size != 18) {
            throw RecordFormatException("Expected size 18 but got (" + size + ")")
        }
        this.objectType = `in`.readShort()
        this.objectId = `in`.readUShort()
        this.option = `in`.readShort()
        this.reserved1 = `in`.readInt()
        this.reserved2 = `in`.readInt()
        this.reserved3 = `in`.readInt()
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[ftCmo]\n")
        buffer.append("    .objectType           = ")
            .append("0x").append(toHex(this.objectType))
            .append(" (").append(this.objectType.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .objectId             = ")
            .append("0x").append(toHex(this.objectId))
            .append(" (").append(this.objectId).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .option               = ")
            .append("0x").append(toHex(this.option))
            .append(" (").append(this.option.toInt()).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("         .locked                   = ").append(this.isLocked).append('\n')
        buffer.append("         .printable                = ").append(this.isPrintable).append('\n')
        buffer.append("         .autofill                 = ").append(this.isAutofill).append('\n')
        buffer.append("         .autoline                 = ").append(this.isAutoline).append('\n')
        buffer.append("    .reserved1            = ")
            .append("0x").append(toHex(this.reserved1))
            .append(" (").append(this.reserved1).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .reserved2            = ")
            .append("0x").append(toHex(this.reserved2))
            .append(" (").append(this.reserved2).append(" )")
        buffer.append(System.getProperty("line.separator"))
        buffer.append("    .reserved3            = ")
            .append("0x").append(toHex(this.reserved3))
            .append(" (").append(this.reserved3).append(" )")
        buffer.append(System.getProperty("line.separator"))

        buffer.append("[/ftCmo]\n")
        return buffer.toString()
    }

    override fun serialize(out: LittleEndianOutput) {
        out.writeShort(Companion.sid.toInt())
        out.writeShort(getDataSize())

        out.writeShort(objectType.toInt())
        out.writeShort(this.objectId)
        out.writeShort(option.toInt())
        out.writeInt(this.reserved1)
        out.writeInt(this.reserved2)
        out.writeInt(this.reserved3)
    }

    override fun getDataSize(): Int {
        return 2 + 2 + 2 + 4 + 4 + 4
    }

    val sid: Short
        get() = Companion.sid

    override fun clone(): Any {
        val rec = CommonObjectDataSubRecord()

        rec.objectType = this.objectType
        rec.objectId = this.objectId
        rec.option = this.option
        rec.reserved1 = this.reserved1
        rec.reserved2 = this.reserved2
        rec.reserved3 = this.reserved3
        return rec
    }


    var isLocked: Boolean
        /**
         * true if object is locked when sheet has been protected
         * @return  the locked field value.
         */
        get() = locked.isSet(option.toInt())
        /**
         * Sets the locked field value.
         * true if object is locked when sheet has been protected
         */
        set(value) {
            this.option =
                locked.setShortBoolean(this.option, value)
        }

    var isPrintable: Boolean
        /**
         * object appears when printed
         * @return  the printable field value.
         */
        get() = printable.isSet(option.toInt())
        /**
         * Sets the printable field value.
         * object appears when printed
         */
        set(value) {
            this.option =
                printable.setShortBoolean(this.option, value)
        }

    var isAutofill: Boolean
        /**
         * whether object uses an automatic fill style
         * @return  the autofill field value.
         */
        get() = autofill.isSet(option.toInt())
        /**
         * Sets the autofill field value.
         * whether object uses an automatic fill style
         */
        set(value) {
            this.option =
                autofill.setShortBoolean(this.option, value)
        }

    var isAutoline: Boolean
        /**
         * whether object uses an automatic line style
         * @return  the autoline field value.
         */
        get() = autoline.isSet(option.toInt())
        /**
         * Sets the autoline field value.
         * whether object uses an automatic line style
         */
        set(value) {
            this.option =
                autoline.setShortBoolean(this.option, value)
        }

    companion object {
        const val sid: Short = 0x0015

        private val locked = getInstance(0x0001)
        private val printable = getInstance(0x0010)
        private val autofill = getInstance(0x2000)
        private val autoline = getInstance(0x4000)

        const val OBJECT_TYPE_GROUP: Short = 0
        const val OBJECT_TYPE_LINE: Short = 1
        const val OBJECT_TYPE_RECTANGLE: Short = 2
        const val OBJECT_TYPE_OVAL: Short = 3
        const val OBJECT_TYPE_ARC: Short = 4
        const val OBJECT_TYPE_CHART: Short = 5
        const val OBJECT_TYPE_TEXT: Short = 6
        const val OBJECT_TYPE_BUTTON: Short = 7
        const val OBJECT_TYPE_PICTURE: Short = 8
        const val OBJECT_TYPE_POLYGON: Short = 9
        const val OBJECT_TYPE_RESERVED1: Short = 10
        const val OBJECT_TYPE_CHECKBOX: Short = 11
        const val OBJECT_TYPE_OPTION_BUTTON: Short = 12
        const val OBJECT_TYPE_EDIT_BOX: Short = 13
        const val OBJECT_TYPE_LABEL: Short = 14
        const val OBJECT_TYPE_DIALOG_BOX: Short = 15
        const val OBJECT_TYPE_SPINNER: Short = 16
        const val OBJECT_TYPE_SCROLL_BAR: Short = 17
        const val OBJECT_TYPE_LIST_BOX: Short = 18
        const val OBJECT_TYPE_GROUP_BOX: Short = 19
        const val OBJECT_TYPE_COMBO_BOX: Short = 20
        const val OBJECT_TYPE_RESERVED2: Short = 21
        const val OBJECT_TYPE_RESERVED3: Short = 22
        const val OBJECT_TYPE_RESERVED4: Short = 23
        const val OBJECT_TYPE_RESERVED5: Short = 24
        const val OBJECT_TYPE_COMMENT: Short = 25
        const val OBJECT_TYPE_RESERVED6: Short = 26
        const val OBJECT_TYPE_RESERVED7: Short = 27
        const val OBJECT_TYPE_RESERVED8: Short = 28
        const val OBJECT_TYPE_RESERVED9: Short = 29
        const val OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING: Short = 30
    }
}
