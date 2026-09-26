// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.EOFException
import java.io.IOException
import java.io.InputStream

/**
 * Class to read bits from a Stream, allowing for byte synchronization. Signed,
 * Unsigned, Booleans and Floats can be read.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 */
open class BitInputStream
/**
 * Create a Bit input stream from viven input
 * 
 * @param in
 * stream to read from
 */(`in`: InputStream) : DecompressableInputStream(`in`) {
    /**
     * This is a prefetched byte used to construct signed and unsigned numbers
     * with an arbitrary number of significant bits.
     */
    private var bits = 0

    /**
     * The number of valid bits remaining in the bits field.
     */
    private var validBits = 0

    /**
     * A utility method to fetch the next byte in preparation for constructing a
     * bit field. There is no protection for this method; ensure that it is only
     * called when a byte must be fetched.
     * 
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    protected fun fetchByte() {
        bits = read()
        if (bits < 0) {
            throw EOFException()
        }
        validBits = MASK_SIZE
    }

    /**
     * A utility to force the next read to be byte-aligned.
     */
    fun byteAlign() {
        validBits = 0
    }

    /**
     * Read a bit from the input stream and interpret this as a boolean value. A
     * 1-bit is true; a 0-bit is false.
     * 
     * @return true if read bit was 1
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readBitFlag(): Boolean {
        if (validBits == 0) {
            fetchByte()
        }
        return ((bits and BIT_MASK[--validBits]) != 0)
    }

    /**
     * Read a signed value of n-bits from the input stream.
     * 
     * @param n
     * number of bits to read
     * @return value made up of read bits
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readSBits(n: Int): Long {
        var n = n
        if (n == 0) {
            return 0
        }
        var value: Int = if (readBitFlag()) ONES else ZERO
        value = value shl (--n)
        return (value.toLong() or readUBits(n))
    }

    /**
     * Read a float value of n-bits from the stream.
     * 
     * @param n
     * number of bits to read
     * @return value made up of read bits
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readFBits(n: Int): Float {
        if (n == 0) {
            return 0.0f
        }
        return (readSBits(n).toFloat()) / 0x1000
    }

    /**
     * Read an unsigned value of n-bits from the input stream.
     * 
     * @param n
     * number of bits to read
     * @return value made up of read bits
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    fun readUBits(n: Int): Long {
        var n = n
        var value = ZERO.toLong()
        while (n > 0) {
            // Take the needed bits or the number which are valid
            // whichever is less.

            if (validBits == 0) {
                fetchByte()
            }
            val nbits = if (n > validBits) validBits else n

            // Take the bits and update the counters.
            val temp = ((bits shr (validBits - nbits)) and FIELD_MASK[nbits - 1])
            validBits -= nbits
            n -= nbits

            // Shift the value up to accomodate new bits.
            value = value shl nbits
            value = value or temp.toLong()
        }
        return value
    }

    companion object {
        protected const val MASK_SIZE: Int = 8

        protected const val ZERO: Int = 0

        protected val ONES: Int = 0.inv()

        protected val BIT_MASK: IntArray = IntArray(MASK_SIZE)

        protected val FIELD_MASK: IntArray = IntArray(MASK_SIZE)

        // Generate the needed masks for various bit fields and for
        // individual bits.
        init {
            var tempBit = 1
            var tempField = 1
            for (i in 0..<MASK_SIZE) {
                // Set the masks.

                BIT_MASK[i] = tempBit
                FIELD_MASK[i] = tempField

                // Update the temporary values.
                tempBit = tempBit shl 1
                tempField = tempField shl 1
                tempField++
            }
        }
    }
}
