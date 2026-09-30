/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           HSSFAutoShape.java
 *  
 * 编译器:             android2.2
 * 时间:               下午3:37:10
 */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * autoshape data
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
 * 日期:           2013-3-27
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
open class HSSFAutoShape(
    workbook: AWorkbook?, escherContainer: EscherContainerRecord?, parent: HSSFShape?,
    anchor: HSSFAnchor?, shapeType: Int
) : HSSFTextbox(escherContainer, parent, anchor) {
    fun setAdjustmentValue(escherContainer: EscherContainerRecord?) {
        this.adjustmentValue = ShapeKit.getAdjustmentValue(escherContainer)
    }

    var adjustmentValue: Array<Float?>? = null
        private set

    init {
        this.shapeType = shapeType
        processLineWidth()
        processLine(escherContainer, workbook)
        processSimpleBackground(escherContainer, workbook)
        processRotationAndFlip(escherContainer)


        //word art
        val unicodeText = ShapeKit.getUnicodeGeoText(escherContainer)
        if (unicodeText != null && unicodeText.length > 0) {
            setString(HSSFRichTextString(unicodeText))
            isWordArt = true

            isNoFill = true
            fontColor = fillColor
        }
    }
}
