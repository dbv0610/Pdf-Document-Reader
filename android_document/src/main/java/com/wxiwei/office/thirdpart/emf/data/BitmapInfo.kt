// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream

class BitmapInfo(private var header: BitmapInfoHeader) {
    constructor(emf: EMFInputStream) : this(BitmapInfoHeader(emf))

    override fun toString(): String {
        return "  BitmapInfo\n$header"
    }

    fun getHeader(): BitmapInfoHeader {
        return header
    }
}
