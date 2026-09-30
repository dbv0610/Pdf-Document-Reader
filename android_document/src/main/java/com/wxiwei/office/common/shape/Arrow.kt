/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          Arrow.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:44:59
 */
package com.wxiwei.office.common.shape

/**
 * TODO: 文件注释
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
 * 日期:            2013-5-22
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
class Arrow
    (@JvmField var type: Byte, width: Int, length: Int) {
    @JvmField
    var width: Int = 1
    @JvmField
    var length: Int = 1

    init {
        this.width = width
        this.length = length
    }

    companion object {
        const val Arrow_None: Byte = 0 //No arrow
        const val Arrow_Triangle: Byte = 1
        const val Arrow_Arrow: Byte = 5 //open arrow
        const val Arrow_Diamond: Byte = 3 //diamond arrow
        const val Arrow_Stealth: Byte = 2 //stealth arrow
        const val Arrow_Oval: Byte = 4 //oval arrow

        fun getArrowSize(size: String?): Int {
            if (size == null || size == "med") {
                return 1
            }
            if (size == "sm") {
                return 0
            } else if (size == "lg") {
                return 2
            } else {
                return 1
            }
        }

        /**
         * parse arrow type
         * @param type
         * @return
         */
        fun getArrowType(type: String?): Byte {
            if (type != null && type.length > 0) {
                if ("triangle".equals(type, ignoreCase = true)) {
                    return Arrow_Triangle
                } else if ("arrow".equals(type, ignoreCase = true)) {
                    return Arrow_Arrow
                } else if ("diamond".equals(type, ignoreCase = true)) {
                    return Arrow_Diamond
                } else if ("stealth".equals(type, ignoreCase = true)) {
                    return Arrow_Stealth
                } else if ("oval".equals(type, ignoreCase = true)) {
                    return Arrow_Oval
                }
            }

            return Arrow_None
        }
    }
}
