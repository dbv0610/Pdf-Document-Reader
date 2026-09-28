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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.EmbeddedObjectRefSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.SubRecord
import com.wxiwei.office.fc.poifs.filesystem.DirectoryEntry
import com.wxiwei.office.fc.util.HexDump.toHex
import java.io.IOException

/**
 * Represents binary object (i.e. OLE) data stored in the file.  Eg. A GIF, JPEG etc...
 * 
 * @author Daniel Noll
 */
class HSSFObjectData
/**
 * Constructs object data by wrapping a lower level object record.
 * 
 * @param record the low-level object record.
 * @param root the root of the filesystem, required for retrieving the object data.
 */(
    /**
     * Underlying object record ultimately containing a reference to the object.
     */
    private val _record: ObjRecord,
    /**
     * Reference to the filesystem root, required for retrieving the object data.
     */
    private val _root: DirectoryEntry
) {
    val oLE2ClassName: String
        /**
         * Returns the OLE2 Class Name of the object
         */
        get() = findObjectRecord().getOLEClassName() ?: ""

    @get:Throws(IOException::class)
    val directory: DirectoryEntry
        /**
         * Gets the object data. Only call for ones that have
         * data though. See [.hasDirectoryEntry]
         * 
         * @return the object data as an OLE2 directory.
         * @throws IOException if there was an error reading the data.
         */
        get() {
            val subRecord = findObjectRecord()

            val streamId: Int = subRecord.getStreamId() ?: 0
            val streamName =
                "MBD" + toHex(streamId)

            val entry = _root.getEntry(streamName)
            if (entry is DirectoryEntry) {
                return entry
            }
            throw IOException("Stream " + streamName + " was not an OLE2 directory")
        }

    val objectData: ByteArray
        /**
         * Returns the data portion, for an ObjectData
         * that doesn't have an associated POIFS Directory
         * Entry
         */
        get() = findObjectRecord().getObjectData()

    /**
     * Does this ObjectData have an associated POIFS
     * Directory Entry?
     * (Not all do, those that don't have a data portion)
     */
    fun hasDirectoryEntry(): Boolean {
        val subRecord = findObjectRecord()

        // 'stream id' field tells you
        val streamId: Int? = subRecord.getStreamId()
        return streamId != null && streamId != 0
    }

    /**
     * Finds the EmbeddedObjectRefSubRecord, or throws an
     * Exception if there wasn't one
     */
    protected fun findObjectRecord(): EmbeddedObjectRefSubRecord {
        val subRecordIter: MutableIterator<SubRecord> = _record.getSubRecords()!!.iterator()

        while (subRecordIter.hasNext()) {
            val subRecord: Any? = subRecordIter.next()
            if (subRecord is EmbeddedObjectRefSubRecord) {
                return subRecord
            }
        }

        throw IllegalStateException("Object data does not contain a reference to an embedded object OLE2 directory")
    }
}
