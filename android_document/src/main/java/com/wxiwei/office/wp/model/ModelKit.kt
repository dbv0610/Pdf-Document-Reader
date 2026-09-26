package com.wxiwei.office.wp.model

import com.wxiwei.office.constant.wp.WPModelConstant

class ModelKit private constructor() {
    fun getArea(offset: Long): Long = offset and WPModelConstant.AREA_MASK

    companion object {
        private val kit = ModelKit()

        @JvmStatic
        fun instance(): ModelKit = kit
    }
}
