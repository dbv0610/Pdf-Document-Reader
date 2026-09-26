// Copyright FreeHEP, 2009
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream

/**
 * Handles read by always throwing IOExceptions even in the middle of a read of
 * an array. IMPORTANT: inherits from InputStream rather than FilterInputStream
 * so that the correct read(byte[], int, int) method is used.
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
abstract class DecodingInputStream : InputStream() {
    /**
     * Overridden to make sure it ALWAYS throws an IOException while a problem
     * occurs in read().
     */
    @Throws(IOException::class)
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        if (b == null) {
            throw NullPointerException()
        } else if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }

        var c = read()
        if (c == -1) {
            return -1
        }
        b[off] = c.toByte()

        var i = 1
        while (i < len) {
            c = read()
            if (c == -1) {
                break
            }
            b[off + i] = c.toByte()
            i++
        }
        return i
    }
}
