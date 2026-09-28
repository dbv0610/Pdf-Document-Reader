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

/**
 * Environment, which contains lots of settings for the document.
 * 
 * @author Nick Burch
 */
class Environment protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    /**
     * We are of type 1010
     */
    public override fun getRecordType(): Long {
        return _type
    }


    /**
     * 
     */
    override fun dispose() {
        _header = null
        if (fontCollection != null) {
            fontCollection!!.dispose()
            fontCollection = null
        }
        if (this.txMasterStyleAtom != null) {
            txMasterStyleAtom!!.dispose()
            this.txMasterStyleAtom = null
        }
    }

    private var _header: ByteArray?

    /**
     * Returns the FontCollection of this Environment
     */
    // Links to our more interesting children
    var fontCollection: FontCollection? = null
        private set

    //master style for text with type=TextHeaderAtom.OTHER_TYPE
    var txMasterStyleAtom: TxMasterStyleAtom? = null
        private set

    /**
     * Set things up, and find our more interesting children
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)

        // Find our FontCollection record
        for (i in _children.indices) {
            if (_children[i] is FontCollection) {
                fontCollection = _children[i] as FontCollection
            } else if (_children[i] is TxMasterStyleAtom) {
                this.txMasterStyleAtom = _children[i] as TxMasterStyleAtom
            }
        }

        checkNotNull(fontCollection) { "Environment didn't contain a FontCollection record!" }
    }

    companion object {
        private const val _type: Long = 1010
    }
}
