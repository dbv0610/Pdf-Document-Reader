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

/**
 * A TextHeaderAtom  (type 3999). Holds information on what kind of
 * text is contained in the TextBytesAtom / TextCharsAtom that follows
 * straight after
 * 
 * @author Nick Burch
 */
class TextHeaderAtom : RecordAtom, ParentAwareRecord {
    private var _header: ByteArray?
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getParentRecordProperty")
    @set:JvmName("setParentRecordProperty")
    override var parentRecord: RecordContainer? = null

    /** The kind of text it is  */
    var textType: Int

    /* *************** record code follows ********************** */
    /**
     * For the TextHeader Atom
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Sanity Checking - we're always 12 bytes long
        var len = len
        if (len < 12) {
            len = 12
            if (source.size - start < 12) {
                throw RuntimeException(
                    "Not enough data to form a TextHeaderAtom (always 12 bytes long) - found "
                            + (source.size - start)
                )
            }
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the type
        textType = getInt(source, start + 8)
    }

    /**
     * Create a new TextHeader Atom, for an unknown type of text
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 0)
        LittleEndian.putUShort(_header!!, 2, _type.toInt())
        LittleEndian.putInt(_header!!, 4, 4)

        textType = OTHER_TYPE
    }

    /**
     * We are of type 3999
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        parentRecord = null
    }

    companion object {
        private const val _type = 3999L
        const val TITLE_TYPE: Int = 0
        const val BODY_TYPE: Int = 1
        const val NOTES_TYPE: Int = 2
        const val OTHER_TYPE: Int = 4
        const val CENTRE_BODY_TYPE: Int = 5
        const val CENTER_TITLE_TYPE: Int = 6
        const val HALF_BODY_TYPE: Int = 7
        const val QUARTER_BODY_TYPE: Int = 8
    }
}
