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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import com.wxiwei.office.fc.util.RecordFormatException
import java.util.Arrays
import java.util.Collections
import kotlin.math.max
import kotlin.math.min


/**
 * This record defines the drawing groups used for a particular sheet.
 */
class EscherDggRecord : EscherRecord() {
    /**
     * The maximum is actually the next available. shape id.
     */
    var shapeIdMax: Int = 0

    //    private int field_2_numIdClusters;      // for some reason the number of clusters is actually the real number + 1
    var numShapesSaved: Int = 0
    var drawingsSaved: Int = 0
    var fileIdClusters: Array<FileIdCluster?>? = null

    /**
     * @return The maximum drawing group ID
     */
    var maxDrawingGroupId: Int = 0

    class FileIdCluster
        (val drawingGroupId: Int, var numShapeIdsUsed: Int) {
        fun incrementShapeId() {
            this.numShapeIdsUsed++
        }
    }

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0
        this.shapeIdMax = getInt(data, pos + size)
        size += 4
        getInt(data, pos + size)
        size += 4 // field_2_numIdClusters
        this.numShapesSaved = getInt(data, pos + size)
        size += 4
        this.drawingsSaved = getInt(data, pos + size)
        size += 4
        this.fileIdClusters =
            arrayOfNulls<FileIdCluster>((bytesRemaining - size) / 8) // Can't rely on field_2_numIdClusters
        for (i in fileIdClusters!!.indices) {
            this.fileIdClusters!![i] =
                FileIdCluster(getInt(data, pos + size), getInt(data, pos + size + 4))
            this.maxDrawingGroupId = max(
                this.maxDrawingGroupId,
                this.fileIdClusters!![i]!!.drawingGroupId
            )
            size += 8
        }
        bytesRemaining -= size
        if (bytesRemaining != 0) throw RecordFormatException("Expecting no remaining data but got " + bytesRemaining + " byte(s).")
        return 8 + size + bytesRemaining
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        var pos = offset
        putShort(data, pos, options)
        pos += 2
        putShort(data, pos, recordId)
        pos += 2
        val remainingBytes = recordSize - 8
        putInt(data, pos, remainingBytes)
        pos += 4

        putInt(data, pos, this.shapeIdMax)
        pos += 4
        putInt(data, pos, this.numIdClusters)
        pos += 4
        putInt(data, pos, this.numShapesSaved)
        pos += 4
        putInt(data, pos, this.drawingsSaved)
        pos += 4
        for (i in fileIdClusters!!.indices) {
            putInt(data, pos, this.fileIdClusters!![i]!!.drawingGroupId)
            pos += 4
            putInt(data, pos, this.fileIdClusters!![i]!!.numShapeIdsUsed)
            pos += 4
        }

        listener.afterRecordSerialize(pos, recordId, recordSize, this)
        return recordSize
    }

    override val recordSize: Int
        get() {
        return 8 + 16 + (8 * fileIdClusters!!.size)
    }

    override var recordId: Short
        set(value) {
            super.recordId = value
        }
        get() {
        return RECORD_ID
    }

    override val recordName: String
        get() {
        return "Dgg"
    }

    override fun toString(): String {
        val field_5_string = StringBuffer()
        val clustersList = this.fileIdClusters
        if (clustersList != null) for (i in clustersList.indices) {
            field_5_string.append("  DrawingGroupId").append(i + 1).append(": ")
            field_5_string.append(clustersList[i]!!.drawingGroupId)
            field_5_string.append('\n')
            field_5_string.append("  NumShapeIdsUsed").append(i + 1).append(": ")
            field_5_string.append(clustersList[i]!!.numShapeIdsUsed)
            field_5_string.append('\n')
        }
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(RECORD_ID) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  ShapeIdMax: " + this.shapeIdMax + '\n' +
                "  NumIdClusters: " + this.numIdClusters + '\n' +
                "  NumShapesSaved: " + this.numShapesSaved + '\n' +
                "  DrawingsSaved: " + this.drawingsSaved + '\n' +
                "" + field_5_string.toString()
    }

    val numIdClusters: Int
        /**
         * Number of id clusters + 1
         */
        get() = (if (this.fileIdClusters == null) 0 else (fileIdClusters!!.size + 1))

    /**
     * Add a new cluster
     * 
     * @param dgId  id of the drawing group (stored in the record options)
     * @param numShapedUsed initial value of the numShapedUsed field
     * @param sort if true then sort clusters by drawing group id.(
     * In Excel the clusters are sorted but in PPT they are not)
     */
    @JvmOverloads
    fun addCluster(dgId: Int, numShapedUsed: Int, sort: Boolean = true) {
        val currentClusters = this.fileIdClusters ?: emptyArray()
        val clusters: MutableList<FileIdCluster?> = ArrayList(currentClusters.toList())
        clusters.add(FileIdCluster(dgId, numShapedUsed))
        if (sort) Collections.sort<FileIdCluster?>(clusters, MY_COMP)
        this.maxDrawingGroupId = min(this.maxDrawingGroupId, dgId)
        this.fileIdClusters = clusters.toTypedArray<FileIdCluster?>()
    }

    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF006.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtDgg"

        private val MY_COMP: Comparator<FileIdCluster?> = Comparator { f1, f2 ->
            if (f1 == null || f2 == null) 0
            else if (f1.drawingGroupId == f2.drawingGroupId) 0
            else if (f1.drawingGroupId < f2.drawingGroupId) -1
            else 1
        }
    }
}
