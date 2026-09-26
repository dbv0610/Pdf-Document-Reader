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
import com.wxiwei.office.fc.util.LittleEndian.putInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Hashtable
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/**
 * Storage for embedded OLE objects.
 * 
 * @author Daniel Noll
 */
class ExOleObjStg : RecordAtom, PositionDependentRecord, PersistRecord {
    private var _persistId = 0 // Found from PersistPtrHolder

    /**
     * Record header.
     */
    private var _header: ByteArray? = null

    /**
     * Record data.
     */
    var rawData: ByteArray? = null
        private set

    /**
     * Constructs a new empty storage container.
     */
    constructor() {
        _header = ByteArray(8)
        this.rawData = ByteArray(0)

        LittleEndian.putShort(_header!!, 0, 0x10.toShort())
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, rawData!!.size)
    }

    /**
     * Constructs the link related atom record from its
     * source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray?, start: Int, len: Int)

    val dataLength: Int
        /**
         * Gets the uncompressed length of the data.
         * 
         * @return the uncompressed length of the data.
         */
        get() = LittleEndian.getInt(this.rawData!!, 0)

    val data: InputStream
        /**
         * Opens an input stream which will decompress the data on the fly.
         * 
         * @return the data input stream.
         */
        get() {
            val compressedStream: InputStream =
                ByteArrayInputStream(this.rawData, 4, rawData!!.size)
            return InflaterInputStream(compressedStream)
        }

    /**
     * Sets the embedded data.
     * 
     * @param data the embedded data.
     */
    @Throws(IOException::class)
    fun setData(data: ByteArray) {
        val out = ByteArrayOutputStream()
        //first four bytes is the length of the raw data
        val b = ByteArray(4)
        putInt(b, data.size)
        out.write(b)

        val def = DeflaterOutputStream(out)
        def.write(data, 0, data.size)
        def.finish()
        this.rawData = out.toByteArray()
        LittleEndian.putInt(_header!!, 4, rawData!!.size)
    }

    /**
     * Gets the record type.
     * 
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExOleObjStg.typeID.toLong()
    }

    /**
     * Fetch our sheet ID, as found from a PersistPtrHolder.
     * Should match the RefId of our matching SlidePersistAtom
     */
    override var persistId: Int
        get() = _persistId
        /**
         * Set our sheet ID, as found from a PersistPtrHolder
         */
        set(id) {
            _persistId = id
        }

    /** Our location on the disk, as of the last write out  */
    protected var myLastOnDiskOffset: Int = 0

    /** Fetch our location on the disk, as of the last write out  */
    override var lastOnDiskOffset: Int
        get() = myLastOnDiskOffset
        /**
         * Update the Record's idea of where on disk it lives, after a write out.
         * Use with care...
         */
        set(offset) {
            myLastOnDiskOffset = offset
        }

    override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
        return
    }

    /**
     * 
     */
    override fun dispose() {
        _header = null
        this.rawData = null
    }
}
