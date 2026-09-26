package com.wxiwei.office.fc.util

import com.wxiwei.office.fc.util.LittleEndian.BufferUnderrunException
import java.io.IOException
import java.io.InputStream

class LongField : FixedField {
    private var value: Long = 0
    private val offset: Int

    constructor(offset: Int) {
        if (offset < 0) {
            throw ArrayIndexOutOfBoundsException("Illegal offset: $offset")
        }
        this.offset = offset
    }

    constructor(offset: Int, value: Long) : this(offset) {
        set(value)
    }
    constructor(offset: Int, data: ByteArray) : this(offset) {
        readFromBytes(data)
    }
    constructor(offset: Int, value: Long, data: ByteArray) : this(offset) {
        set(value, data)
    }

    fun get(): Long = value
    fun set(value: Long) {
        this.value = value
    }
    fun set(value: Long, data: ByteArray) {
        this.value = value
        writeToBytes(data)
    }
    override fun readFromBytes(data: ByteArray) { value = LittleEndian.getLong(data, offset) }

    @Throws(IOException::class, BufferUnderrunException::class)
    override fun readFromStream(stream: InputStream) { value = LittleEndian.readLong(stream) }

    override fun writeToBytes(data: ByteArray) { LittleEndian.putLong(data, offset, value) }
    override fun toString(): String = value.toString()
}
