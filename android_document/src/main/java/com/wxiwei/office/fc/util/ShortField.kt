package com.wxiwei.office.fc.util

import com.wxiwei.office.fc.util.LittleEndian.BufferUnderrunException
import java.io.IOException
import java.io.InputStream

class ShortField : FixedField {
    private var value: Short = 0
    private val offset: Int

    constructor(offset: Int) {
        if (offset < 0) {
            throw ArrayIndexOutOfBoundsException("Illegal offset: $offset")
        }
        this.offset = offset
    }

    constructor(offset: Int, value: Short) : this(offset) {
        set(value)
    }
    constructor(offset: Int, data: ByteArray) : this(offset) {
        readFromBytes(data)
    }
    constructor(offset: Int, value: Short, data: ByteArray) : this(offset) {
        set(value, data)
    }

    fun get(): Short = value
    fun set(value: Short) {
        this.value = value
    }
    fun set(value: Short, data: ByteArray) {
        this.value = value
        writeToBytes(data)
    }
    override fun readFromBytes(data: ByteArray) { value = LittleEndian.getShort(data, offset) }

    @Throws(IOException::class, BufferUnderrunException::class)
    override fun readFromStream(stream: InputStream) { value = LittleEndian.readShort(stream) }

    override fun writeToBytes(data: ByteArray) { LittleEndian.putShort(data, offset, value) }
    override fun toString(): String = value.toString()
}
