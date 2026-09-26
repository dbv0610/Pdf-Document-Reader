// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream

/**
 * The input buffer can be limited to less than the number of bytes of the
 * underlying buffer. Only one real input stream exists, which is where the
 * reads take place. A buffer is limited by some length. If more is read, -1 is
 * returned. Multiple limits can be set by calling pushBuffer. If bytes are left
 * in the buffer when popBuffer is called, they are returned in an array.
 * Otherwise null is returned.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 */
open class ByteCountInputStream(
    `in`: InputStream, littleEndian: Boolean,
    stackDepth: Int
) : ByteOrderInputStream(`in`, littleEndian) {
    private var index: Int

    private val size: IntArray

    private var len: Long = 0

    /**
     * Create a Byte Count input stream from given stream
     * 
     * @param in
     * stream to read from
     * @param littleEndian
     * true if stream should be little endian
     * @param stackDepth
     * maximum number of buffers used while reading
     */
    init {
        size = IntArray(stackDepth)
        index = -1
    }

    @Throws(IOException::class)
    override fun read(): Int {
        // original stream
        if (index == -1) {
            len++
            return super.read()
        }

        // end of buffer
        if (size[index] <= 0) {
            return -1
        }

        // decrease counter
        size[index]--

        len++
        return super.read()
    }

    /**
     * Push the current buffer to the stack
     * 
     * @param len
     * number of bytes that can be read from the current buffer
     */
    fun pushBuffer(len: Int) {
        if (index >= size.size - 1) {
            System.err
                .println(
                    "ByteCountInputStream: trying to push more buffers than stackDepth: "
                            + size.size
                )
            return
        }

        if (index >= 0) {
            if (size[index] < len) {
                System.err
                    .println(
                        ("ByteCountInputStream: trying to set a length: "
                                + len
                                + ", longer than the underlying buffer: "
                                + size[index])
                    )
                return
            }
            size[index] -= len
        }
        index++
        size[index] = len
    }

    /**
     * Pops the buffer from the stack and returns leftover bytes in a byte array
     * 
     * @return null if buffer was completely read. Otherwise rest of buffer is
     * read and returned.
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun popBuffer(): ByteArray? {
        if (index >= 0) {
            val len = size[index]
            if (len > 0) {
                return readByte(len)
            } else if (len < 0) {
                System.err.println("ByteCountInputStream: Internal Error")
            }
            index--
        }
        return null
    }

    val length: Long
        /**
         * @return number of bytes that can be read from the current buffer
         */
        get() = if (index >= 0) size[index].toLong() else len
}
