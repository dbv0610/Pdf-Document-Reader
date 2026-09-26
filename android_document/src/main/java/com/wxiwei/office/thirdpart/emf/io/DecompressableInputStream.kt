// Copyright 2001-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream
import java.util.zip.InflaterInputStream

/**
 * Special stream that can be used to read uncompressed first and compressed
 * from a certain byte.
 * 
 * @author Mark Donszelmann
 */
open class DecompressableInputStream(private val `in`: InputStream) : DecodingInputStream() {
    private var decompress = false

    private var iis: InflaterInputStream? = null

    private var b: ByteArray? = null

    private var len = 0

    private var i = 0

    /**
     * Creates a Decompressable input stream from given stream.
     * 
     * @param input
     * stream to read from.
     */
    init {
        try {
            len = `in`.available()
            b = ByteArray(len)
            `in`.read(b)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun read(): Int {
        //return (decompress) ? iis.read() : in.read();
        if (i >= len) {
            return -1
        }
        return (b!![i++].toInt() and 0x000000FF)
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        //return (decompress) ? iis.skip(n) : in.skip(n);
        i += n.toInt()
        return n
    }

    /**
     * Start reading in compressed mode from the next byte.
     * 
     * @throws IOException
     * if read fails.
     */
    @Throws(IOException::class)
    fun startDecompressing() {
        decompress = true
        iis = InflaterInputStream(`in`)
    }
}
