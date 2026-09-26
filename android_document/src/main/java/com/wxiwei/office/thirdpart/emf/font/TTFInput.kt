package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException
import java.util.LinkedList
import java.util.Stack

/**
 * Data input for true type files. All methods are named as the data formats in
 * the true type specification.
 * 
 * @author Simon Fischer
 * @version $Id: TTFInput.java 8584 2006-08-10 23:06:37Z duns $
 */
abstract class TTFInput {
    private val filePosStack: Stack<Long> = Stack<Long>()

    private var tempFlags = 0

    // --------------- IO ---------------
    @Throws(IOException::class)
    abstract fun seek(offset: Long)

    @Throws(IOException::class)
    abstract fun getPointer(): Long

    @Throws(IOException::class)
    fun pushPos() {
        filePosStack.push(getPointer())
    }

    @Throws(IOException::class)
    fun popPos() {
        seek(filePosStack.pop())
    }

    // ---------- Simple Data Types --------------
    @Throws(IOException::class)
    abstract fun readRawByte(): Int

    @Throws(IOException::class)
    abstract fun readByte(): Int

    @Throws(IOException::class)
    abstract fun readShort(): Short

    @Throws(IOException::class)
    abstract fun readUShort(): Int

    @Throws(IOException::class)
    abstract fun readULong(): Long

    @Throws(IOException::class)
    abstract fun readLong(): Int

    @Throws(IOException::class)
    abstract fun readChar(): Byte

    @Throws(IOException::class)
    fun readFWord(): Short {
        return readShort()
    }

    @Throws(IOException::class)
    fun readUFWord(): Int {
        return readUShort()
    }

    @Throws(IOException::class)
    fun readFixed(): Double {
        val major = readShort().toInt()
        val minor = readShort().toInt()
        return major.toDouble() + minor.toDouble() / 16384.0
    }

    @Throws(IOException::class)
    fun readF2Dot14(): Double {
        val major = readByte()
        val minor = readByte()
        val fraction = minor + ((major and 0x3f) shl 8)
        var mantissa = major shr 6
        if (mantissa >= 2) mantissa -= 4
        return mantissa.toDouble() + fraction.toDouble() / 16384.0
    }

    // ------------------------------------------------------------
    @Throws(IOException::class)
    fun checkShortZero() {
        if (readShort().toInt() != 0) {
            System.err.println("Reserved bit should be 0.")
        }
    }

    // ---------------- Flags --------------------
    /**
     * Reads unsigned short flags into a temporary variable which can be queried
     * using the flagBit method.
     */
    @Throws(IOException::class)
    fun readUShortFlags() {
        tempFlags = readUShort()
    }

    /**
     * Reads byte flags into a temporary variable which can be queried using the
     * flagBit method.
     */
    @Throws(IOException::class)
    fun readByteFlags() {
        tempFlags = readByte()
    }

    fun flagBit(bit: Int): Boolean {
        return flagBit(tempFlags, bit)
    }

    // ---------------- Arrays -------------------
    @Throws(IOException::class)
    abstract fun readFully(b: ByteArray)

    @Throws(IOException::class)
    fun readFFFFTerminatedUShortArray(): IntArray {
        val values: MutableList<Int> = LinkedList<Int>()
        var ushort = -1
        do {
            ushort = readUShort()
            values.add(ushort)
        } while (ushort != 0xFFFF)
        val shorts = IntArray(values.size)
        val i: MutableIterator<*> = values.iterator()
        var j = 0
        while (i.hasNext()) {
            shorts[j++] = (i.next() as Int)
        }
        return shorts
    }

    @Throws(IOException::class)
    fun readUShortArray(n: Int): IntArray {
        val temp = IntArray(n)
        for (i in temp.indices) temp[i] = readUShort()
        return temp
    }

    @Throws(IOException::class)
    fun readShortArray(n: Int): ShortArray {
        val temp = ShortArray(n)
        for (i in temp.indices) temp[i] = readShort()
        return temp
    }

    companion object {
        @Throws(IOException::class)
        fun checkZeroBit(b: Int, bit: Int, name: String?): Boolean {
            if (flagBit(b, bit)) {
                System.err.println(
                    ("Reserved bit " + bit + " in " + name
                            + " not 0.")
                )
                return false
            } else {
                return true
            }
        }

        fun flagBit(b: Int, bit: Int): Boolean {
            return (b and (1 shl bit)) > 0
        }
    }
}
