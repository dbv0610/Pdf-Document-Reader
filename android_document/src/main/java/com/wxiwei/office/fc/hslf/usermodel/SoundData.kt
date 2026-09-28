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
package com.wxiwei.office.fc.hslf.usermodel

import com.wxiwei.office.fc.hslf.record.Document
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordContainer
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.record.Sound

/**
 * A class that represents sound data embedded in a slide show.
 * 
 * @author Yegor Kozlov
 */
class SoundData
    (container: Sound) {
    /**
     * The record that contains the object data.
     */
    private val _container: Sound

    /**
     * Creates the object data wrapping the record that contains the sound data.
     * 
     * @param container the record that contains the sound data.
     */
    init {
        this._container = container
    }

    val soundName: String
        /**
         * Name of the sound (e.g. "crash")
         * 
         * @return name of the sound
         */
        get() = _container.soundName

    val soundType: String
        /**
         * Type of the sound (e.g. ".wav")
         * 
         * @return type of the sound
         */
        get() = _container.soundType

    val data: ByteArray?
        /**
         * Gets an input stream which returns the binary of the sound data.
         * 
         * @return the input stream which will contain the binary of the sound data.
         */
        get() = _container.soundData

    companion object {
        /**
         * Find all sound records in the supplied Document records
         * 
         * @param document the document to find in
         * @return the array with the sound data
         */
        fun find(document: Document): Array<SoundData?> {
            val lst = ArrayList<SoundData?>()
            val ch: Array<Record> = document.getChildRecords()
            for (i in ch.indices) {
                if (ch[i].getRecordType() == RecordTypes.SoundCollection.typeID.toLong()) {
                    val col = ch[i] as RecordContainer
                    val sr: Array<Record> = col.getChildRecords()
                    for (j in sr.indices) {
                        if (sr[j] is Sound) {
                            lst.add(SoundData(sr[j] as Sound))
                        }
                    }
                }
            }
            return lst.toTypedArray<SoundData?>()
        }
    }
}
