/*
 * 文件名称:          SimpleArrow.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:07:55
 */
package com.wxiwei.office.common.autoshape.pathbuilder.arrow

import android.graphics.Rect
import com.wxiwei.office.common.shape.AutoShape

/**
 * TODO: LeftArrow, RightArrow, UpArrow, DownArrow, LeftRightArrow, UpDownArrow
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2012-9-17
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
open class ArrowPathBuilder {
    companion object {
        /**
         * get autoshape path
         * @param shape
         * @param rect
         * @return
         */
        fun getArrowPath(shape: AutoShape, rect: Rect): Any? {
            if (shape.isAutoShape07()) {
                return LaterArrowPathBuilder.getArrowPath(shape, rect)
            } else {
                return EarlyArrowPathBuilder.getArrowPath(shape, rect)
            }
        }
    }
}
