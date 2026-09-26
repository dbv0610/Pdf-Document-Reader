// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.DataInput
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import kotlin.Boolean
import kotlin.Byte
import kotlin.ByteArray
import kotlin.Char
import kotlin.Deprecated
import kotlin.IndexOutOfBoundsException
import kotlin.Int
import kotlin.IntArray
import kotlin.Long
import kotlin.LongArray
import kotlin.Short
import kotlin.ShortArray
import kotlin.String
import kotlin.Throws
import kotlin.also
import kotlin.code

/**
 * Class to read bytes and pairs of bytes in both little and big endian order.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 */
open class ByteOrderInputStream
/**
 * Create a byte order (big-endian) input stream from given stream.
 * 
 * @param in
 * stream to read from
 */ @JvmOverloads constructor(`in`: InputStream, protected var little: Boolean = false) :
    BitInputStream(`in`), DataInput {
    /**
     * Create a byte order input stream from given stream.
     * 
     * @param in
     * stream to read from
     * @param littleEndian
     * true if stream should be little endian.
     */

    @Throws(IOException::class)
    override fun readFully(b: ByteArray?) {
        readFully(b, 0, b!!.size)
    }

    @Throws(IOException::class)
    override fun readFully(b: ByteArray?, off: Int, len: Int) {
        if (len < 0) {
            throw IndexOutOfBoundsException()
        }
        var n = 0
        while (n < len) {
            val count = read(b, off + n, len - n)
            if (count < 0) {
                throw EOFException()
            }
            n += count
        }
    }

    @Throws(IOException::class)
    override fun skipBytes(n: Int): Int {
        var total = 0
        var cur = 0

        while ((total < n) && ((skip((n - total).toLong()).toInt().also { cur = it }) > 0)) {
            total += cur
        }

        return total
    }

    @Throws(IOException::class)
    override fun readBoolean(): Boolean {
        val b = readUnsignedByte()
        return (b != 0)
    }

    @Throws(IOException::class)
    override fun readChar(): Char {
        val b1 = readUnsignedByte()
        val b2 = readUnsignedByte()
        return if (little) ((b1 shl 8) + b2).toChar() else ((b2 shl 8) + b1).toChar()
    }

    /**
     * Read a signed byte.
     */
    @Throws(IOException::class)
    override fun readByte(): Byte {
        byteAlign()
        val b = read()
        if (b < 0) {
            throw EOFException()
        }
        return b.toByte()
    }

    /**
     * Read n bytes and return in byte array.
     * 
     * @param n
     * number of bytes to read
     * @return byte array
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readByte(n: Int): ByteArray {
        byteAlign()
        val bytes = ByteArray(n)
        for (i in 0..<n) {
            val b = read()
            if (b < 0) {
                throw EOFException()
            }
            bytes[i] = b.toByte()
        }
        return bytes
    }

    /**
     * Read an unsigned byte.
     */
    @Throws(IOException::class)
    override fun readUnsignedByte(): Int {
        byteAlign()
        val ub = read()
        if (ub < 0) {
            throw EOFException()
        }
        return ub
    }

    /**
     * Read n unsigned bytes and return in int array.
     * 
     * @param n
     * number of bytes to read
     * @return int array
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readUnsignedByte(n: Int): IntArray {
        byteAlign()
        val bytes = IntArray(n)
        for (i in 0..<n) {
            val ub = read()
            if (ub < 0) {
                throw EOFException()
            }
            bytes[i] = ub
        }
        return bytes
    }

    /**
     * Read a signed short.
     */
    @Throws(IOException::class)
    override fun readShort(): Short {
        val i1 = readUnsignedByte()
        val i2 = readUnsignedByte()
        return if (little) ((i2 shl 8) + i1).toShort() else ((i1 shl 8) + i2).toShort()
    }

    /**
     * Read n shorts and return in short array
     * 
     * @param n
     * number of shorts to read
     * @return short array
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readShort(n: Int): ShortArray {
        val shorts = ShortArray(n)
        for (i in 0..<n) {
            shorts[i] = readShort()
        }
        return shorts
    }

    /**
     * Read an unsigned short.
     */
    @Throws(IOException::class)
    override fun readUnsignedShort(): Int {
        byteAlign()
        val i1 = readUnsignedByte()
        val i2 = readUnsignedByte()
        return if (little) (i2 shl 8) + i1 else (i1 shl 8) + i2
    }

    /**
     * Read n unsigned shorts and return in int array
     * 
     * @param n
     * number of shorts to read
     * @return int array
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readUnsignedShort(n: Int): IntArray {
        val shorts = IntArray(n)
        for (i in 0..<n) {
            shorts[i] = readUnsignedShort()
        }
        return shorts
    }

    /**
     * Read a signed integer.
     */
    @Throws(IOException::class)
    override fun readInt(): Int {
        val i1 = readUnsignedByte()
        val i2 = readUnsignedByte()
        val i3 = readUnsignedByte()
        val i4 = readUnsignedByte()
        return if (little) (i4 shl 24) + (i3 shl 16) + (i2 shl 8) + i1 else ((i1 shl 24)
                + (i2 shl 16) + (i3 shl 8) + i4)
    }

    /**
     * Read n ints and return in int array.
     * 
     * @param n
     * number of ints to read
     * @return int array
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readInt(n: Int): IntArray {
        val ints = IntArray(n)
        for (i in 0..<n) {
            ints[i] = readInt()
        }
        return ints
    }

    /**
     * Read an unsigned integer.
     * 
     * @return long
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readUnsignedInt(): Long {
        val i1 = readUnsignedByte().toLong()
        val i2 = readUnsignedByte().toLong()
        val i3 = readUnsignedByte().toLong()
        val i4 = readUnsignedByte().toLong()
        return if (little) (i4 shl 24) + (i3 shl 16) + (i2 shl 8) + i1 else ((i1 shl 24)
                + (i2 shl 16) + (i3 shl 8) + i4)
    }

    /**
     * Read n unsigned ints and return in long array.
     * 
     * @param n
     * number of ints to read
     * @return long array
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readUnsignedInt(n: Int): LongArray {
        val ints = LongArray(n)
        for (i in 0..<n) {
            ints[i] = readUnsignedInt()
        }
        return ints
    }

    @Throws(IOException::class)
    override fun readLong(): Long {
        val i1 = readInt().toLong()
        val i2 = readInt().toLong()
        return if (little) (i2 shl 32) + (i1 and 0xFFFFFFFFL) else ((i1 shl 32)
                + (i2 and 0xFFFFFFFFL))
    }

    @Throws(IOException::class)
    override fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    @Throws(IOException::class)
    override fun readDouble(): Double {
        return Double.fromBits(readLong())
    }

    @Deprecated("")
    @Throws(IOException::class)
    override fun readLine(): String? {
        throw IOException(
            "ByteOrderInputStream.readLine() is deprecated and not implemented."
        )
    }

    /**
     * Read a string (UTF).
     * 
     * @return string
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readString(): String? {
        return readUTF()
    }

    @Throws(IOException::class)
    override fun readUTF(): String? {
        return DataInputStream.readUTF(this)
    }

    /**
     * Read an ascii-z (0 terminated c-string).
     * 
     * @return string
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readAsciiZString(): String {
        val buffer = StringBuffer()
        var c = readUnsignedByte().toChar()
        while (c.code != 0) {
            buffer.append(c)
            c = readUnsignedByte().toChar()
        }
        return buffer.toString()
    }
}
