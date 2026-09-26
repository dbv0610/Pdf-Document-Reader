// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFTag

class FlattenPath : EMFTag(65, 1) {
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return this
    }
}
