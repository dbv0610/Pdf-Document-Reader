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

/**
 * Master container for Notes. There is one of these for every page of
 * notes, and they have certain specific children
 * 
 * @author Nick Burch
 */
class Notes protected constructor(source: ByteArray, start: Int, len: Int) : SheetContainer() {
    private val _header: ByteArray?

    /**
     * Returns the PPDrawing of this Notes, which has all the
     * interesting data in it
     */
    public override fun getPPDrawing(): PPDrawing? {
        return ppDrawing
    }

    /**
     * We are of type 1008
     */
    public override fun getRecordType(): Long {
        return _type
    }

    public override fun getColorScheme(): ColorSchemeAtom? {
        return _colorScheme
    }

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
        if (notesAtom != null) {
            notesAtom!!.dispose()
            notesAtom = null
        }
        if (ppDrawing != null) {
            ppDrawing!!.dispose()
            ppDrawing = null
        }
        if (_colorScheme != null) {
            _colorScheme!!.dispose()
            _colorScheme = null
        }
    }


    /**
     * Returns the NotesAtom of this Notes
     */
    // Links to our more interesting children
    var notesAtom: NotesAtom? = null
        private set
    private var ppDrawing: PPDrawing? = null
    private var _colorScheme: ColorSchemeAtom? = null

    /**
     * Set things up, and find our more interesting children
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)

        // Find the interesting ones in there
        for (i in _children.indices) {
            if (_children[i] is NotesAtom) {
                notesAtom = _children[i] as NotesAtom
                //System.out.println("Found notes for sheet " + notesAtom.getSlideID());
            }
            if (_children[i] is PPDrawing) {
                ppDrawing = _children[i] as PPDrawing
            }
            if (ppDrawing != null && _children[i] is ColorSchemeAtom) {
                _colorScheme = _children[i] as ColorSchemeAtom
            }
        }
    }

    companion object {
        private const val _type = 1008L
    }
}
