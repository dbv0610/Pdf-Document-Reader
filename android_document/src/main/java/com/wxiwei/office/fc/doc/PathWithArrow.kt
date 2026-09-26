package com.wxiwei.office.fc.doc

import android.graphics.Path

class PathWithArrow(
    private val polygon: Array<Path>,
    private val startArrow: Path?,
    private val endArrow: Path?
) {

    fun getStartArrow(): Path? {
        return startArrow
    }

    fun getEndArrow(): Path? {
        return endArrow
    }

    fun getPolygonPath(): Array<Path> {
        return polygon
    }
}
