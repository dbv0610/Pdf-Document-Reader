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

import com.wxiwei.office.fc.util.POILogger

/**
 * `FontCollection` ia a container that holds information
 * about all the fonts in the presentation.
 * 
 * @author Yegor Kozlov
 */
class FontCollection protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordContainer() {
    /**
     * Return the type, which is 2005
     */
    public override fun getRecordType(): Long {
        return RecordTypes.FontCollection.typeID.toLong()
    }

    /**
     * Add font with the specified name to the font collection.
     * If the font is already present return its index.
     * @param name of the font
     * @return zero based index of the font in the collection
     */
    fun addFont(name: String): Int {
        val idx = getFontIndex(name)
        if (idx != -1) return idx

        return addFont(name, 0, 0, 4, 34)
    }

    fun addFont(name: String, charset: Int, flags: Int, type: Int, pitch: Int): Int {
        val fnt = FontEntityAtom()
        fnt.fontIndex = fonts!!.size shl 4
        fnt.fontName = name
        fnt.charSet = charset
        fnt.fontFlags = flags
        fnt.fontType = type
        fnt.pitchAndFamily = pitch
        fonts!!.add(name)

        // Append new child to the end
        appendChildRecord(fnt)

        return fonts!!.size - 1 //the added font is the last in the list
    }

    /**
     * @return zero based index of the font in the collection or -1 if not found
     */
    fun getFontIndex(name: String?): Int {
        for (i in fonts!!.indices) {
            if (fonts!!.get(i) == name) {
                //if the font is already present return its index
                return i
            }
        }
        return -1
    }

    val numberOfFonts: Int
        get() = fonts!!.size

    /**
     * Get the name of the font at the given ID, or null if there is
     * no font at that ID.
     * @param id
     */
    fun getFontWithId(id: Int): String? {
        if (id >= fonts!!.size) {
            // No font with that id
            return null
        }
        return fonts!!.get(id)
    }

    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
        if (fonts != null) {
            fonts!!.clear()
            fonts = null
        }
    }

    private var fonts: MutableList<String?>?
    private var _header: ByteArray?

    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        _children = findChildRecords(source, start + 8, len - 8)

        // Save font names into <code>List</code>
        fonts = ArrayList<String?>()
        for (i in _children.indices) {
            if (_children[i] is FontEntityAtom) {
                val atom = _children[i] as FontEntityAtom
                fonts!!.add(atom.fontName)
            } else {
                logger.log(
                    POILogger.WARN,
                    "Warning: FontCollection child wasn't a FontEntityAtom, was " + _children[i]
                )
            }
        }
    }
}
