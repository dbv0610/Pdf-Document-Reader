package com.wxiwei.office.thirdpart.emf.font

/**
 * FIXME: These methods are not really tested yet.
 * 
 * @author Simon Fischer
 * @version $Id: TTFMemoryInput.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFMemoryInput(private val data: ByteArray) : TTFInput() {
    private var pointer = 0

    override fun seek(offset: Long) {
        pointer = offset.toInt()
    }

    override fun getPointer(): Long {
        return pointer.toLong()
    }

    // ---------- Simple Data Types --------------
    override fun readChar(): Byte {
        return data[pointer++]
    }

    override fun readRawByte(): Int {
        return data[pointer++].toInt() and 0x00ff
    }

    override fun readByte(): Int {
        return data[pointer++].toInt() and 0x00ff
    }

    override fun readShort(): Short {
        val result = data[pointer++].toInt()
        return ((result shl 8) or data[pointer++].toInt()).toShort()
    }

    override fun readUShort(): Int {
        return (data[pointer++].toInt() shl 8) or data[pointer++].toInt()
    }

    override fun readLong(): Int {
        val result = data[pointer++].toInt()
        return ((result shl 24) or (data[pointer++].toInt() shl 16
                ) or (data[pointer++].toInt() shl 8) or data[pointer++].toInt()).toShort().toInt()
    }

    override fun readULong(): Long {
        val temp = ByteArray(4)
        readFully(temp)
        var l: Long = 0
        for (i in temp.indices) {
            l = l or ((temp[3 - i].toInt() and 255) shl (8 * i)).toLong()
        }
        return l
    }

    // ---------------- Arrays -------------------
    override fun readFully(b: ByteArray) {
        for (i in b.indices) {
            b[i] = data[pointer++]
        }
    }
}
