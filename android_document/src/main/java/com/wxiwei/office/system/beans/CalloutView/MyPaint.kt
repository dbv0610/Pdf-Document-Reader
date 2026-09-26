package com.wxiwei.office.system.beans.CalloutView

import android.graphics.Paint

class MyPaint : Paint() {
    init {
        isAntiAlias = true
        isDither = true
        style = Style.STROKE
        strokeJoin = Join.ROUND
        strokeCap = Cap.ROUND
    }
}
