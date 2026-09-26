// Copyright 2003-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

open class DCTInputStream(input: InputStream) : FilterInputStream(input) {
    @Throws(IOException::class)
    override fun read(): Int {
        throw IOException("$javaClass: read() not implemented, use readImage().")
    }
}
