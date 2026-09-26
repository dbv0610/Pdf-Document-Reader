package com.wxiwei.office.fc.ppt.reader

/** XML boolean values used by DrawingML; absence keeps the caller's default. */
internal fun pptXmlBoolean(value: String?, default: Boolean = false): Boolean =
    when (value?.trim()) {
        "true", "1" -> true
        "false", "0" -> false
        else -> default
    }
