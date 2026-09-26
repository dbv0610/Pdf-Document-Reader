package com.wxiwei.office.wp.control

import com.wxiwei.office.common.shape.AbstractShape

class WPShapeManage {
    private val shapes: MutableMap<Int, AbstractShape> = HashMap(20)

    fun addShape(shape: AbstractShape): Int {
        val size = shapes.size
        shapes[size] = shape
        return size
    }

    fun getShape(index: Int): AbstractShape? {
        if (index < 0 || index >= shapes.size) {
            return null
        }
        return shapes[index]
    }

    fun dispose() {
        for (shape in shapes.values) {
            shape.dispose()
        }
        shapes.clear()
    }
}
