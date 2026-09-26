// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * The ASCII85InputStream decodes ASCII base-85 encoded data. The exact
 * definition of ASCII base-85 encoding can be found in the PostScript Language
 * Reference (3rd ed.) chapter 3.13.3. More info also on
 * http://en.wikipedia.org/wiki/Ascii85
 * 
 * @author Mark Donszelmann
 */
class ASCII85InputStream(private val `in`: InputStream) : DecodingInputStream(), ASCII85 {
    private var endReached = false

    private val b: IntArray? = IntArray(4)

    private var bIndex = 0

    private var bLength = 0

    private val c: IntArray? = IntArray(5)

    /**
     * @return current line number
     */
    var lineNo: Int = 1
        private set

    private var prev: Int

    /**
     * Create an ASCII85 Input Stream from given stream.
     * 
     * @param input
     * input to use
     */
    init {
        prev = -1
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (bIndex >= bLength) {
            if (endReached) {
                return -1
            }
            bLength = readTuple()
            if (bLength < 0) {
                return -1
            }
            bIndex = 0
        }
        val a = b!![bIndex]
        bIndex++

        return a
    }

    @Throws(IOException::class, EncodingException::class)
    private fun readTuple(): Int {
        var cIndex = 0
        var ch = -1
        while ((!endReached) && (cIndex < 5)) {
            prev = ch
            ch = `in`.read()
            when (ch) {
                -1 -> throw EncodingException(
                    "ASCII85InputStream: missing '~>' at end of stream"
                )

                'z'.code -> {
                    if (cIndex != 0) {
                        throw EncodingException(
                            "ASCII85InputStream: 'z' encoding can only appear at the start of a group, cIndex: "
                                    + cIndex
                        )
                    }
                    run {
                        b!![3] = 0
                        b[2] = b[3]
                        b[1] = b[2]
                        b[0] = b[1]
                    }
                    return 4
                }

                '~'.code -> {
                    ch = `in`.read()
                    while ((ch >= 0) && (ch != '>'.code) && Character.isWhitespace(ch)) {
                        ch = `in`.read()
                    }
                    if (ch != '>'.code) {
                        throw EncodingException(
                            "ASCII85InputStream: Invalid EOD, expected '>', found "
                                    + ch
                        )
                    }
                    endReached = true
                }

                '\r'.code -> lineNo++
                '\n'.code -> if (prev != '\r'.code) {
                    lineNo++
                }

                ' '.code, '\t'.code, '\u000C'.code, 0 -> {}
                else -> {
                    c!![cIndex] = ch
                    cIndex++
                }
            }
        }

        if (cIndex > 0) {
            // fill the rest, avoid rounding by padding with "u" characters.
            for (i in c!!.indices) {
                if (i >= cIndex) {
                    c[i] = 'u'.code
                } else {
                    c[i] -= '!'.code
                }
            }

            // convert
            val d = (((c[0] * ASCII85.a85p4) + (c[1] * ASCII85.a85p3) + (c[2] * ASCII85.a85p2)
                    + (c[3] * ASCII85.a85p1) + c[4])) and 0x00000000FFFFFFFFL

            b!![0] = ((d shr 24) and 0x00FFL).toInt()
            b[1] = ((d shr 16) and 0x00FFL).toInt()
            b[2] = ((d shr 8) and 0x00FFL).toInt()
            b[3] = (d and 0x00FFL).toInt()
        }
        return cIndex - 1
    }

    companion object {
        /**
         * Print out ASCII85 of a file
         * 
         * @param args
         * filename
         * @throws Exception
         * when file does not exist
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 1) {
                System.err.println("Usage: ASCII85InputStream filename")
                System.exit(1)
            }
            val `in` = ASCII85InputStream(
                FileInputStream(
                    args[0]
                )
            )
            var b = `in`.read()
            while (b != -1) {
                System.out.write(b)
                b = `in`.read()
            }
            `in`.close()
            System.out.flush()
        }
    }
}
