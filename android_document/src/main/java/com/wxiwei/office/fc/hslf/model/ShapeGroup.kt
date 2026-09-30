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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherChildAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherSpgrRecord
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putUShort
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Rectangle2D

/**
 * Represents a group of shapes.
 * 
 * @author Yegor Kozlov
 */
open class ShapeGroup
/**
 * Create a ShapeGroup object and initilize it from the supplied Record container.
 * 
 * @param escherRecord       `EscherSpContainer` container which holds information about this shape
 * @param parent    the parent of the shape
 */
    (escherRecord: EscherContainerRecord?, parent: Shape?) : Shape(escherRecord, parent) {
    /**
     * Create a new ShapeGroup. This constructor is used when a new shape is created.
     * 
     */
    constructor() : this(null, null) {
        spContainer = createSpContainer(false)
    }

    /**
     * @return id for the shape.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getShapeIdProperty")
    override var shapeId: Int
        get() {
            val iter = spContainer!!.childIterator

            // group shape ID
            var grpShapeID = 0
            if (iter.hasNext()) {
                val r = iter.next()
                if (r is EscherContainerRecord) {
                    // Create the Shape for it
                    val container = r
                    val spRecord = container.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
                    grpShapeID = spRecord!!.shapeId
                }
            }
            return grpShapeID
        }
        set(id) {
            super.shapeId = id
        }

    val shapes: Array<Shape?>
        /**
         * @return the shapes contained in this group container
         */
        get() {
            // Out escher container record should contain several
            //  SpContainers, the first of which is the group shape itself
            val iter =
                spContainer!!.childIterator


            // Don't include the first SpContainer, it is always NotPrimitive
            if (iter.hasNext()) {
                iter.next()
            }
            val shapeList: MutableList<Shape?> =
                ArrayList<Shape?>()
            while (iter.hasNext()) {
                val r = iter.next()
                if (r is EscherContainerRecord) {
                    // Create the Shape for it
                    val container = r
                    val shape =
                        ShapeFactory.createShape(container, this)
                    shape.sheet = sheet
                    shapeList.add(shape)
                }
            }

            // Put the shapes into an array, and return
            val shapes =
                shapeList.toTypedArray<Shape?>()
            return shapes
        }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in Master units (576 dpi).
     * 
     * @param anchor new anchor
     */
    fun setAnchor(anchor: Rectangle) {
        val spContainer = spContainer!!.getChild(0) as EscherContainerRecord?

        val clientAnchor = ShapeKit.getEscherChild(
            spContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
        ) as EscherClientAnchorRecord?
        //hack. internal variable EscherClientAnchorRecord.shortRecord can be
        //initialized only in fillFields(). We need to set shortRecord=false;
        val header = ByteArray(16)
        putUShort(header, 0, 0)
        putUShort(header, 2, 0)
        putInt(header, 4, 8)
        clientAnchor!!.fillFields(header, 0, null)

        clientAnchor.flag =
            (anchor.y * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt().toShort()
        clientAnchor.col1 =
            (anchor.x * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt().toShort()
        clientAnchor.dx1 =
            ((anchor.width + anchor.x) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                .toShort()
        clientAnchor.row1 =
            ((anchor.height + anchor.y) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                .toShort()

        val spgr = ShapeKit.getEscherChild(
            spContainer,
            EscherSpgrRecord.RECORD_ID.toInt()
        ) as EscherSpgrRecord?

        spgr!!.rectX1 = (anchor.x * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
        spgr.rectY1 = (anchor.y * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
        spgr.rectX2 =
            ((anchor.x + anchor.width) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
        spgr.rectY2 =
            ((anchor.y + anchor.height) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
    }

    var coordinates: Rectangle2D
        /**
         * Gets the coordinate space of this group.  All children are constrained
         * to these coordinates.
         * 
         * @return the coordinate space of this group
         */
        get() {
            val spContainer =
                spContainer!!.getChild(0) as EscherContainerRecord?
            val spgr = ShapeKit.getEscherChild(
                spContainer,
                EscherSpgrRecord.RECORD_ID.toInt()
            ) as EscherSpgrRecord?

            val anchor =
                Rectangle2D.Float()
            anchor.x = spgr!!.rectX1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
            anchor.y = spgr.rectY1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
            anchor.width =
                (spgr.rectX2 - spgr.rectX1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
            anchor.height =
                (spgr.rectY2 - spgr.rectY1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI

            return anchor
        }
        /**
         * Sets the coordinate space of this group.  All children are constrained
         * to these coordinates.
         * 
         * @param anchor the coordinate space of this group
         */
        set(anchor) {
            val spContainer =
                spContainer!!.getChild(0) as EscherContainerRecord?
            val spgr = ShapeKit.getEscherChild(
                spContainer,
                EscherSpgrRecord.RECORD_ID.toInt()
            ) as EscherSpgrRecord?

            val x1 =
                Math.round(anchor.getX() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI)
                    .toInt()
            val y1 =
                Math.round(anchor.getY() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI)
                    .toInt()
            val x2 =
                Math.round((anchor.getX() + anchor.getWidth()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI)
                    .toInt()
            val y2 =
                Math.round((anchor.getY() + anchor.getHeight()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI)
                    .toInt()

            spgr!!.rectX1 = x1
            spgr.rectY1 = y1
            spgr.rectX2 = x2
            spgr.rectY2 = y2
        }

    /**
     * 
     * @return
     */
    fun getClientAnchor2D(shape: Shape): Rectangle2D {
        var anchor = shape.anchor2D
        if (shape != null && shape.parent != null) {
            val clientAnchor =
                (shape.parent as ShapeGroup).getClientAnchor2D(shape.parent!!)
            val spgrAnchor =
                (shape.parent as ShapeGroup).coordinates

            val scalex = spgrAnchor.getWidth() / clientAnchor.getWidth()
            val scaley = spgrAnchor.getHeight() / clientAnchor.getHeight()

            val x = clientAnchor.getX() + (anchor.getX() - spgrAnchor.getX()) / scalex
            val y = clientAnchor.getY() + (anchor.getY() - spgrAnchor.getY()) / scaley
            val width = anchor.getWidth() / scalex
            val height = anchor.getHeight() / scaley

            anchor = Rectangle2D.Double(x, y, width, height)
        }
        return anchor
    }

    /**
     * Create a new ShapeGroup and create an instance of `EscherSpgrContainer` which represents a group of shapes
     */
    override fun createSpContainer(isChild: Boolean): EscherContainerRecord {
        val spgr = EscherContainerRecord()
        spgr.recordId = EscherContainerRecord.SPGR_CONTAINER
        spgr.options = 15.toShort()

        //The group itself is a shape, and always appears as the first EscherSpContainer in the group container.
        val spcont = EscherContainerRecord()
        spcont.recordId = EscherContainerRecord.SP_CONTAINER
        spcont.options = 15.toShort()

        val spg = EscherSpgrRecord()
        spg.options = 1.toShort()
        spcont.addChildRecord(spg)

        val sp = EscherSpRecord()
        val type = ((ShapeTypes.NotPrimitive shl 4) + 2).toShort()
        sp.options = type
        sp.flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_GROUP
        spcont.addChildRecord(sp)

        val anchor = EscherClientAnchorRecord()
        spcont.addChildRecord(anchor)

        spgr.addChildRecord(spcont)
        return spgr
    }

    /**
     * Add a shape to this group.
     * 
     * @param shape - the Shape to add
     */
    fun addShape(shape: Shape) {
        spContainer!!.addChildRecord(shape.spContainer)

        val sheet = sheet
        shape.sheet = sheet
        shape.shapeId = sheet!!.allocateShapeId()
        shape.afterInsert(sheet)
    }

    /**
     * Moves this `ShapeGroup` to the specified location.
     * 
     * 
     * @param x the x coordinate of the top left corner of the shape in new location
     * @param y the y coordinate of the top left corner of the shape in new location
     */
    fun moveTo(x: Int, y: Int) {
        val anchor = anchor
        val dx = x - anchor.x
        val dy = y - anchor.y
        anchor.translate(dx, dy)
        setAnchor(anchor)

        val shape = this.shapes
        for (i in shape.indices) {
            val chanchor = shape[i]!!.anchor
            chanchor.translate(dx, dy)
            shape[i]!!.setAnchor(chanchor)
        }
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape group.
     * All coordinates are expressed in points (72 dpi).
     * 
     * @return the anchor of this shape group
     */
    override val anchor2D: Rectangle2D
        get() {
            val spContainer = spContainer!!.getChild(0) as EscherContainerRecord?
            val clientAnchor = ShapeKit.getEscherChild(
                spContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
            ) as EscherClientAnchorRecord?
            var anchor = Rectangle2D.Float()
            if (clientAnchor == null) {
                /*logger
                    .log(POILogger.INFO,
                        "EscherClientAnchorRecord was not found for shape group. Searching for EscherChildAnchorRecord.");*/
                val rec = ShapeKit.getEscherChild(
                    spContainer,
                    EscherChildAnchorRecord.RECORD_ID.toInt()
                ) as EscherChildAnchorRecord?
                anchor = Rectangle2D.Float(
                    rec!!.dx1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                    rec.dy1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                    (rec.dx2 - rec.dx1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                    (rec.dy2 - rec.dy1).toFloat() * MainConstant.POINT_DPI
                            / ShapeKit.MASTER_DPI
                )
            } else {
                anchor.x = clientAnchor.col1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
                anchor.y = clientAnchor.flag.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
                anchor.width =
                    ((clientAnchor.dx1 - clientAnchor.col1).toFloat() * MainConstant.POINT_DPI
                            / ShapeKit.MASTER_DPI)
                anchor.height =
                    ((clientAnchor.row1 - clientAnchor.flag).toFloat() * MainConstant.POINT_DPI
                            / ShapeKit.MASTER_DPI)
            }

            return anchor
        }

    /**
     * Return type of the shape.
     * In most cases shape group type is [ShapeTypes.NotPrimitive]
     * 
     * @return type of the shape.
     */
    override var shapeType: Int
        get() {
            val groupInfoContainer = spContainer!!
                .getChild(0) as EscherContainerRecord?
            val spRecord = groupInfoContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            return spRecord!!.options.toInt() shr 4
        }
        set(type) {
            super.shapeType = type
        }

    /**
     * Returns `null` - shape groups can't have hyperlinks
     * 
     * @return `null`.
     */
    override val hyperlink: Hyperlink?
        get() = null

    /**
     * Whether the shape is horizontally flipped
     * 
     * @return whether the shape is horizontally flipped
     */
    override val flipHorizontal: Boolean
        get() = ShapeKit.getGroupFlipHorizontal(spContainer)

    /**
     * Whether the shape is vertically flipped
     * 
     * @return whether the shape is vertically flipped
     */
    override val flipVertical: Boolean
        get() = ShapeKit.getGroupFlipVertical(spContainer)

    /**
     * Rotation angle in degrees
     * 
     * @return rotation angle in degrees
     */
    override val rotation: Int
        get() = ShapeKit.getGroupRotation(spContainer)

    override fun dispose() {
        super.dispose()
    }
}
