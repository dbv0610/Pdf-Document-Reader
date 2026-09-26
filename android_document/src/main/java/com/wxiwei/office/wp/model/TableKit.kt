package com.wxiwei.office.wp.model

class TableKit private constructor() {
    companion object {
        private val kit = TableKit()

        @JvmStatic
        fun instance(): TableKit = kit
    }
}
