package com.wxiwei.office.common.autoshape.pathbuilder

import android.graphics.Path
import android.graphics.PointF

class ArrowPathAndTail {
    fun reset() {
        this.arrowPath = null
        this.arrowTailCenter = null
    }

    fun setArrowTailCenter(x: Float, y: Float) {
        this.arrowTailCenter = PointF(x, y)
    }

    //arrow path
    var arrowPath: Path? = null

    //arrow tail center position
    var arrowTailCenter: PointF? = null
        private set
}
