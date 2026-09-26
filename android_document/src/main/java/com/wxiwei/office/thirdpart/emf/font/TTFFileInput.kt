package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException
import java.io.RandomAccessFile

/**
 * Concrete implementation of the TrueType Input for one Table, read from a TTF
 * File.
 * 
 * Reads one table from the file.
 * 
 * @author Simon Fischer
 * @version $Id: TTFFileInput.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFFileInput(
    private val ttf: RandomAccessFile,
    private val offset: Long,
    private val length: Long,
    private val checksum: Long
) : TTFInput() {
    // --------------- IO ---------------
    @Throws(IOException::class)
    override fun seek(offset: Long) {
        ttf.seek(this.offset + offset)
        // System.out.println("seek "+(this.offset+offset));
    }

    @Throws(IOException::class)
    override fun getPointer(): Long {
        return ttf.getFilePointer() - offset
    }

    // ---------- Simple Data Types --------------
    @Throws(IOException::class)
    override fun readByte(): Int {
        return ttf.readUnsignedByte()
    }

    @Throws(IOException::class)
    override fun readRawByte(): Int {
        return ttf.readByte().toInt() and 255
    }

    @Throws(IOException::class)
    override fun readShort(): Short {
        return ttf.readShort()
    }

    @Throws(IOException::class)
    override fun readUShort(): Int {
        return ttf.readUnsignedShort()
    }

    @Throws(IOException::class)
    override fun readLong(): Int {
        return ttf.readInt()
    }

    @Throws(IOException::class)
    override fun readULong(): Long {
        val temp = ByteArray(4)
        ttf.readFully(temp)
        var l: Long = 0
        var weight: Long = 1
        for (i in temp.indices) {
            // l |= (temp[3-i]&255) << (8*i);
            l += (temp[3 - i].toInt() and 255) * weight
            weight *= 256
        }
        return l
    }

    @Throws(IOException::class)
    override fun readChar(): Byte {
        return ttf.readByte()
    }

    // ---------------- Arrays -------------------
    @Throws(IOException::class)
    override fun readFully(b: ByteArray) {
        ttf.readFully(b)
    }

    override fun toString(): String {
        return offset.toString() + "-" + (offset + length - 1) + " - " + checksum
    }
}
