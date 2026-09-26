package com.wxiwei.office.fc.hslf.util

import java.io.ByteArrayOutputStream

class MutableByteArrayOutputStream : ByteArrayOutputStream() {
    fun getBytesWritten(): Int {
        return -1
    }

    override fun write(b: ByteArray) {
    }

    override fun write(b: Int) {
    }

    fun overwrite(b: ByteArray, startPos: Int) {
    }
}
