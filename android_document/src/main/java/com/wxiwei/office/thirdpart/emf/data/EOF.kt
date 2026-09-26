// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag

class EOF : EMFTag(14, 1) {
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        emf.readUnsignedByte(len)
        return EOF()
    }

    override fun render(renderer: EMFRenderer) {
    }
}
