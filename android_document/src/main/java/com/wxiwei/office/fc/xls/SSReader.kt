package com.wxiwei.office.fc.xls

import com.wxiwei.office.system.AbstractReader

open class SSReader : AbstractReader() {
    fun abortCurrentReading() {
        abortReader = false
    }
}
