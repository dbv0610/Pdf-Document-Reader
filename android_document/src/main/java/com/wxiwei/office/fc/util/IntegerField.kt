package com.wxiwei.office.fc.util

import com.wxiwei.office.fc.util.LittleEndian.BufferUnderrunException
import java.io.IOException
import java.io.InputStream

class IntegerField : FixedField {
    private var value: Int = 0
    private val offset: Int

    constructor(offset: Int) {
        if (offset < 0) {
            throw ArrayIndexOutOfBoundsException("negative offset")
        }
        this.offset = offset
    }

    constructor(offset: Int, value: Int) : this(offset) {
        set(value)
    }
    constructor(offset: Int, data: ByteArray) : this(offset) {
        readFromBytes(data)
    }
    constructor(offset: Int, value: Int, data: ByteArray) : this(offset) {
        set(value, data)
    }

    fun get(): Int = value
    fun set(value: Int) {
        this.value = value
    }
    fun set(value: Int, data: ByteArray) {
        this.value = value
        writeToBytes(data)
    }
    override fun readFromBytes(data: ByteArray) { value = LittleEndian.getInt(data, offset) }

    @Throws(IOException::class, BufferUnderrunException::class)
    override fun readFromStream(stream: InputStream) { value = LittleEndian.readInt(stream) }

    override fun writeToBytes(data: ByteArray) { LittleEndian.putInt(data, offset, value) }
    override fun toString(): String = value.toString()
}
