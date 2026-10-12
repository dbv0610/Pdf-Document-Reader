// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf

import android.graphics.Point
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.thirdpart.emf.io.ActionHeader
import com.wxiwei.office.thirdpart.emf.io.TagHeader
import com.wxiwei.office.thirdpart.emf.io.TaggedInputStream
import java.io.IOException
import java.io.InputStream

/**
 * This class extends the TaggedInputStream with several methods to read EMF
 * primitives from the stream and to read TagHeaders.
 * 
 * @author Mark Donszelmann
 * @version $Id: EMFInputStream.java 10367 2007-01-22 19:26:48Z duns $
 */
class EMFInputStream(`is`: InputStream, tagSet: EMFTagSet) :
    TaggedInputStream(`is`, tagSet, null, true), EMFConstants {
    @JvmOverloads
    constructor(`is`: InputStream, version: Int = DEFAULT_VERSION) : this(
        `is`,
        EMFTagSet(version)
    )

    @Throws(IOException::class)
    fun readDWORD(): Int {
        val i = readUnsignedInt()
        return i.toInt()
    }

    @Throws(IOException::class)
    fun readDWORD(size: Int): IntArray {
        val x = IntArray(size)
        for (i in x.indices) {
            x[i] = readDWORD()
        }
        return x
    }

    @Throws(IOException::class)
    fun readWORD(): Int {
        return readUnsignedShort()
    }

    @Throws(IOException::class)
    fun readLONG(): Int {
        return readInt()
    }

    @Throws(IOException::class)
    fun readLONG(size: Int): IntArray {
        val x = IntArray(size)
        for (i in x.indices) {
            x[i] = readLONG()
        }
        return x
    }

    @Throws(IOException::class)
    fun readFLOAT(): Float {
        return readFloat()
    }

    @Throws(IOException::class)
    fun readUINT(): Int {
        return readUnsignedInt().toInt()
    }

    @Throws(IOException::class)
    fun readULONG(): Int {
        return readUnsignedInt().toInt()
    }

    @Throws(IOException::class)
    fun readCOLORREF(): Color {
        val c = Color(readUnsignedByte(), readUnsignedByte(), readUnsignedByte())
        readByte()
        return c
    }

    @Throws(IOException::class)
    fun readCOLOR16(): Color {
        return Color(
            readShort().toInt() shr 8,
            readShort().toInt() shr 8,
            readShort().toInt() shr 8,
            readShort().toInt() shr 8
        )
    }

    @Throws(IOException::class)
    fun readXFORM(): AffineTransform {
        return AffineTransform(
            readFLOAT(), readFLOAT(), readFLOAT(), readFLOAT(), readFLOAT(),
            readFLOAT()
        )
    }

    @Throws(IOException::class)
    fun readRECTL(): Rectangle {
        val x = readLONG()
        val y = readLONG()
        val w = readLONG() - x
        val h = readLONG() - y
        return Rectangle(x, y, w, h)
    }

    @Throws(IOException::class)
    fun readPOINTL(): Point {
        val x = readLONG()
        val y = readLONG()
        return Point(x, y)
    }

    @Throws(IOException::class)
    fun readPOINTL(size: Int): Array<Point?> {
        val p = arrayOfNulls<Point>(size)
        for (i in p.indices) {
            p[i] = readPOINTL()
        }
        return p
    }

    @Throws(IOException::class)
    fun readPOINTS(): Point {
        val x = readShort().toInt()
        val y = readShort().toInt()
        return Point(x, y)
    }

    @Throws(IOException::class)
    fun readPOINTS(size: Int): Array<Point?> {
        val p = arrayOfNulls<Point>(size)
        for (i in p.indices) {
            p[i] = readPOINTS()
        }
        return p
    }

    @Throws(IOException::class)
    fun readSIZEL(): Dimension {
        return Dimension(readLONG(), readLONG())
    }

    @Throws(IOException::class)
    fun readBYTE(): Int {
        return readByte().toInt()
    }

    @Throws(IOException::class)
    fun readBYTE(size: Int): ByteArray {
        val x = ByteArray(size)
        for (i in x.indices) {
            x[i] = readBYTE().toByte()
        }
        return x
    }

    @Throws(IOException::class)
    fun readBOOLEAN(): Boolean {
        return (readBYTE() != 0)
    }

    @Throws(IOException::class)
    fun readWCHAR(size: Int): String {
        val bytes = readByte(2 * size)
        var length = 2 * size
        run {
            var i = 0
            while (i < 2 * size) {
                if (bytes[i].toInt() == 0 && bytes[i + 1].toInt() == 0) {
                    length = i
                    break
                }
                i += 2
            }
        }
        return String(bytes, 0, length, charset("UTF-16LE"))
    }

    @Throws(IOException::class)
    override fun readTagHeader(): TagHeader? {
        // Read the tag.
        // byteAlign();
        var tagID = read()
        // End of stream
        if (tagID == -1) return null

        tagID = tagID or (readUnsignedByte() shl 8)
        tagID = tagID or (readUnsignedByte() shl 16)
        tagID = tagID or (readUnsignedByte() shl 24)

        val length = readDWORD().toLong()
        return TagHeader(tagID, length - 8)
    }

    @Throws(IOException::class)
    override fun readActionHeader(): ActionHeader? {
        return null
    }

    private var header: EMFHeader? = null

    @Throws(IOException::class)
    fun readHeader(): EMFHeader {
        if (header == null) {
            header = EMFHeader(this)
        }
        return header!!
    }

    val version: Int
        get() = DEFAULT_VERSION

    companion object {
        @JvmField
        var DEFAULT_VERSION: Int = 1
    }
}
