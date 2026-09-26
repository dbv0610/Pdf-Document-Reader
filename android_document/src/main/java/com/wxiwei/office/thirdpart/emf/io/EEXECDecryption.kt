// Copyright 2001 freehep
package com.wxiwei.office.thirdpart.emf.io

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Decrypts using the EEXEC form (Used by Type 1 fonts).
 * 
 * @author Simon Fischer
 * @version $Id: src/main/java/org/freehep/util/io/EEXECDecryption.java
 * 96b41b903496 2005/11/21 19:50:18 duns $
 */
class EEXECDecryption @JvmOverloads constructor(
    private var `in`: InputStream,
    private var r: Int = EEXECConstants.EEXEC_R.code,
    private val n: Int = EEXECConstants.N.code
) : InputStream(), EEXECConstants {
    private val c1: Int
    private val c2: Int

    private var first = true

    /**
     * Creates an EEXECDecryption from the given stream
     * 
     * @param in
     * stream to read from
     * @param r
     * @param n
     */
    /**
     * Creates an EEXECDecryption from the given stream
     * 
     * @param in
     * stream to read from
     */
    init {
        this.c1 = EEXECConstants.C1.code
        this.c2 = EEXECConstants.C2.code
    }

    private fun decrypt(cipher: Int): Int {
        val plain = (cipher xor (r ushr 8)) % 256
        r = ((cipher + r) * c1 + c2) % 65536
        return plain
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (first) {
            val bytes = ByteArray(n)
            var notHex = false
            for (i in bytes.indices) {
                val c = `in`.read()
                bytes[i] = c.toByte()
                if (!Character.isDigit(c.toChar()) && !((c >= 'a'.code) && (c <= 'f'.code)) && !((c >= 'A'.code) && (c <= 'F'.code))) {
                    notHex = true
                }
            }
            if (notHex) {
                for (i in bytes.indices) {
                    decrypt(bytes[i].toInt() and 0x00ff)
                }
            } else {
                val tempIn: InputStream = ASCIIHexInputStream(
                    ByteArrayInputStream(bytes), true
                )
                var asciiDecoded: Int
                var byteCount = 0
                while ((tempIn.read().also { asciiDecoded = it }) >= 0) {
                    decrypt(asciiDecoded)
                    byteCount++
                }
                `in` = ASCIIHexInputStream(`in`, true)
                while (byteCount < n) {
                    decrypt(`in`.read())
                    byteCount++
                }
            }
            first = false
        }

        val b = `in`.read()
        if (b == -1) {
            return -1
        } else {
            return decrypt(b)
        }
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        `in`.close()
    }
}
