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
 * This class holds the links to exernal objects referenced
 * from the document.
 * @author Nick Burch
 */
class ExObjList : RecordContainer {
    private var _header: ByteArray?

    /**
     * Returns the ExObjListAtom of this list
     */
    // Links to our more interesting children
    var exObjListAtom: ExObjListAtom? = null
        private set

    val exHyperlinks: Array<ExHyperlink?>
        /**
         * Returns all the ExHyperlinks
         */
        get() {
            val links = ArrayList<ExHyperlink?>()
            for (i in _children.indices) {
                if (_children[i] is ExHyperlink) {
                    links.add(_children[i] as ExHyperlink)
                }
            }

            return links.toTypedArray<ExHyperlink?>()
        }

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
        // First child should be the atom
        if (_children[0] is ExObjListAtom) {
            exObjListAtom = _children[0] as ExObjListAtom
        } else {
            throw IllegalStateException(
                "First child record wasn't a ExObjListAtom, was of type "
                        + _children[0].getRecordType()
            )
        }
    }

    /**
     * Create a new ExObjList, with blank fields
     */
    constructor() {
        _header = ByteArray(8)
        val newChildren = arrayOfNulls<Record>(1)

        // Setup our header block
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, _type.toShort())

        // Setup our child records
        newChildren[0] = ExObjListAtom()
        _children = newChildren.requireNoNulls()
        findInterestingChildren()
    }

    /**
     * We are of type 1033
     */
    public override fun getRecordType(): Long {
        return _type
    }


    /**
     * Lookup a hyperlink by its unique id
     * 
     * @param id hyperlink id
     * @return found `ExHyperlink` or `null`
     */
    fun get(id: Int): ExHyperlink? {
        for (i in _children.indices) {
            if (_children[i] is ExHyperlink) {
                val rec = _children[i] as ExHyperlink
                if (rec.exHyperlinkAtom!!.number == id) {
                    return rec
                }
            }
        }
        return null
    }

    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
        if (exObjListAtom != null) {
            exObjListAtom!!.dispose()
            exObjListAtom = null
        }
    }

    companion object {
        private const val _type: Long = 1033
    }
}
