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

import com.wxiwei.office.fc.ddf.DefaultEscherRecordFactory
import com.wxiwei.office.fc.ddf.EscherBSERecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherDggRecord
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Container records which always exists inside Document.
 * It always acts as a holder for escher DGG container
 * which may contain which Escher BStore container information
 * about pictures containes in the presentation (if any).
 * 
 * @author Yegor Kozlov
 */
class PPDrawingGroup protected constructor(source: ByteArray, start: Int, len: Int) : RecordAtom() {
    /**
     * We are type 1035
     */
    public override fun getRecordType(): Long {
        return RecordTypes.PPDrawingGroup.typeID.toLong()
    }

    /**
     * We're pretending to be an atom, so return null
     */
    override fun getChildRecords(): Array<Record>? {
        return null
    }

    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        val bout = ByteArrayOutputStream()
        val iter: MutableIterator<EscherRecord?> = dggContainer!!.childIterator
        while (iter.hasNext()) {
            val r = iter.next()!!
            if (r.recordId == EscherContainerRecord.BSTORE_CONTAINER) {
                val bstore = r as EscherContainerRecord

                val b2 = ByteArrayOutputStream()
                val it: MutableIterator<EscherRecord?> = bstore.childIterator
                while (it.hasNext()) {
                    val bse = it.next() as EscherBSERecord
                    val b = ByteArray(36 + 8)
                    bse.serialize(0, b)
                    b2.write(b)
                }
                val bstorehead = ByteArray(8)
                putShort(bstorehead, 0, bstore.options)
                putShort(bstorehead, 2, bstore.recordId)
                putInt(bstorehead, 4, b2.size())
                bout.write(bstorehead)
                bout.write(b2.toByteArray())
            } else {
                bout.write(r.serialize())
            }
        }
        val size = bout.size()

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header!!, 4, size + 8)

        // Write out our header
        out.write(_header)

        val dgghead = ByteArray(8)
        putShort(dgghead, 0, dggContainer.options)
        putShort(dgghead, 2, dggContainer.recordId)
        putInt(dgghead, 4, size)
        out.write(dgghead)

        // Finally, write out the children
        out.write(bout.toByteArray())
    }

    val escherDggRecord: EscherDggRecord?
        get() {
            if (dgg == null) {
                val it: MutableIterator<EscherRecord?> =
                    dggContainer!!.childIterator
                while (it.hasNext()) {
                    val r: EscherRecord? = it.next()
                    if (r is EscherDggRecord) {
                        dgg = r
                        break
                    }
                }
            }
            return dgg
        }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (dggContainer != null) {
            dggContainer.dispose()
        }
        if (dgg != null) {
            dgg!!.dispose()
            dgg = null
        }
    }

    private var _header: ByteArray?
    val dggContainer: EscherContainerRecord?

    //cached dgg
    private var dgg: EscherDggRecord? = null

    init {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the contents for now
        val contents = ByteArray(len)
        System.arraycopy(source, start, contents, 0, len)

        val erf = DefaultEscherRecordFactory()
        val child = erf.createRecord(contents, 0)
        child.fillFields(contents, 0, erf)
        dggContainer = child.getChild(0) as EscherContainerRecord?
    }
}
