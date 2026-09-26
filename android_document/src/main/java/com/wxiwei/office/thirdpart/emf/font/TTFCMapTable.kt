// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.font

import java.io.IOException

/**
 * CMAP Table.
 * 
 * @author Simon Fischer
 * @version $Id: TTFCMapTable.java 8584 2006-08-10 23:06:37Z duns $
 */
class TTFCMapTable : TTFTable() {
    inner class EncodingTable {
        var platformID: Int = 0

        var encodingID: Int = 0

        var offset: Long = 0

        var format: Int = 0

        var length: Int = 0

        var version: Int = 0

        var tableFormat: TableFormat? = null

        @Throws(IOException::class)
        fun readHeader() {
            platformID = ttf.readUShort()
            encodingID = ttf.readUShort()
            offset = ttf.readULong()
        }

        @Throws(IOException::class)
        fun readBody() {
            ttf.seek(offset)
            format = ttf.readUShort()
            length = ttf.readUShort()
            version = ttf.readUShort()
            when (format) {
                0 -> tableFormat = TableFormat0()
                4 -> tableFormat = TableFormat4()
                2, 6 -> System.err.println("Unimplementet encoding table format: " + format)
                else -> System.err.println("Illegal value for encoding table format: " + format)
            }
            if (tableFormat != null) tableFormat!!.read()
        }

        override fun toString(): String {
            val str = ("[encoding] PID:" + platformID + " EID:" + encodingID + " format:"
                    + format + " v" + version
                    + (if (tableFormat != null) tableFormat.toString() else " [no data read]"))
            return str
        }
    }

    abstract inner class TableFormat {
        @Throws(IOException::class)
        abstract fun read()

        abstract fun getGlyphIndex(character: Int): Int
    }

    inner class TableFormat0 : TableFormat() {
        var glyphIdArray: IntArray = IntArray(256)

        @Throws(IOException::class)
        override fun read() {
            for (i in glyphIdArray.indices) glyphIdArray[i] = ttf.readByte()
        }

        override fun toString(): String {
            var str = ""
            for (i in glyphIdArray.indices) {
                if (i % 16 == 0) str += "\n    " + Integer.toHexString(i / 16) + "x: "
                var number = glyphIdArray[i].toString() + ""
                while (number.length < 3) number = " " + number
                str += number + " "
            }
            return str
        }

        override fun getGlyphIndex(character: Int): Int {
            return glyphIdArray[character]
        }
    }

    inner class TableFormat4 : TableFormat() {
        var segCount: Int = 0

        lateinit var endCount: IntArray
        lateinit var startCount: IntArray
        lateinit var idRangeOffset: IntArray

        lateinit var idDelta: ShortArray // could be int (ushort) as well

        @Throws(IOException::class)
        override fun read() {
            segCount = ttf.readUShort() / 2
            // dump the next three ushorts to /dev/null as they guy
            // who invented them really must have drunk a lot
            ttf.readUShort()
            ttf.readUShort()
            ttf.readUShort()

            // endCount = readFFFFTerminatedUShortArray();
            endCount = ttf.readUShortArray(segCount)
            val reservedPad = ttf.readUShort()
            if (reservedPad != 0) System.err.println("reservedPad not 0, but " + reservedPad + ".")

            startCount = ttf.readUShortArray(endCount.size)
            // the deltas should be unsigned, but due to
            // modulo arithmetic it makes no difference
            idDelta = ttf.readShortArray(endCount.size)
            idRangeOffset = ttf.readUShortArray(endCount.size)
        }

        override fun toString(): String {
            var str = "\n   " + endCount.size + " sections:"
            for (i in endCount.indices) str += ("\n    " + startCount[i] + " to " + endCount[i] + " : " + idDelta[i] + " ("
                    + idRangeOffset[i] + ")")
            return str
        }

        override fun getGlyphIndex(character: Int): Int {
            return 0
        }
    }

    var version: Int = 0

    var encodingTable: Array<EncodingTable?>? = null

    override fun getTag(): String {
        return "cmap"
    }

    @Throws(IOException::class)
    override fun readTable() {
        version = ttf.readUShort()
        encodingTable = arrayOfNulls<EncodingTable>(ttf.readUShort())
        for (i in encodingTable!!.indices) {
            encodingTable!![i] = EncodingTable()
            encodingTable!![i]!!.readHeader()
        }
        for (i in encodingTable!!.indices) {
            encodingTable!![i]!!.readBody()
        }
    }

    override fun toString(): String {
        var str = super.toString() + " v" + version
        for (i in encodingTable!!.indices) str += "\n  " + encodingTable!![i]
        return str
    }
}
