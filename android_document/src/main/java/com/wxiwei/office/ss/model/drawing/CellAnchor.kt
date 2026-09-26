/*
 * 文件名称:          CellAnchor.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:26:18
 */
package com.wxiwei.office.ss.model.drawing

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-3-1
 * 负责人:           jqin
 */
open class CellAnchor(type: Short) {
    private var type: Short = TWOCELLANCHOR

    @JvmField
    protected var start: AnchorPoint? = null
    private var end: AnchorPoint? = null

    //
    private var width = 0
    private var height = 0

    init {
        this.type = type
    }

    fun getType(): Short = type

    fun setStart(start: AnchorPoint?) {
        this.start = start
    }

    fun getStart(): AnchorPoint? = start

    fun setEnd(end: AnchorPoint?) {
        this.end = end
    }

    fun getEnd(): AnchorPoint? = end

    fun setWidth(width: Int) {
        this.width = width
    }

    fun getWidth(): Int = width

    fun setHeight(height: Int) {
        this.height = height
    }

    fun getHeight(): Int = height

    fun dispose() {
        if (start != null) {
            start!!.dispose()
            start = null
        }

        if (end != null) {
            end!!.dispose()
            end = null
        }
    }

    companion object {
        const val ONECELLANCHOR: Short = 0x0
        const val TWOCELLANCHOR: Short = 0x1
    }
}
