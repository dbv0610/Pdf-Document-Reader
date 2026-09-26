package com.wxiwei.office.fc.util

import com.wxiwei.office.fc.util.LittleEndian.BufferUnderrunException
import java.io.IOException
import java.io.InputStream
import java.nio.BufferUnderflowException

class ByteField : FixedField {
    private var value: Byte = 0
    private val offset: Int

    constructor(offset: Int) : this(offset, 0)

    constructor(offset: Int, value: Byte) {
        if (offset < 0) {
            throw ArrayIndexOutOfBoundsException("offset cannot be negative")
        }
        this.offset = offset
        set(value)
    }

    constructor(offset: Int, data: ByteArray) : this(offset) {
        readFromBytes(data)
    }

    constructor(offset: Int, value: Byte, data: ByteArray) : this(offset, value) {
        writeToBytes(data)
    }

    fun get(): Byte = value

    fun set(value: Byte) {
        this.value = value
    }

    fun set(value: Byte, data: ByteArray) {
        set(value)
        writeToBytes(data)
    }

    override fun readFromBytes(data: ByteArray) { value = data[offset] }

    @Throws(IOException::class, BufferUnderrunException::class)
    override fun readFromStream(stream: InputStream) {
        val read = stream.read()
        if (read < 0) throw BufferUnderflowException()
        value = read.toByte()
    }

    override fun writeToBytes(data: ByteArray) { data[offset] = value }

    override fun toString(): String = value.toString()
}
