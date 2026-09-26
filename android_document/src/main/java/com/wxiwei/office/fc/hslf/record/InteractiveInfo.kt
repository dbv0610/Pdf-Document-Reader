/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian

/**
 * This class represents the metadata of a link in a slide/notes/etc.
 * It normally just holds a InteractiveInfoAtom, with the metadata
 * in it.
 * @author Nick Burch
 */
class InteractiveInfo : RecordContainer {
    private var _header: ByteArray?

    /**
     * Returns the InteractiveInfoAtom of this InteractiveInfo
     */
    // Links to our more interesting children
    var interactiveInfoAtom: InteractiveInfoAtom? = null
        private set

    /**
     * Set things up, and find our more interesting children
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
        findInterestingChildren()
    }

    /**
     * Go through our child records, picking out the ones that are
     * interesting, and saving those for use by the easy helper
     * methods.
     */
    private fun findInterestingChildren() {
        // First child should be the InteractiveInfoAtom
        if (_children[0] is InteractiveInfoAtom) {
            this.interactiveInfoAtom = _children[0] as InteractiveInfoAtom
        } else {
            throw IllegalStateException(
                "First child record wasn't a InteractiveInfoAtom, was of type "
                        + _children[0].getRecordType()
            )
        }
    }

    /**
     * Create a new InteractiveInfo, with blank fields
     */
    constructor() {
        _header = ByteArray(8)
        val newChildren = arrayOfNulls<Record>(1)

        // Setup our header block
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, _type.toShort())

        // Setup our child records
        this.interactiveInfoAtom = InteractiveInfoAtom()
        newChildren[0] = this.interactiveInfoAtom!!
        _children = newChildren.requireNoNulls()
    }

    /**
     * We are of type 4802
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (this.interactiveInfoAtom != null) {
            interactiveInfoAtom!!.dispose()
            this.interactiveInfoAtom = null
        }
    }

    companion object {
        private const val _type: Long = 4082
    }
}
