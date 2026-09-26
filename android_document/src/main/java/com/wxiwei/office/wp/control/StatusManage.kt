package com.wxiwei.office.wp.control

class StatusManage {
    private var selectText = false
    private var pressOffset = 0L
    private var isTouchDown = false

    fun isSelectTextStatus(): Boolean = selectText

    fun setSelectTextStatus(selectText: Boolean) {
        this.selectText = selectText
    }

    fun getPressOffset(): Long = pressOffset

    fun setPressOffset(pressOffset: Long) {
        this.pressOffset = pressOffset
    }

    fun isTouchDown(): Boolean = isTouchDown

    fun setTouchDown(isTouchDown: Boolean) {
        this.isTouchDown = isTouchDown
    }

    fun dispose() {
    }
}
