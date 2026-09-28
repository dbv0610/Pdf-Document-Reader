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

import com.wxiwei.office.fc.util.StringUtil.getFromUnicodeLE


/**
 * A Document Encryption Atom (type 12052). Holds information
 * on the Encryption of a Document
 * 
 * @author Nick Burch
 */
class DocumentEncryptionAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordAtom() {
    private var _header: ByteArray?
    private var data: ByteArray?

    /**
     * Return the name of the encryption provider used
     */
    var encryptionProviderName: String?
        private set

    /**
     * For the Document Encryption Atom
     */
    init {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab everything else, for now
        data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, data, 0, len - 8)

        // Grab the provider, from byte 8+44 onwards
        // It's a null terminated Little Endian String
        var endPos = -1
        var pos = start + 8 + 44
        while (pos < (start + len) && endPos < 0) {
            if (source[pos].toInt() == 0 && source[pos + 1].toInt() == 0) {
                // Hit the end
                endPos = pos
            }
            pos += 2
        }
        pos = start + 8 + 44
        val stringLen = (endPos - pos) / 2
        encryptionProviderName = getFromUnicodeLE(source, pos, stringLen)
    }

    val keyLength: Int
        /**
         * Return the length of the encryption key, in bits
         */
        get() = data!![28].toInt()

    /**
     * We are of type 12052
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        data = null
        encryptionProviderName = null
    }


    companion object {
        private const val _type = 12052L
    }
}
