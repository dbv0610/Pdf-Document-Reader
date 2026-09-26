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
 * A container holding information about a sound. It contains:
 * 
 * 
 *  * 1. CString (4026), Instance 0: Name of sound (e.g. "crash")
 *  * 2. CString (4026), Instance 1: Type of sound (e.g. ".wav")
 *  * 3. CString (4026), Instance 2: Reference id of sound in sound collection
 *  * 4. CString (4026), Instance 3, optional: Built-in id of sound, for sounds we ship. This is the id that?s in the reg file.
 *  * 5. SoundData (2023), optional
 * 
 * 
 * @author Yegor Kozlov
 */
class Sound protected constructor(source: ByteArray, start: Int, len: Int) : RecordContainer() {
    private fun findInterestingChildren() {
        // First child should be the ExHyperlinkAtom
        if (_children[0] is CString) {
            _name = _children[0] as CString
        }

        /*else
        {
            logger.log(POILogger.ERROR, "First child record wasn't a CString, was of type "
                + _children[0].getRecordType());
        }*/

        // Second child should be the ExOleObjAtom
        if (_children[1] is CString) {
            _type = _children[1] as CString
        }

        /*else
        {
            logger.log(POILogger.ERROR, "Second child record wasn't a CString, was of type "
                + _children[1].getRecordType());
        }*/
        for (i in 2..<_children.size) {
            if (_children[i] is SoundData) {
                _data = _children[i] as SoundData
                break
            }
        }
    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     * 
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.Sound.typeID.toLong()
    }


    val soundName: String
        /**
         * Name of the sound (e.g. "crash")
         * 
         * @return name of the sound
         */
        get() = _name!!.text

    val soundType: String
        /**
         * Type of the sound (e.g. ".wav")
         * 
         * @return type of the sound
         */
        get() = _type!!.text

    val soundData: ByteArray?
        /**
         * The sound data
         * 
         * @return the sound data.
         */
        get() = if (_data == null) null else _data!!.data

    /**
     * 
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
        if (_name != null) {
            _name!!.dispose()
            _name = null
        }
        if (_type != null) {
            _type!!.dispose()
            _type = null
        }
        if (_data != null) {
            _data!!.dispose()
            _data = null
        }
    }

    /**
     * Record header data.
     */
    private var _header: ByteArray?

    // Links to our more interesting children
    private var _name: CString? = null
    private var _type: CString? = null
    private var _data: SoundData? = null

    /**
     * Set things up, and find our more interesting children
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
        findInterestingChildren()
    }
}
