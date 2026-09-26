// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag

class SaveDC : EMFTag(33, 1) {
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return this
    }

    override fun render(renderer: EMFRenderer) {
        renderer.saveDC()
    }
}
