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
import com.wxiwei.office.fc.util.POILogger

/**
 * A container record that specifies information about a movie stored externally.
 * 
 * @author Yegor Kozlov
 */
open class ExMCIMovie : RecordContainer {
    // TODO - instantiable superclass
    private var _header: ByteArray?

    /**
     * Returns the ExVideoContainer that specifies information about the MCI movie
     */
    //An ExVideoContainer record that specifies information about the MCI movie
    var exVideo: ExVideoContainer? = null
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
     * Create a new ExMCIMovie, with blank fields
     */
    constructor() {
        _header = ByteArray(8)
        // Setup our header block
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())

        exVideo = ExVideoContainer()
        _children = arrayOf<Record>(exVideo!!)
    }

    /**
     * Go through our child records, picking out the ones that are
     * interesting, and saving those for use by the easy helper
     * methods.
     */
    private fun findInterestingChildren() {
        // First child should be the ExVideoContainer

        if (_children[0] is ExVideoContainer) {
            exVideo = _children[0] as ExVideoContainer
        } else {
            logger.log(
                POILogger.ERROR,
                "First child record wasn't a ExVideoContainer, was of type " + _children[0].getRecordType()
            )
        }
    }

    /**
     * We are of type 4103
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExMCIMovie.typeID.toLong()
    }

    /**
     * 
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
        if (exVideo != null) {
            exVideo!!.dispose()
            exVideo = null
        }
    }
}
