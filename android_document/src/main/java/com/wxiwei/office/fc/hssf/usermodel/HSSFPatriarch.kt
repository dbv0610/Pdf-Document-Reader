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

import com.wxiwei.office.fc.ddf.EscherComplexProperty
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.hssf.record.EscherAggregate
import com.wxiwei.office.fc.ss.usermodel.Chart
import com.wxiwei.office.fc.ss.usermodel.Drawing
import com.wxiwei.office.fc.util.Internal
import com.wxiwei.office.fc.util.StringUtil.getFromUnicodeLE
import com.wxiwei.office.ss.model.XLSModel.ASheet

/**
 * The patriarch is the toplevel container for shapes in a sheet.  It does
 * little other than act as a container for other shapes and groups.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class HSSFPatriarch
/**
 * Creates the patriarch.
 * 
 * @param sheet the sheet this patriarch is stored in.
 */(// TODO make private
    var _sheet: ASheet?,
    /**
     * The EscherAggregate we have been bound to.
     * (This will handle writing us out into records,
     * and building up our shapes from the records)
     */
    private var _boundAggregate: EscherAggregate?
) : HSSFShapeContainer, Drawing {
    private var _shapes: MutableList<HSSFShape>? = ArrayList<HSSFShape>()

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

    /**
     * Creates a new group record stored under this patriarch.
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return  the newly created group.
     */
    fun createGroup(anchor: HSSFClientAnchor?): HSSFShapeGroup {
        val group = HSSFShapeGroup(null, null, anchor)
        group.setAnchor(anchor)
        addShape(group)
        return group
    }

    /**
     * Creates a simple shape.  This includes such shapes as lines, rectangles,
     * and ovals.
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return  the newly created shape.
     */
    fun createSimpleShape(anchor: HSSFClientAnchor?): HSSFSimpleShape {
        val shape = HSSFSimpleShape(null, null, anchor)
        shape.setAnchor(anchor)
        addShape(shape)
        return shape
    }

    /**
     * Creates a picture.
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return  the newly created shape.
     */
    fun createPicture(anchor: HSSFClientAnchor?, pictureIndex: Int): HSSFPicture {
        val shape = HSSFPicture(null, null, anchor)
        shape.pictureIndex = pictureIndex
        shape.setAnchor(anchor)
        addShape(shape)

        val bse = _sheet!!.getAWorkbook()!!.getInternalWorkbook()!!.getBSERecord(pictureIndex)
        bse!!.ref = bse.ref + 1
        return shape
    }

    override fun createPicture(anchor: IClientAnchor?, pictureIndex: Int): HSSFPicture {
        return createPicture(anchor as HSSFClientAnchor?, pictureIndex)
    }

    /**
     * Creates a polygon
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return  the newly created shape.
     */
    fun createPolygon(anchor: HSSFClientAnchor?): HSSFPolygon {
        val shape = HSSFPolygon(null, null, anchor)
        shape.setAnchor(anchor)
        addShape(shape)
        return shape
    }

    /**
     * Constructs a textbox under the patriarch.
     * 
     * @param anchor    the client anchor describes how this group is attached
     * to the sheet.
     * @return      the newly created textbox.
     */
    fun createTextbox(anchor: HSSFClientAnchor?): HSSFTextbox {
        val shape = HSSFTextbox(null, null, anchor)
        shape.setAnchor(anchor)
        addShape(shape)
        return shape
    }

    /**
     * Constructs a cell comment.
     * 
     * @param anchor    the client anchor describes how this comment is attached
     * to the sheet.
     * @return      the newly created comment.
     */
    fun createComment(anchor: HSSFAnchor?): HSSFComment {
        val shape = HSSFComment(null, null, anchor)
        shape.setAnchor(anchor)
        addShape(shape)
        return shape
    }

    /**
     * YK: used to create autofilters
     * 
     * @see HSSFSheet.setAutoFilter
     */
    fun createComboBox(anchor: HSSFAnchor?): HSSFSimpleShape {
        val shape = HSSFSimpleShape(null, null, anchor)
        shape.shapeType = HSSFSimpleShape.OBJECT_TYPE_COMBO_BOX.toInt()
        shape.setAnchor(anchor)
        addShape(shape)
        return shape
    }

    override fun createCellComment(anchor: IClientAnchor?): HSSFComment {
        return createComment(anchor as HSSFAnchor?)
    }

    /**
     * Returns a list of all shapes contained by the patriarch.
     */
    override fun getChildren(): MutableList<HSSFShape> {
        return _shapes!!
    }

    /**
     * add a shape to this drawing
     */
    @Internal
    fun addShape(shape: HSSFShape) {
        shape._patriarch = this
        _shapes!!.add(shape)
    }

    /**
     * Total count of all children and their children's children.
     */
    fun countOfAllChildren(): Int {
        var count = _shapes!!.size
        val iterator = _shapes!!.iterator()
        while (iterator.hasNext()) {
            val shape = iterator.next()
            count += shape.countOfAllChildren()
        }
        return count
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
     * Does this HSSFPatriarch contain a chart?
     * (Technically a reference to a chart, since they
     * get stored in a different block of records)
     * FIXME - detect chart in all cases (only seems
     * to work on some charts so far)
     */
    fun containsChart(): Boolean {
        // TODO - support charts properly in usermodel

        // We're looking for a EscherOptRecord

        val optRecord =
            _boundAggregate!!.findFirstWithId(EscherOptRecord.RECORD_ID) as EscherOptRecord?
        if (optRecord == null) {
            // No opt record, can't have chart
            return false
        }

        val it = optRecord.escherProperties.iterator()
        while (it.hasNext()) {
            val prop = it.next()
            if (prop.propertyNumber.toInt() == 896 && prop.isComplex) {
                val cp = prop as EscherComplexProperty
                val str = getFromUnicodeLE(cp.complexData)

                if (str == "Chart 1\u0000") {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Returns the aggregate escher record we're bound to
     */
    protected fun _getBoundAggregate(): EscherAggregate? {
        return _boundAggregate
    }

    /**
     * Creates a new client anchor and sets the top-left and bottom-right
     * coordinates of the anchor.
     * 
     * @param dx1  the x coordinate in EMU within the first cell.
     * @param dy1  the y coordinate in EMU within the first cell.
     * @param dx2  the x coordinate in EMU within the second cell.
     * @param dy2  the y coordinate in EMU within the second cell.
     * @param col1 the column (0 based) of the first cell.
     * @param row1 the row (0 based) of the first cell.
     * @param col2 the column (0 based) of the second cell.
     * @param row2 the row (0 based) of the second cell.
     * @return the newly created client anchor
     */
    override fun createAnchor(
        dx1: Int,
        dy1: Int,
        dx2: Int,
        dy2: Int,
        col1: Int,
        row1: Int,
        col2: Int,
        row2: Int
    ): HSSFClientAnchor {
        return HSSFClientAnchor(dx1, dy1, dx2, dy2, col1.toShort(), row1, col2.toShort(), row2)
    }

    override fun createChart(anchor: IClientAnchor?): Chart? {
        throw RuntimeException("NotImplemented")
    }

    fun dispose() {
        _shapes!!.clear()
        _shapes = null

        _boundAggregate = null
        _sheet = null
    }
}
