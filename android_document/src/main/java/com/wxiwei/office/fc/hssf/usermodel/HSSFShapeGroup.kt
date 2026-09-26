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

import com.wxiwei.office.fc.ddf.EscherContainerRecord

/**
 * A shape group may contain other shapes.  It was no actual form on the
 * sheet.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class HSSFShapeGroup(
    escherContainer: EscherContainerRecord?,
    parent: HSSFShape?,
    anchor: HSSFAnchor?
) : HSSFShape(escherContainer, parent, anchor), HSSFShapeContainer {
    /**
     * Create another group under this group.
     * @param anchor    the position of the new group.
     * @return  the group
     */
    fun createGroup(anchor: HSSFChildAnchor?): HSSFShapeGroup {
        val group = HSSFShapeGroup(null, this, anchor)
        group.setAnchor(anchor)
        shapes.add(group)
        return group
    }

    /**
     * Create a new simple shape under this group.
     * @param anchor    the position of the shape.
     * @return  the shape
     */
    fun createShape(anchor: HSSFChildAnchor?): HSSFSimpleShape {
        val shape = HSSFSimpleShape(null, this, anchor)
        shape.setAnchor(anchor)
        shapes.add(shape)
        return shape
    }

    /**
     * Create a new textbox under this group.
     * @param anchor    the position of the shape.
     * @return  the textbox
     */
    fun createTextbox(anchor: HSSFChildAnchor?): HSSFTextbox {
        val shape = HSSFTextbox(null, this, anchor)
        shape.setAnchor(anchor)
        shapes.add(shape)
        return shape
    }

    /**
     * Creates a polygon
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return  the newly created shape.
     */
    fun createPolygon(anchor: HSSFChildAnchor?): HSSFPolygon {
        val shape = HSSFPolygon(null, this, anchor)
        shape.setAnchor(anchor)
        shapes.add(shape)
        return shape
    }

    /**
     * Creates a picture.
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return  the newly created shape.
     */
    fun createPicture(anchor: HSSFChildAnchor?, pictureIndex: Int): HSSFPicture {
        val shape = HSSFPicture(null, this, anchor)
        shape.setAnchor(anchor)
        shape.pictureIndex = pictureIndex
        shapes.add(shape)
        return shape
    }

    /**
     * 
     * @param shape
     */
    fun addChildShape(shape: HSSFShape) {
        shapes.add(shape)
    }

    /**
     * Return all children contained by this shape.
     */
    override fun getChildren(): MutableList<HSSFShape> {
        return shapes
    }

    /**
     * Sets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     */
    fun setCoordinates(x1: Int, y1: Int, x2: Int, y2: Int) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
    }

    /**
     * Count of all children and their childrens children.
     */
    override fun countOfAllChildren(): Int {
        var count = shapes.size
        val iterator = shapes.iterator()
        while (iterator.hasNext()) {
            val shape = iterator.next()
            count += shape.countOfAllChildren()
        }
        return count
    }

    /**
     * The top left x coordinate of this group.
     */
    var x1: Int = 0
        private set

    /**
     * The top left y coordinate of this group.
     */
    var y1: Int = 0
        private set

    /**
     * The bottom right x coordinate of this group.
     */
    var x2: Int = 1023
        private set

    /**
     * The bottom right y coordinate of this group.
     */
    var y2: Int = 255
        private set
    private val shapes: MutableList<HSSFShape> = ArrayList<HSSFShape>()
}
