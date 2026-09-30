/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           LineShape.java
 *  
 * 编译器:             android2.2
 * 时间:               下午3:37:58
 */
package com.wxiwei.office.common.shape

/**
 * lineShape
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
 * 日期:           2012-9-3
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
open class LineShape
/**
 * 
 */
    : AutoShape() {
    /**
     * 
     * 
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_LINE

    fun createStartArrow(type: Byte, width: Int, length: Int) {
        if (startArrow == null) {
            startArrow = Arrow(type, width, length)
        } else {
            startArrow!!.type = type
            startArrow!!.width = width
            startArrow!!.length = length
        }
    }

    fun createEndArrow(type: Byte, width: Int, length: Int) {
        if (endArrow == null) {
            endArrow = Arrow(type, width, length)
        } else {
            endArrow!!.type = type
            endArrow!!.width = width
            endArrow!!.length = length
        }
    }

    val startArrowhead: Boolean
        /**
         * 
         * @return
         */
        get() = startArrow != null

    var startArrowWidth: Int
        get() {
            if (startArrow != null) {
                return startArrow!!.width
            }
            return -1
        }
        set(startArrowWidth) {
            if (startArrow != null) {
                startArrow!!.width = startArrowWidth
            }
        }

    var startArrowLength: Int
        get() {
            if (startArrow != null) {
                return startArrow!!.length
            }
            return -1
        }
        set(startArrowLength) {
            if (startArrow != null) {
                startArrow!!.length = startArrowLength
            }
        }

    var startArrowType: Byte
        /**
         * 
         * @return
         */
        get() {
            if (startArrow != null) {
                return startArrow!!.type
            }

            return Arrow.Companion.Arrow_None
        }
        /**
         * 
         * @param type
         */
        set(type) {
            if (startArrow != null) {
                startArrow!!.type = type
            }
        }

    val endArrowhead: Boolean
        /**
         * 
         * @return
         */
        get() = endArrow != null

    var endArrowWidth: Int
        get() {
            if (endArrow != null) {
                return endArrow!!.width
            }
            return -1
        }
        set(endArrowWidth) {
            if (endArrow != null) {
                endArrow!!.width = endArrowWidth
            }
        }

    var endArrowLength: Int
        get() {
            if (endArrow != null) {
                return endArrow!!.length
            }
            return -1
        }
        set(endArrowLength) {
            if (endArrow != null) {
                endArrow!!.length = endArrowLength
            }
        }

    var endArrowType: Byte
        /**
         * 
         * @return
         */
        get() {
            if (endArrow != null) {
                return endArrow!!.type
            }
            return Arrow.Companion.Arrow_None
        }
        /**
         * 
         * @param type
         */
        set(type) {
            if (endArrow != null) {
                endArrow!!.type = type
            }
        }

    /**
     * 
     * (non-Javadoc)
     * @see AutoShape.dispose
     */
    override fun dispose() {
        startArrow = null
        endArrow = null
    }

    // start arror or not
    var startArrow: Arrow? = null
        private set

    // end arror or not
    var endArrow: Arrow? = null
        private set
}
