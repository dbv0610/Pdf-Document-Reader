// Copyright 2001 freehep
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.OutputStream

//import java.util.Random;

/**
 * Encrypts using the EEXEC form (Used by Type 1 fonts).
 * 
 * @author Simon Fischer
 * @version $Id: src/main/java/org/freehep/util/io/EEXECEncryption.java
 * 96b41b903496 2005/11/21 19:50:18 duns $
 */
class EEXECEncryption @JvmOverloads constructor(
    private val out: OutputStream,
    private var r: Int = EEXECConstants.EEXEC_R.code,
    private val n: Int = EEXECConstants.N.code
) : OutputStream(), EEXECConstants {
    private val c1: Int
    private val c2: Int

    private var first = true

    /**
     * Creates an EEXECEncryption from given stream.
     * 
     * @param out
     * stream to write
     * @param r
     * @param n
     */
    /**
     * Creates an EEXECEncryption from given stream.
     * 
     * @param out
     * stream to write
     */
    /**
     * Creates an EEXECEncryption from given stream.
     * 
     * @param out
     * stream to write
     * @param r
     */
    init {
        this.c1 = EEXECConstants.C1.code
        this.c2 = EEXECConstants.C2.code
    }

    private fun encrypt(plainByte: Int): Int {
        val cipher = (plainByte xor (r ushr 8)) % 256
        r = ((cipher + r) * c1 + c2) % 65536
        return cipher
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        if (first) {
            for (i in 0..<n) {
                out.write(encrypt(0))
            }
            first = false
        }

        out.write(encrypt(b))
    }

    @Throws(IOException::class)
    override fun flush() {
        super.flush()
        out.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        flush()
        super.close()
        out.close()
    }

    private class IntOutputStream(size: Int) : OutputStream() {
        // public String getString() { return str; }
        var ints: IntArray

        var i: Int = 0

        init {
            this.ints = IntArray(size)
        }

        override fun write(b: Int) {
            this.ints[i++] = b
        } // str += (char)b; }
    }

    companion object {
        /**
         * Encrypt array of characters.
         * 
         * @param chars
         * int array to encrypt
         * @param r
         * @param n
         * @return encrypted array
         * @throws IOException
         * if write fails (never happens)
         */
        @Throws(IOException::class)
        fun encryptString(chars: IntArray, r: Int, n: Int): IntArray {
            val resultStr = IntOutputStream(chars.size + 4)
            val eout = EEXECEncryption(resultStr, r, n)
            for (i in chars.indices) {
                eout.write(chars[i])
            }
            return resultStr.ints
        }
    }
}
