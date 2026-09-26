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
 * Master slide
 * 
 * @author Yegor Kozlov
 */
class MainMaster protected constructor(source: ByteArray, start: Int, len: Int) : SheetContainer() {
    private var _header: ByteArray?

    /**
     * Returns the PPDrawing of this Slide, which has all the
     * interesting data in it
     */
    public override fun getPPDrawing(): PPDrawing? {
        return ppDrawing
    }

    /**
     * We are of type 1016
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
        _header = null
        if (slideAtom != null) {
            slideAtom!!.dispose()
            slideAtom = null
        }
        if (ppDrawing != null) {
            ppDrawing!!.dispose()
            ppDrawing = null
        }
        if (this.txMasterStyleAtoms != null) {
            for (tms in this.txMasterStyleAtoms!!) {
                tms.dispose()
            }
            this.txMasterStyleAtoms = null
        }
        if (this.colorSchemeAtoms != null) {
            for (csa in this.colorSchemeAtoms!!) {
                csa.dispose()
            }
            this.colorSchemeAtoms = null
        }
        if (_colorScheme != null) {
            _colorScheme!!.dispose()
            _colorScheme = null
        }
    }


    /**
     * Returns the SlideAtom of this Slide
     */
    // Links to our more interesting children
    var slideAtom: SlideAtom? = null
        private set
    private var ppDrawing: PPDrawing? = null
    var txMasterStyleAtoms: Array<TxMasterStyleAtom>?
        private set
    var colorSchemeAtoms: Array<ColorSchemeAtom>?
        private set
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

        val tx = ArrayList<TxMasterStyleAtom>()
        val clr = ArrayList<ColorSchemeAtom>()
        // Find the interesting ones in there
        for (i in _children.indices) {
            if (_children[i] is SlideAtom) {
                slideAtom = _children[i] as SlideAtom
            } else if (_children[i] is PPDrawing) {
                ppDrawing = _children[i] as PPDrawing
            } else if (_children[i] is TxMasterStyleAtom) {
                tx.add(_children[i] as TxMasterStyleAtom)
            } else if (_children[i] is ColorSchemeAtom) {
                clr.add(_children[i] as ColorSchemeAtom)
            }

            if (ppDrawing != null && _children[i] is ColorSchemeAtom) {
                _colorScheme = _children[i] as ColorSchemeAtom
            }
        }
        this.txMasterStyleAtoms = tx.toTypedArray()
        this.colorSchemeAtoms = clr.toTypedArray()
    }

    companion object {
        private const val _type: Long = 1016
    }
}
