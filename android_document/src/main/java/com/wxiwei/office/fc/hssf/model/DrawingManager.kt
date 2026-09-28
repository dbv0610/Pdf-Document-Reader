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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.ddf.EscherDgRecord
import com.wxiwei.office.fc.ddf.EscherDggRecord


/**
 * Provides utilities to manage drawing groups.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class DrawingManager
    (var dgg: EscherDggRecord) {
    var dgMap: MutableMap<Any?, Any?> =
        HashMap<Any?, Any?>() // key = Short(drawingId), value=EscherDgRecord

    fun createDgRecord(): EscherDgRecord {
        val dg = EscherDgRecord()
        dg.recordId = EscherDgRecord.RECORD_ID
        val dgId = findNewDrawingGroupId()
        dg.options = (dgId.toInt() shl 4).toShort()
        dg.numShapes = 0
        dg.lastMSOSPID = -1
        dgg.addCluster(dgId.toInt(), 0)
        dgg.drawingsSaved = dgg.drawingsSaved + 1
        dgMap.put(dgId, dg)
        return dg
    }

    /**
     * Allocates new shape id for the new drawing group id.
     * 
     * @return a new shape id.
     */
    fun allocateShapeId(drawingGroupId: Short): Int {
        // Get the last shape id for this drawing group.
        val dg = dgMap.get(drawingGroupId) as EscherDgRecord?
        val lastShapeId = dg!!.lastMSOSPID


        // Have we run out of shapes for this cluster?
        var newShapeId = 0
        if (lastShapeId % 1024 == 1023) {
            // Yes:
            // Find the starting shape id of the next free cluster
            newShapeId = findFreeSPIDBlock()
            // Create a new cluster in the dgg record.
            dgg.addCluster(drawingGroupId.toInt(), 1)
        } else {
            // No:
            // Find the cluster for this drawing group with free space.
            for (i in dgg.fileIdClusters!!.indices) {
                val c = dgg.fileIdClusters!![i]
                if (c!!.drawingGroupId == drawingGroupId.toInt()) {
                    if (c.numShapeIdsUsed != 1024) {
                        // Increment the number of shapes used for this cluster.
                        c.incrementShapeId()
                    }
                }
                // If the last shape id = -1 then we know to find a free block;
                if (dg.lastMSOSPID == -1) {
                    newShapeId = findFreeSPIDBlock()
                } else {
                    // The new shape id to be the last shapeid of this cluster + 1
                    newShapeId = dg.lastMSOSPID + 1
                }
            }
        }
        // Increment the total number of shapes used in the dgg.
        dgg.numShapesSaved = dgg.numShapesSaved + 1
        // Is the new shape id >= max shape id for dgg?
        if (newShapeId >= dgg.shapeIdMax) {
            // Yes:
            // Set the max shape id = new shape id + 1
            dgg.shapeIdMax = newShapeId + 1
        }
        // Set last shape id for this drawing group.
        dg.lastMSOSPID = newShapeId
        // Increased the number of shapes used for this drawing group.
        dg.incrementShapeCount()


        return newShapeId
    }

    /**/////////  Non-public methods ///////////// */
    fun findNewDrawingGroupId(): Short {
        var dgId: Short = 1
        while (drawingGroupExists(dgId)) dgId++
        return dgId
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
