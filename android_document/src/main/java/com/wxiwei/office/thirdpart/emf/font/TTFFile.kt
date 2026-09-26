// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException
import java.io.RandomAccessFile

/**
 * Concrete implementation of the TrueType Font, read from a TTF File.
 * 
 * @author Mark Donszelmann
 * @version $Id: TTFFile.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFFile(private val fileName: String?) : TTFFont() {
    private val ttf: RandomAccessFile

    private val sfntMajorVersion: Int

    private val sfntMinorVersion: Int

    private val numberOfTables: Int

    private val searchRange: Int

    private val entrySelector: Int

    private val rangeShift: Int

    init {
        ttf = RandomAccessFile(fileName, mode)

        // read table directory
        ttf.seek(0)
        sfntMajorVersion = ttf.readUnsignedShort()
        sfntMinorVersion = ttf.readUnsignedShort()
        numberOfTables = ttf.readUnsignedShort()
        searchRange = ttf.readUnsignedShort()
        entrySelector = ttf.readUnsignedShort()
        rangeShift = ttf.readUnsignedShort()

        // read table entries
        for (i in 0..<numberOfTables) {
            ttf.seek((12 + i * 16).toLong())
            val b: ByteArray? = ByteArray(4)
            ttf.readFully(b)
            val tag = kotlin.text.String(b!!)
            val checksum = ttf.readInt()
            val offset = ttf.readInt()
            val len = ttf.readInt()
            val input: TTFInput =
                TTFFileInput(ttf, offset.toLong(), len.toLong(), checksum.toLong())
            newTable(tag, input)
        }
    }

    override fun getFontVersion(): Int {
        return sfntMajorVersion
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        ttf.close()
    }

    override fun show() {
        super.show()

        println("Font: " + fileName)
        println("  sfnt: " + sfntMajorVersion + "." + sfntMinorVersion)
        println("  numTables: " + numberOfTables)
        println("  searchRange: " + searchRange)
        println("  entrySelector: " + entrySelector)
        println("  rangeShift: " + rangeShift)
    }

    companion object {
        private const val mode = "r"
    }
}
