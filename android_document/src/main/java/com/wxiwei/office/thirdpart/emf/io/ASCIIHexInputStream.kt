// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream

/**
 * The ASCIIHexOutputStream decodes ASCII Hexadecimal. The exact definition of
 * ASCII Hex encoding can be found in the PostScript Language Reference (3rd
 * ed.) chapter 3.13.3.
 * 
 * @author Mark Donszelmann
 */
class ASCIIHexInputStream @JvmOverloads constructor(
    private val `in`: InputStream,
    private val ignoreIllegalChars: Boolean = false
) : DecodingInputStream() {
    private var endReached = false

    private var prev: Int

    /**
     * @return current line number
     */
    var lineNo: Int = 1
        private set

    /**
     * Create an ASCIIHex Input Stream from given stream and have it optionally
     * ignore illegal characters
     * 
     * @param input
     * input stream to use
     * @param ignore
     * ignore illegal characters
     */
    /**
     * Create an ASCIIHex Input Stream from given stream
     * 
     * @param input
     * input stream to use
     */
    init {
        prev = -1
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (endReached) {
            return -1
        }

        val b0 = readPart()
        if (b0 == -1) {
            return -1
        }

        var b1 = readPart()
        if (b1 == -1) {
            b1 = 0
        }

        val d = (b0 shl 4 or b1) and 0x00FF
        return d
    }

    @Throws(IOException::class, EncodingException::class)
    private fun readPart(): Int {
        while (true) {
            val b = `in`.read()
            when (b) {
                -1 -> {
                    endReached = true
                    if (!ignoreIllegalChars) {
                        throw EncodingException(
                            "missing '>' at end of ASCII HEX stream"
                        )
                    }
                    return -1
                }

                '>'.code -> {
                    endReached = true
                    return -1
                }

                '\r'.code -> {
                    lineNo++
                    prev = b
                }

                '\n'.code -> {
                    if (prev != '\r'.code) {
                        lineNo++
                    }
                    prev = b
                }

                ' '.code, '\t'.code, '\u000C'.code, 0 ->                // skip whitespace
                    prev = b

                '0'.code -> return 0
                '1'.code -> return 1
                '2'.code -> return 2
                '3'.code -> return 3
                '4'.code -> return 4
                '5'.code -> return 5
                '6'.code -> return 6
                '7'.code -> return 7
                '8'.code -> return 8
                '9'.code -> return 9
                'A'.code, 'a'.code -> return 10
                'B'.code, 'b'.code -> return 11
                'C'.code, 'c'.code -> return 12
                'D'.code, 'd'.code -> return 13
                'E'.code, 'e'.code -> return 14
                'F'.code, 'f'.code -> return 15
                else -> {
                    if (!ignoreIllegalChars) {
                        throw EncodingException(
                            ("Illegal char " + b
                                    + " in HexStream")
                        )
                    }
                    prev = b
                }
            }
        }
    }
}
