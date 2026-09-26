/*
 * 文件名称:          PathExtend.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:42:18
 */
package com.wxiwei.office.common.autoshape

import android.graphics.Path
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line

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
 * 日期:            2012-9-27
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
class ExtendPath {
    constructor() {
        path = Path()
        this.backgroundAndFill = null
    }

    constructor(extendPath: ExtendPath) {
        path = Path(extendPath.path)
        this.backgroundAndFill = extendPath.backgroundAndFill
        hasLine = extendPath.hasLine()
        line = extendPath.line
        this.isArrowPath = extendPath.isArrowPath
    }


    /*
     * 
     */
    fun hasLine(): Boolean {
        return hasLine
    }

    /**
     * 
     * @param border
     */
    fun setLine(hasLine: Boolean) {
        this.hasLine = hasLine
        if (hasLine && line == null) {
            line = Line()
        }
    }

    /**
     * 
     */
    fun setLine(line: Line?) {
        this.line = line
        if (line != null) {
            hasLine = true
        } else {
            hasLine = false
        }
    }

    fun setArrowFlag(isArrow: Boolean) {
        this.isArrowPath = isArrow
    }

    fun dispose() {
        path = null
        if (this.backgroundAndFill != null) {
            backgroundAndFill!!.dispose()
        }
    }


    var path: Path?
    var backgroundAndFill: BackgroundAndFill?
    private var hasLine = false

    /**
     * 
     * @return
     */
    var line: Line? = null
        private set
    var isArrowPath: Boolean = false
        private set
}
