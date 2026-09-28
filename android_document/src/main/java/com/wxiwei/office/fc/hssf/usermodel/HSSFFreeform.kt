/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           HSSFFreeform.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:18:18
 */
package com.wxiwei.office.fc.hssf.usermodel

import android.graphics.Path
import android.graphics.PointF
import com.wxiwei.office.common.autoshape.pathbuilder.ArrowPathAndTail
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2013-4-2
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class HSSFFreeform(
    workbook: AWorkbook?, escherContainer: EscherContainerRecord?,
    parent: HSSFShape?, anchor: HSSFAnchor?, shapeType: Int
) : HSSFAutoShape(workbook, escherContainer, parent, anchor, shapeType) {
    init {
        processLineWidth()
        processArrow(escherContainer)
    }

    /**
     * Gets the freeform path
     * 
     * @return the freeform path
     */
    fun getFreeformPath(
        rect: Rectangle?,
        startArrowTailCenter: PointF?,
        startArrowType: Byte,
        endArrowTailCenter: PointF?,
        endArrowType: Byte
    ): Array<Path?>? {
        return ShapeKit.getFreeformPath(
            escherContainer,
            rect,
            startArrowTailCenter,
            startArrowType,
            endArrowTailCenter,
            endArrowType
        )
    }

    fun getStartArrowPath(rect: Rectangle?): ArrowPathAndTail? {
        return ShapeKit.getStartArrowPathAndTail(escherContainer, rect)
    }

    fun getEndArrowPath(rect: Rectangle?): ArrowPathAndTail? {
        return ShapeKit.getEndArrowPathAndTail(escherContainer, rect)
    }
}
