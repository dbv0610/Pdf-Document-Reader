// Copyright 2003-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream

/**
 * The Base64InputStream decodes binary data according to RFC 2045. see also:
 * http://en.wikipedia.org/wiki/Base64
 * 
 * @author Mark Donszelmann
 */
class Base64InputStream
/**
 * Creates a Base64 input stream for given input
 * 
 * @param input
 * to be read from
 */(private val `in`: InputStream) : DecodingInputStream() {
    private val b: IntArray? = IntArray(3)

    private var bIndex = 0

    private var bLength = 0

    private var endReached = false

    /**
     * @return current line number
     */
    var lineNo: Int = 1
        private set

    @Throws(IOException::class)
    override fun read(): Int {
        if (bIndex >= bLength) {
            if (endReached) {
                return -1
            }
            bLength = readTuple()
            if (bLength <= 0) {
                return -1
            }
            bIndex = 0
        }
        val a = b!![bIndex]
        bIndex++

        if (a < 0 || a > 0xFF) {
            throw EncodingException(
                (javaClass
                    .toString() + " internal error, byte output out of range: " + a)
            )
        }
        return a
    }

    @Throws(IOException::class, EncodingException::class)
    private fun readTuple(): Int {
        val c: ByteArray? = ByteArray(4)
        var cIndex = 0
        var encoding = 0
        var prevEncoding = 0
        var i = 0

        if (endReached) {
            return 0
        }

        while (i < c!!.size) {
            val ch = `in`.read()
            if (ch < 0) {
                endReached = true
                if (cIndex == 0) {
                    // still a proper end.
                    return 0
                } else {
                    throw EncodingException(
                        "Improperly padded Base64 Input."
                    )
                }
            }

            prevEncoding = encoding
            encoding = base64toInt!![ch and 0x7f].toInt()
            when (encoding) {
                ILLEGAL -> {
                    if (ch < 0) {
                        throw EncodingException(
                            ("Illegal character in Base64 encoding '" + ch
                                    + "'.")
                        )
                    }
                    // ignore, but keep reading padding
                    i++
                }

                EQUALS ->
                    i++

                CARRIAGERETURN -> lineNo++
                LINEFEED -> if (prevEncoding != CARRIAGERETURN) {
                    lineNo++
                }

                else -> {
                    c[cIndex] = (encoding and 0xFF).toByte()
                    cIndex++
                    i++
                }
            }
        }

        val data: Int
        when (cIndex) {
            2 -> {
                data = (c[0].toInt() shl 18) or (c[1].toInt() shl 12)

                b!![0] = (data ushr 16) and 0xFF
                return 1
            }

            3 -> {
                data = (c[0].toInt() shl 18) or (c[1].toInt() shl 12) or (c[2].toInt() shl 6)

                b!![0] = (data ushr 16) and 0xFF
                b[1] = (data ushr 8) and 0xFF
                return 2
            }

            4 -> {
                data =
                    (c[0].toInt() shl 18) or (c[1].toInt() shl 12) or (c[2].toInt() shl 6) or (c[3]).toInt()

                b!![0] = (data ushr 16) and 0xFF
                b[1] = (data ushr 8) and 0xFF
                b[2] = data and 0xFF
                return 3
            }

            else -> throw EncodingException("Base64InputStream: internal error.")
        }
    }

    companion object {
        private val ILLEGAL = -1

        // private static final int WHITESPACE = -2;
        private val LINEFEED = -3

        private val CARRIAGERETURN = -4

        private val EQUALS = -5

        private val base64toInt: ByteArray? = byteArrayOf(
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -2, -3, -1, -1, -4,
            -1,
            -1,  // Tab, LineFeed, CarriageReturn
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1,
            63,  // Space, Plus, Slash
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -5, -1,
            -1,  // 0 - 9, =
            -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14,  // A-Z
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1,
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,  // a-z
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
        )
    }
}
