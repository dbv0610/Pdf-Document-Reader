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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.ddf.EscherDgRecord
import com.wxiwei.office.fc.ddf.EscherDggRecord


/**
 * Provides utilities to manage drawing groups.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class DrawingManager2
    (@JvmField var dgg: EscherDggRecord) {
    var drawingGroups: MutableList<Any?> = ArrayList<Any?>()


    /**
     * Clears the cached list of drawing groups
     */
    fun clearDrawingGroups() {
        drawingGroups.clear()
    }

    fun createDgRecord(): EscherDgRecord {
        val dg = EscherDgRecord()
        dg.recordId = EscherDgRecord.RECORD_ID
        val dgId = findNewDrawingGroupId()
        dg.options = (dgId.toInt() shl 4).toShort()
        dg.numShapes = 0
        dg.lastMSOSPID = -1
        drawingGroups.add(dg)
        dgg.addCluster(dgId.toInt(), 0)
        dgg.drawingsSaved = dgg.drawingsSaved + 1
        return dg
    }

    /**
     * Allocates new shape id for the new drawing group id.
     * 
     * @return a new shape id.
     */
    fun allocateShapeId(drawingGroupId: Short): Int {
        val dg = getDrawingGroup(drawingGroupId.toInt())
        return allocateShapeId(drawingGroupId, dg)
    }

    /**
     * Allocates new shape id for the new drawing group id.
     * 
     * @return a new shape id.
     */
    fun allocateShapeId(drawingGroupId: Short, dg: EscherDgRecord): Int {
        dgg.numShapesSaved = dgg.numShapesSaved + 1

        // Add to existing cluster if space available
        for (i in dgg.fileIdClusters!!.indices) {
            val c = dgg.fileIdClusters!![i]
            if (c!!.drawingGroupId == drawingGroupId.toInt() && c.numShapeIdsUsed != 1024) {
                val result = c.numShapeIdsUsed + (1024 * (i + 1))
                c.incrementShapeId()
                dg.numShapes = dg.numShapes + 1
                dg.lastMSOSPID = result
                if (result >= dgg.shapeIdMax) dgg.shapeIdMax = result + 1
                return result
            }
        }

        // Create new cluster
        dgg.addCluster(drawingGroupId.toInt(), 0)
        dgg.fileIdClusters!![dgg.fileIdClusters!!.size - 1]!!.incrementShapeId()
        dg.numShapes = dg.numShapes + 1
        val result = (1024 * dgg.fileIdClusters!!.size)
        dg.lastMSOSPID = result
        if (result >= dgg.shapeIdMax) dgg.shapeIdMax = result + 1
        return result
    }

    /* ////////  Non-public methods ///////////// */
    /**
     * Finds the next available (1 based) drawing group id
    */
    fun findNewDrawingGroupId(): Short {
        var dgId: Short = 1
        while (drawingGroupExists(dgId)) dgId++
        return dgId
    }

    fun getDrawingGroup(drawingGroupId: Int): EscherDgRecord {
        return drawingGroups.get(drawingGroupId - 1) as EscherDgRecord
    }

    fun drawingGroupExists(dgId: Short): Boolean {
        for (i in dgg.fileIdClusters!!.indices) {
            if (dgg.fileIdClusters!![i]!!.drawingGroupId == dgId.toInt()) return true
        }
        return false
    }

    fun findFreeSPIDBlock(): Int {
        val max = dgg.shapeIdMax
        val next = ((max / 1024) + 1) * 1024
        return next
    }
}
